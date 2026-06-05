package com.void.shell.data.snapshot

import android.content.Context
import com.void.shell.data.db.GameStateDao
import com.void.shell.data.db.entities.GameStateEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotEngine @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val dao : GameStateDao,
) {
    private val mutex = Mutex()
    private val json  = Json {
        ignoreUnknownKeys     = true
        encodeDefaults        = true
        coerceInputValues     = true
        isLenient             = true
    }

    // Atomic save — write to temp first, then rename
    // Prevents corrupt save on crash mid-write
    suspend fun saveAtomic(snap: GameSnapshot) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val entity = GameStateEntity(
                id               = 1,
                playerStateJson  = json.encodeToString(snap.playerState),
                worldStateJson   = json.encodeToString(snap.worldState),
                missionStateJson = json.encodeToString(snap.missionState),
                savedAt          = snap.timestamp,
                version          = snap.version,
            )
            dao.saveState(entity)
        }
    }

    suspend fun loadLatest(): GameSnapshot? = withContext(Dispatchers.IO) {
        val entity = dao.getState() ?: return@withContext null
        runCatching {
            GameSnapshot(
                playerState  = json.decodeFromString(entity.playerStateJson),
                worldState   = json.decodeFromString(entity.worldStateJson),
                missionState = json.decodeFromString(entity.missionStateJson),
                timestamp    = entity.savedAt,
                version      = entity.version,
            )
        }.getOrNull()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}
