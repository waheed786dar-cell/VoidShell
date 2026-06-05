package com.void.shell.data.snapshot

import com.void.shell.domain.mission.MissionState
import com.void.shell.domain.player.PlayerState
import com.void.shell.domain.world.WorldState
import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(
    val playerState  : PlayerState,
    val worldState   : WorldState,
    val missionState : MissionState,
    val timestamp    : Long,
    val version      : Int,
) {
    companion object {
        const val VERSION = 1
    }
}
