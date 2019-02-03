package com.pierfrancescosoffritti.slidingdrawer

import com.pierfrancescosoffritti.slidingdrawer.utils.Utils
import org.junit.Test

import org.junit.Assert.*

class UtilsTest {
    @Test
    fun normalize_basicTest() {
        // ARRANGE
        val original = 0.5f
        val scale = 10
        val scaledValue = original*scale

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
        val scaledValue = original*scale

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
        val scaledValue = original*scale

        // ACT
        val result = Utils.normalizeScreenCoordinate(scaledValue, scale.toFloat())

        // ASSERT
        assertEquals(result, 0f)
    }
}