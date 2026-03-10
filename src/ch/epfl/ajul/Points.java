package ch.epfl.ajul;

public final class Points {
    //Améliorer la définition des attributs
    public static final int FULL_ROW_BONUS_POINTS = 2;
    public static final int FULL_COLUMN_BONUS_POINTS = 7;
    public static final int FULL_COLOR_BONUS_POINTS = 10;
    private static final int FLOOR_PENALTY = 0x3322211;
    private static final int FLOOR_PENALTY_MASK = 0b1111;
    //private static final int TOTAL_FLOOR_PENALTY = 0xEB864210;
    private static final int TOTAL_FLOOR_PENALTY = (FLOOR_PENALTY * 0x1111111) << 4;


    public static int newWallTilePoints(int hGroupSize, int vGroupSize) {
        if (vGroupSize == 1) {
            return hGroupSize;
        }
        else if (hGroupSize == 1){
            return vGroupSize;
        }
        return hGroupSize + vGroupSize;
    }

    public static int floorPenalty(int tileIndex){
        return (FLOOR_PENALTY >> (TileSource.Factory.TILES_PER_FACTORY*tileIndex)) & (FLOOR_PENALTY_MASK);

    }

    public static int totalFloorPenalty(int tilesCount) {
        return (TOTAL_FLOOR_PENALTY >> (TileSource.Factory.TILES_PER_FACTORY*tilesCount)) & (FLOOR_PENALTY_MASK);
    }
}
