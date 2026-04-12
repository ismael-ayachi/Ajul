package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import java.util.Arrays;

public final class RankComputer {

    private static final int POINTS_WEIGHT_OFFSET = 3;
    private static final int SCORE_OFFSET = 2;
    private static final int PLAYER_ID_MASK = 0b11;

    public static void playersRank(ReadOnlyGameState gameState, int[] array) {
        assert isArraySizeValid(gameState, array);
        for (PlayerId playerId : gameState.playerIds()) {
            int points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            int playerWall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
            int weightedPoints = points << POINTS_WEIGHT_OFFSET;
            int rankScore = weightedPoints | fullRowCount(playerWall);
            int rankScorePacked = (rankScore << SCORE_OFFSET) | playerId.ordinal();
            array[playerId.ordinal()] = rankScorePacked;
        }

        Arrays.sort(array);
        //Tri par ordre décroissant
        for (int i = 0; i < array.length/2 ; i++) {
            int tmp = array[i];
            array[i] = array[(array.length - i) - 1];
            array[(array.length - i) - 1] = tmp;
        }

        int[] tempArray = Arrays.copyOf(array, array.length);
        //On place les rangs dans la liste
        int previousScore = array[0];
        array[0] = 0;
        for (int i = 1; i < array.length; i++) {
            int currentScore = array[i];
            array[i] = (currentScore >> SCORE_OFFSET == previousScore >> SCORE_OFFSET) ? array[i - 1] : i;
            previousScore = currentScore;
        }

        //On associe à chaque joueur (dans l'ordre) son rang
        int[] result = new int[tempArray.length];
        for (int i = 0; i < tempArray.length; i++){
            int playerIndex = tempArray[i] & PLAYER_ID_MASK;
            result[playerIndex] = array[i];
        }
        System.arraycopy(result, 0, array, 0, array.length);

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
