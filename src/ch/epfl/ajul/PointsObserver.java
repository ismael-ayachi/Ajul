package ch.epfl.ajul;

public interface PointsObserver {

    PointsObserver EMPTY = null;

    default void newWallTile(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color, int points){}
    default void floor(PlayerId playerId, int penalty){}
    default void fullRow(PlayerId playerId, TileDestination.Pattern line, int points){}
    default void fullColumn(PlayerId playerId, int column, int points){}
    default void fullColor(PlayerId playerId, TileKind.Colored color, int points){}

}
