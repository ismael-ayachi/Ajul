package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_6 {
    private SignatureChecks_6() {}

    void checkMutableGameState() {
        v02 = (ch.epfl.ajul.gamestate.ReadOnlyGameState) v01;
        v01 = new ch.epfl.ajul.gamestate.MutableGameState(v03, v04);
        v01 = new ch.epfl.ajul.gamestate.MutableGameState(v03);
        v05 = v01.currentPlayerId();
        v01.endGame();
        v01.endRound();
        v01.fillFactories(v06);
        v07 = v01.game();
        v08 = v01.pkPlayerStates();
        v09 = v01.pkTileBag();
        v08 = v01.pkTileSources();
        v09 = v01.pkUniqueTileSources();
        v01.registerMove(v10);
    }

    void checkPointsObserver() {
        v04 = ch.epfl.ajul.PointsObserver.EMPTY;
        v04.floor(v05, v09);
        v04.fullColor(v05, v11, v09);
        v04.fullColumn(v05, v09, v09);
        v04.fullRow(v05, v12, v09);
        v04.newWallTile(v05, v12, v11, v09);
    }

    ch.epfl.ajul.gamestate.MutableGameState v01;
    java.lang.Object v02;
    ch.epfl.ajul.gamestate.ReadOnlyGameState v03;
    ch.epfl.ajul.PointsObserver v04;
    ch.epfl.ajul.PlayerId v05;
    java.util.random.RandomGenerator v06;
    ch.epfl.ajul.Game v07;
    ch.epfl.ajul.intarray.ReadOnlyIntArray v08;
    int v09;
    short v10;
    ch.epfl.ajul.TileKind.Colored v11;
    ch.epfl.ajul.TileDestination.Pattern v12;
}
