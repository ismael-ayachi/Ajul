package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.random.RandomGenerator;

public final class MutableGameState implements ReadOnlyGameState{

    private static Game game;
    private static int pkTileBag;
    private static int[] pkTileSources;
    private static int pkUniqueTileSources;
    private static int[] pkPlayerStates;
    private static PlayerId currentPlayerId;
    private static PointsObserver pointsObserver;

    public MutableGameState(ReadOnlyGameState initalState, PointsObserver pointsObserver){
        game = initalState.game();
        pkTileBag = initalState.pkTileBag();
        pkTileSources = initalState.pkTileSources().toArray();
        pkUniqueTileSources = initalState.pkUniqueTileSources();
        pkPlayerStates = initalState.pkPlayerStates().toArray();
        currentPlayerId = initalState.currentPlayerId();
        this.pointsObserver = pointsObserver;

    }
    public MutableGameState(ReadOnlyGameState initialState){
        this(initialState, PointsObserver.EMPTY);
    }


    @Override
    public Game game() {
        return game;
    }

    @Override
    public int pkTileBag() {
        return pkTileBag;
    }

    @Override
    public ReadOnlyIntArray pkTileSources() {
        return ImmutableIntArray.copyOf(pkTileSources);
    }

    @Override
    public int pkUniqueTileSources() {
        return pkUniqueTileSources;
    }

    @Override
    public ReadOnlyIntArray pkPlayerStates() {
        return pkPlayerStates; //????
    }

    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    public void fillFactories(RandomGenerator randomGenerator) {

        int tilesNeeded = game().factoriesCount()*TileSource.Factory.TILES_PER_FACTORY;
        TileKind.Colored[] coloredTiles = new TileKind.Colored[tilesNeeded];
        if (PkTileSet.size(pkTileBag()) > tilesNeeded){
            PkTileSet.sampleColoredInto(pkTileBag(), coloredTiles, 0, randomGenerator);
            for (TileKind.Colored colored : coloredTiles) {
                pkTileBag = PkTileSet.remove(pkTileBag, colored);
            }
        }

        else {
            int pkTileBagNotDiscarded = pkTileBag();
            PkTileSet.sampleColoredInto(pkTileBagNotDiscarded, coloredTiles, 0, randomGenerator);
            pkTileBag = PkTileSet.difference(pkTileBag,pkTileBag);
            pkTileBag = PkTileSet.union(pkTileBag, pkDiscardedTiles());
            PkTileSet.sampleColoredInto(pkTileBag, coloredTiles, PkTileSet.size(pkTileBagNotDiscarded), randomGenerator);
        }


        TileKind.Colored.shuffle(coloredTiles, randomGenerator);
        int coloredTilesIndex = 0;
        for (int i=0; i < game().factoriesCount() ; i++){
            for (int j=0; j < TileSource.Factory.TILES_PER_FACTORY; j++){
                pkTileSources[i] = PkTileSet.add(pkTileSources[i], coloredTiles[coloredTilesIndex]);
                coloredTilesIndex++;
            }
        }

        pkUniqueTileSources = PkIntSet32.EMPTY;  // Nécessaire pour éviter une accumulation si plusieurs appels ?
        for (int i=pkTileSources().size() - 1 ; i >= 0; i--) {
            boolean isNotSame = false;
            int pkTileSource = pkTileSources().get(i);
            for (int j=0; j < i; j++) {
                for (TileKind.Colored tileKind: TileKind.Colored.ALL) {
                    isNotSame = isNotSame || (!(PkTileSet.isEmpty(pkTileSource)) &&
                            PkTileSet.countOf(pkTileSource, tileKind) != 0);
                }
                if ((pkTileSources().get(j) == pkTileSource)){
                    isNotSame = false;
                    break;
                }

            }
            if (isNotSame){
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i );
            }

        }
    }

    public void registerMove(short pkMove) {
        Move playerMove = Move.ofPacked(pkMove);
        TileSource playerMoveSource = playerMove.source();
        TileKind.Colored playerMoveColor = playerMove.tileColor();
        TileDestination playerMoveDestination = playerMove.destination();
        int pkTileSourcePlayerMove = pkTileSources[playerMoveSource.index()];
        int pkTileSourceColorCount = PkTileSet.countOf(pkTileSources().get(playerMoveSource.index()), playerMoveColor);
        int pkFloorPlayer = PkFloor.EMPTY;
        int pkPatternPlayer = PkPatterns.EMPTY;
        for (int i = 0; i < pkTileSourceColorCount; i++){
            pkTileSourcePlayerMove = PkTileSet.remove(pkTileSourcePlayerMove, playerMoveColor);
        }

        if (playerMoveSource instanceof TileSource.Factory && pkTileSourcePlayerMove != PkTileSet.EMPTY) {
            pkTileSources[0] = PkTileSet.union(pkTileSources().get(0), pkTileSourcePlayerMove);
        }

        else if (playerMoveSource instanceof TileSource.CenterArea &&
                PkTileSet.countOf(pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER) == 1) {
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);

        }


        else if (playerMoveDestination instanceof TileDestination.Pattern line) {
            int remainingTilesPkPattern = (line.capacity() - PkPatterns.size(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()), line));
            if (PkPatterns.canContain(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()),line, playerMoveColor)
                    && (remainingTilesPkPattern < pkTileSourceColorCount)) {


                pkPatternPlayer = PkPatterns.withAddedTiles(pkPatternPlayer, line, remainingTilesPkPattern, playerMoveColor);
                PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId(), pkPatternPlayer);
                int remainingTileCount = pkTileSourceColorCount - remainingTilesPkPattern;
                pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer, PkTileSet.of(remainingTileCount, playerMoveColor));
                PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
            }

            else if (PkPatterns.canContain(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()),line, playerMoveColor)
                    && (remainingTilesPkPattern >= pkTileSourceColorCount)){
                pkPatternPlayer = PkPatterns.withAddedTiles(pkPatternPlayer, line,
                        pkTileSourceColorCount, playerMoveColor);
                PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId(), pkPatternPlayer);
            }

        }

        else if (playerMoveDestination instanceof TileDestination.Floor) {
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer, PkTileSet.of(pkTileSourceColorCount, playerMoveColor));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
        }

        if (currentPlayerId().ordinal() + 1 < playerIds().size()) {
            currentPlayerId = playerIds().get(currentPlayerId().ordinal() + 1);
        }
    }
}

