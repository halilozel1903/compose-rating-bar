package io.github.halilozel1903.ratingbar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RatingMathTest {

    @Test
    fun `clamp keeps values in range and maps NaN to the minimum`() {
        assertEquals(0f, RatingMath.clamp(-1f, 5))
        assertEquals(5f, RatingMath.clamp(7.5f, 5))
        assertEquals(3.5f, RatingMath.clamp(3.5f, 5))
        assertEquals(0f, RatingMath.clamp(Float.NaN, 5))
        assertEquals(1f, RatingMath.clamp(0.2f, 5, min = 1f))
        assertEquals(1f, RatingMath.clamp(Float.NaN, 5, min = 1f))
        assertEquals(5f, RatingMath.clamp(Float.POSITIVE_INFINITY, 5))
    }

    @Test
    fun `clamp rejects a negative maximum`() {
        assertFailsWith<IllegalArgumentException> { RatingMath.clamp(1f, -1) }
    }

    @Test
    fun `snap rounds to the nearest allowed value`() {
        assertEquals(3f, RatingMath.snap(3.3f, RatingStep.Full))
        assertEquals(4f, RatingMath.snap(3.5f, RatingStep.Full))
        assertEquals(3.5f, RatingMath.snap(3.3f, RatingStep.Half))
        assertEquals(3f, RatingMath.snap(3.2f, RatingStep.Half))
        assertEquals(4f, RatingMath.snap(3.75f, RatingStep.Half))
        assertEquals(3.37f, RatingMath.snap(3.37f, RatingStep.Free))
        assertEquals(0f, RatingMath.snap(0.2f, RatingStep.Full))
    }

    @Test
    fun `snapUp rounds up and ignores float noise`() {
        assertEquals(3f, RatingMath.snapUp(2.1f, RatingStep.Full))
        assertEquals(2f, RatingMath.snapUp(2.00001f, RatingStep.Full))
        assertEquals(2.5f, RatingMath.snapUp(2.1f, RatingStep.Half))
        assertEquals(3f, RatingMath.snapUp(2.6f, RatingStep.Half))
        assertEquals(2.5f, RatingMath.snapUp(2.5f, RatingStep.Half))
        assertEquals(2.6f, RatingMath.snapUp(2.6f, RatingStep.Free))
    }

    @Test
    fun `content width includes the gaps between items`() {
        assertEquals(5 * 40f + 4 * 8f, RatingMath.contentWidth(40f, 8f, 5))
        assertEquals(40f, RatingMath.contentWidth(40f, 8f, 1))
        assertEquals(0f, RatingMath.contentWidth(40f, 8f, 0))
        assertEquals(200f, RatingMath.contentWidth(40f, -3f, 5))
    }

    // Five 40 px stars with 10 px gaps: star i spans [50 i, 50 i + 40].
    private fun at(x: Float, step: RatingStep = RatingStep.Full, rtl: Boolean = false, width: Float = 240f) =
        RatingMath.valueAt(x, itemSize = 40f, spacing = 10f, itemCount = 5, step = step, isRtl = rtl, layoutWidth = width)

    @Test
    fun `full steps select the touched item`() {
        assertEquals(1f, at(5f))
        assertEquals(1f, at(39f))
        assertEquals(3f, at(101f))
        assertEquals(3f, at(139f))
        assertEquals(5f, at(239f))
    }

    @Test
    fun `touching a gap selects the item before it`() {
        assertEquals(1f, at(45f))
        assertEquals(4f, at(195f))
    }

    @Test
    fun `half steps split every item`() {
        assertEquals(0.5f, at(10f, RatingStep.Half))
        assertEquals(1f, at(30f, RatingStep.Half))
        assertEquals(2.5f, at(110f, RatingStep.Half))
        assertEquals(3f, at(125f, RatingStep.Half))
        assertEquals(4.5f, at(210f, RatingStep.Half))
        assertEquals(1f, at(47f, RatingStep.Half))
    }

    @Test
    fun `free steps follow the finger`() {
        assertEquals(0.25f, at(10f, RatingStep.Free), 1e-5f)
        assertEquals(3.7f, at(178f, RatingStep.Free), 1e-5f)
        assertEquals(2f, at(48f + 50f, RatingStep.Free), 1e-5f)
    }

    @Test
    fun `positions outside the items clamp to the ends`() {
        assertEquals(0f, at(-20f))
        assertEquals(0f, at(0f))
        assertEquals(5f, at(240f))
        assertEquals(5f, at(500f, RatingStep.Half))
        assertEquals(0f, at(Float.NaN))
    }

    @Test
    fun `rtl mirrors the position from the right edge`() {
        // Star 1 is now at [200, 240].
        assertEquals(1f, at(230f, rtl = true))
        assertEquals(0.5f, at(230f, RatingStep.Half, rtl = true))
        assertEquals(1f, at(205f, RatingStep.Half, rtl = true))
        assertEquals(5f, at(10f, rtl = true))
        assertEquals(0f, at(250f, rtl = true))
        assertEquals(5f, at(-5f, rtl = true))
    }

    @Test
    fun `rtl uses the measured width when the bar is wider than its items`() {
        // fillMaxWidth: 400 px wide, items occupy [160, 400] in RTL.
        assertEquals(1f, at(390f, rtl = true, width = 400f))
        assertEquals(1f, at(375f, RatingStep.Half, rtl = true, width = 400f))
        assertEquals(0.5f, at(385f, RatingStep.Half, rtl = true, width = 400f))
        assertEquals(0f, at(401f, rtl = true, width = 400f))
        assertEquals(5f, at(165f, rtl = true, width = 400f))
        assertEquals(5f, at(100f, rtl = true, width = 400f))
    }

    @Test
    fun `ltr ignores extra width after the items`() {
        assertEquals(5f, at(300f, width = 400f))
    }

    @Test
    fun `valueAt rejects a non positive item size and handles zero items`() {
        assertFailsWith<IllegalArgumentException> {
            RatingMath.valueAt(10f, itemSize = 0f, spacing = 0f, itemCount = 5, step = RatingStep.Full)
        }
        assertEquals(0f, RatingMath.valueAt(10f, itemSize = 40f, spacing = 0f, itemCount = 0, step = RatingStep.Full))
    }

    @Test
    fun `valueAt defaults the layout width to the content width`() {
        assertEquals(
            1f,
            RatingMath.valueAt(235f, itemSize = 40f, spacing = 10f, itemCount = 5, step = RatingStep.Full, isRtl = true),
        )
    }

    @Test
    fun `fill fractions fill the items before the value`() {
        assertEquals(listOf(1f, 1f, 1f, 0.5f, 0f), RatingMath.fillFractions(3.5f, 5))
        assertEquals(listOf(0f, 0f, 0f), RatingMath.fillFractions(0f, 3))
        assertEquals(listOf(1f, 1f, 1f), RatingMath.fillFractions(9f, 3))
        assertEquals(0.7f, RatingMath.fillFraction(3.7f, 3), 1e-5f)
        assertEquals(0f, RatingMath.fillFraction(Float.NaN, 0))
        assertTrue(RatingMath.fillFractions(1f, -2).isEmpty())
    }

    @Test
    fun `selected index is the item the value ends in`() {
        assertEquals(-1, RatingMath.selectedIndex(0f))
        assertEquals(0, RatingMath.selectedIndex(0.5f))
        assertEquals(0, RatingMath.selectedIndex(1f))
        assertEquals(2, RatingMath.selectedIndex(2.5f))
        assertEquals(2, RatingMath.selectedIndex(3f))
        assertEquals(2, RatingMath.selectedIndex(3.00001f))
        assertEquals(-1, RatingMath.selectedIndex(Float.NaN))
    }

    @Test
    fun `increase and decrease move one keyboard step`() {
        assertEquals(4f, RatingMath.increase(3f, RatingStep.Full, 5))
        assertEquals(4f, RatingMath.increase(3.5f, RatingStep.Full, 5))
        assertEquals(5f, RatingMath.increase(5f, RatingStep.Full, 5))
        assertEquals(2f, RatingMath.decrease(3f, RatingStep.Full, 5))
        assertEquals(3f, RatingMath.decrease(3.5f, RatingStep.Full, 5))
        assertEquals(0f, RatingMath.decrease(0f, RatingStep.Full, 5))

        assertEquals(4f, RatingMath.increase(3.5f, RatingStep.Half, 5))
        assertEquals(3.5f, RatingMath.increase(3.2f, RatingStep.Half, 5))
        assertEquals(3f, RatingMath.decrease(3.5f, RatingStep.Half, 5))
        assertEquals(3f, RatingMath.decrease(3.2f, RatingStep.Half, 5))

        assertEquals(4.4f, RatingMath.increase(4.37f, RatingStep.Free, 5), 1e-5f)
        assertEquals(4.3f, RatingMath.decrease(4.37f, RatingStep.Free, 5), 1e-5f)
        assertEquals(3.8f, RatingMath.increase(3.7f, RatingStep.Free, 5), 1e-5f)
        assertEquals(3.6f, RatingMath.decrease(3.7f, RatingStep.Free, 5), 1e-5f)
    }

    @Test
    fun `increase from out of range values clamps first`() {
        assertEquals(1f, RatingMath.increase(-3f, RatingStep.Full, 5))
        assertEquals(4f, RatingMath.decrease(9f, RatingStep.Full, 5))
    }

    @Test
    fun `semantic steps count the values strictly between the ends`() {
        assertEquals(4, RatingMath.semanticSteps(RatingStep.Full, 5))
        assertEquals(9, RatingMath.semanticSteps(RatingStep.Half, 5))
        assertEquals(0, RatingMath.semanticSteps(RatingStep.Free, 5))
        assertEquals(19, RatingMath.semanticSteps(RatingStep.Half, 10))
        assertEquals(0, RatingMath.semanticSteps(RatingStep.Full, 1))
        assertEquals(0, RatingMath.semanticSteps(RatingStep.Full, 0))
    }

    @Test
    fun `tick bucket changes every half item`() {
        assertEquals(0, RatingMath.tickBucket(0f))
        assertEquals(1, RatingMath.tickBucket(0.3f))
        assertEquals(1, RatingMath.tickBucket(0.5f))
        assertEquals(2, RatingMath.tickBucket(0.51f))
        assertEquals(7, RatingMath.tickBucket(3.5f))
        assertEquals(8, RatingMath.tickBucket(3.7f))
    }

    @Test
    fun `step properties`() {
        assertEquals(1f, RatingStep.Full.increment)
        assertEquals(0.5f, RatingStep.Half.increment)
        assertEquals(0.1f, RatingStep.Free.increment, 1e-6f)
        assertTrue(RatingStep.Free.isContinuous)
        assertTrue(!RatingStep.Half.isContinuous)
    }
}
