package ch.epfl.ajul.gamestate.packed;


import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

public final class PkTileSet {

    static final int TILE_BITS_MASK = 0b11111;

    static final int COLOR_OFFSET_A = 0;
    static final int COLOR_BITS_A = 5;
    static final int COLOR_MASK_A = (1 << COLOR_BITS_A) - 1;

    static final int COLOR_OFFSET_B = COLOR_OFFSET_A + COLOR_BITS_A + 1; //On rajoute +1 car on cherche un offset de 6 sauf que pour A on a 5 bits (6e exclu car 0)
    static final int COLOR_BITS_B = 5;
    static final int COLOR_MASK_B = (1 << COLOR_BITS_B) - 1;

    static final int COLOR_OFFSET_C = COLOR_OFFSET_B + COLOR_BITS_B + 1; //On rajoute +1 car on cherche un offset de 6 sauf que pour A on a 5 bits (6e exclu car 0)
    static final int COLOR_BITS_C = 5;
    static final int COLOR_MASK_C = (1 << COLOR_BITS_C) - 1;

    static final int COLOR_OFFSET_D = COLOR_OFFSET_C + COLOR_BITS_C + 1; //On rajoute +1 car on cherche un offset de 6 sauf que pour A on a 5 bits (6e exclu car 0)
    static final int COLOR_BITS_D = 5;
    static final int COLOR_MASK_D = (1 << COLOR_BITS_D) - 1;

    static final int COLOR_OFFSET_E = COLOR_OFFSET_D + COLOR_BITS_D + 1; //On rajoute +1 car on cherche un offset de 6 sauf que pour A on a 5 bits (6e exclu car 0)
    static final int COLOR_BITS_E = 5;
    static final int COLOR_MASK_E = (1 << COLOR_BITS_E) - 1;

    static final int COLOR_OFFSET_M = COLOR_OFFSET_E + COLOR_BITS_E + 1; //On rajoute +1 car on cherche un offset de 6 sauf que pour A on a 5 bits (6e exclu car 0)
    static final int COLOR_BITS_M = 1;
    static final int COLOR_MASK_M = (1 << COLOR_BITS_M) - 1;

    public static final int EMPTY = 0;
    public static final int FULL = computeFull(); //5*20 + 1
    public static final int FULL_COLORED = 100; // 5*20

    static int numberOfTileA(int pkTile) {

        return (pkTile >> COLOR_OFFSET_A) & COLOR_MASK_A;
    }
    static int numberOfTileB(int pkTile) {
        return (pkTile >> COLOR_OFFSET_B) & COLOR_MASK_B;

    }
    static int numberOfTileC(int pkTile) {
        return (pkTile >> COLOR_OFFSET_C) & COLOR_MASK_C;
    }
    static int numberOfTileD(int pkTile) {
        return (pkTile >> COLOR_OFFSET_D) & COLOR_MASK_D;
    }
    static int numberOfTileE(int pkTile) {
        return (pkTile >> COLOR_OFFSET_E) & COLOR_MASK_E;
    }
    static int numberOfTileFirstPlayerMarker(int pkTile) {
        return (pkTile >> COLOR_OFFSET_M) & COLOR_MASK_M;
    }

    public static int of(int count, TileKind tileKind){
        return count << tileKind.index()*6;
    }

    public static boolean isEmpty(int pkTileSet){
        return pkTileSet == EMPTY;
    }

    public static int size(int pkTileTileSet) {
        int somme1 = pkTileTileSet + pkTileTileSet>>6;
        int extract_1 = somme1 & COLOR_MASK_A;
        int extract_2 = (somme1 >>  COLOR_OFFSET_C) & COLOR_MASK_C;
        int extract_3 = (somme1 >> COLOR_OFFSET_E) & COLOR_OFFSET_E;
        return extract_1 + extract_2 + extract_3;
    }

    public static int countOf(int pkTileSet, TileKind tileKind){
        return (pkTileSet >> tileKind.index()*6) & TILE_BITS_MASK;  //Considérer le cas FMP ?
    }
    public static int subsetOf(int pkTileSet, TileKind tileKind) {
        return pkTileSet & (TILE_BITS_MASK << tileKind.index()*6);
    }

    public static int add(int pkTileSet, TileKind tileKind) {
        return pkTileSet + (1 << tileKind.index()*6);
    }
    public static int remove(int pkTileSet, TileKind tileKind){
        return pkTileSet - (1 << tileKind.index()*6);
    }
    public static int union(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 + pkTileSet2;
    }
    public static int difference(int pkTileSet1, int pkTileSet2){
        return pkTileSet1 - pkTileSet2;
    }
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination){
        int counter = 0;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int numberOfTile_X = countOf(pkTileSet, tileKind);
            for (int i=0; i<=numberOfTile_X; i++) {
                //destination[counter] = tileKind;
                Arrays.fill(destination, tileKind);
                counter++;
            }
        }
        return counter;
    }
    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator){
        int i = offset;
        for (TileKind.Colored tileKind: TileKind.Colored.ALL) {
            int numberOfTile_X = countOf(pkTileSet, tileKind);
            for (int y=0; y < numberOfTile_X; y++){
                if (i == offset) {
                    destination[offset] = tileKind;
                }
                else {
                    int j = randomGenerator.nextInt(offset, i+1);
                    if (j==offset) {
                        destination[offset] = tileKind;
                    }
                }
                i++;
            }
        }
        return i;
    }

    public static String toString(int pkTileSet) {
        StringJoiner j = new StringJoiner(",");
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int numberOfTile_X = countOf(pkTileSet, tileKind);
            if (numberOfTile_X > 0) {
                j.add(numberOfTile_X + "*" + tileKind.name());
            }
        }
        int numberOfTile_M = countOf(pkTileSet, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER);
        if (numberOfTile_M > 0){
            j.add(numberOfTile_M + "*" + TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER.name());
        }

        return "{" + j + "}";

    }
    private static int computeFull() {
        int tilesOfA = of(  20, TileKind.A);
        int tilesOfB = of(  20, TileKind.B);
        int tilesOfC = of(  20, TileKind.C);
        int tilesOfD = of(  20, TileKind.D);
        int tilesOfE = of(  20, TileKind.E);
        int tilesOfM = of(  1, TileKind.FIRST_PLAYER_MARKER);

        int union1 = union(tilesOfA,tilesOfB);
        int union2 = union(tilesOfC, tilesOfD);
        int union3 = union(tilesOfE,tilesOfM);

        return union(union(union1,union2),union3);

    }
}
