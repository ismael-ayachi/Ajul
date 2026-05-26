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

/// Sélectionneur de coups heuristique utilisé lors de la phase de simulation du MCTS.
/// <p>
/// Plutôt que de choisir un coup totalement au hasard, les coups sont répartis en trois
/// catégories de priorité décroissante, et un coup est tiré aléatoirement dans la
/// catégorie non vide la plus prioritaire.
///
/// @author Ismaël Ayachi (393163)
public final class HeuristicMoveSelector {

    /// Sélectionne un coup parmi les {@code validMoves} premiers coups de
    /// {@code packedMoveArray} et retourne son index dans ce tableau.
    /// <p>
    /// Les coups sont partitionnés en trois catégories : ceux qui remplissent
    /// totalement une ligne de motif (priorité maximale), ceux qui la remplissent
    /// partiellement, et les autres (débordement ou plancher). Un coup est tiré
    /// uniformément au hasard dans la catégorie non vide la plus prioritaire.
    ///
    /// @param randomGenerator le générateur aléatoire utilisé pour le tirage
    /// @param gameState       l'état de la partie courant
    /// @param packedMoveArray le tableau des coups empaquetés candidats
    /// @param validMoves      le nombre de coups valides dans {@code packedMoveArray}
    /// @return l'index du coup choisi dans {@code packedMoveArray}
    public static int selectMove(RandomGenerator randomGenerator,
                                 ReadOnlyGameState gameState,
                                 short[] packedMoveArray,
                                 int validMoves) {

        ReservoirSampler res1 = new ReservoirSampler();
        ReservoirSampler res2 = new ReservoirSampler();
        ReservoirSampler res3 = new ReservoirSampler();

        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());

        for (int i = 0; i < validMoves; i++){
            TileDestination destination = PkMove.destination(packedMoveArray[i]);
            if (destination instanceof TileDestination.Pattern line) {
                TileKind.Colored color = PkMove.color(packedMoveArray[i]);
                TileSource source = PkMove.source(packedMoveArray[i]);
                int remaining = line.capacity() - PkPatterns.size(pattern, line);
                int tileCount = PkTileSet.countOf(gameState.pkTileSources().get(source.index()), color);

                // Cas 1 : les coups remplissent totalement la ligne de motif
                if (tileCount == remaining)
                    res1.add(i, randomGenerator);

                // Cas 2 : les coups remplissent partiellement une ligne de motif
                else if (tileCount < remaining)
                    res2.add(i, randomGenerator);

                // Cas 3 : Autres coups/Les coups qui remplissent une ligne de motif avec des tuiles excédentaires
                else
                    res3.add(i, randomGenerator);
            }
            // Cas 3 : Autres coups/Les coups qui remplissent la ligne plancher
            else res3.add(i, randomGenerator);
        }

        if (res1.get() != -1) return res1.get();
        if (res2.get() != -1) return res2.get();
        return res3.get();
    }

    /// Échantillonneur par réservoir de taille 1 : sélectionne uniformément au hasard
    /// l'un des éléments qui lui sont successivement présentés, sans les stocker tous.
    private static final class ReservoirSampler {
        private int count = 0;
        private int res = -1;

        private void add(int move, RandomGenerator rng){
            count++;
            if (count == 1 || rng.nextInt(count) == 0) res = move;
        }

        private int get(){
            return res;
        }
    }
}
