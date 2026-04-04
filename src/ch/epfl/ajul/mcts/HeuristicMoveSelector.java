package ch.epfl.ajul.mcts;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;

import java.util.random.RandomGenerator;

public final class HeuristicMoveSelector {

    public static int selectMove(RandomGenerator randomGenerator,
                                 ReadOnlyGameState gameState,
                                 int[] packedMoveArray,
                                 int valideMoves) {


        return 0;
    }


    private static boolean isIndexValid (int i, int[] packedMoveArray){
        return i >= 0 && i < packedMoveArray.length;
    }

}
