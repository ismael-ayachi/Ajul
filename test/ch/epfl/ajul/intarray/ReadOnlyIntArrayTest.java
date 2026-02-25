package ch.epfl.ajul.intarray;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

abstract class ReadOnlyIntArrayTest {
    private final RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    abstract ReadOnlyIntArray newInstance(int[] array);

    @Test
    void readOnlyIntArraySizeReturnsSize() {
        for (var i = 0; i < 100; i += 1) {
            var array = newInstance(new int[i]);
            assertEquals(i, array.size());
        }
    }

    @Test
    void readOnlyIntArrayGetReturnsCorrectElement() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedElements = underlyingArray.clone();
            var array = newInstance(underlyingArray);
            for (var j = 0; j < i; j += 1) assertEquals(expectedElements[j], array.get(j));
        }
    }

    @Test
    void readOnlyIntArrayImmutableReturnsArrayWithCorrectContents() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedElements = underlyingArray.clone();
            var array = newInstance(underlyingArray).immutable();
            assertEquals(array.size(), i);
            for (var j = 0; j < i; j += 1) assertEquals(expectedElements[j], array.get(j));
        }
    }

    @Test
    void readOnlyIntArrayImmutableReturnsImmutableArray() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedElements = underlyingArray.clone();
            var array = newInstance(underlyingArray).immutable();

            Arrays.fill(underlyingArray, rng.nextInt());
            for (var j = 0; j < i; j += 1) assertEquals(expectedElements[j], array.get(j));
        }
    }

    @Test
    void readOnlyIntArrayToArrayReturnsArrayWithCorrectContents() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedArray = underlyingArray.clone();
            var actualArray = newInstance(underlyingArray).toArray();
            assertArrayEquals(expectedArray, actualArray);
        }
    }

    @Test
    void readOnlyIntArrayToArrayReturnsFreshArray() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedArray = underlyingArray.clone();
            var actualArray = newInstance(underlyingArray).toArray();
            assertNotSame(underlyingArray, actualArray);
            Arrays.fill(underlyingArray, rng.nextInt());
            assertArrayEquals(expectedArray, actualArray);
        }
    }

    @Test
    void readOnlyIntArrayToStringReturnsCorrectValue() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i += 1) {
            var underlyingArray = new int[i];
            for (var j = 0; j < i; j += 1) underlyingArray[j] = rng.nextInt();

            var expectedString = Arrays.toString(underlyingArray);
            var actualString = newInstance(underlyingArray).toString();
            assertEquals(expectedString, actualString);
        }
    }
}