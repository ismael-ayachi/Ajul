package ch.epfl.ajul.intarray;

class ImmutableIntArrayTest extends ReadOnlyIntArrayTest {
    @Override
    ReadOnlyIntArray newInstance(int[] array) {
        return ImmutableIntArray.copyOf(array);
    }
}