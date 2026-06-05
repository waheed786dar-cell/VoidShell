package com.void.shell.domain.player

import com.void.shell.core.events.PlayerStat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val id              : String,
    val callsign        : String,
    val ghostLevel      : Int,
    val xp              : Long,
    val xpToNext        : Long,
    val health          : Float,
    val energy          : Float,
    val dataBits        : Long,
    val darkCoins       : Long,
    val skills          : SkillTree,
    val inventory       : ImmutableList<InventoryItem>,
    val factionRep      : ImmutableMap<String, Int>,
    val stats           : PlayerStatistics,
    val achievements    : ImmutableList<String>,
    val unlockedCmds    : ImmutableList<String>,
    val stealthRating   : Float,
    val activeMissionId : String?,
    val lastSeen        : Long,
) {
    companion object {
        fun default() = PlayerState(
            id              = "GHOST_${System.currentTimeMillis()}",
            callsign        = "GHOST",
            ghostLevel      = 1,
            xp              = 0L,
            xpToNext        = 1000L,
            health          = 100f,
            energy          = 100f,
            dataBits        = 500L,
            darkCoins       = 0L,
            skills          = SkillTree(),
            inventory       = persistentListOf(),
            factionRep      = persistentMapOf(
                "GHOST_COLLECTIVE"    to 50,
                "MEGACORP_NEXUS"      to 0,
                "GOV_CIPHER"          to 0,
                "DARK_WEB_SYNDICATE"  to 20
            ),
            stats           = PlayerStatistics(),
            achievements    = persistentListOf(),
            unlockedCmds    = persistentListOf(
                "help","ls","cd","cat","ping","scan","status","whoami","clear","history","uptime"
            ),
            stealthRating   = 0.5f,
            activeMissionId = null,
            lastSeen        = 0L
        )
    }

    fun xpPct()     : Float = if (xpToNext == 0L) 1f else (xp.toFloat() / xpToNext).coerceIn(0f, 1f)
    fun hpPct()     : Float = (health / 100f).coerceIn(0f, 1f)
    fun energyPct() : Float = (energy / 100f).coerceIn(0f, 1f)
    fun isAlive()   : Boolean = health > 0f
    fun hasEnergy(n: Float) = energy >= n
    fun rep(fid: String) = factionRep[fid] ?: 0
    fun tier(fid: String) = FactionTier.from(rep(fid))
    fun stat(s: PlayerStat) = when(s) {
        PlayerStat.HEALTH          -> health
        PlayerStat.ENERGY          -> energy
        PlayerStat.GHOST_LEVEL     -> ghostLevel.toFloat()
        PlayerStat.INTRUSION_SKILL -> skills.intrusion.toFloat()
        PlayerStat.STEALTH_SKILL   -> skills.stealth.toFloat()
        PlayerStat.ENCRYPTION_SKILL-> skills.encryption.toFloat()
        PlayerStat.SOCIAL_SKILL    -> skills.social.toFloat()
        PlayerStat.HARDWARE_SKILL  -> skills.hardware.toFloat()
        PlayerStat.GHOST_OPS_SKILL -> skills.ghostOps.toFloat()
    }
}

@Serializable
data class SkillTree(
    val intrusion  : Int = 1, val stealth    : Int = 1,
    val encryption : Int = 1, val social     : Int = 1,
    val hardware   : Int = 1, val ghostOps   : Int = 0,
    val points     : Int = 5,
) {
    fun intrusionEff() = 1f + intrusion * 0.02f + hardware * 0.005f
    fun stealthMult()  = 1f + stealth   * 0.025f + ghostOps * 0.01f
    fun encStrength()  = 1f + encryption* 0.03f  + intrusion * 0.005f
}

@Serializable
data class PlayerStatistics(
    val missionsDone  : Int  = 0, val missionsFailed: Int  = 0,
    val totalPlayMs   : Long = 0, val cmdExecuted   : Long = 0,
    val nodesOwned    : Int  = 0, val bitsEarned    : Long = 0,
    val bestStealth   : Float= 0f,val timesDetected : Int  = 0,
    val perfectRuns   : Int  = 0,
)

@Serializable
data class InventoryItem(
    val id         : String, val name     : String,
    val nameHindi  : String, val type     : ItemType,
    val tier       : ItemTier, val quantity: Int,
    val durability : Float,  val stats    : Map<String, Float>,
)

enum class ItemType    { EXPLOIT, STEALTH, DECRYPT, HARDWARE, SOCIAL, DEFENSE, CONSUMABLE, UPGRADE }
enum class ItemTier    { BASIC, ADVANCED, ELITE, LEGENDARY, QUANTUM }
enum class FactionTier(val range: IntRange) {
    ENEMY(-100..-50), HOSTILE(-49..-1),
    NEUTRAL(0..24),   FRIENDLY(25..49),
    TRUSTED(50..74),  ALLIED(75..100);
    companion object { fun from(rep: Int) = values().first { rep in it.range } }
}
