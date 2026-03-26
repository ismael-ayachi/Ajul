package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyPointsObserverTest {

    @Test
    void pointsObserverEmptyIsNotNull() {
        assertNotNull(PointsObserver.EMPTY);
    }

    @Test
    void pointsObserverEmptyNewWallTileDoesNothing() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.newWallTile(
                PlayerId.P1, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A, 3));
    }

    @Test
    void pointsObserverEmptyFloorDoesNothing() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.floor(PlayerId.P1, 4));
    }

    @Test
    void pointsObserverEmptyFullRowDoesNothing() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullRow(
                PlayerId.P1, TileDestination.Pattern.PATTERN_1, Points.FULL_ROW_BONUS_POINTS));
    }

    @Test
    void pointsObserverEmptyFullColumnDoesNothing() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullColumn(PlayerId.P1, 0, Points.FULL_COLUMN_BONUS_POINTS));
    }

    @Test
    void pointsObserverEmptyFullColorDoesNothing() {
        assertDoesNotThrow(() -> PointsObserver.EMPTY.fullColor(
                PlayerId.P1, TileKind.Colored.A, Points.FULL_COLOR_BONUS_POINTS));
    }
}