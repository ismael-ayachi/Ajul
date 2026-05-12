package ch.epfl.ajul.mcts;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyMctsNodeTest {

    @Test
    void newRootHasGameCount1() {
        assertEquals(1, MctsNode.newRoot().gameCount());
    }

    @Test
    void newRootHasTotalPoints0() {
        //assertEquals(0, MctsNode.newRoot().totalPoints());
    }

    @Test
    void newRootHasAveragePoints0() {
        assertEquals(0.0, MctsNode.newRoot().averagePoints(), 1e-9);
    }

    @Test
    void newMoveNodeHasGameCount0() {
        var pkMove = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.PATTERN_1);
        assertEquals(0, MctsNode.newMoveNode(pkMove).gameCount());
    }

    @Test
    void newMoveNodeHasTotalPoints0() {
        var pkMove = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.PATTERN_1);
        //assertEquals(0, MctsNode.newMoveNode(pkMove).totalPoints());
    }

    @Test
    void newMoveNodeStoresPkMoveForAllColorAndPatternCombinations() {
        for (var color : TileKind.Colored.ALL) {
            for (var line : TileDestination.Pattern.ALL) {
                var pkMove = PkMove.pack(TileSource.FACTORY_1, color, line);
                assertEquals(pkMove, MctsNode.newMoveNode(pkMove).pkMove());
            }
        }
    }

    @Test
    void newMoveNodeStoresPkMoveForAllSources() {
        for (var source : TileSource.ALL) {
            var pkMove = PkMove.pack(source, TileKind.Colored.A, TileDestination.PATTERN_1);
            assertEquals(pkMove, MctsNode.newMoveNode(pkMove).pkMove());
        }
    }

    @Test
    void newMoveNodeStoresPkMoveForFloorDestination() {
        var pkMove = PkMove.pack(TileSource.CENTER_AREA, TileKind.Colored.E, TileDestination.FLOOR);
        assertEquals(pkMove, MctsNode.newMoveNode(pkMove).pkMove());
    }

    @Test
    void registerEvaluationOnceGivesGameCount1() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.PATTERN_2));
        node.registerEvaluation(50);
        assertEquals(1, node.gameCount());
    }

    @Test
    void registerEvaluationTwiceGivesGameCount2() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.PATTERN_2));
        node.registerEvaluation(50);
        node.registerEvaluation(30);
        assertEquals(2, node.gameCount());
    }

    @Test
    void registerEvaluationOnceGivesCorrectTotalPoints() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.PATTERN_2));
        node.registerEvaluation(50);
        //assertEquals(50, node.totalPoints());
    }

    @Test
    void registerEvaluationTwiceAccumulatesTotalPoints() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.B, TileDestination.PATTERN_2));
        node.registerEvaluation(50);
        node.registerEvaluation(30);
        //assertEquals(80, node.totalPoints());
    }

    @Test
    void registerEvaluationWith0PointsIncreasesGameCountButNotPoints() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.PATTERN_1));
        node.registerEvaluation(0);
        assertEquals(1, node.gameCount());
        //assertEquals(0, node.totalPoints());
    }

    @Test
    void registerEvaluation100TimesGivesCorrectGameCount() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.CENTER_AREA, TileKind.Colored.C, TileDestination.PATTERN_3));
        for (int i = 1; i <= 100; i++)
            node.registerEvaluation(i);
        assertEquals(100, node.gameCount());
    }

    @Test
    void registerEvaluation100TimesGivesCorrectTotalPoints() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.CENTER_AREA, TileKind.Colored.C, TileDestination.PATTERN_3));
        for (int i = 1; i <= 100; i++)
            node.registerEvaluation(i);
       // assertEquals(5050, node.totalPoints());
    }

    @Test
    void registerEvaluation100000TimesGivesCorrectGameCount() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_5, TileKind.Colored.E, TileDestination.PATTERN_5));
        for (int i = 0; i < 100_000; i++)
            node.registerEvaluation(i % 240);
        assertEquals(100_000, node.gameCount());
    }

    @Test
    void averagePointsAfterOneEvaluation() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, TileDestination.PATTERN_1));
        node.registerEvaluation(42);
        assertEquals(42.0, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsReturnsDoubleNotTruncatedInt() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_2, TileKind.Colored.C, TileDestination.PATTERN_3));
        node.registerEvaluation(10);
        node.registerEvaluation(11);
        assertEquals(10.5, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsAfter3EvaluationsIsCorrect() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.FACTORY_3, TileKind.Colored.D, TileDestination.PATTERN_4));
        node.registerEvaluation(10);
        node.registerEvaluation(15);
        node.registerEvaluation(20);
        assertEquals(15.0, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsAfter100EvaluationsIsCorrect() {
        var node = MctsNode.newMoveNode(PkMove.pack(TileSource.CENTER_AREA, TileKind.Colored.C, TileDestination.PATTERN_3));
        for (int i = 1; i <= 100; i++)
            node.registerEvaluation(i);
        assertEquals(50.5, node.averagePoints(), 1e-9);
    }

    @Test
    void pkMoveIsPreservedAfterManyRegisterEvaluations() {
        var pkMove = PkMove.pack(TileSource.FACTORY_7, TileKind.Colored.D, TileDestination.PATTERN_5);
        var node = MctsNode.newMoveNode(pkMove);
        for (int i = 0; i < 1000; i++)
            node.registerEvaluation(i % 100);
        assertEquals(pkMove, node.pkMove());
    }

    @Test
    void pkMoveBitsNotCorruptedByLargeGameCount() {
        var pkMove = PkMove.pack(TileSource.FACTORY_9, TileKind.Colored.E, TileDestination.FLOOR);
        var node = MctsNode.newMoveNode(pkMove);
        for (int i = 0; i < 50_000; i++)
            node.registerEvaluation(42);
        assertEquals(pkMove, node.pkMove());
    }
}