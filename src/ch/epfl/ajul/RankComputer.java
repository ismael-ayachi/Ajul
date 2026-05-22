package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import java.util.Arrays;

/// Classe utilitaire calculant le rang final des joueurs d'une partie.
/// <p>
/// Le classement se fait d'abord selon le nombre de points, puis, en cas d'égalité,
/// selon le nombre de lignes horizontales complètes du mur. Les joueurs à égalité
/// parfaite reçoivent le même rang.
///
/// @author Ismaël Ayachi (393163)
public final class RankComputer {

    private static final int POINTS_WEIGHT_OFFSET = 3;
    private static final int SCORE_OFFSET = 2;
    private static final int PLAYER_ID_MASK = 0b11;

    /// Calcule le rang de chaque joueur de {@code gameState} et l'écrit dans {@code array},
    /// le rang du joueur d'ordinal {@code i} étant placé à l'index {@code i}.
    /// Le rang 0 correspond au meilleur joueur ; les égalités donnent un rang identique.
    ///
    /// @param gameState l'état de la partie terminée
    /// @param array     le tableau de destination, de taille égale au nombre de joueurs
    public static void playersRank(ReadOnlyGameState gameState, int[] array) {
        assert isArraySizeValid(gameState, array);
        for (PlayerId playerId : gameState.playerIds()) {
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            int playerWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
            int rankScore = points << POINTS_WEIGHT_OFFSET | fullRowCount(playerWall);
            array[playerId.ordinal()] = (rankScore << SCORE_OFFSET) | playerId.ordinal();
        }
        Arrays.sort(array);

        // Calcul des rangs des joueurs
        int[] playersRank = new int[array.length];
        int previousScore = -1, previousRank = 0;
        for (int i = 0; i < array.length; i++) {
            int index = array.length - 1 - i;
            int score = array[index] >> SCORE_OFFSET;
            int rank = (score == previousScore) ? previousRank : i;
            int playerIndex = array[index] & PLAYER_ID_MASK;
            playersRank[playerIndex] = rank;
            previousScore = score;
            previousRank = rank;
        }

        System.arraycopy(playersRank, 0, array, 0, array.length);

    }

    private static int fullRowCount(int pkWall) {
        int fullRowCount = 0;
        for (TileDestination.Pattern line: TileDestination.Pattern.ALL) {
            if (PkWall.isRowFull(pkWall, line)){
                fullRowCount++;
            }
        }
        return fullRowCount;
    }

    private static boolean isArraySizeValid (ReadOnlyGameState gameState, int[] array){
        return array.length == gameState.playerIds().size();
    }
}
