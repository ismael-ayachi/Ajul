package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import java.util.Arrays;

public final class RankComputer {

    private static final int POINTS_WEIGHT_OFFSET = 3;

    public static void playersRank(ReadOnlyGameState gameState, int[] array) {
        assert isArraySizeValid(gameState, array);
        for (PlayerId playerId : gameState.playerIds()) {
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            int playerWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
            int weightedPoints = points << POINTS_WEIGHT_OFFSET;
            int rankScore = weightedPoints | fullRowCount(playerWall);
            int rankScorePacked = (rankScore << 2) | playerId.ordinal();
            array[playerId.ordinal()] = rankScorePacked;
        }
        Arrays.sort(array);
        //Tri par ordre décroissant
        for (int i = 0; i < array.length/2 ; i++) {
            int tmp = array[i];
            array[i] = array[(array.length - i) - 1];
            array[(array.length - i) - 1] = tmp;
        }
        //Remplacement des scores dans array par les rangs des joueurs
        int previousScore = array[0];
        array[0] = 0;
        for (int i = 1; i < array.length; i++) {
            int currentScore = array[i];
            array[i] = (currentScore == previousScore) ? array[i - 1] : i;
            previousScore = currentScore;
        }

    }

    private static boolean isArraySizeValid (ReadOnlyGameState gameState, int[] array){
        return array.length == gameState.playerIds().size();
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
}
