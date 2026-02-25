package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

public final class PkMove {
    static final int SOURCE_OFFSET = 0;
    static final int SOURCE_BITS = 4;
    static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;

    static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;
    static final int COLOR_BITS  = 3;
    static final int COLOR_MASK = (1 << COLOR_BITS) - 1;

    static final int DESTINATION_OFFSET =   COLOR_OFFSET + COLOR_BITS;
    static final int DESTINATION_BITS = 3;
    static final int DESTINATION_MASK = (1 << DESTINATION_BITS) - 1;
    static short pkMove;


    public static short pack(TileSource source, TileKind.Colored color, TileDestination destination) {
        PkMove.pkMove = (short) ((source.index() & SOURCE_MASK) << SOURCE_OFFSET
                | ((color.index() & COLOR_MASK) << COLOR_OFFSET)
                | ((destination.index() & DESTINATION_MASK) << DESTINATION_OFFSET));
        return PkMove.pkMove;
    }
    public static TileSource source(short pkMove) {
        int sourceIndex = (pkMove >> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }
    public static TileKind.Colored color(short pkMove) {
        int colorIndex = (pkMove >> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(colorIndex);
    }

    public static TileDestination destination(short pkMove) {
        int destinationIndex = (pkMove >> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(destinationIndex);
    }
}
