package com.void.shell.domain.mission

import com.void.shell.core.engine.TickData
import com.void.shell.core.state.StateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionManager @Inject constructor(
    private val stateManager: StateManager
) {
    suspend fun initialize() { /* Phase 11 */ }

    fun tick(td: TickData) {
        val active = stateManager.readMission { it.activeMission }
        if (active != null) {
            stateManager.updateMission { ms ->
                ms.copy(
                    activeMission = active.copy(
                        elapsedMs = active.elapsedMs + td.deltaMs
                    )
                )
            }
        }
    }
}
