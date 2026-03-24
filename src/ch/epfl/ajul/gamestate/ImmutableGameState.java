package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;

import static java.util.Objects.requireNonNull;

/// Enregistrement représentant un état de jeu immuable.
/// Implémente {@link ReadOnlyGameState} et regroupe toutes les composantes
/// de l'état d'une partie d'Ajul dans une représentation compacte et immuable.
///
/// @param game                 la configuration de la partie
/// @param pkTileBag            le contenu empaqueté du sac de tuiles
/// @param pkTileSources        le tableau immuable des contenus empaquetés des sources
/// @param pkUniqueTileSources  l'ensemble empaqueté des index des sources uniques
/// @param pkPlayerStates       le tableau immuable des états empaquetés des joueurs
/// @param currentPlayerId      l'identité du joueur courant
/// @author Ismaël Ayachi (393163)
public record ImmutableGameState(Game game, int pkTileBag, ImmutableIntArray pkTileSources,
                                 int pkUniqueTileSources, ImmutableIntArray pkPlayerStates,
                                 PlayerId currentPlayerId) implements ReadOnlyGameState {

    /// Constructeur compact vérifiant qu'aucun argument pouvant être {@code null} ne l'est.
    ///
    /// @throws NullPointerException si {@code game}, {@code pkTileSources},
    ///                              {@code pkPlayerStates} ou {@code currentPlayerId} est {@code null}
    public ImmutableGameState {
        requireNonNull(game);
        requireNonNull(pkTileSources);
        requireNonNull(pkPlayerStates);
        requireNonNull(currentPlayerId);
    }

    /// Retourne l'état initial d'une partie, dans lequel toutes les sources sont vides
    /// sauf la zone centrale qui contient le marqueur de premier joueur, le sac contient
    /// la totalité des tuiles colorées (20 de chaque couleur), et le joueur courant est
    /// le premier joueur de la partie.
    ///
    /// @param game la configuration de la partie
    /// @return l'état initial de la partie
    public static ImmutableGameState initial(Game game) {
        int[] pkTileSources = new int[game.tileSourcesCount()];
        for (int i=0; i < game.tileSourcesCount(); i++){
            pkTileSources[i] = PkTileSet.EMPTY;
        }
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        return new ImmutableGameState(game, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(pkTileSources), PkIntSet32.EMPTY,
                PkPlayerStates.initial(game), game.playerIds().getFirst());
    }

    /// Retourne cet état lui-même, évitant ainsi la création d'une copie inutile
    /// d'un état déjà immuable.
    ///
    /// @return {@code this}
    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}





