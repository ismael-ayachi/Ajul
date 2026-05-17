package ch.epfl.ajul.mcts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class MctsNode {

    private int pkMoveAndCount;
    private int totalPoints;
    MctsNode[] children;

    private static final int COUNTER_BITS = 22;
    private static final int COUNTER_OFFSET = 10;
    private static final int COUNTER_MASK = (1 << COUNTER_BITS) - 1;
    private static final int MOVE_BITS = 10;
    private static final int MOVE_OFFSET = 0;
    private static final int MOVE_MASK = (1 << MOVE_BITS) - 1;

    private static final int EXPLORATION_CONSTANT = 80;

    private MctsNode(int pkMove, int initialCount){
        this.pkMoveAndCount = (pkMove << MOVE_OFFSET) | (initialCount << COUNTER_OFFSET);
    }

    public static MctsNode newRoot() {
        return new MctsNode(MOVE_MASK, 1);
    }

    public static MctsNode newMoveNode(int pkMove) {
        return new MctsNode(pkMove, 0);
    }

    public short pkMove() {
        return (short) ((pkMoveAndCount >> MOVE_OFFSET) & MOVE_MASK);
    }

    public int gameCount() {
        return (pkMoveAndCount >> COUNTER_OFFSET) & COUNTER_MASK;
    }

    public double averagePoints() {
        assert isValid(gameCount());
        return (double) totalPoints / gameCount();
    }

    public void registerEvaluation(int points) {
        totalPoints += points;
        pkMoveAndCount += (1 << COUNTER_OFFSET);
    }

    public int indexOfChildToExplore() {
        if (gameCount() <= children.length) {
            return gameCount() - 1;
        }
        return IntStream.range(0, children.length) //Efficacité à vérifier
                .boxed()
                .max(Comparator.comparingDouble(i -> priority(children[i])))
                .orElseThrow();
    }

    private double priority(MctsNode childNode) {
        double explorationBonus = Math.sqrt((double) 2 * Math.log(gameCount()) / childNode.gameCount());
        double exploration = EXPLORATION_CONSTANT * explorationBonus;
        return childNode.averagePoints() + exploration;
    }

    private static boolean isValid (int gameCount){
        return gameCount > 0;
    }
}
