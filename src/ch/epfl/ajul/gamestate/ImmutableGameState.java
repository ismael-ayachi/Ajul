package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import static java.util.Objects.requireNonNull;

public record ImmutableGameState(Game game, int pkTileBag, ImmutableIntArray pkTileSources,
                                 int pkUniqueTileSources, ImmutableIntArray pkPlayerStates,
                                 PlayerId currentPlayerId) implements ReadOnlyGameState {
    public ImmutableGameState {
        requireNonNull(game);
        requireNonNull(pkTileSources);
        requireNonNull(pkPlayerStates);
        requireNonNull(currentPlayerId);
    }

    public static ImmutableGameState initial(Game game) {
        int[] pkTileSources = new int[game.tileSourcesCount()];
        for (int i=0; i < game.tileSourcesCount(); i++){
            pkTileSources[i] = PkTileSet.EMPTY;
        }
        pkTileSources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        return new ImmutableGameState(game, PkTileSet.FULL_COLORED, ImmutableIntArray.copyOf(pkTileSources),
                PkIntSet32.EMPTY, PkPlayerStates.initial(game), game.playerIds().getFirst());
    }

    @Override
    public ImmutableGameState immutable() {
        return this;
    }
}
