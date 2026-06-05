package com.void.shell.core.state

import com.void.shell.core.events.*
import com.void.shell.domain.mission.MissionState
import com.void.shell.domain.player.PlayerState
import com.void.shell.domain.world.WorldState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.collections.immutable.toPersistentList

object EventStateReducer {

    fun reduce(
        event   : VoidEvent,
        player  : MutableStateFlow<PlayerState>,
        world   : MutableStateFlow<WorldState>,
        mission : MutableStateFlow<MissionState>,
        onDelta : (StateDelta) -> Unit
    ) {
        when (event) {
            is PlayerEvent.LevelUp -> {
                val old = player.value
                val new = old.copy(ghostLevel = event.new)
                player.value = new
                onDelta(StateDelta.Player(old, new))
            }
            is PlayerEvent.StatChanged -> {
                val old = player.value
                val new = when (event.stat) {
                    PlayerStat.HEALTH -> old.copy(health = event.new)
                    PlayerStat.ENERGY -> old.copy(energy = event.new)
                    else              -> old
                }
                player.value = new
                onDelta(StateDelta.Player(old, new))
            }
            is WorldEvent.AlertChanged -> {
                val old = world.value
                val new = old.copy(globalAlertLevel = event.new)
                world.value = new
                onDelta(StateDelta.World(old, new))
            }
            is WorldEvent.NodeDiscovered -> {
                val old  = world.value
                val list = old.discoveredNodes.toPersistentList().add(event.id)
                val new  = old.copy(discoveredNodes = list)
                world.value = new
                onDelta(StateDelta.World(old, new))
            }
            is MissionEvent.Started -> {
                val old = player.value
                val new = old.copy(activeMissionId = event.id)
                player.value = new
                onDelta(StateDelta.Player(old, new))
            }
            is MissionEvent.Completed -> {
                val old = player.value
                val new = old.copy(activeMissionId = null)
                player.value = new
                onDelta(StateDelta.Player(old, new))
            }
            else -> { /* Other events — no direct state mutation */ }
        }
    }
}
