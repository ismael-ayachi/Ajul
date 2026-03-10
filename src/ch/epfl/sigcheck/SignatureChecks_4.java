package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_4 {
    private SignatureChecks_4() {}

    void checkPkIntSet32() {
        v01 = new ch.epfl.ajul.gamestate.packed.PkIntSet32();
        v02 = ch.epfl.ajul.gamestate.packed.PkIntSet32.EMPTY;
        v02 = ch.epfl.ajul.gamestate.packed.PkIntSet32.add(v02, v02);
        v03 = ch.epfl.ajul.gamestate.packed.PkIntSet32.contains(v02, v02);
        v03 = ch.epfl.ajul.gamestate.packed.PkIntSet32.containsAll(v02, v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkIntSet32.remove(v02, v02);
    }

    void checkPkWall() {
        v04 = new ch.epfl.ajul.gamestate.packed.PkWall();
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.EMPTY;
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.WALL_HEIGHT;
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.WALL_WIDTH;
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.asPkTileSet(v02);
        v06 = ch.epfl.ajul.gamestate.packed.PkWall.colorAt(v05, v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.column(v05, v06);
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.hGroupSize(v02, v05, v06);
        v03 = ch.epfl.ajul.gamestate.packed.PkWall.hasFullRow(v02);
        v03 = ch.epfl.ajul.gamestate.packed.PkWall.hasTileAt(v02, v05, v06);
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.indexOf(v05, v06);
        v03 = ch.epfl.ajul.gamestate.packed.PkWall.isColorFull(v02, v06);
        v03 = ch.epfl.ajul.gamestate.packed.PkWall.isColumnFull(v02, v02);
        v03 = ch.epfl.ajul.gamestate.packed.PkWall.isRowFull(v02, v05);
        v07 = ch.epfl.ajul.gamestate.packed.PkWall.toString(v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.vGroupSize(v02, v05, v06);
        v02 = ch.epfl.ajul.gamestate.packed.PkWall.withTileAt(v02, v05, v06);
    }

    void checkPoints() {
        v08 = new ch.epfl.ajul.Points();
        v02 = ch.epfl.ajul.Points.FULL_COLOR_BONUS_POINTS;
        v02 = ch.epfl.ajul.Points.FULL_COLUMN_BONUS_POINTS;
        v02 = ch.epfl.ajul.Points.FULL_ROW_BONUS_POINTS;
        v02 = ch.epfl.ajul.Points.floorPenalty(v02);
        v02 = ch.epfl.ajul.Points.newWallTilePoints(v02, v02);
        v02 = ch.epfl.ajul.Points.totalFloorPenalty(v02);
    }

    ch.epfl.ajul.gamestate.packed.PkIntSet32 v01;
    int v02;
    boolean v03;
    ch.epfl.ajul.gamestate.packed.PkWall v04;
    ch.epfl.ajul.TileDestination.Pattern v05;
    ch.epfl.ajul.TileKind.Colored v06;
    java.lang.String v07;
    ch.epfl.ajul.Points v08;
}
