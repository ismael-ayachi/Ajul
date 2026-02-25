package ch.epfl.ajul.intarray;

class MutableIntArrayTest extends ReadOnlyIntArrayTest {
    @Override
    ReadOnlyIntArray newInstance(int[] array) {
        return MutableIntArray.wrapping(array);
    }
}