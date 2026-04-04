package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.platform.commons.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        for (int i = 0; i < array.length/2 ; i++) {
            int tmp = array[i];
            array[i] = array[(array.length - i) - 1];
            array[(array.length - i) - 1] = tmp;
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
