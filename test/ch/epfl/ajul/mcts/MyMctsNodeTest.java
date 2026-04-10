package ch.epfl.ajul.mcts;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyMctsNodeTest {
    @Test
    void mctsNodeNewRootIsCorrectlyInitialized() {
        MctsNode root = MctsNode.newRoot();
        assertEquals(1, root.gameCount(), "La racine doit commencer avec N=1");
        assertEquals(0, root.totalPoints());
        assertEquals(0.0, root.averagePoints(), 1e-9);
    }

    @Test
    void mctsNodeNewMoveNodeIsCorrectlyInitialized() {
        int packedMove = 0b1010101010; // Un coup fictif sur 10 bits
        MctsNode node = MctsNode.newMoveNode(packedMove);
        assertEquals(packedMove, node.pkMove());
        assertEquals(0, node.gameCount());
        assertEquals(0, node.totalPoints());
    }

    @Test
    void registerEvaluationCorrectlyUpdatesPackedInt() {
        MctsNode node = MctsNode.newMoveNode(0xFF);
        node.registerEvaluation(10);
        node.registerEvaluation(20);

        assertEquals(2, node.gameCount());
        assertEquals(30, node.totalPoints());
        assertEquals(15.0, node.averagePoints(), 1e-9);
        assertEquals(0xFF, node.pkMove(), "Le coup empaqueté ne doit pas être modifié par N");
    }

    @Test
    void indexOfChildToExploreFollowsSequentialRuleThenFormula() {
        MctsNode root = MctsNode.newRoot();
        // Création manuelle des enfants (visibles dans le paquetage)
        root.children = new MctsNode[] {
                MctsNode.newMoveNode(1),
                MctsNode.newMoveNode(2)
        };

        // Règle séquentielle : N(parent) <= nb enfants
        // N est initialisé à 1 pour la racine, donc 1 <= 2 -> index = 1 - 1 = 0
        assertEquals(0, root.indexOfChildToExplore());

        root.registerEvaluation(0); // N passe à 2. 2 <= 2 -> index = 2 - 1 = 1
        assertEquals(1, root.indexOfChildToExplore());

        root.registerEvaluation(0); // N passe à 3. 3 > 2 -> Utilise la formule de priorité

        // On donne l'avantage à l'enfant 0 par les points
        root.children[0].registerEvaluation(100);
        root.children[1].registerEvaluation(10);

        // Avec C=80, l'enfant 0 (moyenne 100) sera bien au-dessus de l'enfant 1 (moyenne 10)
        assertEquals(0, root.indexOfChildToExplore());
    }
}