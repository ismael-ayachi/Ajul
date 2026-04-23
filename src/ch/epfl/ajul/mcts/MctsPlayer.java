package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
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
        int[] playersRank = new int[gameState.game().playersCount()]; //Passé à playersRank de RankComputer
        int[] generalizedPoints = new int[gameState.game().playersCount()];
        byte[] browsedNode = new byte[32];
        byte[] playerArray = new byte[32];
        MctsNode[] nodeArray = new MctsNode[32];

        MctsNode root = MctsNode.newRoot();
        RandomGenerator fixedGenerator = randomGeneratorFactory.create(2026);

        for (int i = 0; i < iterationCount; i++){

            MutableGameState mutableGameState = new MutableGameState(gameState, PointsObserver.EMPTY);
            MctsNode currentNode = root;
            int depth = 0;

            //Sélection
            while (currentNode.gameCount() > 0 && !mutableGameState.isGameOver()){
                nodeArray[depth] = currentNode;
                currentNode = MctsNode.newMoveNode(0);
                MctsNode[] childNode = currentNode.childNode;

                if (childNode == null){
                    int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);
                    childNode = new MctsNode[validMovesCount];
                    for (int j = 0; j < validMovesCount; j++) {
                        childNode[j] = MctsNode.newMoveNode(validMoves[j]);
                    }
                }

                int toExploreIndex = currentNode.indexOfChildToExplore();
                browsedNode[i] = (byte) (Byte.toUnsignedInt((byte) toExploreIndex));
                playerArray[i] = (byte) mutableGameState.currentPlayerId().ordinal();
                currentNode = childNode[toExploreIndex];
                nodeArray[depth] = currentNode;
                depth++;

                mutableGameState.registerMove(validMoves[toExploreIndex]);

                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    mutableGameState.fillFactories(fixedGenerator);
                }
            }

            //Simulation
            while (!mutableGameState.isGameOver()){
                RandomGenerator randomGenerator = randomGeneratorFactory.create(currentNode.totalPoints());
                int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);
                int selectedMove =
                        HeuristicMoveSelector.selectMove(fixedGenerator, mutableGameState, validMoves, validMovesCount);
                mutableGameState.registerMove(validMoves[selectedMove]);
                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    mutableGameState.fillFactories(randomGenerator);
                }
            }
            mutableGameState.endGame();

            //Calcul et propagation des points
            for (int j = 0; j < gameState.game().playersCount(); j++){
                ReadOnlyIntArray pkPlaterStates = mutableGameState.pkPlayerStates();
                PlayerId playerId = mutableGameState.currentPlayerId();
                RankComputer.playersRank(mutableGameState, playersRank);
                int rankComplement = (mutableGameState.game().playersCount() - 1) - playersRank[i];
                int points = PkPlayerStates.points(pkPlaterStates, playerId);
                int generalized = rankComplement * 256 + points;
                generalizedPoints[j] = generalized;
            }

            for (int k = depth ; k >= 0; k--){
                byte playerIndex = playerArray[k];
                nodeArray[k].registerEvaluation(generalizedPoints[playerIndex]);
            }
            root.registerEvaluation(0);
        }

        Optional<MctsNode> maxAverageNode =
                Arrays.stream(root.childNode).max(Comparator.comparingDouble(MctsNode::averagePoints));

        return Move.ofPacked((short) maxAverageNode.orElseThrow().pkMove());
    }
}
