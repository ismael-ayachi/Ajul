package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import java.util.ArrayList;

public final class PkPatterns {
    private static final int PATTERN_LINE_MASK = 0b111;
    private static final int PATTERN_MASK = (PATTERN_LINE_MASK << 3) | PATTERN_LINE_MASK;

    private static final int PATTERN_LINE_OFFSET = 6;

    private static final int COLOR_BITS_PKTILESET = 5;

    public static final int EMPTY = 0;

    public static int size(int pkPatterns, TileDestination.Pattern line) {
        return ((pkPatterns >> (line.index() * PATTERN_LINE_OFFSET))) & PATTERN_LINE_MASK;
    }
    public static TileKind.Colored color(int pkPatterns, TileDestination.Pattern line) {

        assert isSizeValid(pkPatterns, line);

        return TileKind.Colored.ALL.get((pkPatterns >> (3 + line.index()*PATTERN_LINE_OFFSET )) & PATTERN_LINE_MASK);
    }


    private static boolean isSizeValid(int pkPatterns, TileDestination.Pattern line) {

        return size(pkPatterns, line) > 0;

    }

    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) == line.capacity();
    }

    public static boolean canContain(int pkPatterns, TileDestination.Pattern line, TileKind.Colored color){

        return (size(pkPatterns, line) == EMPTY) ||
                ((color == color(pkPatterns, line)) && size(pkPatterns, line) != EMPTY);

    }

    public static int withAddedTiles(int pkPatterns, TileDestination.Pattern line, int tileCount, TileKind.Colored color) {

        assert isTileValid(pkPatterns, line, tileCount, color);
        if (size(pkPatterns, line) == EMPTY) {
            return pkPatterns + ((tileCount << (line.index()*PATTERN_LINE_OFFSET)) ) +
                    ((color.index() << (3+ line.index()*PATTERN_LINE_OFFSET)) );
        }
        else if (canContain(pkPatterns,line,color)){
            return pkPatterns + ((tileCount << (line.index()*PATTERN_LINE_OFFSET)));

        }
        return pkPatterns;
    }

    private static boolean isTileValid(int pkPatterns, TileDestination.Pattern line, int tileCount, TileKind.Colored color) {
        boolean tilesCountValid = tileCount <= (line.capacity() - size(pkPatterns,line));
        return canContain(pkPatterns, line,color) & (size(pkPatterns, line) != line.capacity()) & tilesCountValid;
    }

    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {

        return pkPatterns & ~(PATTERN_MASK << line.index() * PATTERN_LINE_OFFSET);

    }

    public static int asPkTileSet(int pkPatterns){
        int pkTileSet = 0;
        for (TileDestination.Pattern line: TileDestination.Pattern.ALL) {
            if (size(pkPatterns, line) > 0){ //Condition à vérifier
                TileKind.Colored lineColor = color(pkPatterns, line);
                int numberOfColor = size(pkPatterns, line);
                pkTileSet += (numberOfColor << (lineColor.index() * (COLOR_BITS_PKTILESET + 1)));
            }

        }
        return pkTileSet;
    }

    public static String toString(int pkPatterns) {

        ArrayList<String> strArray= new ArrayList<>();

        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            int repeatPatternLine = size(pkPatterns, line);

            if (repeatPatternLine == EMPTY) {
                strArray.add(".".repeat(line.capacity()));
            }
            else {
                TileKind.Colored pkPatternsColorLine = color(pkPatterns, line);
                if (repeatPatternLine < line.capacity()){
                    strArray.add((pkPatternsColorLine.toString().repeat(repeatPatternLine)) + ".".repeat(line.capacity() - repeatPatternLine));
                }
                else {
                    strArray.add((pkPatternsColorLine.toString().repeat(repeatPatternLine)));
                }
            }

        }
        return strArray.toString();
    }


}
