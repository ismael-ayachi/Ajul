package ch.epfl.ajul.gamestate.packed;

public final class PkIntSet32 {
    public static final int EMPTY = 0;
    public static final int INTSET_MASK = 0b1;

    public static boolean contains(int pkIntSet32, int i) {
        assert isIndexValid(i);
        return (((pkIntSet32 >> i) & INTSET_MASK) == 1);
    }

    public static boolean containsAll(int pkIntSet32a, int pkIntSet32b) {
        return (pkIntSet32a == pkIntSet32b); // ???
    }

    public static int add(int pkIntSet32, int i) {
        assert isIndexValid(i);
        if (!contains(pkIntSet32,i)){
            return (pkIntSet32 + (INTSET_MASK << i));
        }
        else {
            return pkIntSet32;
        }

    }

    public static int remove(int pkIntSet32, int i){
        assert isIndexValid(i);
        if (!contains(pkIntSet32,i)) {
            return (pkIntSet32 - (INTSET_MASK << i));
        }
        else {
            return pkIntSet32;
        }

    }

    private static boolean isIndexValid(int i){
        return i >= 0; // Condition à rajouter ?

    }


}
