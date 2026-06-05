package com.void.shell.core.tick

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TickManager @Inject constructor() {
    private val global  = AtomicLong(0L)
    private val world   = AtomicLong(0L)
    private val ai      = AtomicLong(0L)

    fun nextTick()      : Long = global.incrementAndGet()
    fun nextWorldTick() : Long = world.incrementAndGet()
    fun nextAiTick()    : Long = ai.incrementAndGet()
    fun currentTick()   : Long = global.get()
    fun msToTicks(ms: Long, tps: Long = 20L) = (ms * tps) / 1000L
    fun hasTicksPassed(since: Long, n: Long) = (global.get() - since) >= n
    fun reset() { global.set(0L); world.set(0L); ai.set(0L) }
}
