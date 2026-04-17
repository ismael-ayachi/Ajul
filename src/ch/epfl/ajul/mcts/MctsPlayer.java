package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class MctsPlayer implements Player {


    private final RandomGeneratorFactory<RandomGenerator> randomGeneratorFactory;
    private final int iterationCount;

    public MctsPlayer(RandomGeneratorFactory<RandomGenerator> randomGeneratorFactory, int iterationCount ){
        this.randomGeneratorFactory = randomGeneratorFactory;
        this.iterationCount = iterationCount;
    }


    @Override
    public Move nextMove(ReadOnlyGameState gameState) {
        return null;
    }
}
