package ch.epfl.ajul.mcts;

import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
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

/// Joueur simulé par ordinateur déterminant ses coups au moyen de l'algorithme
/// de recherche arborescente Monte Carlo (MCTS).
///
/// @author Ismaël Ayachi (393163)
public final class MctsPlayer implements Player {
    private static final int RANK_OFFSET = 8;
    private static final int INITIAL_SIZE = 32;

    private final RandomGeneratorFactory<RandomGenerator> randomGeneratorFactory;
    private final int iterationCount;

    /// Construit un joueur MCTS.
    ///
    /// @param randomGeneratorFactory la fabrique de générateurs aléatoires utilisée pour les simulations
    /// @param iterationCount         le nombre d'itérations MCTS effectuées à chaque coup
    public MctsPlayer(RandomGeneratorFactory<RandomGenerator> randomGeneratorFactory, int iterationCount) {
        this.randomGeneratorFactory = randomGeneratorFactory;
        this.iterationCount = iterationCount;
    }

    /// Retourne le coup choisi par ce joueur depuis l'état {@code gameState}, déterminé
    /// en construisant un arbre MCTS sur {@code iterationCount} itérations puis en
    /// retournant le coup de la racine ayant la meilleure moyenne de points.
    ///
    /// @param gameState l'état de la partie depuis lequel jouer
    /// @return le coup choisi
    @Override
    public Move nextMove(ReadOnlyGameState gameState) {
        short[] validMoves = new short[Move.MAX_MOVES];
        int[] playersRank = new int[gameState.game().playersCount()];
        int[] generalizedPoints = new int[gameState.game().playersCount()];
        byte[] playerAtDepth = new byte[INITIAL_SIZE];
        MctsNode[] nodeAtDepth = new MctsNode[INITIAL_SIZE];

        MctsNode root = MctsNode.newRoot();
        RandomGenerator endGameGenerator = randomGeneratorFactory.create(gameState.pkTileBag());

        for (int i = 0; i < iterationCount; i++){

            MutableGameState mutableGameState = new MutableGameState(gameState);
            MctsNode currentNode = root;
            int depth = 0;

            // Sélection
            while (currentNode.gameCount() > 0 && !mutableGameState.isGameOver()){
                if (currentNode.children == null){
                    int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);

                    currentNode.children = new MctsNode[validMovesCount];
                    for (int j = 0; j < validMovesCount; j++) {
                        currentNode.children[j] = MctsNode.newMoveNode(validMoves[j]);
                    }
                }
                int toExploreIndex = currentNode.indexOfChildToExplore();
                if (depth == nodeAtDepth.length) {
                    nodeAtDepth = Arrays.copyOf(nodeAtDepth,   nodeAtDepth.length << 1);
                    playerAtDepth = Arrays.copyOf(playerAtDepth, playerAtDepth.length << 1);
                }
                playerAtDepth[depth] = (byte) mutableGameState.currentPlayerId().ordinal();
                currentNode = currentNode.children[toExploreIndex];
                nodeAtDepth[depth] = currentNode;
                depth++;

                mutableGameState.registerMove(currentNode.pkMove());

                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    if (!mutableGameState.isGameOver())
                        mutableGameState.fillFactories(randomGeneratorFactory.create(currentNode.pkMove()));
                }
            }

            // Simulation
            while (!mutableGameState.isGameOver()){
                int validMovesCount = mutableGameState.uniqueValidMoves(validMoves);
                int selectedMove = HeuristicMoveSelector.selectMove(endGameGenerator, mutableGameState,
                        validMoves, validMovesCount);
                mutableGameState.registerMove(validMoves[selectedMove]);
                if (mutableGameState.isRoundOver()) {
                    mutableGameState.endRound();
                    if (!mutableGameState.isGameOver()){
                        mutableGameState.fillFactories(endGameGenerator);
                    }
                }
            }
            mutableGameState.endGame();

            // Calcul et propagation des points
            RankComputer.playersRank(mutableGameState, playersRank);
            ReadOnlyIntArray pkPlaterStates = mutableGameState.pkPlayerStates();
            for (int j = 0; j < gameState.game().playersCount(); j++){
                PlayerId playerId = PlayerId.ALL.get(j);
                int rankComplement = (mutableGameState.game().playersCount() - 1) - playersRank[j];
                int points = PkPlayerStates.points(pkPlaterStates, playerId);
                int generalized = (rankComplement << RANK_OFFSET) + points;
                generalizedPoints[j] = generalized;
            }

            root.registerEvaluation(0);
            for (int k = depth - 1 ; k >= 0; k--){
                byte playerIndex = playerAtDepth[k];
                nodeAtDepth[k].registerEvaluation(generalizedPoints[playerIndex]);
            }
        }

        Optional<MctsNode> maxAverageNode = Arrays.stream(root.children)
                .max(Comparator.comparingDouble(MctsNode::averagePoints));

        return Move.ofPacked(maxAverageNode.orElseThrow().pkMove());
    }
}
