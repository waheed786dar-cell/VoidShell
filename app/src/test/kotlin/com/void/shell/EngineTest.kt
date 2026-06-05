package com.void.shell

import com.void.shell.core.tick.TickManager
import com.void.shell.core.time.VirtualTimeController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TickManagerTest {

    private lateinit var tickManager: TickManager

    @Before fun setup() { tickManager = TickManager() }

    @Test fun `nextTick increments correctly`() {
        val t1 = tickManager.nextTick()
        val t2 = tickManager.nextTick()
        val t3 = tickManager.nextTick()
        assertEquals(1L, t1)
        assertEquals(2L, t2)
        assertEquals(3L, t3)
    }

    @Test fun `currentTick matches last nextTick`() {
        tickManager.nextTick()
        tickManager.nextTick()
        assertEquals(2L, tickManager.currentTick())
    }

    @Test fun `reset sets all counters to zero`() {
        tickManager.nextTick(); tickManager.nextTick()
        tickManager.reset()
        assertEquals(0L, tickManager.currentTick())
    }

    @Test fun `msToTicks calculation correct`() {
        // 1000ms at 20 TPS = 20 ticks
        assertEquals(20L, tickManager.msToTicks(1000L, 20L))
        // 500ms at 20 TPS = 10 ticks
        assertEquals(10L, tickManager.msToTicks(500L, 20L))
    }

    @Test fun `hasTicksPassed returns true after n ticks`() {
        val start = tickManager.currentTick()
        repeat(5) { tickManager.nextTick() }
        assertTrue(tickManager.hasTicksPassed(start, 5L))
    }
}

class VirtualTimeTest {

    private lateinit var vtc: VirtualTimeController

    @Before fun setup() { vtc = VirtualTimeController() }

    @Test fun `time does not advance when stopped`() {
        vtc.advance(1000L)
        assertEquals(0L, vtc.currentVirtualTime)
    }

    @Test fun `time advances when running`() {
        vtc.start()
        vtc.advance(1000L)
        assertTrue(vtc.currentVirtualTime > 0L)
    }

    @Test fun `time stops advancing after pause`() {
        vtc.start()
        vtc.advance(500L)
        val t1 = vtc.currentVirtualTime
        vtc.pause()
        vtc.advance(500L)
        assertEquals(t1, vtc.currentVirtualTime)
    }

    @Test fun `multiplier changes advance speed`() {
        vtc.start()
        vtc.setMultiplier(2.0f)
        vtc.advance(1000L)
        val fast = vtc.currentVirtualTime

        val vtc2 = VirtualTimeController()
        vtc2.start()
        vtc2.setMultiplier(1.0f)
        vtc2.advance(1000L)
        val normal = vtc2.currentVirtualTime

        assertTrue(fast > normal)
    }
}
