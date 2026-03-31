package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.Arrays;
import java.util.random.RandomGenerator;

/// Classe représentant un état de partie non immuable d'Ajul, dont le contenu
/// peut évoluer en fonction des coups joués par les joueurs.
/// Implémente {@link ReadOnlyGameState} et offre des méthodes pour remplir les
/// fabriques, enregistrer un coup, terminer une manche et terminer une partie.
///
/// @author Ismaël Ayachi (393163)
public final class MutableGameState implements ReadOnlyGameState {
    private static final int CENTER_AREA_INDEX = TileSource.CENTER_AREA.index();

    private final Game game;
    private int pkTileBag;
    private final int[] pkTileSources;
    private int pkUniqueTileSources;
    private final int[] pkPlayerStates;
    private PlayerId currentPlayerId;
    private final PointsObserver pointsObserver;

    /// Construit un état de partie mutable ayant le même contenu que {@code initialState},
    /// auquel l'observateur de points {@code pointsObserver} est attaché.
    ///
    /// @param initialState   l'état initial de la partie
    /// @param pointsObserver l'observateur de points à notifier lors des changements de score
    public MutableGameState(ReadOnlyGameState initialState, PointsObserver pointsObserver) {
        game = initialState.game();
        pkTileBag = initialState.pkTileBag();
        pkTileSources = initialState.pkTileSources().toArray();
        pkUniqueTileSources = initialState.pkUniqueTileSources();
        pkPlayerStates = initialState.pkPlayerStates().toArray();
        currentPlayerId = initialState.currentPlayerId();
        this.pointsObserver = pointsObserver;
    }

    /// Construit un état de partie mutable ayant le même contenu que {@code initialState},
    /// avec un observateur de points vide.
    ///
    /// @param initialState l'état initial de la partie
    public MutableGameState(ReadOnlyGameState initialState) {
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
        return MutableIntArray.wrapping(pkTileSources);
    }

    @Override
    public int pkUniqueTileSources() {
        return pkUniqueTileSources;
    }

    @Override
    public ReadOnlyIntArray pkPlayerStates() {
        return MutableIntArray.wrapping(pkPlayerStates);
    }

    @Override
    public PlayerId currentPlayerId() {
        return currentPlayerId;
    }

    /// Remplit les fabriques au début d'une manche en extrayant des tuiles aléatoirement
    /// du sac. Si le sac contient strictement plus de tuiles que nécessaire, on en extrait
    /// le nombre requis ; sinon, on vide le sac, on le remplit avec les tuiles sorties du
    /// jeu, puis on extrait le complément. Les tuiles extraites sont mélangées puis
    /// réparties dans les fabriques. L'ensemble des sources uniques est mis à jour.
    ///
    /// @param randomGenerator le générateur aléatoire utilisé pour l'extraction et le mélange
    public void fillFactories(RandomGenerator randomGenerator) {
        int tilesNeeded = game().factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
        TileKind.Colored[] coloredTiles = new TileKind.Colored[tilesNeeded];
        if (PkTileSet.size(pkTileBag()) > tilesNeeded) {
            int pkTileBagSize = PkTileSet.sampleColoredInto(pkTileBag(), coloredTiles, 0, randomGenerator);
            if (pkTileBagSize < tilesNeeded) {
                coloredTiles = Arrays.copyOf(coloredTiles, pkTileBagSize);
            }
            for (TileKind.Colored colored : coloredTiles){
                pkTileBag = PkTileSet.remove(pkTileBag, colored);
            }

        }

        else {
            int pkTileBagNotDiscarded = pkTileBag();
            PkTileSet.sampleColoredInto(pkTileBagNotDiscarded, coloredTiles, 0, randomGenerator);
            int pkDiscardedTiles = pkDiscardedTiles();
            pkTileBag = PkTileSet.EMPTY;
            pkTileBag = PkTileSet.union(pkTileBag, pkDiscardedTiles);
            int pkTileBagSize = PkTileSet.sampleColoredInto(pkTileBag, coloredTiles,
                    PkTileSet.size(pkTileBagNotDiscarded), randomGenerator);
            if (pkTileBagSize < tilesNeeded){
                coloredTiles = Arrays.copyOf(coloredTiles, pkTileBagSize);
            }
            for (int i = PkTileSet.size(pkTileBagNotDiscarded); i < coloredTiles.length; i++){
                pkTileBag = PkTileSet.remove(pkTileBag, coloredTiles[i]);
            }
        }

        TileKind.Colored.shuffle(coloredTiles, randomGenerator);
        int coloredTilesIndex = 0;
        for (int i = 1; i < game.tileSourcesCount()  ; i++) {
            for (int j = 0; j < TileSource.Factory.TILES_PER_FACTORY; j++) {
                if (coloredTilesIndex < coloredTiles.length) {
                    pkTileSources[i] = PkTileSet.add(pkTileSources[i], coloredTiles[coloredTilesIndex]);
                    coloredTilesIndex++;
                }

            }
        }
        pkUniqueTileSourcesUpdate();
    }

    /// Met à jour l'ensemble empaqueté des sources uniques de tuiles en fonction
    /// du contenu actuel de toutes les sources. Une source est unique ssi elle
    /// contient au moins une tuile colorée et que son contenu diffère de toutes
    /// les sources qui la précèdent.
    private void pkUniqueTileSourcesUpdate() {
        pkUniqueTileSources = PkIntSet32.EMPTY;
        for (int i = pkTileSources().size() - 1; i >= 0; i--) {
            boolean isNotSame = false;
            int pkTileSource = pkTileSources().get(i);
            for (int j = 0; j < i; j++) {
                for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
                    isNotSame = isNotSame || (!PkTileSet.isEmpty(pkTileSource) &&
                            PkTileSet.countOf(pkTileSource, tileKind) != 0);
                }
                if (pkTileSources().get(j) == pkTileSource) {
                    isNotSame = false;
                    break;
                }
            }
            if (isNotSame)
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i);
        }
        for (TileKind.Colored colored : TileKind.Colored.ALL) {
            if (!PkTileSet.isEmpty(pkTileSources().get(CENTER_AREA_INDEX)) &&
                    PkTileSet.countOf(pkTileSources().get(CENTER_AREA_INDEX), colored) != 0) {
                pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, 0);
            }
        }
    }

    /// Modifie l'état de la partie pour tenir compte du coup empaqueté {@code pkMove}
    /// joué par le joueur courant. Les tuiles de la couleur choisie sont retirées de la
    /// source, les tuiles restantes d'une fabrique sont déplacées vers la zone centrale,
    /// le marqueur de premier joueur est transféré au plancher si nécessaire, et les
    /// tuiles sont placées à la destination choisie (avec excédent au plancher si besoin).
    /// Le joueur suivant devient ensuite le joueur courant.
    ///
    /// @param pkMove le coup empaqueté joué par le joueur courant (voir {@link PkMove})
    public void registerMove(short pkMove) {
        Move playerMove = Move.ofPacked(pkMove);
        TileSource playerMoveSource = playerMove.source();
        TileKind.Colored playerMoveColor = playerMove.tileColor();
        TileDestination playerMoveDestination = playerMove.destination();
        int pkTileSourcePlayerMove = pkTileSources[playerMoveSource.index()];
        int pkTileSourceColorCount = PkTileSet.countOf(
                pkTileSources().get(playerMoveSource.index()), playerMoveColor);
        int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), currentPlayerId());
        int pkPatternPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());

        for (int i = 0; i < pkTileSourceColorCount; i++){
            pkTileSourcePlayerMove = PkTileSet.remove(pkTileSourcePlayerMove, playerMoveColor);
        }
        pkTileSources[playerMoveSource.index()] = pkTileSourcePlayerMove;

        if (playerMoveSource instanceof TileSource.Factory && pkTileSourcePlayerMove != PkTileSet.EMPTY) {
            pkTileSources[CENTER_AREA_INDEX] = PkTileSet.union(pkTileSources().get(CENTER_AREA_INDEX), pkTileSourcePlayerMove);
            pkTileSources[playerMoveSource.index()] = PkTileSet.EMPTY;
        }
        else if (playerMoveSource instanceof TileSource.CenterArea &&
                PkTileSet.countOf(pkTileSources().get(CENTER_AREA_INDEX), TileKind.FIRST_PLAYER_MARKER) == 1) {
            pkTileSources[CENTER_AREA_INDEX] = PkTileSet.remove(pkTileSources().get(CENTER_AREA_INDEX),
                    TileKind.FIRST_PLAYER_MARKER);
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
        }

        if (playerMoveDestination instanceof TileDestination.Pattern line) {
            int remainingTilesPkPattern = line.capacity() -
                    PkPatterns.size(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()), line);
            if (PkPatterns.canContain(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()),
                    line, playerMoveColor) && remainingTilesPkPattern < pkTileSourceColorCount) {
                pkPatternPlayer = PkPatterns.withAddedTiles(
                        pkPatternPlayer, line, remainingTilesPkPattern, playerMoveColor);
                PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId(), pkPatternPlayer);
                int remainingTileCount = pkTileSourceColorCount - remainingTilesPkPattern;
                pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                        PkTileSet.of(remainingTileCount, playerMoveColor));
                PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
            }
            else if (PkPatterns.canContain(PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId()),
                    line, playerMoveColor) && remainingTilesPkPattern >= pkTileSourceColorCount) {
                pkPatternPlayer = PkPatterns.withAddedTiles(
                        pkPatternPlayer, line, pkTileSourceColorCount, playerMoveColor);
                PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId(), pkPatternPlayer);
            }
        }
        else if (playerMoveDestination instanceof TileDestination.Floor) {
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                    PkTileSet.of(pkTileSourceColorCount, playerMoveColor));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
        }

        pkUniqueTileSourcesUpdate();
        currentPlayerId = playerIds().get((currentPlayerId().ordinal() + 1) % playerIds().size());
    }

    /// Termine la manche courante pour tous les joueurs : les lignes de motif pleines
    /// sont transférées sur le mur et rapportent des points, la pénalité de plancher
    /// est déduite du score (sans jamais le rendre négatif), le plancher est vidé,
    /// et le marqueur de premier joueur est replacé dans la zone centrale si nécessaire.
    /// Les méthodes de l'observateur de points sont appelées pour chaque changement de score.
    public void endRound() {
        for (PlayerId playerId : game().playerIds()) {
            int pkPatternsPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), playerId);
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), playerId);

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(pkPatternsPlayer, line)) {
                    TileKind.Colored pkPatternColorLine = PkPatterns.color(pkPatternsPlayer, line);
                    pkWallPlayer = PkWall.withTileAt(pkWallPlayer, line, pkPatternColorLine);
                    PkPlayerStates.setPkWall(pkPlayerStates, playerId, pkWallPlayer);
                    pkPatternsPlayer = PkPatterns.withEmptyLine(pkPatternsPlayer, line);
                    PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, pkPatternsPlayer);
                    int hGroupSize = PkWall.hGroupSize(pkWallPlayer, line, pkPatternColorLine);
                    int vGroupSize = PkWall.vGroupSize(pkWallPlayer, line, pkPatternColorLine);
                    int wallTilePoints = Points.newWallTilePoints(hGroupSize, vGroupSize);
                    pointsObserver.newWallTile(playerId, line, pkPatternColorLine, wallTilePoints);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, wallTilePoints);
                }
            }

            int pointsPlayer = PkPlayerStates.points(pkPlayerStates(), playerId);
            if (PkFloor.size(pkFloorPlayer) != 0) {
                int floorPenalty = Points.totalFloorPenalty(PkFloor.size(pkFloorPlayer));
                if (floorPenalty <= pointsPlayer) {
                    pointsObserver.floor(playerId, floorPenalty);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, -floorPenalty);
                }
                else {
                    pointsObserver.floor(playerId, pointsPlayer);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, -pointsPlayer);
                }

                if (PkFloor.containsFirstPlayerMarker(pkFloorPlayer)) {
                    pkTileSources[CENTER_AREA_INDEX] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
                    pkUniqueTileSourcesUpdate();
                    pkFloorPlayer = PkFloor.EMPTY;
                    PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloorPlayer);
                    currentPlayerId = playerId;
                }
                else {
                    pkFloorPlayer = PkFloor.EMPTY;
                    PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloorPlayer);
                }
            }
        }
    }

    /// Termine la partie en ajoutant les points bonus à tous les joueurs pour chaque
    /// ligne, colonne et couleur complètes dans leur mur. Les méthodes de l'observateur
    /// de points sont appelées pour chaque bonus accordé.
    public void endGame() {
        for (PlayerId playerId : game().playerIds()) {
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(pkWallPlayer, line)) {
                    pointsObserver.fullRow(playerId, line, Points.FULL_ROW_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_ROW_BONUS_POINTS);
                }
            }

            for (int col = 0; col < PkWall.WALL_WIDTH; col++) {
                if (PkWall.isColumnFull(pkWallPlayer, col)) {
                    pointsObserver.fullColumn(playerId, col, Points.FULL_COLUMN_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLUMN_BONUS_POINTS);
                }
            }

            for (TileKind.Colored colored : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(pkWallPlayer, colored)) {
                    pointsObserver.fullColor(playerId, colored, Points.FULL_COLOR_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLOR_BONUS_POINTS);
                }
            }
        }
    }
}