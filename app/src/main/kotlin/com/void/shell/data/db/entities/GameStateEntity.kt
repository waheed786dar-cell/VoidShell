package com.void.shell.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id          : Int    = 1,
    val playerStateJson          : String = "",
    val worldStateJson           : String = "",
    val missionStateJson         : String = "",
    val savedAt                  : Long   = System.currentTimeMillis(),
    val version                  : Int    = 1,
)
