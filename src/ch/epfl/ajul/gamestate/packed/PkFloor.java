package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;


import java.util.ArrayList;



public final class PkFloor {

    private static final int PATTERN_FLOOR_MASK = 0b111;
    private static final int FLOOR_BITS = 3;
    public static final int EMPTY = 0;

    public static int size(int pkFloor) {
      return pkFloor & PATTERN_FLOOR_MASK;
    }

    public static TileKind tileAt(int pkFloor, int i) {
        assert isValid(pkFloor, i);
        int extractColor = (pkFloor >> (i+1)*FLOOR_BITS) & PATTERN_FLOOR_MASK;
        return TileKind.ALL.get(extractColor);
    }

    private static boolean isValid(int pkFloor, int i){
        return (i>=0) && (i < size(pkFloor));
    }

    public static int withAddedTiles(int pkFloor, int pkTileSet) { //A compléter
        int pkFloorSize = size(pkFloor);
        int pkFloorUpdated = pkFloor;

        if (pkFloorSize == 7) {
            if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 0){
                return pkFloorUpdated;
            }
            else {
                pkFloorUpdated &= ~(PATTERN_FLOOR_MASK << (FLOOR_BITS * 7));
                pkFloorUpdated += (TileKind.FIRST_PLAYER_MARKER.index() << (FLOOR_BITS * 7));
            }
            return pkFloorUpdated;
        }

        if ((pkFloorSize < 7)) {

            for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
                int tileKindCountToAdd = Math.min(PkTileSet.countOf(pkTileSet, tileKind), 7 - pkFloorSize);
                int targetSize = pkFloorSize + tileKindCountToAdd;
                for (int i=pkFloorSize; i < targetSize; i++) {

                    pkFloorUpdated += (tileKind.index() << (FLOOR_BITS * (i + 1)));
                    pkFloorUpdated++;
                    pkFloorSize++;
                }
            }
            if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 1) {
                pkFloorUpdated += (TileKind.FIRST_PLAYER_MARKER.index() << (FLOOR_BITS * (pkFloorSize + 1)));
                pkFloorUpdated++;
            }
        }
        return pkFloorUpdated;
    }

    public static boolean containsFirstPlayerMarker(int pkFloor) {
        boolean containsFirstPlayerMarker = false;
        for (int i = 0; i < size(pkFloor); i++) {
            containsFirstPlayerMarker = containsFirstPlayerMarker || (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER);
        }
        return containsFirstPlayerMarker;
    }

    public static int asPkTileSet(int pkFloor) {
        int newPkTileSet = 0;
        /*ArrayList<TileKind> tileKindArray = new ArrayList<>();
        for (int i=0; i < size(pkFloor); i++) {
            tileKindArray.add(tileAt(pkFloor,i));
        }
        for (TileKind tileKind: TileKind.ALL) {
            int count = Collections.frequency(tileKindArray, tileKind);
            newPkTileSet += PkTileSet.of(count, tileKind);
        }*/

        for (int i=0; i < size(pkFloor); i++) {
           newPkTileSet= PkTileSet.add(newPkTileSet, tileAt(pkFloor, i));
        }

        return newPkTileSet;

    }

    public static String toString(int pkFloor) {
        ArrayList<String> strArray= new ArrayList<>();
        for (int i=0; i < size(pkFloor); i++){
            strArray.add(tileAt(pkFloor,i).toString());
        }
        return strArray.toString();
    }


}
