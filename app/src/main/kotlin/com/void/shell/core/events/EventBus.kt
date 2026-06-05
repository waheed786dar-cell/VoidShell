package com.void.shell.core.events

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventBus @Inject constructor() {

    private val _flow = MutableSharedFlow<VoidEvent>(
        replay              = 0,
        extraBufferCapacity = 512,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST
    )
    val eventFlow: SharedFlow<VoidEvent> = _flow.asSharedFlow()

    private val seq = AtomicLong(0L)

    suspend fun emit(event: VoidEvent) {
        _flow.emit(stamp(event))
    }

    fun emitSync(event: VoidEvent): Boolean =
        _flow.tryEmit(stamp(event))

    inline fun <reified T : VoidEvent> observe() =
        eventFlow.filter { it is T }

    private fun stamp(e: VoidEvent): VoidEvent {
        e.id        = UUID.randomUUID().toString()
        e.sequence  = seq.incrementAndGet()
        e.timestamp = System.currentTimeMillis()
        return e
    }
}

// ── Base ─────────────────────────────────────────────────────────────
sealed class VoidEvent {
    var id        : String = ""
    var sequence  : Long   = 0L
    var timestamp : Long   = 0L
}

// ── System ───────────────────────────────────────────────────────────
sealed class SystemEvent : VoidEvent() {
    object EngineReady                          : SystemEvent()
    object AutoSaveComplete                     : SystemEvent()
    data class AutoSaveFailed(val r: String)    : SystemEvent()
    data class EngineError(val msg: String)     : SystemEvent()
}

// ── Player ───────────────────────────────────────────────────────────
sealed class PlayerEvent : VoidEvent() {
    data class CommandEntered(
        val raw: String, val parsed: ParsedCommand?
    ) : PlayerEvent()
    data class StatChanged(
        val stat: PlayerStat, val old: Float, val new: Float
    ) : PlayerEvent()
    data class LevelUp(val old: Int, val new: Int)          : PlayerEvent()
    data class SkillUnlocked(val skillId: String)           : PlayerEvent()
    data class FactionRepChanged(
        val factionId: String, val delta: Int, val total: Int
    ) : PlayerEvent()
}

// ── Mission ──────────────────────────────────────────────────────────
sealed class MissionEvent : VoidEvent() {
    data class Started(val id: String, val type: MissionType)  : MissionEvent()
    data class ObjectiveDone(val mId: String, val oId: String) : MissionEvent()
    data class Completed(
        val id: String, val score: Int,
        val timeMs: Long, val stealth: Float
    ) : MissionEvent()
    data class Failed(val id: String, val reason: FailReason)  : MissionEvent()
    data class DetectionChanged(
        val id: String, val old: Float, val new: Float
    ) : MissionEvent()
}

// ── World ────────────────────────────────────────────────────────────
sealed class WorldEvent : VoidEvent() {
    data class NodeDiscovered(val id: String, val type: NetworkNodeType) : WorldEvent()
    data class NodeCompromised(val id: String, val by: String)           : WorldEvent()
    data class AlertChanged(val old: AlertLevel, val new: AlertLevel)    : WorldEvent()
    data class FactionWarStarted(val f1: String, val f2: String)         : WorldEvent()
}

// ── Terminal ─────────────────────────────────────────────────────────
sealed class TerminalEvent : VoidEvent() {
    data class Output(val text: String, val type: OutputType, val animate: Boolean = true) : TerminalEvent()
    object ClearScreen  : TerminalEvent()
    object PromptReady  : TerminalEvent()
}

// ── AI ───────────────────────────────────────────────────────────────
sealed class AIEvent : VoidEvent() {
    data class EnemyDetected(val enemyId: String, val conf: Float)    : AIEvent()
    data class TraceProgress(val progress: Float, val msLeft: Long)   : AIEvent()
    data class TraceComplete(val missionId: String)                   : AIEvent()
}

// ── Supporting Types ─────────────────────────────────────────────────
enum class PlayerStat { HEALTH, ENERGY, GHOST_LEVEL, INTRUSION_SKILL, STEALTH_SKILL, ENCRYPTION_SKILL, SOCIAL_SKILL, HARDWARE_SKILL, GHOST_OPS_SKILL }
enum class MissionType { INFILTRATE, EXTRACT, DESTROY, PROTECT, TRACE, SOCIAL_ENGINEER, DARK_OPS, FACTION_WAR }
enum class FailReason  { DETECTED, TRACED, TIME_EXPIRED, NODE_LOST }
enum class NetworkNodeType { CORPORATE, GOVERNMENT, DARK_WEB, CIVILIAN, PLAYER_BASE, FACTION_HQ, HONEYPOT, QUANTUM }
enum class AlertLevel  { NONE, LOW, MEDIUM, HIGH, CRITICAL, LOCKDOWN }
enum class OutputType  { INFO, SUCCESS, ERROR, WARNING, SYSTEM, DATA, ENEMY }
data class ParsedCommand(val command: String, val flags: Map<String, String>, val args: List<String>, val rawInput: String)
