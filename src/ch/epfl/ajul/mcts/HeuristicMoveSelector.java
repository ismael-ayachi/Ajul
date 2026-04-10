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

        int index1 = 0, index2 = 0 , index3 = 0;
        int moveIndex = 0;
        int[] subMoves1 = new int[validMoves];
        int[] subMoves2 = new int[validMoves];
        int[] subMoves3 = new int[validMoves];
        for (short move: packedMoveArray){

            TileSource sourceMove = PkMove.source(move);
            TileKind colorMove = PkMove.color(move);

            if (PkMove.destination(move) instanceof TileDestination.Pattern){
                TileDestination patternMove = PkMove.destination(move);
                int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());
                int remainingTiles =
                        patternMove.capacity() - PkPatterns.size(pattern, (TileDestination.Pattern) patternMove);
                int tilesMoveCount = PkTileSet.countOf(gameState.pkTileSources().get(sourceMove.index()), colorMove);
                if (tilesMoveCount == remainingTiles) {
                    subMoves1[index1] = moveIndex;
                    index1++;
                }
                else if (tilesMoveCount < remainingTiles) {
                    subMoves2[index2] = moveIndex;
                    index2++;
                }
            }
            else {
                subMoves3[index3] = moveIndex;
                index3++;
            }
            moveIndex++;
        }

        if (subMoves1.length != 0) {
            int selectedIndex = subMoves1[randomGenerator.nextInt(0, index1 + 1 )];
            if (selectedIndex >= 0 && selectedIndex < validMoves)
                return selectedIndex;
        }
        else if (subMoves2.length != 0) {
            int selectedIndex = subMoves2[randomGenerator.nextInt(0, index2 + 1)];
            if (selectedIndex >= 0 && selectedIndex < validMoves)
                return selectedIndex;
        }

        return subMoves3[randomGenerator.nextInt(0, index3 + 1)];
    }


    private static boolean isIndexValid (int i, int[] packedMoveArray){
        return i >= 0 && i < packedMoveArray.length;
    }

}
