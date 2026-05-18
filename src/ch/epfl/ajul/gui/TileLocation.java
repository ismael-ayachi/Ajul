package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

public sealed interface TileLocation {

    record OffBoard(TileKind tileKind, int index) implements TileLocation {}
    record OnSource(TileSource tileSource, int index) implements TileLocation {}
    record OnPattern(PlayerId playerId, TileDestination.Pattern pattern, int index) implements TileLocation {}
    record OnWall(PlayerId playerId, TileDestination.Pattern pattern, TileKind.Colored tileKind)
            implements TileLocation {}
    record OnFloor(PlayerId playerId, int index) implements TileLocation {}

}
