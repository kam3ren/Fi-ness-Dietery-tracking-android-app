package com.example.finess

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BmiFragmentTest {
    private lateinit var fragment: BmiFragment

    @Before
    fun setup() {
        fragment = BmiFragment().apply {
            weight = 70
            userHeight = 175
            age = 25
        }
    }

    @Test
    fun test01() { // Asian Male bone density
        fragment.selectedEthnicity = "Asian"
        fragment.selectedGender = "Male"
        assertEquals(1.15 * 1.05, fragment.calculateBoneDensity(), 0.01)
    }

    @Test
    fun test02() { // Muscle mass age reduction
        fragment.age = 45
        val expected = (70.0 - 1.15) * (1 - 0.005 * 5)
        assertEquals(expected, fragment.calculateMuscleMass(1.15), 0.1)
    }

    @Test
    fun test03() { // BMI category check
        assertEquals("Athletic Build", fragment.determineCategory(30.0))
    }

    @Test
    fun test04() { // Caucasian Female calculation
        fragment.selectedEthnicity = "Caucasian"
        fragment.selectedGender = "Female"
        assertEquals(1.20 * 0.95, fragment.calculateBoneDensity(), 0.01)
    }

    @Test
    fun test05() { // Age boundary check
        fragment.age = 30
        assertEquals(1.15 * 1.05, fragment.calculateBoneDensity(), 0.01)
    }

    @Test
    fun test06() { // Extreme muscle mass reduction
        fragment.age = 100
        val result = fragment.calculateMuscleMass(1.15)
        assertTrue(result >= (70.0 - 1.15) * 0.7)
    }

    @Test
    fun test07() { // Low BMI category
        assertEquals("Low Muscle Mass", fragment.determineCategory(18.4))
    }

    @Test
    fun test08() { // High BMI category
        assertEquals("High Muscle Mass", fragment.determineCategory(25.0))
    }

    @Test
    fun test09() { // Exceptional BMI category
        assertEquals("Exceptional Muscle Mass", fragment.determineCategory(35.0))
    }
}