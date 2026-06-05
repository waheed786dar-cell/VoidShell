package com.void.shell.core.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualTimeController @Inject constructor() {

    private val _flow = MutableStateFlow(0L)
    val timeFlow: StateFlow<Long> = _flow.asStateFlow()

    var currentVirtualTime: Long = 0L
        private set

    private var multiplier : Float   = 1.0f
    private var running    : Boolean = false

    // 1 real second = 60 virtual seconds (1 virtual minute)
    private val RATIO = 60L

    fun start()  { running = true }
    fun stop()   { running = false }
    fun pause()  { running = false }
    fun resume() { running = true }

    fun advance(realMs: Long) {
        if (!running) return
        val delta = (realMs * RATIO * multiplier).toLong()
        currentVirtualTime += delta
        _flow.value = currentVirtualTime
    }

    fun setMultiplier(m: Float) { multiplier = m.coerceIn(0.1f, 10f) }

    fun formattedTime(): String {
        val s  = currentVirtualTime / 1000L
        val d  = s / 86400; val h = (s % 86400) / 3600
        val m  = (s % 3600) / 60; val sec = s % 60
        return "DAY $d — %02d:%02d:%02d".format(h, m, sec)
    }
}
