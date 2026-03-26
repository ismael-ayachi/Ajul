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
        return ImmutableIntArray.copyOf(pkPlayerStates);
    }

    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    public void fillFactories(RandomGenerator randomGenerator) {

        int tilesNeeded = game().factoriesCount()*TileSource.Factory.TILES_PER_FACTORY;
        TileKind.Colored[] coloredTiles = new TileKind.Colored[tilesNeeded];
        if (PkTileSet.size(pkTileBag()) > tilesNeeded){ //Transformation ternaire ?
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
        for (int i=1; i <= game().factoriesCount() ; i++){
            for (int j=0; j < TileSource.Factory.TILES_PER_FACTORY; j++){
                pkTileSources[i] = PkTileSet.add(pkTileSources[i], coloredTiles[coloredTilesIndex]);
                coloredTilesIndex++;
            }
        }
        pkUniqueTileSourcesUpdate();
    }

    private void pkUniqueTileSourcesUpdate() {
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
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i);
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
        int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), currentPlayerId());
        int pkPatternPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());
        for (int i = 0; i < pkTileSourceColorCount; i++){
            pkTileSourcePlayerMove = PkTileSet.remove(pkTileSourcePlayerMove, playerMoveColor);
        }
        pkTileSources[playerMoveSource.index()] = pkTileSourcePlayerMove;

        if (playerMoveSource instanceof TileSource.Factory && pkTileSourcePlayerMove != PkTileSet.EMPTY) {
            pkTileSources[0] = PkTileSet.union(pkTileSources().get(0), pkTileSourcePlayerMove);
            pkTileSources[playerMoveSource.index()] = PkTileSet.EMPTY;
        }


        else if (playerMoveSource instanceof TileSource.CenterArea &&
                PkTileSet.countOf(pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER) == 1) {
            pkTileSources[0] = PkTileSet.remove(pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER);
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer, PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);

        }


        if (playerMoveDestination instanceof TileDestination.Pattern line) {
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

        pkUniqueTileSourcesUpdate();


        /*if (currentPlayerId().ordinal() + 1 < playerIds().size()) {
            currentPlayerId = playerIds().get(currentPlayerId().ordinal() + 1);
        }

         */
        currentPlayerId = playerIds().get((currentPlayerId().ordinal() + 1) % playerIds().size());
    }

    public void endRound() {

        for (PlayerId playerId : game().playerIds()){
            int pkPatternsPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), playerId);
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), playerId);

            for (TileDestination.Pattern line: TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(pkPatternsPlayer, line)){
                    TileKind.Colored pkPatternColorLine = PkPatterns.color(pkPatternsPlayer, line);
                    pkWallPlayer = PkWall.withTileAt(pkWallPlayer, line, pkPatternColorLine);
                    PkPlayerStates.setPkWall(pkPlayerStates, playerId, pkWallPlayer);
                    pkPatternsPlayer = PkPatterns.withEmptyLine(pkPatternsPlayer, line);
                    PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, pkPatternsPlayer);
                    int hGroupSizeWallPlayer = PkWall.hGroupSize(pkWallPlayer, line, pkPatternColorLine);
                    int vGroupSizeWallPlayer = PkWall.vGroupSize(pkWallPlayer, line, pkPatternColorLine);
                    int wallTilePointsPlayer = Points.newWallTilePoints(hGroupSizeWallPlayer,vGroupSizeWallPlayer);
                    pointsObserver.newWallTile(playerId, line, pkPatternColorLine, wallTilePointsPlayer);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, wallTilePointsPlayer);
                }
            }
            int pointsPlayer = PkPlayerStates.points(pkPlayerStates(), playerId);
            if (PkFloor.size(pkFloorPlayer) != 0) {
                int floorPenaltyPlayer = Points.totalFloorPenalty(PkFloor.size(pkFloorPlayer));
                if (floorPenaltyPlayer <= pointsPlayer) { //Transformation ternaire
                    pointsObserver.floor(playerId, floorPenaltyPlayer);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, - (floorPenaltyPlayer));

                }
                else {
                    pointsObserver.floor(playerId, pointsPlayer);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, - (pointsPlayer));
                }

                if (PkFloor.containsFirstPlayerMarker(pkFloorPlayer)) {
                    pkTileSources[0] = PkTileSet.add(pkTileSources[0], TileKind.FIRST_PLAYER_MARKER);
                    pkUniqueTileSourcesUpdate();
                    pkFloorPlayer = PkFloor.EMPTY;
                    PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloorPlayer);
                    currentPlayerId = playerId; // À vérifier
                }
                else if (PkTileSet.countOf(pkTileSources[0], TileKind.FIRST_PLAYER_MARKER) == 1){ // À supprimer ?
                    pkFloorPlayer = PkFloor.EMPTY;
                    PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloorPlayer);
                    currentPlayerId = currentPlayerId();
                }
            }

        }

    }

    public void endGame() {
        for (PlayerId playerId : game().playerIds()) {
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL){
                if (PkWall.isRowFull(pkWallPlayer, line)){
                    pointsObserver.fullRow(playerId, line, Points.FULL_ROW_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_ROW_BONUS_POINTS);
                }
            }

            for (int col = 0; col < PkWall.WALL_WIDTH; col++){  // À optimiser ?
                if (PkWall.isColumnFull(pkWallPlayer, col)) {
                    pointsObserver.fullColumn(playerId, col,
                            Points.FULL_COLUMN_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLUMN_BONUS_POINTS);
                }
            }

            for (TileKind.Colored colored : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(pkWallPlayer, colored)){
                    pointsObserver.fullColor(playerId, colored, Points.FULL_COLOR_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLOR_BONUS_POINTS);
                }
            }


        }
        
    }

}


