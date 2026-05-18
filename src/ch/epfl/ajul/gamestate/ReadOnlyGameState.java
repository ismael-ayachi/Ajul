package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.List;

/// Interface représentant un état de partie en lecture seule.
/// Fournit un accès aux différentes composantes de l'état d'une partie d'Ajul,
/// ainsi que des méthodes par défaut pour calculer des informations dérivées
/// comme les coups jouables ou les tuiles sorties du jeu.
///
/// @author Ismaël Ayachi (393163)
public interface ReadOnlyGameState {

    /// Retourne la configuration de la partie.
    ///
    /// @return la configuration de la partie
    Game game();

    /// Retourne le contenu du sac de tuiles sous la forme d'un ensemble empaqueté
    /// (voir {@link PkTileSet}).
    ///
    /// @return le contenu empaqueté du sac
    int pkTileBag();

    /// Retourne un tableau décrivant le contenu des sources de tuiles,
    /// l'élément à l'index {@code i} étant un ensemble de tuiles empaqueté
    /// (voir {@link PkTileSet}) correspondant au contenu de la source d'index {@code i}.
    ///
    /// @return le tableau des contenus empaquetés des sources de tuiles
    ReadOnlyIntArray pkTileSources();

    /// Retourne l'ensemble empaqueté (voir {@link PkIntSet32}) des index des sources
    /// uniques de tuiles, c'est-à-dire celles qui contiennent au moins une tuile colorée
    /// et dont le contenu diffère de toutes les fabriques qui les précèdent.
    ///
    /// @return l'ensemble empaqueté des index des sources uniques
    int pkUniqueTileSources();

    /// Retourne un tableau contenant les états empaquetés de tous les joueurs
    /// (voir {@link PkPlayerStates}).
    ///
    /// @return le tableau des états empaquetés des joueurs
    ReadOnlyIntArray pkPlayerStates();

    /// Retourne l'identité du joueur courant.
    ///
    /// @return l'identité du joueur courant
    PlayerId currentPlayerId();

    /// Retourne une version immuable de l'état de la partie.
    ///
    /// @return une version immuable de cet état
    default ImmutableGameState immutable() {
        return new ImmutableGameState(
                game(),
                pkTileBag(),
                pkTileSources().immutable(),
                pkUniqueTileSources(),
                pkPlayerStates().immutable(),
                currentPlayerId());
    }

    /// Retourne la liste des identités des joueurs de la partie.
    ///
    /// @return la liste des identités des joueurs
    default List<PlayerId> playerIds() {
        return game().playerIds();
    }

    /// Retourne {@code true} si la manche est terminée, c'est-à-dire si aucune source
    /// de tuiles ne contient de tuile colorée. Cela est équivalent à ce que l'ensemble
    /// des sources uniques soit vide.
    ///
    /// @return {@code true} si la manche est terminée
    default boolean isRoundOver() {
        return pkUniqueTileSources() == PkIntSet32.EMPTY;
    }

    /// Retourne {@code true} si la partie est terminée, c'est-à-dire si la manche est
    /// terminée et qu'au moins un joueur possède une ligne horizontale complète dans
    /// son mur.
    ///
    /// @return {@code true} si la partie est terminée
    default boolean isGameOver() {
        if (!isRoundOver()) return false;
        for (PlayerId playerId : playerIds()) {
            if (PkWall.hasFullRow(PkPlayerStates.pkWall(pkPlayerStates(),playerId))){
                return true;
            }
        }
        return false;
    }

    /// Retourne l'ensemble empaqueté (voir {@link PkTileSet}) des tuiles sorties du jeu,
    /// calculé en retirant de l'ensemble total des tuiles celles se trouvant dans le sac,
    /// dans les sources, ou sur les plateaux des joueurs.
    ///
    /// @return l'ensemble empaqueté des tuiles sorties du jeu
    default int pkDiscardedTiles() {
        int tileSourcesSum, pkPatternsSum, pkFloorSum, pkWallSum;
        tileSourcesSum = pkPatternsSum = pkFloorSum = pkWallSum = 0;
        for (PlayerId playerId : playerIds()) {
            int pkPatterns = PkPlayerStates.pkPatterns(pkPlayerStates(), playerId);
            int pkFloor = PkPlayerStates.pkFloor(pkPlayerStates(), playerId);
            int pkWall = PkPlayerStates.pkWall(pkPlayerStates(), playerId);
            pkPatternsSum = PkTileSet.union(pkPatternsSum, PkPatterns.asPkTileSet(pkPatterns));
            pkFloorSum = PkTileSet.union(pkFloorSum, PkFloor.asPkTileSet(pkFloor));
            pkWallSum = PkTileSet.union(pkWallSum, PkWall.asPkTileSet(pkWall));
        }
        for (TileSource tileSource : game().tileSources()){
            tileSourcesSum = PkTileSet.union(tileSourcesSum, pkTileSources().get(tileSource.index()));
        }

        int pkTileSetSum = PkTileSet.union(tileSourcesSum, pkPatternsSum);
        pkTileSetSum = PkTileSet.union(pkTileSetSum, pkFloorSum);
        pkTileSetSum = PkTileSet.union(pkTileSetSum, pkWallSum);
        pkTileSetSum = PkTileSet.union(pkTileSetSum, pkTileBag());

        return PkTileSet.difference(PkTileSet.FULL, pkTileSetSum);
    }

    /// Place dans le tableau {@code destination} tous les coups empaquetés
    /// (voir {@link PkMove}) que le joueur courant pourrait jouer, en considérant
    /// toutes les sources de tuiles, et retourne leur nombre.
    /// La taille du tableau {@code destination} doit être au moins égale à
    /// {@link Move#MAX_MOVES}.
    ///
    /// @param destination le tableau dans lequel les coups sont placés
    /// @return le nombre de coups jouables
    default int validMoves(short[] destination) {
        return validMovesCommon(destination, true);
    }

    /// Place dans le tableau {@code destination} tous les coups empaquetés
    /// (voir {@link PkMove}) que le joueur courant pourrait jouer, en ne considérant
    /// que les sources uniques de tuiles, et retourne leur nombre.
    /// La taille du tableau {@code destination} doit être au moins égale à
    /// {@link Move#MAX_MOVES}.
    ///
    /// @param destination le tableau dans lequel les coups sont placés
    /// @return le nombre de coups jouables depuis les sources uniques
    default int uniqueValidMoves(short[] destination) {
        return validMovesCommon(destination, false);
    }

    /// Méthode privée commune à {@link #validMoves} et {@link #uniqueValidMoves}.
    /// Si {@code allSources} est {@code true}, toutes les sources sont considérées ;
    /// sinon, seules les sources uniques le sont.
    ///
    /// @param destination le tableau dans lequel les coups sont placés
    /// @param allSources  {@code true} pour considérer toutes les sources
    /// @return le nombre de coups placés dans {@code destination}
    private int validMovesCommon(short[] destination, boolean allSources) {
        int count = 0;
        if (destination.length >= Move.MAX_MOVES) {
            int currentPlayerPkPattern = PkPlayerStates.pkPatterns(pkPlayerStates(), currentPlayerId());
            int currentPlayerPkWall = PkPlayerStates.pkWall(pkPlayerStates(), currentPlayerId());
            // Enumération de toutes les combinaisons source × couleur × destination
            for (TileSource tileSource : game().tileSources()) {
                if (allSources || PkIntSet32.contains(pkUniqueTileSources(), tileSource.index())) {
                    for (TileKind.Colored colored : TileKind.Colored.ALL) {

                        // Coup vers la ligne plancher (toujours valide si la source contient la couleur)
                        if (PkTileSet.countOf(pkTileSources().get(tileSource.index()), colored) != PkTileSet.EMPTY) {
                            destination[count] = PkMove.pack(tileSource, colored, TileDestination.FLOOR);
                            count++;
                        }

                        // Coup vers une ligne de motif (si la source contient la couleur et la ligne peut l'accueillir)
                        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {

                            boolean pkPatternCanContain = pkPatternCanContain(currentPlayerPkPattern,
                                    currentPlayerPkWall, line, colored);

                            if (PkTileSet.countOf(pkTileSources().get(tileSource.index()), colored) != PkTileSet.EMPTY
                                    && pkPatternCanContain) {
                                destination[count] = PkMove.pack(tileSource, colored, line);
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    /// Vérifie si une tuile peut être placée sur une ligne de motif
    private boolean pkPatternCanContain(int pkPatterns, int pkWall,
                                        TileDestination.Pattern line, TileKind.Colored colored) {
        return !PkPatterns.isFull(pkPatterns, line)
                && PkPatterns.canContain(pkPatterns, line, colored)
                && !PkWall.hasTileAt(pkWall, line, colored);

    }
}