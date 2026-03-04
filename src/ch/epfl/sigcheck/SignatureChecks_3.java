package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_3 {
    private SignatureChecks_3() {}

    void checkPkPatterns() {
        v01 = new ch.epfl.ajul.gamestate.packed.PkPatterns();
        v02 = ch.epfl.ajul.gamestate.packed.PkPatterns.EMPTY;
        v02 = ch.epfl.ajul.gamestate.packed.PkPatterns.asPkTileSet(v02);
        v05 = ch.epfl.ajul.gamestate.packed.PkPatterns.canContain(v02, v03, v04);
        v04 = ch.epfl.ajul.gamestate.packed.PkPatterns.color(v02, v03);
        v05 = ch.epfl.ajul.gamestate.packed.PkPatterns.isFull(v02, v03);
        v02 = ch.epfl.ajul.gamestate.packed.PkPatterns.size(v02, v03);
        v06 = ch.epfl.ajul.gamestate.packed.PkPatterns.toString(v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkPatterns.withAddedTiles(v02, v03, v02, v04);
        v02 = ch.epfl.ajul.gamestate.packed.PkPatterns.withEmptyLine(v02, v03);
    }

    void checkPkFloor() {
        v07 = new ch.epfl.ajul.gamestate.packed.PkFloor();
        v02 = ch.epfl.ajul.gamestate.packed.PkFloor.EMPTY;
        v02 = ch.epfl.ajul.gamestate.packed.PkFloor.asPkTileSet(v02);
        v05 = ch.epfl.ajul.gamestate.packed.PkFloor.containsFirstPlayerMarker(v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkFloor.size(v02);
        v08 = ch.epfl.ajul.gamestate.packed.PkFloor.tileAt(v02, v02);
        v06 = ch.epfl.ajul.gamestate.packed.PkFloor.toString(v02);
        v02 = ch.epfl.ajul.gamestate.packed.PkFloor.withAddedTiles(v02, v02);
    }

    void checkPreconditions() {
        v09 = new ch.epfl.ajul.Preconditions();
        ch.epfl.ajul.Preconditions.checkArgument(v05);
    }

    void checkGame() {
        v10 = new ch.epfl.ajul.Game(v11);
        v02 = v10.centralAreaMaxSize();
        v12 = v10.factories();
        v02 = v10.factoriesCount();
        v11 = v10.playerDescriptions();
        v13 = v10.playerIds();
        v02 = v10.playersCount();
        v14 = v10.tileSources();
        v02 = v10.tileSourcesCount();
    }

    void checkGame_PlayerDescription() {
        v16 = (java.lang.Record) v15;
        v15 = new ch.epfl.ajul.Game.PlayerDescription(v17, v06, v18);
        v05 = v15.equals(v16);
        v02 = v15.hashCode();
        v17 = v15.id();
        v18 = v15.kind();
        v06 = v15.name();
        v06 = v15.toString();
    }

    void checkGame_PlayerDescription_PlayerKind() {
        v16 = (java.lang.Enum) v18;
        v18 = ch.epfl.ajul.Game.PlayerDescription.PlayerKind.AI;
        v18 = ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
        v18 = ch.epfl.ajul.Game.PlayerDescription.PlayerKind.valueOf(v06);
        v19 = ch.epfl.ajul.Game.PlayerDescription.PlayerKind.values();
    }

    ch.epfl.ajul.gamestate.packed.PkPatterns v01;
    int v02;
    ch.epfl.ajul.TileDestination.Pattern v03;
    ch.epfl.ajul.TileKind.Colored v04;
    boolean v05;
    java.lang.String v06;
    ch.epfl.ajul.gamestate.packed.PkFloor v07;
    ch.epfl.ajul.TileKind v08;
    ch.epfl.ajul.Preconditions v09;
    ch.epfl.ajul.Game v10;
    java.util.List<ch.epfl.ajul.Game.PlayerDescription> v11;
    java.util.List<ch.epfl.ajul.TileSource.Factory> v12;
    java.util.List<ch.epfl.ajul.PlayerId> v13;
    java.util.List<ch.epfl.ajul.TileSource> v14;
    ch.epfl.ajul.Game.PlayerDescription v15;
    java.lang.Object v16;
    ch.epfl.ajul.PlayerId v17;
    ch.epfl.ajul.Game.PlayerDescription.PlayerKind v18;
    ch.epfl.ajul.Game.PlayerDescription.PlayerKind[] v19;
}
