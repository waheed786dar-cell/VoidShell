package com.void.shell.simulation.process

import com.void.shell.core.engine.TickData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessSimulator @Inject constructor() {
    private val processes = mutableMapOf<String, SimProcess>()

    suspend fun initialize() {
        // Boot default system processes
        processes["init"]     = SimProcess("1",  "init",          ProcessState.RUNNING)
        processes["ghost"]    = SimProcess("42", "ghost_daemon",  ProcessState.RUNNING)
        processes["firewall"] = SimProcess("99", "void_firewall", ProcessState.RUNNING)
    }

    fun tick(td: TickData) {
        processes.values.forEach { proc ->
            if (proc.state == ProcessState.RUNNING) {
                // Simulate CPU usage fluctuation
                proc.cpuUsage = (proc.cpuUsage + (-0.5f..0.5f).random())
                    .coerceIn(0.1f, 95f)
            }
        }
    }

    fun getAllProcesses()  : List<SimProcess> = processes.values.toList()
    fun getProcess(id: String): SimProcess?  = processes[id]
    fun releaseCache() { /* GC non-critical data */ }

    private fun ClosedFloatingPointRange<Float>.random() =
        start + (Math.random() * (endInclusive - start)).toFloat()
}

data class SimProcess(
    val pid   : String,
    val name  : String,
    var state : ProcessState,
    var cpuUsage: Float = 1f,
    var memMB   : Int   = 4,
)

enum class ProcessState { RUNNING, SLEEPING, ZOMBIE, STOPPED }
