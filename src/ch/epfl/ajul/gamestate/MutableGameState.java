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
        assert isRoundOver();
        int tilesNeeded = game().factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
        TileKind.Colored[] coloredTiles = new TileKind.Colored[tilesNeeded];

        // Si pas assez de tuiles dans le sac, on le vide et on le recharge
        int pkTileBagNotDiscardedSize = 0;
        if (PkTileSet.size(pkTileBag()) <= tilesNeeded) {
            pkTileBagNotDiscardedSize = PkTileSet.copyColoredInto(pkTileBag(), coloredTiles);
            pkTileBag = pkDiscardedTiles();
        }

        // On tire les tuiles manquantes du sac (potentiellement rechargé)
        int pkTileBagSize = PkTileSet.sampleColoredInto(pkTileBag(), coloredTiles, pkTileBagNotDiscardedSize, randomGenerator);
        if (pkTileBagSize < tilesNeeded)
            coloredTiles = Arrays.copyOf(coloredTiles, pkTileBagSize);

        // On retire les tuiles tirées du sac
        for (int i = pkTileBagNotDiscardedSize; i < coloredTiles.length; i++)
            pkTileBag = PkTileSet.remove(pkTileBag, coloredTiles[i]);

        // Mélange et distribution dans les fabriques
        TileKind.Colored.shuffle(coloredTiles, randomGenerator);
        int coloredTilesIndex = 0;
        for (int i = 1; i < game.tileSourcesCount(); i++) {
            for (int j = 0; j < TileSource.Factory.TILES_PER_FACTORY; j++) {
                if (coloredTilesIndex < coloredTiles.length)
                    pkTileSources[i] = PkTileSet.add(pkTileSources[i], coloredTiles[coloredTilesIndex++]);
            }
        }

        // Mise à jour de pkUniqueTileSources

        pkUniqueTileSources = PkIntSet32.EMPTY;
        loop :
        for (int i = 1; i < game.tileSourcesCount(); i++) {
            if (PkTileSet.isEmpty(pkTileSources[i]))
                continue;
            for (int j = 1; j < i; j++)
                if (pkTileSources[j] == pkTileSources[i]) continue loop;
            pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i);
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
        int oldSourceContent = pkTileSourcePlayerMove;
        int pkTileSourceColorCount = PkTileSet.countOf(
                pkTileSources[playerMoveSource.index()], playerMoveColor);
        int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), currentPlayerId());
        int pkPatternPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());

        // Retrait des tuiles de la couleur choisie de la source
        for (int i = 0; i < pkTileSourceColorCount; i++){
            pkTileSourcePlayerMove = PkTileSet.remove(pkTileSourcePlayerMove, playerMoveColor);
        }
        pkTileSources[playerMoveSource.index()] = pkTileSourcePlayerMove;

        // Déplacement des tuiles restantes vers le centre / gestion du marqueur de premier joueur
        if (playerMoveSource instanceof TileSource.Factory && pkTileSourcePlayerMove != PkTileSet.EMPTY) {
            pkTileSources[CENTER_AREA_INDEX] =
                    PkTileSet.union(pkTileSources[CENTER_AREA_INDEX], pkTileSourcePlayerMove);
            pkTileSources[playerMoveSource.index()] = PkTileSet.EMPTY;
        }

        else if (playerMoveSource instanceof TileSource.CenterArea &&
                PkTileSet.countOf(pkTileSources[CENTER_AREA_INDEX], TileKind.FIRST_PLAYER_MARKER) == 1) {
            pkTileSources[CENTER_AREA_INDEX] = PkTileSet.remove(pkTileSources[CENTER_AREA_INDEX],
                    TileKind.FIRST_PLAYER_MARKER);

            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
        }

        // Mise à jour de pkUniqueTileSources
        if (playerMoveSource instanceof TileSource.Factory){
            pkUniqueTileSources = PkIntSet32.remove(pkUniqueTileSources,playerMoveSource.index());
            boolean notFound = true;
            for (int i = playerMoveSource.index() + 1 ; i < game().tileSourcesCount() && notFound; i++){
                if (pkTileSources[i] == oldSourceContent) {
                    for (int j = 1; j < i; j++){
                        if (pkTileSources[j] == pkTileSources[i]) {
                            notFound = false;
                            break;
                        }
                    }
                    if (notFound) {
                        pkUniqueTileSources = PkIntSet32.add(pkUniqueTileSources, i);
                        notFound = false;

                    }
                }
            }
        }

        int centerAreaColored = pkTileSources[CENTER_AREA_INDEX]
                - PkTileSet.subsetOf(pkTileSources[CENTER_AREA_INDEX], TileKind.FIRST_PLAYER_MARKER);
        pkUniqueTileSources = centerAreaColored != PkTileSet.EMPTY
                ? PkIntSet32.add(pkUniqueTileSources, CENTER_AREA_INDEX)
                : PkIntSet32.remove(pkUniqueTileSources, CENTER_AREA_INDEX);

        // Placement des tuiles sur la destination choisie
        if (playerMoveDestination instanceof TileDestination.Pattern line
                && PkPatterns.canContain(pkPatternPlayer, line, playerMoveColor)) {
            int remaining = line.capacity() - PkPatterns.size(pkPatternPlayer, line);
            int tilesToAdd = Math.min(pkTileSourceColorCount, remaining);
            pkPatternPlayer = PkPatterns.withAddedTiles(pkPatternPlayer, line, tilesToAdd, playerMoveColor);
            PkPlayerStates.setPkPatterns(pkPlayerStates, currentPlayerId(), pkPatternPlayer);
            if (tilesToAdd < pkTileSourceColorCount) {
                pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                        PkTileSet.of(pkTileSourceColorCount - tilesToAdd, playerMoveColor));
                PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
            }
        }
        else if (playerMoveDestination instanceof TileDestination.Floor) {
            pkFloorPlayer = PkFloor.withAddedTiles(pkFloorPlayer,
                    PkTileSet.of(pkTileSourceColorCount, playerMoveColor));
            PkPlayerStates.setPkFloor(pkPlayerStates, currentPlayerId(), pkFloorPlayer);
        }

        currentPlayerId = playerIds().get((currentPlayerId().ordinal() + 1) % playerIds().size());
    }

    /// Termine la manche courante pour tous les joueurs : les lignes de motif pleines
    /// sont transférées sur le mur et rapportent des points, la pénalité de plancher
    /// est déduite du score (sans jamais le rendre négatif), le plancher est vidé,
    /// et le marqueur de premier joueur est replacé dans la zone centrale si nécessaire.
    /// Les méthodes de l'observateur de points sont appelées pour chaque changement de score.
    public void endRound() {
        assert isRoundOver();
        for (PlayerId playerId : game().playerIds()) {
            int pkPatternsPlayer = PkPlayerStates.pkPatterns(pkPlayerStates(), playerId);
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            int pkFloorPlayer = PkPlayerStates.pkFloor(pkPlayerStates(), playerId);

            // Transfert des lignes de motif pleines sur le mur
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkPatterns.isFull(pkPatternsPlayer, line)) {
                    TileKind.Colored colorLine = PkPatterns.color(pkPatternsPlayer, line);
                    pkWallPlayer = PkWall.withTileAt(pkWallPlayer, line, colorLine);
                    pkPatternsPlayer = PkPatterns.withEmptyLine(pkPatternsPlayer, line);
                    int wallTilePoints = Points.newWallTilePoints(
                            PkWall.hGroupSize(pkWallPlayer, line, colorLine),
                            PkWall.vGroupSize(pkWallPlayer, line, colorLine));
                    pointsObserver.newWallTile(playerId, line, colorLine, wallTilePoints);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, wallTilePoints);
                }
            }
            PkPlayerStates.setPkWall(pkPlayerStates, playerId, pkWallPlayer);
            PkPlayerStates.setPkPatterns(pkPlayerStates, playerId, pkPatternsPlayer);

            // Pénalité de la ligne plancher et gestion du marqueur de premier joueur
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
                    pkUniqueTileSources = PkIntSet32.EMPTY;
                    currentPlayerId = playerId;
                }
                pkFloorPlayer = PkFloor.EMPTY;
                PkPlayerStates.setPkFloor(pkPlayerStates, playerId, pkFloorPlayer);
            }
        }
    }

    /// Termine la partie en ajoutant les points bonus à tous les joueurs pour chaque
    /// ligne, colonne et couleur complètes dans leur mur. Les méthodes de l'observateur
    /// de points sont appelées pour chaque bonus accordé.
    public void endGame() {
        assert isGameOver();
        for (PlayerId playerId : game().playerIds()) {
            int pkWallPlayer = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            // Bonus pour chaque ligne complète
            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                if (PkWall.isRowFull(pkWallPlayer, line)) {
                    pointsObserver.fullRow(playerId, line, Points.FULL_ROW_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_ROW_BONUS_POINTS);
                }
            }

            // Bonus pour chaque colonne complète
            for (int col = 0; col < PkWall.WALL_WIDTH; col++) {
                if (PkWall.isColumnFull(pkWallPlayer, col)) {
                    pointsObserver.fullColumn(playerId, col, Points.FULL_COLUMN_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLUMN_BONUS_POINTS);
                }
            }

            // Bonus pour chaque couleur complète
            for (TileKind.Colored colored : TileKind.Colored.ALL) {
                if (PkWall.isColorFull(pkWallPlayer, colored)) {
                    pointsObserver.fullColor(playerId, colored, Points.FULL_COLOR_BONUS_POINTS);
                    PkPlayerStates.addPoints(pkPlayerStates, playerId, Points.FULL_COLOR_BONUS_POINTS);
                }
            }
        }
    }
}