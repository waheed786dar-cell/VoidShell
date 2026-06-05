package com.void.shell.core.state

import com.void.shell.core.engine.TickData
import com.void.shell.core.events.VoidEvent
import com.void.shell.data.snapshot.GameSnapshot
import com.void.shell.domain.mission.MissionState
import com.void.shell.domain.player.PlayerState
import com.void.shell.domain.world.WorldState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.read
import kotlin.concurrent.write

@Singleton
class StateManager @Inject constructor() {

    private val _player  = MutableStateFlow(PlayerState.default())
    private val _world   = MutableStateFlow(WorldState.default())
    private val _mission = MutableStateFlow(MissionState.default())

    val playerState  : StateFlow<PlayerState>  = _player.asStateFlow()
    val worldState   : StateFlow<WorldState>   = _world.asStateFlow()
    val missionState : StateFlow<MissionState> = _mission.asStateFlow()

    // ReentrantReadWriteLock: many readers, single writer
    private val lock  = ReentrantReadWriteLock()
    private val deltas = mutableListOf<StateDelta>()

    fun updatePlayer(fn: (PlayerState) -> PlayerState) = lock.write {
        val old = _player.value; val new = fn(old)
        if (old != new) { _player.value = new; deltas += StateDelta.Player(old, new) }
    }

    fun updateWorld(fn: (WorldState) -> WorldState) = lock.write {
        val old = _world.value; val new = fn(old)
        if (old != new) { _world.value = new; deltas += StateDelta.World(old, new) }
    }

    fun updateMission(fn: (MissionState) -> MissionState) = lock.write {
        val old = _mission.value; val new = fn(old)
        if (old != new) { _mission.value = new; deltas += StateDelta.Mission(old, new) }
    }

    fun <T> readPlayer(sel: (PlayerState) -> T) : T = lock.read { sel(_player.value) }
    fun <T> readWorld(sel: (WorldState) -> T)   : T = lock.read { sel(_world.value) }
    fun <T> readMission(sel: (MissionState) -> T): T= lock.read { sel(_mission.value) }

    fun processEvent(event: VoidEvent) = lock.write {
        EventStateReducer.reduce(event, _player, _world, _mission) { d -> deltas += d }
    }

    fun commitTickDelta(td: TickData) {
        if (deltas.size > 1000) {
            val trimmed = deltas.takeLast(500)
            deltas.clear(); deltas.addAll(trimmed)
        }
    }

    fun createSnapshot(): GameSnapshot = lock.read {
        GameSnapshot(_player.value, _world.value, _mission.value, System.currentTimeMillis(), GameSnapshot.VERSION)
    }

    fun restoreFromSnapshot(snap: GameSnapshot) = lock.write {
        _player.value  = snap.playerState
        _world.value   = snap.worldState
        _mission.value = snap.missionState
    }

    fun initializeFreshState() = lock.write {
        _player.value  = PlayerState.default()
        _world.value   = WorldState.default()
        _mission.value = MissionState.default()
    }
}

sealed class StateDelta {
    data class Player (val before: PlayerState,  val after: PlayerState)  : StateDelta()
    data class World  (val before: WorldState,   val after: WorldState)   : StateDelta()
    data class Mission(val before: MissionState, val after: MissionState) : StateDelta()
}
