package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_5 {
    private SignatureChecks_5() {}

    void checkImmutableGameState() {
        v02 = (java.lang.Record) v01;
        v02 = (ch.epfl.ajul.gamestate.ReadOnlyGameState) v01;
        v01 = new ch.epfl.ajul.gamestate.ImmutableGameState(v03, v04, v05, v04, v05, v06);
        v01 = ch.epfl.ajul.gamestate.ImmutableGameState.initial(v03);
        v06 = v01.currentPlayerId();
        v07 = v01.equals(v02);
        v03 = v01.game();
        v04 = v01.hashCode();
        v01 = v01.immutable();
        v05 = v01.pkPlayerStates();
        v04 = v01.pkTileBag();
        v05 = v01.pkTileSources();
        v04 = v01.pkUniqueTileSources();
        v08 = v01.toString();
    }

    void checkPkPlayerStates() {
        v09 = new ch.epfl.ajul.gamestate.packed.PkPlayerStates();
        ch.epfl.ajul.gamestate.packed.PkPlayerStates.addPoints(v10, v06, v04);
        v05 = ch.epfl.ajul.gamestate.packed.PkPlayerStates.initial(v03);
        v04 = ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkFloor(v11, v06);
        v04 = ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkPatterns(v11, v06);
        v04 = ch.epfl.ajul.gamestate.packed.PkPlayerStates.pkWall(v11, v06);
        v04 = ch.epfl.ajul.gamestate.packed.PkPlayerStates.points(v11, v06);
        ch.epfl.ajul.gamestate.packed.PkPlayerStates.setPkFloor(v10, v06, v04);
        ch.epfl.ajul.gamestate.packed.PkPlayerStates.setPkPatterns(v10, v06, v04);
        ch.epfl.ajul.gamestate.packed.PkPlayerStates.setPkWall(v10, v06, v04);
    }

    void checkReadOnlyGameState() {
        v06 = v12.currentPlayerId();
        v03 = v12.game();
        v01 = v12.immutable();
        v07 = v12.isGameOver();
        v07 = v12.isRoundOver();
        v04 = v12.pkDiscardedTiles();
        v11 = v12.pkPlayerStates();
        v04 = v12.pkTileBag();
        v11 = v12.pkTileSources();
        v04 = v12.pkUniqueTileSources();
        v13 = v12.playerIds();
        v04 = v12.uniqueValidMoves(v14);
        v04 = v12.validMoves(v14);
    }

    ch.epfl.ajul.gamestate.ImmutableGameState v01;
    java.lang.Object v02;
    ch.epfl.ajul.Game v03;
    int v04;
    ch.epfl.ajul.intarray.ImmutableIntArray v05;
    ch.epfl.ajul.PlayerId v06;
    boolean v07;
    java.lang.String v08;
    ch.epfl.ajul.gamestate.packed.PkPlayerStates v09;
    int[] v10;
    ch.epfl.ajul.intarray.ReadOnlyIntArray v11;
    ch.epfl.ajul.gamestate.ReadOnlyGameState v12;
    java.util.List<ch.epfl.ajul.PlayerId> v13;
    short[] v14;
}
