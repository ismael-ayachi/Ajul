package ch.epfl.ajul.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class MctsNode {

    private int packedMove;
    private int totalPoints;
    MctsNode[] childNode;

    private static final int COUNTER_OFFSET = 10;
    private static final int MOVE_MASK = (1 << COUNTER_OFFSET) - 1;
    private static final int EXPLORATION_CONSTANT = 80;

    private MctsNode(int packedMove, int initialCount){
        this.packedMove = packedMove | (initialCount << COUNTER_OFFSET);
    }

    public static MctsNode newRoot() {
        return new MctsNode(MOVE_MASK, 1);
    }

    public static MctsNode newMoveNode(int move) {
        return new MctsNode(move, 0);
    }

    public int pkMove() {
        return packedMove & MOVE_MASK;

    }

    public int gameCount() {
        return packedMove >> COUNTER_OFFSET;
    }

    public int totalPoints() {
        return totalPoints;
    }

    public double averagePoints() {
        assert isValid(gameCount());
        return (double) totalPoints() / gameCount();
    }

    public void registerEvaluation(int points) {
        totalPoints += points;
        packedMove += (1 << COUNTER_OFFSET);
    }

    public int indexOfChildToExplore() {
        if (gameCount() <= childNode.length) {
            return gameCount() - 1;
        } else {
            List<Double> childNodePriority = new ArrayList<>();
            //Vérifier la validité de cette façon de procéder.
            for (MctsNode childNode : childNode) {
                assert isValid(childNode.gameCount()) && isValid(gameCount()); // Ou alors juste un if ?
                int parentGameCount = gameCount();
                double exploration = EXPLORATION_CONSTANT
                                * Math.sqrt((double) 2 * Math.log(parentGameCount) / childNode.gameCount());
                double nodePriority = childNode.averagePoints() + exploration;
                childNodePriority.add(nodePriority);
            }
            Optional<Double> childNodePriorityMaxOptional = childNodePriority.stream().max(Double::compareTo);
            double childNodePriorityMax = childNodePriorityMaxOptional.orElseThrow();
            return childNodePriority.indexOf(childNodePriorityMax);
        }
    }

    private static boolean isValid (int gameCount){
        return gameCount > 0;
    }
}
