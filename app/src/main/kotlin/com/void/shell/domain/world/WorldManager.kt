package com.void.shell.domain.world

import com.void.shell.core.engine.TickData
import com.void.shell.core.state.StateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldManager @Inject constructor(
    private val stateManager: StateManager
) {
    suspend fun initialize() {
        // Generate or load world — Phase 11 (PCG)
    }

    fun tick(td: TickData) {
        // Advance world age
        stateManager.updateWorld { w ->
            w.copy(worldAge = w.worldAge + td.deltaMs)
        }
    }
}
