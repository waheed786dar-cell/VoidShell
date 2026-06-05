package com.void.shell.domain.player

import com.void.shell.core.engine.TickData
import com.void.shell.core.state.StateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    private val stateManager: StateManager
) {
    suspend fun initialize() {
        // Load player profile from DB — Phase 9
    }

    fun tick(td: TickData) {
        // Passive energy regen per tick
        stateManager.updatePlayer { p ->
            val regenRate = 0.5f * (td.deltaMs / 1000f)
            p.copy(energy = (p.energy + regenRate).coerceAtMost(100f))
        }
    }
}
