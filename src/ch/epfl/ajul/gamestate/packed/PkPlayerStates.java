package ch.epfl.ajul.gamestate.packed;
import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

public final class PkPlayerStates {

    private static final int PLAYER_STATE_CONTENT_SIZE = 4;

    private static final int PATTERN_OFFSET = 0;
    private static final int FLOOR_OFFSET = 1;
    private static final int WALL_OFFSET = 2;
    private static final int POINTS_OFFSET = 3;

    public static ImmutableIntArray initial(Game game){
        int[] initialState = new int[PLAYER_STATE_CONTENT_SIZE*game.playersCount()];
        for (int i=0; i < game.playersCount(); i++){
            initialState[PLAYER_STATE_CONTENT_SIZE*i + PATTERN_OFFSET] = PkPatterns.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE*i + FLOOR_OFFSET] = PkFloor.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE*i + WALL_OFFSET] = PkWall.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE*i + POINTS_OFFSET] = 0;
        }
        return ImmutableIntArray.copyOf(initialState);
    }

    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + PATTERN_OFFSET);
    }

    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId){
        return pkPlayerStates.get(playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + FLOOR_OFFSET);
    }

    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + WALL_OFFSET);
    }

    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + POINTS_OFFSET);
    }

    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + PATTERN_OFFSET] = pkPatterns;
    }

    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + FLOOR_OFFSET] = pkFloor;
    }
    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + WALL_OFFSET] = pkWall;
    }

    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        pkPlayerStates[playerId.ordinal()*PLAYER_STATE_CONTENT_SIZE + POINTS_OFFSET] += pointsToAdd;
    }


}
