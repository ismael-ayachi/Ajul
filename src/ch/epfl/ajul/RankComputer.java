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
