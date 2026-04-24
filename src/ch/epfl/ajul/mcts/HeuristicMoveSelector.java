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

        ReservoirSampler res1 = new ReservoirSampler();
        ReservoirSampler res2 = new ReservoirSampler();
        ReservoirSampler res3 = new ReservoirSampler();

        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());

        for (int i = 0; i < validMoves; i++){
            TileKind.Colored color = PkMove.color(packedMoveArray[i]);
            TileDestination destination = PkMove.destination(packedMoveArray[i]);
            TileSource source = PkMove.source(packedMoveArray[i]);

            if (destination instanceof TileDestination.Pattern line) {
                int remaining = line.capacity() - PkPatterns.size(pattern, line);
                int tileCount = PkTileSet.countOf(
                        gameState.pkTileSources().get(source.index()), color);

                //Cas 1 : les coups remplissent totalement la ligne de motif
                if (tileCount == remaining)
                    res1.add(i, randomGenerator);

                //Cas 2 : les coups remplissent partiellement une ligne de motif
                else if (tileCount < remaining)
                    res2.add(i, randomGenerator);

                //Cas 3 : Autres coups/Les coups qui remplissent une ligne de motif avec des tuiles excédentaires
                else
                    res3.add(i, randomGenerator);

            }
            //Cas 3 : Autres coups/Les coups qui remplissent la ligne plancher
            else res3.add(i, randomGenerator);
        }

        if (res1.get() != -1) return res1.get();
        if (res2.get() != -1) return res2.get();
        return res3.get();
    }

    private static class ReservoirSampler {

        private int count = 0;
        private int res = -1;

        public void add(int move, RandomGenerator rng){
            int j = rng.nextInt(count + 1);
            if (j == 0) res = move;
            count++;
        }

        public int get(){
            return res;
        }

    }



}
