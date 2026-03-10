package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PkWall {
    public static final int EMPTY = 0;
    public static final int WALL_WIDTH = 5; //Ou alors TileKind.Colored.COUNT ?
    public static final int WALL_HEIGHT = 5;
    public static final int ROW0_MASK = 0b00000_00000_00000_00000_11111;
    public static final int COLUMN_MASK = 0b1;
    public static final int COLOR_A_MASK = 0b10000_01000_00100_00010_00001;
    public static final int COLOR_B_MASK = 0b00001_10000_01000_00100_00010;
    public static final int COLOR_C_MASK = 0b00010_00001_10000_01000_00100;
    public static final int COLOR_D_MASK = 0b00100_00010_00001_10000_01000;
    public static final int COLOR_E_MASK = 0b01000_00100_00010_00001_10000;
    public static final ArrayList<Integer> COLOR_MASK_LIST = new ArrayList<>(List.of(COLOR_A_MASK, COLOR_B_MASK, COLOR_C_MASK,
            COLOR_D_MASK, COLOR_E_MASK));


    public static int indexOf(TileDestination.Pattern line, TileKind.Colored color) {
        return (line.index() * WALL_WIDTH) + column(line, color);
    }
    public static int column(TileDestination.Pattern line, TileKind.Colored color){
        return (color.index() + line.index()) % WALL_WIDTH;
    }

    public static TileKind.Colored colorAt(TileDestination.Pattern line, int column) {
        return TileKind.Colored.ALL.get((line.index()*4 + column) % 5);
    }

    public static int withTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        return pkWall + (1 << indexOf(line, color));
    }

    public static boolean hasTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        return ((pkWall >> indexOf(line, color)) == 1); //1 = PkIntSet32.contains(pkWall, 1); ?
    }

    public static int hGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        //return (pkWall & (ROW0_MASK << (line.index() * WALL_WIDTH))); // ???
        assert hasTileAt(pkWall, line, color);
        int hGroupSizeColor = 1;
        int lineSize = WALL_WIDTH - Integer.bitCount((pkWall >> (WALL_WIDTH*line.index())) & ROW0_MASK);
        if (lineSize == WALL_WIDTH) {
            return WALL_WIDTH;
        }
        for (int i = 1; i <= lineSize ; i++) {

            if (hasTileAt(pkWall, line, TileKind.Colored.ALL.get(color.index() + i))) {
                hGroupSizeColor++;
            }
            else if( ((color.index() - i) >= 0) && hasTileAt(pkWall, line, TileKind.Colored.ALL.get(color.index() - i ))) {
                hGroupSizeColor++;
            }

        }
        //int extractPkWallLine = (pkWall & (ROW0_MASK << (line.index() * WALL_WIDTH))) & COLOR_MASK_LIST.get(color.index());
        return hGroupSizeColor;
    }

    public static int vGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert hasTileAt(pkWall, line, color);
        int vGroupSizeColor = 1;
        int columnSize = WALL_HEIGHT - Integer.bitCount(columnSize(pkWall, column(line, color)));

        return 1;
    }

    public static boolean hasFullRow(int pkWall) {
        boolean fullRow = false;
        for (int i=0; i < WALL_WIDTH; i++) {
            fullRow = ((pkWall >> (WALL_WIDTH * i)) & ROW0_MASK) == ROW0_MASK ;
        }
        return fullRow;
    }

    public static boolean isRowFull(int pkWall, TileDestination.Pattern line){
        return ((pkWall >> (WALL_WIDTH * line.index())) & ROW0_MASK) == ROW0_MASK;
    }

    public static boolean isColumnFull(int pkWall, int column) {
        return columnSize(pkWall,column) == WALL_HEIGHT;
    }

    private static int columnSize(int pkWall, int column) {
        int sum = 0;
        for (int i=0; i < WALL_HEIGHT; i++) {
            sum = sum + ((pkWall >> (column * WALL_HEIGHT)) & COLUMN_MASK);
        }
        return sum;
    }

    public static boolean isColorFull(int pkWall, TileKind.Colored color) {

        return (pkWall & (COLOR_MASK_LIST.get(color.index()))) == COLOR_MASK_LIST.get(color.index());
    }

    public static int asPkTileSet(int pkWall){
        int newPkTileSet = 0;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int tileKindCount = 0;
            for (int i=0; i < WALL_WIDTH; i++) {
               tileKindCount += Integer.bitCount(((pkWall & (ROW0_MASK << (WALL_WIDTH * i))) & COLOR_MASK_LIST.get(tileKind.index())));
            }
            newPkTileSet = PkTileSet.union(newPkTileSet, PkTileSet.of(tileKindCount, tileKind));
        }

        return newPkTileSet;
    }

    public static String toString(int pkWall) {
        StringBuilder b = new StringBuilder();
        for (TileDestination.Pattern line: TileDestination.Pattern.ALL) {
            for (int i=0; i < WALL_HEIGHT; i++) {
                if (hasTileAt(pkWall, line, colorAt(line, i))) {
                    b.append(colorAt(line,i).toString().toUpperCase());
                }
                else {
                    b.append(colorAt(line, i).toString().toLowerCase());
                }
            }
            b.append(", ");
        }

        return b.toString();

    }
}
