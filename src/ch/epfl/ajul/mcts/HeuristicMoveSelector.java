package ch.epfl.ajul.mcts;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;

import java.util.random.RandomGenerator;

public final class HeuristicMoveSelector {

    public static int selectMove(RandomGenerator randomGenerator,
                                 ReadOnlyGameState gameState,
                                 short[] packedMoveArray,
                                 int validMoves) {

        //Revoir le code en utilisant l'échantillonage par réservoir
        int index1 = 0, index2 = 0 , index3 = 0;
        int[] subMoves1 = new int[validMoves];
        int[] subMoves2 = new int[validMoves];
        int[] subMoves3 = new int[validMoves];

        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());
        for (int i = 0; i < validMoves; i++) {
            var color = PkMove.color(packedMoveArray[i]);
            var destination = PkMove.destination(packedMoveArray[i]);
            var source = PkMove.source(packedMoveArray[i]);

            if (destination instanceof TileDestination.Pattern line) {
                int remaining = line.capacity() - PkPatterns.size(pattern, line);
                int tileCount = PkTileSet.countOf(
                        gameState.pkTileSources().get(source.index()), color);
                if (tileCount == remaining) {
                    subMoves1[index1++] = i;
                } else {
                    subMoves2[index2++] = i;
                }
            } else {
                subMoves3[index3++] = i;
            }
        }

        if (index1 > 0) return subMoves1[randomGenerator.nextInt(index1)];
        if (index2 > 0) return subMoves2[randomGenerator.nextInt(index2)];
        return subMoves3[randomGenerator.nextInt(index3)];
    }


}
