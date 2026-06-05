package com.void.shell.domain.world

import com.void.shell.core.events.AlertLevel
import com.void.shell.core.events.NetworkNodeType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class WorldState(
    val nodes           : ImmutableMap<String, NetworkNode>,
    val connections     : ImmutableList<NodeConnection>,
    val globalAlertLevel: AlertLevel,
    val factions        : ImmutableList<FactionState>,
    val discoveredNodes : ImmutableList<String>,
    val compromisedNodes: ImmutableList<String>,
    val playerPosition  : String?,
    val worldSeed       : Long,
    val worldAge        : Long,
) {
    companion object {
        fun default() = WorldState(
            nodes            = persistentMapOf(),
            connections      = persistentListOf(),
            globalAlertLevel = AlertLevel.NONE,
            factions         = persistentListOf(
                FactionState("GHOST_COLLECTIVE",   "Ghost Collective",   "Ghost Sangathan", 50),
                FactionState("MEGACORP_NEXUS",     "MegaCorp Nexus",    "MegaCorp Jaal",   40),
                FactionState("GOV_CIPHER",         "Gov Cipher",        "Sarkar Code",     60),
                FactionState("DARK_WEB_SYNDICATE", "Dark Web Syndicate","Andheri Jaal",    30),
            ),
            discoveredNodes  = persistentListOf(),
            compromisedNodes = persistentListOf(),
            playerPosition   = null,
            worldSeed        = System.currentTimeMillis(),
            worldAge         = 0L,
        )
    }
    fun getNode(id: String) = nodes[id]
    fun adjacent(id: String) = connections.filter { it.from == id || it.to == id }
        .map { if (it.from == id) it.to else it.from }
}

@Serializable
data class NetworkNode(
    val id           : String, val nameEn    : String,
    val nameHindi    : String, val type      : NetworkNodeType,
    val secLevel     : Int,    val hp        : Int,
    val maxHp        : Int,    val ownerId   : String?,
    val defenses     : List<NodeDefense>,
    val pos          : NodePos, val isOnline : Boolean,
    val modified     : Long,
)

@Serializable data class NodePos(val x: Float, val y: Float)

@Serializable
data class NodeConnection(
    val id: String, val from: String, val to: String,
    val bandwidth: Float, val latency: Float,
    val encrypted: Boolean, val active: Boolean,
)

@Serializable
data class FactionState(
    val id: String, val nameEn: String,
    val nameHindi: String, val power: Int,
    val ownedNodes: List<String> = emptyList(),
    val atWar: Boolean = false, val warTarget: String? = null,
)

@Serializable
data class NodeDefense(
    val type: DefenseType, val strength: Float, val active: Boolean,
)

enum class DefenseType { FIREWALL, IDS, HONEYPOT, TRACE, AI_GUARD, ENCRYPT_WALL, KILLSWITCH }
