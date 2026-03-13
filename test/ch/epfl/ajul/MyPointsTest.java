package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyPointsTest {

    // ===================== Constantes =====================

    @Test
    void pointsConstantsAreCorrectlyDefined() {
        assertEquals(2, Points.FULL_ROW_BONUS_POINTS);
        assertEquals(7, Points.FULL_COLUMN_BONUS_POINTS);
        assertEquals(10, Points.FULL_COLOR_BONUS_POINTS);
    }

    // ===================== newWallTilePoints =====================

    @Test
    void newWallTilePointsReturnsHGroupWhenVGroupIsOne() {
        var remaining = 5;
        for (var h = 1; h <= 5; h += 1) {
            assertEquals(h, Points.newWallTilePoints(h, 1));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void newWallTilePointsReturnsVGroupWhenHGroupIsOne() {
        var remaining = 5;
        for (var v = 1; v <= 5; v += 1) {
            assertEquals(v, Points.newWallTilePoints(1, v));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void newWallTilePointsReturnsSumWhenBothGroupsGreaterThanOne() {
        var remaining = 4 * 4;
        for (var h = 2; h <= 5; h += 1) {
            for (var v = 2; v <= 5; v += 1) {
                assertEquals(h + v, Points.newWallTilePoints(h, v));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void newWallTilePointsForIsolatedTileIsOne() {
        assertEquals(1, Points.newWallTilePoints(1, 1));
    }

    @Test
    void newWallTilePointsForFullRowAndColumnIsTen() {
        assertEquals(10, Points.newWallTilePoints(5, 5));
    }

    // ===================== floorPenalty =====================

    @Test
    void floorPenaltyIsCorrectForAllIndices() {
        var expected = new int[]{1, 1, 2, 2, 2, 3, 3};
        var remaining = 7;
        for (var i = 0; i < 7; i += 1) {
            assertEquals(expected[i], Points.floorPenalty(i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void floorPenaltyIsNonDecreasing() {
        for (var i = 0; i < 6; i += 1)
            assertTrue(Points.floorPenalty(i) <= Points.floorPenalty(i + 1));
    }

    @Test
    void floorPenaltyIsPositiveForAllIndices() {
        for (var i = 0; i < 7; i += 1)
            assertTrue(Points.floorPenalty(i) > 0);
    }

    // ===================== totalFloorPenalty =====================

    @Test
    void totalFloorPenaltyIsZeroForEmptyFloor() {
        assertEquals(0, Points.totalFloorPenalty(0));
    }

    @Test
    void totalFloorPenaltyIsCorrectForAllCounts() {
        var expected = new int[]{0, 1, 2, 4, 6, 8, 11, 14};
        var remaining = 8;
        for (var i = 0; i <= 7; i += 1) {
            assertEquals(expected[i], Points.totalFloorPenalty(i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void totalFloorPenaltyIsConsistentWithFloorPenalty() {
        var total = 0;
        for (var i = 0; i < 7; i += 1) {
            total += Points.floorPenalty(i);
            assertEquals(total, Points.totalFloorPenalty(i + 1));
        }
    }

    @Test
    void totalFloorPenaltyForFullFloorIsFourteen() {
        assertEquals(14, Points.totalFloorPenalty(7));
    }
}