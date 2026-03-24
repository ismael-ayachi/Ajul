package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointsTest {
    @Test
    void pointsConstantsAreCorrectlyDefined() {
        assertEquals(2, Points.FULL_ROW_BONUS_POINTS);
        assertEquals(7, Points.FULL_COLUMN_BONUS_POINTS);
        assertEquals(10, Points.FULL_COLOR_BONUS_POINTS);
    }

    @Test
    void pointsNewWallTilePointsWorks() {
        for (var h = 1; h <= 5; h += 1) {
            for (var v = 1; v <= 5; v += 1) {
                var expected = 0;
                if (h == 1) expected = v;
                else if (v == 1) expected = h;
                else expected = h + v;

                assertEquals(expected, Points.newWallTilePoints(h, v));
            }
        }
    }

    @Test
    void pointsFloorPenaltyWorks() {
        assertEquals(1, Points.floorPenalty(0));
        assertEquals(1, Points.floorPenalty(1));
        assertEquals(2, Points.floorPenalty(2));
        assertEquals(2, Points.floorPenalty(3));
        assertEquals(2, Points.floorPenalty(4));
        assertEquals(3, Points.floorPenalty(5));
        assertEquals(3, Points.floorPenalty(6));
    }

    @Test
    void pointsTotalFloorPenaltyWorks() {
        assertEquals(0, Points.totalFloorPenalty(0));
        assertEquals(1, Points.totalFloorPenalty(1));
        assertEquals(2, Points.totalFloorPenalty(2));
        assertEquals(4, Points.totalFloorPenalty(3));
        assertEquals(6, Points.totalFloorPenalty(4));
        assertEquals(8, Points.totalFloorPenalty(5));
        assertEquals(11, Points.totalFloorPenalty(6));
        assertEquals(14, Points.totalFloorPenalty(7));
    }
}