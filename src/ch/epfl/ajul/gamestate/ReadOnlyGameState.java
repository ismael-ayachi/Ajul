package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

public interface ReadOnlyGameState {
    abstract Game game();
    abstract int pkTileBag();
    abstract ReadOnlyIntArray pkTileSources();
    abstract int pkUniqueTileSources();
    abstract ReadOnlyIntArray pkPlayerStates();
    abstract PlayerId currentPlayerId();

    default ImmutableGameState immutable() {
        return new ImmutableGameState(game(), pkTileBag(), pkTileSources().immutable(),
                pkUniqueTileSources(), pkPlayerStates().immutable(), currentPlayerId());
    }

    default List<PlayerId> playerIds(){

        return game().playerIds();
    }

    default boolean isRoundOver() {
        return (pkUniqueTileSources() == PkIntSet32.EMPTY);
    }


    default boolean isGameOver() {
        boolean gameOver = false;
        for (int i = 0; i < playerIds().size(); i++) {
            if (isRoundOver() && PkWall.hasFullRow(PkPlayerStates.pkWall(pkPlayerStates(), playerIds().get(i)))){
                gameOver = true;
                return gameOver;
            }
        }
        return gameOver;
    }

    default int pkDiscardedTiles() {
        int tileSourcesSum, pkPatternsSum, pkFloorSum, pkWallSum ;
        tileSourcesSum = pkPatternsSum = pkFloorSum = pkWallSum = 0;
        for (int i = 0; i < playerIds().size(); i++){
            PlayerId playerId = playerIds().get(i);
            int pkPatternPlayers = PkPatterns.asPkTileSet(PkPlayerStates.pkPatterns(pkPlayerStates(), playerId));
            int pkFloorPlayers = PkFloor.asPkTileSet(PkPlayerStates.pkFloor(pkPlayerStates(), playerId));
            int pkWallPlayers = PkWall.asPkTileSet(PkPlayerStates.pkWall(pkPlayerStates(), playerId));
            pkPatternsSum = PkTileSet.union(pkPatternsSum, pkPatternPlayers);
            pkFloorSum = PkTileSet.union(pkFloorSum, pkFloorPlayers);
            pkWallSum = PkTileSet.union(pkWallSum, pkWallPlayers);
        }
        for (int i = 0; i < pkTileSources().size(); i++) {
            tileSourcesSum = PkTileSet.union(tileSourcesSum, pkTileSources().get(i)) ;
        }
        int pkTileSetSum1 = PkTileSet.union(tileSourcesSum, pkPatternsSum);
        int pkTileSetSum2 = PkTileSet.union(pkFloorSum, pkWallSum);
        int pkTileSetSum3 = PkTileSet.union(pkTileSetSum2, pkTileBag());
        int pkTileSetSum = PkTileSet.union(pkTileSetSum1, pkTileSetSum3);
        return PkTileSet.difference(PkTileSet.FULL, pkTileSetSum);
    }

    default int validMoves(short[] destination) {
        return validMovesCommon(destination, true);
    }

    default int uniqueValidMoves(short[] destination) {
        return validMovesCommon(destination, false);
    }

    private int validMovesCommon(short[] destination, boolean bool){
        int count = 0;
        if (destination.length >= Move.MAX_MOVES){
            for (TileSource tileSource : game().tileSources()){
                if (bool || PkIntSet32.contains(pkUniqueTileSources(), tileSource.index())) {
                for (TileKind.Colored tileKindColored : TileKind.Colored.ALL) {
                    for (TileDestination.Pattern tileDestination : TileDestination.Pattern.ALL) {

                        int currentPlayerPkPattern = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());
                        boolean pkPatternCanContain = !PkPatterns.isFull(currentPlayerPkPattern, tileDestination) &&
                                PkPatterns.canContain(currentPlayerPkPattern, tileDestination , tileKindColored) &&
                                !PkWall.hasTileAt(PkPlayerStates.pkWall(pkPlayerStates(),
                                        currentPlayerId()), tileDestination, tileKindColored);

                        if (PkTileSet.countOf(pkTileSources().get(tileSource.index()), tileKindColored)
                                != PkTileSet.EMPTY && pkPatternCanContain) {
                            destination[count] = PkMove.pack(tileSource, tileKindColored, tileDestination);
                            count++;
                        }
                    }

                    if (PkTileSet.countOf(pkTileSources().get(tileSource.index()), tileKindColored)
                            != PkTileSet.EMPTY) {
                        destination[count] = PkMove.pack(tileSource, tileKindColored , TileDestination.FLOOR);
                        count++;

                        }
                    }
                }
            }
        }
        return count;
    }

}

