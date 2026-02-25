package ch.epfl.ajul;

import ch.epfl.ajul.TileDestination.Floor;
import ch.epfl.ajul.TileDestination.Pattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TileDestinationTest {
    @Test
    void tileDestinationFieldsAreEnumConstants() {
        assertSame(Pattern.PATTERN_1, TileDestination.PATTERN_1);
        assertSame(Pattern.PATTERN_2, TileDestination.PATTERN_2);
        assertSame(Pattern.PATTERN_3, TileDestination.PATTERN_3);
        assertSame(Pattern.PATTERN_4, TileDestination.PATTERN_4);
        assertSame(Pattern.PATTERN_5, TileDestination.PATTERN_5);
        assertSame(Floor.FLOOR, TileDestination.FLOOR);
    }

    @Test
    void tileDestinationAllIsCorrectlyDefined() {
        var expected = List.of(
                TileDestination.PATTERN_1,
                TileDestination.PATTERN_2,
                TileDestination.PATTERN_3,
                TileDestination.PATTERN_4,
                TileDestination.PATTERN_5,
                TileDestination.FLOOR);
        var actual = TileDestination.ALL;
        assertEquals(expected, actual);
    }

    @Test
    void tileDestinationAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            TileDestination.ALL.clear();
        });
    }

    @Test
    void tileDestinationCountIsCorrectlyDefined() {
        assertEquals(6, TileDestination.COUNT);
    }

    @Test
    void tileDestinationIndexAndAllGetAreInverse() {
        for (var i = 0; i < TileDestination.COUNT; i += 1)
            assertEquals(i, TileDestination.ALL.get(i).index());
    }

    @Test
    void tileDestinationCapacityIsCorrect() {
        for (var tileDestination : TileDestination.ALL) {
            var expectedCapacity = switch (tileDestination) {
                case TileDestination d when d == TileDestination.PATTERN_1 -> 1;
                case TileDestination d when d == TileDestination.PATTERN_2 -> 2;
                case TileDestination d when d == TileDestination.PATTERN_3 -> 3;
                case TileDestination d when d == TileDestination.PATTERN_4 -> 4;
                case TileDestination d when d == TileDestination.PATTERN_5 -> 5;
                case TileDestination d when d == TileDestination.FLOOR -> 7;
                case null, default -> fail();
            };
            assertEquals(expectedCapacity, tileDestination.capacity());
        }
    }

    @Test
    void patternAllIsCorrectlyDefined() {
        assertEquals(List.of(Pattern.PATTERN_1,
                        Pattern.PATTERN_2,
                        Pattern.PATTERN_3,
                        Pattern.PATTERN_4,
                        Pattern.PATTERN_5),
                Pattern.ALL);
    }

    @Test
    void patternAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            Pattern.ALL.clear();
        });
    }

    @Test
    void patternCountIsCorrectlyDefined() {
        assertEquals(5, Pattern.COUNT);
    }
}