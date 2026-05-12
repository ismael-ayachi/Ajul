package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.PointsObserver;
import ch.epfl.ajul.RankComputer;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
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
        byte[] playerArray = new byte[256];
        MctsNode[] nodeArray = new MctsNode[256];

        MctsNode root = MctsNode.newRoot();
        RandomGenerator fixedGenerator = randomGeneratorFactory.create(42);
        RandomGenerator endGameGenerator = randomGeneratorFactory.create(gameState.pkTileBag());

        for (int i = 0; i < iterationCount; i++){

            MutableGameState mutableGameState = new MutableGameState(gameState, PointsObserver.EMPTY);
            MctsNode currentNode = root;
            int depth = 0;

            //Sélection
            while (currentNode.gameCount() > 0 && !mutableGameState.isGameOver()){
                //MctsNode[] childNode = currentNode.childNode;

                if (currentNode.childNode == null){
                    int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);
                    currentNode.childNode = new MctsNode[validMovesCount];
                    for (int j = 0; j < validMovesCount; j++) {
                        currentNode.childNode[j] = MctsNode.newMoveNode(validMoves[j]);
                    }
                }

                int toExploreIndex = currentNode.indexOfChildToExplore();
                playerArray[depth] = (byte) mutableGameState.currentPlayerId().ordinal();
                currentNode = currentNode.childNode[toExploreIndex];
                nodeArray[depth] = currentNode;
                depth++;

                mutableGameState.registerMove((short) currentNode.pkMove());

                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    mutableGameState.fillFactories(fixedGenerator);
                }
            }

            //Simulation
            //RandomGenerator randomGenerator = randomGeneratorFactory.create(currentNode.totalPoints());
            while (!mutableGameState.isGameOver()){
                int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);
                int selectedMove =
                        HeuristicMoveSelector.selectMove(endGameGenerator, mutableGameState, validMoves, validMovesCount);
                mutableGameState.registerMove(validMoves[selectedMove]);
                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    if (!mutableGameState.isGameOver()){
                        mutableGameState.fillFactories(endGameGenerator);
                    }

                }
            }
            mutableGameState.endGame();

            //Calcul et propagation des points
            RankComputer.playersRank(mutableGameState, playersRank);
            for (int j = 0; j < gameState.game().playersCount(); j++){
                ReadOnlyIntArray pkPlaterStates = mutableGameState.pkPlayerStates();
                PlayerId playerId = PlayerId.ALL.get(j);
                int rankComplement = (mutableGameState.game().playersCount() - 1) - playersRank[j];
                int points = PkPlayerStates.points(pkPlaterStates, playerId);
                int generalized = rankComplement * 256 + points;
                generalizedPoints[j] = generalized;
            }

            for (int k = depth - 1 ; k >= 0; k--){
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
