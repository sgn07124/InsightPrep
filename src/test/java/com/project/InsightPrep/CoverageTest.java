package com.project.InsightPrep;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CoverageTest {

    Coverage coverage = new Coverage();

    @Test
    void testAdd() {
        assertEquals(5, coverage.add(2, 3));
    }

    @Test
    void testSubtract() {
        assertEquals(1, coverage.subtract(4, 3));
    }

    @Test
    void testIsPositive() {
        assertTrue(coverage.isPositive(10));
        assertFalse(coverage.isPositive(-1));
    }

}