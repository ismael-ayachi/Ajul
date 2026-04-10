package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;

@FunctionalInterface
public interface Player {
    Move nextMove(ReadOnlyGameState gameState);
}
