package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;

import java.util.ArrayList;
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
        short[] validMoves = new short[Move.MAX_MOVES];
        int[] playersRank = new int[gameState.game().playersCount()];
        byte[] browsedNode = new byte[32];
        MctsNode root = MctsNode.newRoot();

        //Sélection

        for (int i = 0; i < iterationCount; i++){

            MutableGameState mutableGameState = new MutableGameState(gameState, PointsObserver.EMPTY);
            MctsNode currentNode = root;

            while (currentNode.gameCount() > 0 && !gameState.isGameOver()){
                currentNode = MctsNode.newMoveNode(0);
                MctsNode[] childNode = currentNode.childNode;
                if (childNode == null){
                    int validMovesCount = gameState.uniqueValidMoves(validMoves);
                    childNode = new MctsNode[validMovesCount];
                    for (int j = 0; j < validMovesCount; j++) {
                        childNode[j] = MctsNode.newMoveNode(validMoves[j]);
                    }
                    int toExploreIndex = currentNode.indexOfChildToExplore();
                    currentNode = childNode[toExploreIndex];
                }

                int toExplore = currentNode.indexOfChildToExplore();



            }

        }








    }
}
