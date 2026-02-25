package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

public record Move(TileSource source, TileKind.Colored tileColor, TileDestination destination) {
    public static final int MAX_MOVES =  TileSource.Factory.COUNT*(TileKind.Colored.COUNT - 1)*TileDestination.COUNT;
    public Move {
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    public static Move ofPacked(short pkMove){
        TileSource pkMoveSource = PkMove.source(pkMove);
        TileKind.Colored pkMoveColor = PkMove.color(pkMove);
        TileDestination pkMoveDestination = PkMove.destination(pkMove);
        return new Move(pkMoveSource,pkMoveColor,pkMoveDestination);
    }
    public short packed() {
        return PkMove.pack(source, tileColor, destination);
    }

}

