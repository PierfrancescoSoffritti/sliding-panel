package com.akbolatss.workshop.slidingpanel

import com.akbolatss.workshop.slidingpanel.utils.Utils
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun normalize_basicTest() {
        // ARRANGE
        val original = 0.5f
        val scale = 10
        val scaledValue = original * scale

        // ACT
        val result = Utils.normalizeScreenCoordinate(scaledValue, scale.toFloat())

        // ASSERT
        assertEquals(result, 0.5f)
    }

    @Test
    fun normalize_zeroTest() {
        // ARRANGE
        val original = 0f
        val scale = 10
        val scaledValue = original * scale

        // ACT
        val result = Utils.normalizeScreenCoordinate(scaledValue, scale.toFloat())

        // ASSERT
        assertEquals(result, 1f)
    }

    @Test
    fun normalize_oneTest() {
        // ARRANGE
        val original = 1f
        val scale = 10
        val scaledValue = original * scale

        // ACT
        val result = Utils.normalizeScreenCoordinate(scaledValue, scale.toFloat())

        // ASSERT
        assertEquals(result, 0f)
    }

    @Test
    fun clamp_notClampedTest() {
        // ARRANGE
        val value = 5f
        val min = 0f
        val max = 10f

        // ACT
        val result = Utils.clamp(value, min, max)

        // ASSERT
        assertEquals(result, 5f)
    }

    @Test
    fun clamp_clampedMaxTest() {
        // ARRANGE
        val value = 50f
        val min = 0f
        val max = 10f

        // ACT
        val result = Utils.clamp(value, min, max)

        // ASSERT
        assertEquals(result, 10f)
    }

    @Test
    fun clamp_clampedMinTest() {
        // ARRANGE
        val value = -10f
        val min = 0f
        val max = 10f

        // ACT
        val result = Utils.clamp(value, min, max)

        // ASSERT
        assertEquals(result, 0f)
    }
}