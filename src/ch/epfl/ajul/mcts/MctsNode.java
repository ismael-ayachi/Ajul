package ch.epfl.ajul.mcts;

import java.util.*;
import java.util.stream.IntStream;

/// Nœud de l'arbre de recherche Monte Carlo (MCTS).
/// <p>
/// Chaque nœud représente un coup potentiel. Le coup empaqueté (10 bits) et
/// le compteur de parties N (22 bits) sont combinés dans un seul {@code int}
/// pour limiter la consommation mémoire. La somme des points S est stockée
/// séparément.
///
/// @author Ismaël Ayachi (393163)
public final class MctsNode {

    /// Coup empaqueté (bits 0–9) et compteur de parties N (bits 10–31) combinés.
    private int pkMoveAndCount;
    /// Somme S des points obtenus lors de toutes les simulations passant par ce nœud.
    private int totalPoints;
    /// Enfants de ce nœud, un par coup légal depuis l'état associé.
    MctsNode[] children;

    private static final int COUNTER_BITS = 22;
    private static final int COUNTER_OFFSET = 10;
    private static final int COUNTER_MASK = (1 << COUNTER_BITS) - 1;
    private static final int MOVE_BITS = 10;
    private static final int MOVE_OFFSET = 0;
    private static final int MOVE_MASK = (1 << MOVE_BITS) - 1;

    /// Constante d'exploration pour le calcul de la priorité des nœuds.
    private static final int EXPLORATION_CONSTANT = 80;

    private MctsNode(int pkMove, int initialCount){
        this.pkMoveAndCount = (pkMove << MOVE_OFFSET) | (initialCount << COUNTER_OFFSET);
    }

    /// Crée et retourne la racine de l'arbre MCTS, initialisée avec S=0 et N=1.
    ///
    /// @return le nœud racine
    public static MctsNode newRoot() {
        return new MctsNode(MOVE_MASK, 1);
    }

    /// Crée et retourne un nœud correspondant au coup empaqueté {@code pkMove},
    /// initialisé avec S=0 et N=0.
    ///
    /// @param pkMove le coup empaqueté associé à ce nœud
    /// @return le nouveau nœud
    public static MctsNode newMoveNode(int pkMove) {
        return new MctsNode(pkMove, 0);
    }

    /// Retourne le coup empaqueté associé à ce nœud.
    ///
    /// @return le coup empaqueté
    public short pkMove() {
        return (short) ((pkMoveAndCount >> MOVE_OFFSET) & MOVE_MASK);
    }

    /// Retourne le nombre N de parties simulées passant par ce nœud.
    ///
    /// @return le nombre de parties
    public int gameCount() {
        return (pkMoveAndCount >> COUNTER_OFFSET) & COUNTER_MASK;
    }

    /// Retourne la moyenne S/N des points obtenus lors des simulations passant par ce nœud.
    ///
    /// @return la moyenne des points
    public double averagePoints() {
        assert isValid(gameCount());
        return (double) totalPoints / gameCount();
    }

    /// Enregistre le résultat d'une simulation en ajoutant {@code points} à S
    /// et en incrémentant N.
    ///
    /// @param points les points obtenus lors de la simulation
    public void registerEvaluation(int points) {
        totalPoints += points;
        pkMoveAndCount += (1 << COUNTER_OFFSET);
    }

    /// Retourne l'index de l'enfant à explorer lors de la phase de sélection MCTS.
    /// <p>
    /// Si certains enfants n'ont pas encore été visités, retourne le prochain non visité.
    /// Sinon, retourne l'index de l'enfant ayant la priorité maximale.
    ///
    /// @return l'index de l'enfant à explorer
    public int indexOfChildToExplore() {
        if (gameCount() <= children.length) {
            return gameCount() - 1;
        }
        double logParentSimulations = Math.log(gameCount());
        double[] priorities = Arrays.stream(children)
                .mapToDouble(child -> priority(child, logParentSimulations))
                .toArray();
        return IntStream.range(0, children.length)
                .reduce(0, (best, i) -> priorities[i] > priorities[best] ? i : best);
    }

    private double priority(MctsNode childNode, double logParentSimulations) {
        double explorationBonus = Math.sqrt((double) 2 * logParentSimulations / childNode.gameCount());
        double exploration = EXPLORATION_CONSTANT * explorationBonus;
        return childNode.averagePoints() + exploration;
    }

    private static boolean isValid (int gameCount){
        return gameCount > 0;
    }
}
