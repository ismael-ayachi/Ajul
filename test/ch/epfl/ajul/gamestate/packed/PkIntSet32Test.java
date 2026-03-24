package ch.epfl.ajul.gamestate.packed;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PkIntSet32Test {
    @Test
    void pkIntSet32EmptyIsDefinedCorrectly() {
        assertEquals(0, PkIntSet32.EMPTY);
    }

    @Test
    void pkIntSet32ContainsWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet = rng.nextInt();
            for (var j = 0; j < Integer.SIZE; j += 1) {
                var expected = ((pkIntSet >> j) & 1) == 1;
                var actual = PkIntSet32.contains(pkIntSet, j);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    void pkIntSet32ContainsAllWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet1 = rng.nextInt();
            var mask = rng.nextInt();
            var pkIntSet2 = pkIntSet1 & mask;

            assertTrue(PkIntSet32.containsAll(pkIntSet1, pkIntSet2));
            assertTrue(PkIntSet32.containsAll(pkIntSet1, pkIntSet1));
            assertTrue(PkIntSet32.containsAll(pkIntSet2, pkIntSet2));
            if (pkIntSet1 == pkIntSet2) System.out.println("eq");
            assertEquals(pkIntSet1 == pkIntSet2, PkIntSet32.containsAll(pkIntSet2, pkIntSet1));
        }
    }

    @Test
    void pkIntSet32AddWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 100; i += 1) {
            var pkIntSet = PkIntSet32.EMPTY;
            var expected = 0;
            for (var j = 0; j < 10; j += 1) {
                var bit = rng.nextInt(32);
                pkIntSet = PkIntSet32.add(pkIntSet, bit);
                expected |= 1 << bit;
            }
            assertEquals(expected, pkIntSet);
        }
    }

    @Test
    void pkIntSet32RemoveWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 100; i += 1) {
            var pkIntSet = rng.nextInt();
            var expected = pkIntSet;
            for (var j = 0; j < 10; j += 1) {
                var bit = rng.nextInt(32);
                pkIntSet = PkIntSet32.remove(pkIntSet, bit);
                expected &= ~(1 << bit);
            }
            assertEquals(expected, pkIntSet);
        }
    }
}