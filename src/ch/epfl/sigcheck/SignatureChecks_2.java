package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_2 {
    private SignatureChecks_2() {}

    void checkPlayerId() {
        v02 = (java.lang.Enum) v01;
        v03 = ch.epfl.ajul.PlayerId.ALL;
        v01 = ch.epfl.ajul.PlayerId.P1;
        v01 = ch.epfl.ajul.PlayerId.P2;
        v01 = ch.epfl.ajul.PlayerId.P3;
        v01 = ch.epfl.ajul.PlayerId.P4;
        v01 = ch.epfl.ajul.PlayerId.valueOf(v04);
        v05 = ch.epfl.ajul.PlayerId.values();
    }

    void checkMove() {
        v02 = (java.lang.Record) v06;
        v06 = new ch.epfl.ajul.gamestate.Move(v07, v08, v09);
        v10 = ch.epfl.ajul.gamestate.Move.MAX_MOVES;
        v06 = ch.epfl.ajul.gamestate.Move.ofPacked(v11);
        v09 = v06.destination();
        v12 = v06.equals(v02);
        v10 = v06.hashCode();
        v11 = v06.packed();
        v07 = v06.source();
        v08 = v06.tileColor();
        v04 = v06.toString();
    }

    void checkPkMove() {
        v13 = new ch.epfl.ajul.gamestate.packed.PkMove();
        v08 = ch.epfl.ajul.gamestate.packed.PkMove.color(v11);
        v09 = ch.epfl.ajul.gamestate.packed.PkMove.destination(v11);
        v11 = ch.epfl.ajul.gamestate.packed.PkMove.pack(v07, v08, v09);
        v07 = ch.epfl.ajul.gamestate.packed.PkMove.source(v11);
    }

    void checkPkTileSet() {
        v14 = new ch.epfl.ajul.gamestate.packed.PkTileSet();
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.EMPTY;
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.FULL;
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.FULL_COLORED;
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.add(v10, v15);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.copyColoredInto(v10, v16);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.countOf(v10, v15);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.difference(v10, v10);
        v12 = ch.epfl.ajul.gamestate.packed.PkTileSet.isEmpty(v10);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.of(v10, v15);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.remove(v10, v15);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.sampleColoredInto(v10, v16, v10, v17);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.size(v10);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.subsetOf(v10, v15);
        v04 = ch.epfl.ajul.gamestate.packed.PkTileSet.toString(v10);
        v10 = ch.epfl.ajul.gamestate.packed.PkTileSet.union(v10, v10);
    }

    ch.epfl.ajul.PlayerId v01;
    java.lang.Object v02;
    java.util.List<ch.epfl.ajul.PlayerId> v03;
    java.lang.String v04;
    ch.epfl.ajul.PlayerId[] v05;
    ch.epfl.ajul.gamestate.Move v06;
    ch.epfl.ajul.TileSource v07;
    ch.epfl.ajul.TileKind.Colored v08;
    ch.epfl.ajul.TileDestination v09;
    int v10;
    short v11;
    boolean v12;
    ch.epfl.ajul.gamestate.packed.PkMove v13;
    ch.epfl.ajul.gamestate.packed.PkTileSet v14;
    ch.epfl.ajul.TileKind v15;
    ch.epfl.ajul.TileKind.Colored[] v16;
    java.util.random.RandomGenerator v17;
}
