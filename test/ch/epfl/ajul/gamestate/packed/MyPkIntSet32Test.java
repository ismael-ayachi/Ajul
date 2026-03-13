package ch.epfl.ajul.gamestate.packed;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyPkIntSet32Test {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    // ===================== EMPTY =====================

    @Test
    void pkIntSet32EmptyIsCorrectlyDefined() {
        assertEquals(0, PkIntSet32.EMPTY);
    }

    // ===================== contains =====================

    @Test
    void pkIntSet32ContainsReturnsFalseOnEmptySet() {
        var remaining = 32;
        for (var i = 0; i <= 31; i += 1) {
            assertFalse(PkIntSet32.contains(PkIntSet32.EMPTY, i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkIntSet32ContainsReturnsTrueForEachSingleBit() {
        var remaining = 32;
        for (var i = 0; i <= 31; i += 1) {
            var pkIntSet32 = 1 << i;
            assertTrue(PkIntSet32.contains(pkIntSet32, i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkIntSet32ContainsReturnsFalseForOtherBits() {
        var remaining = 32 * 31;
        for (var i = 0; i <= 31; i += 1) {
            var pkIntSet32 = 1 << i;
            for (var j = 0; j <= 31; j += 1) {
                if (j != i) {
                    assertFalse(PkIntSet32.contains(pkIntSet32, j));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkIntSet32ContainsWorksOnRandomSets() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            var expected = ((pkIntSet32 >> bit) & 1) == 1;
            assertEquals(expected, PkIntSet32.contains(pkIntSet32, bit));
        }
    }

    // ===================== containsAll =====================

    @Test
    void pkIntSet32ContainsAllReturnsTrueForEmptyInEmpty() {
        assertTrue(PkIntSet32.containsAll(PkIntSet32.EMPTY, PkIntSet32.EMPTY));
    }

    @Test
    void pkIntSet32ContainsAllReturnsTrueForEmptyInFull() {
        assertTrue(PkIntSet32.containsAll(-1, PkIntSet32.EMPTY));
    }

    @Test
    void pkIntSet32ContainsAllReturnsFalseForFullInEmpty() {
        assertFalse(PkIntSet32.containsAll(PkIntSet32.EMPTY, -1));
    }

    @Test
    void pkIntSet32ContainsAllReturnsTrueForFullInFull() {
        assertTrue(PkIntSet32.containsAll(-1, -1));
    }

    @Test
    void pkIntSet32ContainsAllReturnsTrueWhenBIsSubsetOfA() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var a = rng.nextInt();
            var b = a & rng.nextInt(); // b est un sous-ensemble de a
            assertTrue(PkIntSet32.containsAll(a, b));
        }
    }

    @Test
    void pkIntSet32ContainsAllReturnsFalseWhenBHasExtraBit() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var a = rng.nextInt();
            var bit = rng.nextInt(32);
            if (PkIntSet32.contains(a, bit)) continue;
            var b = a | (1 << bit);
            assertFalse(PkIntSet32.containsAll(a, b));
        }
    }

    @Test
    void pkIntSet32ContainsAllWorksForEachSingleBit() {
        var remaining = 32;
        for (var i = 0; i <= 31; i += 1) {
            assertTrue(PkIntSet32.containsAll(-1, 1 << i));
            assertFalse(PkIntSet32.containsAll(PkIntSet32.EMPTY, 1 << i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== add =====================

    @Test
    void pkIntSet32AddMakesBitPresent() {
        var remaining = 32;
        for (var i = 0; i <= 31; i += 1) {
            assertTrue(PkIntSet32.contains(PkIntSet32.add(PkIntSet32.EMPTY, i), i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkIntSet32AddDoesNotAffectOtherBits() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            var result = PkIntSet32.add(pkIntSet32, bit);
            for (var j = 0; j <= 31; j += 1)
                if (j != bit)
                    assertEquals(PkIntSet32.contains(pkIntSet32, j), PkIntSet32.contains(result, j));
        }
    }

    @Test
    void pkIntSet32AddIsIdempotent() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            var once = PkIntSet32.add(pkIntSet32, bit);
            var twice = PkIntSet32.add(once, bit);
            assertEquals(once, twice);
        }
    }

    // ===================== remove =====================

    @Test
    void pkIntSet32RemoveMakesBitAbsent() {
        var remaining = 32;
        for (var i = 0; i <= 31; i += 1) {
            assertFalse(PkIntSet32.contains(PkIntSet32.remove(-1, i), i));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkIntSet32RemoveDoesNotAffectOtherBits() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            var result = PkIntSet32.remove(pkIntSet32, bit);
            for (var j = 0; j <= 31; j += 1)
                if (j != bit)
                    assertEquals(PkIntSet32.contains(pkIntSet32, j), PkIntSet32.contains(result, j));
        }
    }

    @Test
    void pkIntSet32RemoveIsIdempotent() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            var once = PkIntSet32.remove(pkIntSet32, bit);
            var twice = PkIntSet32.remove(once, bit);
            assertEquals(once, twice);
        }
    }

    @Test
    void pkIntSet32RemoveOnAbsentBitIsNoOp() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            if (PkIntSet32.contains(pkIntSet32, bit)) continue;
            assertEquals(pkIntSet32, PkIntSet32.remove(pkIntSet32, bit));
        }
    }

    // ===================== add + remove =====================

    @Test
    void pkIntSet32AddThenRemoveOnAbsentBitGivesOriginal() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            if (PkIntSet32.contains(pkIntSet32, bit)) continue;
            assertEquals(pkIntSet32, PkIntSet32.remove(PkIntSet32.add(pkIntSet32, bit), bit));
        }
    }

    @Test
    void pkIntSet32RemoveThenAddOnPresentBitGivesOriginal() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkIntSet32 = rng.nextInt();
            var bit = rng.nextInt(32);
            if (!PkIntSet32.contains(pkIntSet32, bit)) continue;
            assertEquals(pkIntSet32, PkIntSet32.add(PkIntSet32.remove(pkIntSet32, bit), bit));
        }
    }
}