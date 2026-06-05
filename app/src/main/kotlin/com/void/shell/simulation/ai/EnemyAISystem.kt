package com.void.shell.simulation.ai

import com.void.shell.domain.world.WorldState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyAISystem @Inject constructor() {

    fun tick(worldState: WorldState) {
        // Behavior tree evaluation — Phase 11
    }

    fun hibernateAll() {
        // On critical memory pressure — pause all AI
    }
}
