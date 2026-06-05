package com.void.shell.core.engine

import com.void.shell.MemoryPressure
import com.void.shell.core.events.EventBus
import com.void.shell.core.events.SystemEvent
import com.void.shell.core.state.StateManager
import com.void.shell.core.tick.TickManager
import com.void.shell.core.time.VirtualTimeController
import com.void.shell.data.snapshot.SnapshotEngine
import com.void.shell.domain.mission.MissionManager
import com.void.shell.domain.player.PlayerManager
import com.void.shell.domain.world.WorldManager
import com.void.shell.simulation.ai.EnemyAISystem
import com.void.shell.simulation.network.NetworkGraphSimulator
import com.void.shell.simulation.process.ProcessSimulator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameEngine @Inject constructor(
    private val tickManager      : TickManager,
    private val eventBus         : EventBus,
    private val stateManager     : StateManager,
    private val timeController   : VirtualTimeController,
    private val playerManager    : PlayerManager,
    private val worldManager     : WorldManager,
    private val missionManager   : MissionManager,
    private val processSimulator : ProcessSimulator,
    private val networkSimulator : NetworkGraphSimulator,
    private val enemyAI          : EnemyAISystem,
    private val snapshotEngine   : SnapshotEngine,
) {
    private val _engineState  = MutableStateFlow<EngineState>(EngineState.Idle)
    val engineState: StateFlow<EngineState> = _engineState.asStateFlow()

    private val _isWarmedUp   = MutableStateFlow(false)
    val isWarmedUp: StateFlow<Boolean> = _isWarmedUp.asStateFlow()

    private val _metrics      = MutableStateFlow(EngineMetrics())
    val metrics: StateFlow<EngineMetrics> = _metrics.asStateFlow()

    // Scoped coroutines — each subsystem isolated
    private val rootJob    = SupervisorJob()
    private val engineScope = CoroutineScope(
        rootJob + Dispatchers.Default +
        CoroutineExceptionHandler { _, t -> onEngineCrash(t) }
    )
    private val ioScope     = CoroutineScope(SupervisorJob(rootJob) + Dispatchers.IO)

    // Loop jobs
    private var worldJob   : Job? = null
    private var aiJob      : Job? = null
    private var eventJob   : Job? = null
    private var autoSaveJob: Job? = null

    // Object pool to avoid GC pressure in tight loops
    private val tickPool = ObjectPool(
        factory  = { TickData() },
        reset    = { it.reset() },
        maxSize  = 32
    )

    companion object {
        const val WORLD_TPS       = 20L      // 20 ticks/sec = 50ms/tick
        const val AI_TPS          = 10L      // 10 ticks/sec = 100ms/tick
        const val WORLD_TICK_MS   = 1_000L / WORLD_TPS
        const val AI_TICK_MS      = 1_000L / AI_TPS
        const val AUTO_SAVE_MS    = 30_000L
    }

    suspend fun warmUp() = withContext(Dispatchers.Default) {
        _engineState.value = EngineState.Initializing
        try {
            // Parallel warm-up
            val jobs = listOf(
                engineScope.launch { worldManager.initialize() },
                engineScope.launch { playerManager.initialize() },
                engineScope.launch { networkSimulator.initialize() },
                engineScope.launch { processSimulator.initialize() },
            )
            jobs.forEach { it.join() }

            // Load saved state or fresh
            val snap = snapshotEngine.loadLatest()
            if (snap != null) stateManager.restoreFromSnapshot(snap)
            else              stateManager.initializeFreshState()

            _isWarmedUp.value  = true
            _engineState.value = EngineState.Ready
            eventBus.emit(SystemEvent.EngineReady)
        } catch (e: Exception) {
            _engineState.value = EngineState.Error("WarmUp failed: ${e.message}")
        }
    }

    fun startGameLoop() {
        if (_engineState.value == EngineState.Running) return
        _engineState.value = EngineState.Running
        timeController.start()
        launchWorldLoop()
        launchAILoop()
        launchEventProcessor()
        launchAutoSave()
    }

    private fun launchWorldLoop() {
        worldJob = engineScope.launch {
            var last         = System.nanoTime()
            var accumulated  = 0L
            val intervalNs   = WORLD_TICK_MS * 1_000_000L

            while (isActive) {
                val now   = System.nanoTime()
                val delta = now - last
                last      = now
                accumulated += delta

                // Fixed-timestep accumulator — guarantees determinism
                while (accumulated >= intervalNs) {
                    val t0 = System.nanoTime()
                    doWorldTick(WORLD_TICK_MS)
                    recordTickDuration(System.nanoTime() - t0)
                    accumulated -= intervalNs
                }

                // Sleep remaining to avoid busy-wait
                val sleepMs = ((intervalNs - accumulated) / 1_000_000L).coerceAtLeast(1L)
                delay(sleepMs)
            }
        }
    }

    private fun launchAILoop() {
        aiJob = engineScope.launch {
            while (isActive) {
                val t0 = System.nanoTime()
                enemyAI.tick(stateManager.worldState.value)
                val elapsed = (System.nanoTime() - t0) / 1_000_000L
                delay((AI_TICK_MS - elapsed).coerceAtLeast(1L))
            }
        }
    }

    private fun launchEventProcessor() {
        eventJob = engineScope.launch {
            eventBus.eventFlow.collect { event ->
                stateManager.processEvent(event)
            }
        }
    }

    private fun launchAutoSave() {
        autoSaveJob = ioScope.launch {
            while (isActive) {
                delay(AUTO_SAVE_MS)
                doAutoSave()
            }
        }
    }

    private suspend fun doWorldTick(deltaMs: Long) {
        val td = tickPool.acquire()
        try {
            td.deltaMs      = deltaMs
            td.virtualTime  = timeController.currentVirtualTime
            td.tickNumber   = tickManager.nextTick()

            timeController.advance(deltaMs)
            processSimulator.tick(td)
            networkSimulator.tick(td)
            missionManager.tick(td)
            worldManager.tick(td)
            playerManager.tick(td)
            stateManager.commitTickDelta(td)
        } finally {
            tickPool.release(td)
        }
    }

    private suspend fun doAutoSave() {
        runCatching {
            val snap = stateManager.createSnapshot()
            snapshotEngine.saveAtomic(snap)
            eventBus.emit(SystemEvent.AutoSaveComplete)
        }.onFailure { e ->
            eventBus.emit(SystemEvent.AutoSaveFailed(e.message ?: "unknown"))
        }
    }

    fun onAppBackground() {
        engineScope.launch { doAutoSave() }
        _engineState.value = EngineState.Backgrounded
        worldJob?.cancel()
        aiJob?.cancel()
    }

    fun onAppForeground() {
        if (_engineState.value == EngineState.Backgrounded) startGameLoop()
    }

    fun onAppDestroy() {
        engineScope.launch { doAutoSave() }
        timeController.stop()
        _engineState.value = EngineState.Stopped
        rootJob.cancel()
        ioScope.cancel()
    }

    fun onMemoryPressure(level: MemoryPressure) {
        engineScope.launch {
            when (level) {
                MemoryPressure.LOW      -> tickPool.shrink(16)
                MemoryPressure.MEDIUM   -> {
                    tickPool.shrink(8)
                    networkSimulator.releaseCache(full = false)
                }
                MemoryPressure.HIGH     -> {
                    tickPool.shrink(4)
                    networkSimulator.releaseCache(full = true)
                    processSimulator.releaseCache()
                }
                MemoryPressure.CRITICAL -> {
                    doAutoSave()
                    tickPool.clear()
                    enemyAI.hibernateAll()
                }
            }
        }
    }

    private fun recordTickDuration(ns: Long) {
        val ms  = ns / 1_000_000.0
        val cur = _metrics.value
        _metrics.value = cur.copy(
            lastTickMs  = ms,
            avgTickMs   = cur.avgTickMs * 0.95 + ms * 0.05,
            totalTicks  = cur.totalTicks + 1
        )
    }

    private fun onEngineCrash(t: Throwable) {
        _engineState.value = EngineState.Error(t.message ?: "crash")
        engineScope.launch { doAutoSave() }
    }
}

sealed class EngineState {
    object Idle         : EngineState()
    object Initializing : EngineState()
    object Ready        : EngineState()
    object Running      : EngineState()
    object Backgrounded : EngineState()
    object Stopped      : EngineState()
    data class Error(val msg: String) : EngineState()
}

data class EngineMetrics(
    val lastTickMs  : Double = 0.0,
    val avgTickMs   : Double = 0.0,
    val totalTicks  : Long   = 0L,
    val droppedFrames: Int   = 0,
)

class TickData {
    var deltaMs     : Long = 0L
    var virtualTime : Long = 0L
    var tickNumber  : Long = 0L
    fun reset() { deltaMs = 0L; virtualTime = 0L; tickNumber = 0L }
}

class ObjectPool<T>(
    private val factory : () -> T,
    private val reset   : (T) -> Unit,
    @Volatile private var maxSize: Int
) {
    private val pool = ArrayDeque<T>(maxSize)

    @Synchronized fun acquire()        = if (pool.isEmpty()) factory() else pool.removeFirst()
    @Synchronized fun release(obj: T)  { if (pool.size < maxSize) { reset(obj); pool.addLast(obj) } }
    @Synchronized fun shrink(n: Int)   { while (pool.size > n) pool.removeFirst(); maxSize = n }
    @Synchronized fun clear()          = pool.clear()
}
