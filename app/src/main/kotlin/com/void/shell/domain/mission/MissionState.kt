package com.void.shell.domain.mission

import com.void.shell.core.events.MissionType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class MissionState(
    val activeMission    : ActiveMission?,
    val availableMissions: ImmutableList<Mission>,
    val completedIds     : ImmutableList<String>,
    val failedIds        : ImmutableList<String>,
) {
    companion object {
        fun default() = MissionState(
            activeMission     = null,
            availableMissions = persistentListOf(),
            completedIds      = persistentListOf(),
            failedIds         = persistentListOf(),
        )
    }
}

@Serializable
data class Mission(
    val id           : String, val type       : MissionType,
    val titleEn      : String, val titleHindi : String,
    val descEn       : String, val descHindi  : String,
    val targetNodeId : String, val difficulty : Int,
    val timeLimitMs  : Long,   val rewards    : MissionReward,
    val objectives   : List<Objective>,
    val faction      : String?, val storyNodeId: String?,
)

@Serializable
data class ActiveMission(
    val mission         : Mission,
    val startTimeMs     : Long,
    val elapsedMs       : Long,
    val detectionLevel  : Float,
    val completedObjs   : List<String>,
    val stealthScore    : Float,
)

@Serializable
data class Objective(
    val id: String, val descEn: String, val descHindi: String,
    val type: ObjType, val targetId: String?, val required: Boolean,
)

@Serializable
data class MissionReward(
    val xp: Long, val dataBits: Long,
    val darkCoins: Long, val items: List<String>,
    val repChanges: Map<String, Int>,
)

enum class ObjType { COMPROMISE_NODE, EXFIL_DATA, KILL_PROCESS, PLANT_BACKDOOR, TRACE_SIGNAL, ESCAPE }
