package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

final class SignatureChecks_1 {
    private SignatureChecks_1() {}

    void checkTileKind() {
        v01 = ch.epfl.ajul.TileKind.A;
        v02 = ch.epfl.ajul.TileKind.ALL;
        v01 = ch.epfl.ajul.TileKind.B;
        v01 = ch.epfl.ajul.TileKind.C;
        v03 = ch.epfl.ajul.TileKind.COUNT;
        v01 = ch.epfl.ajul.TileKind.D;
        v01 = ch.epfl.ajul.TileKind.E;
        v01 = ch.epfl.ajul.TileKind.FIRST_PLAYER_MARKER;
        v03 = v01.index();
        v03 = v01.tilesCount();
    }

    void checkTileKind_Colored() {
        v05 = (java.lang.Enum) v04;
        v05 = (ch.epfl.ajul.TileKind) v04;
        v04 = ch.epfl.ajul.TileKind.Colored.A;
        v06 = ch.epfl.ajul.TileKind.Colored.ALL;
        v04 = ch.epfl.ajul.TileKind.Colored.B;
        v04 = ch.epfl.ajul.TileKind.Colored.C;
        v03 = ch.epfl.ajul.TileKind.Colored.COUNT;
        v04 = ch.epfl.ajul.TileKind.Colored.D;
        v04 = ch.epfl.ajul.TileKind.Colored.E;
        ch.epfl.ajul.TileKind.Colored.shuffle(v07, v08);
        v04 = ch.epfl.ajul.TileKind.Colored.valueOf(v09);
        v07 = ch.epfl.ajul.TileKind.Colored.values();
        v03 = v04.index();
        v03 = v04.tilesCount();
    }

    void checkTileKind_FirstPlayerMarker() {
        v05 = (java.lang.Enum) v10;
        v05 = (ch.epfl.ajul.TileKind) v10;
        v10 = ch.epfl.ajul.TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER;
        v10 = ch.epfl.ajul.TileKind.FirstPlayerMarker.valueOf(v09);
        v11 = ch.epfl.ajul.TileKind.FirstPlayerMarker.values();
        v03 = v10.index();
        v03 = v10.tilesCount();
    }

    void checkTileDestination() {
        v12 = ch.epfl.ajul.TileDestination.ALL;
        v03 = ch.epfl.ajul.TileDestination.COUNT;
        v13 = ch.epfl.ajul.TileDestination.FLOOR;
        v13 = ch.epfl.ajul.TileDestination.PATTERN_1;
        v13 = ch.epfl.ajul.TileDestination.PATTERN_2;
        v13 = ch.epfl.ajul.TileDestination.PATTERN_3;
        v13 = ch.epfl.ajul.TileDestination.PATTERN_4;
        v13 = ch.epfl.ajul.TileDestination.PATTERN_5;
        v03 = v13.capacity();
        v03 = v13.index();
    }

    void checkTileDestination_Pattern() {
        v05 = (java.lang.Enum) v14;
        v05 = (ch.epfl.ajul.TileDestination) v14;
        v15 = ch.epfl.ajul.TileDestination.Pattern.ALL;
        v03 = ch.epfl.ajul.TileDestination.Pattern.COUNT;
        v14 = ch.epfl.ajul.TileDestination.Pattern.PATTERN_1;
        v14 = ch.epfl.ajul.TileDestination.Pattern.PATTERN_2;
        v14 = ch.epfl.ajul.TileDestination.Pattern.PATTERN_3;
        v14 = ch.epfl.ajul.TileDestination.Pattern.PATTERN_4;
        v14 = ch.epfl.ajul.TileDestination.Pattern.PATTERN_5;
        v14 = ch.epfl.ajul.TileDestination.Pattern.valueOf(v09);
        v16 = ch.epfl.ajul.TileDestination.Pattern.values();
        v03 = v14.capacity();
        v03 = v14.index();
    }

    void checkTileDestination_Floor() {
        v05 = (java.lang.Enum) v17;
        v05 = (ch.epfl.ajul.TileDestination) v17;
        v17 = ch.epfl.ajul.TileDestination.Floor.FLOOR;
        v17 = ch.epfl.ajul.TileDestination.Floor.valueOf(v09);
        v18 = ch.epfl.ajul.TileDestination.Floor.values();
        v03 = v17.capacity();
        v03 = v17.index();
    }

    void checkTileSource() {
        v19 = ch.epfl.ajul.TileSource.ALL;
        v20 = ch.epfl.ajul.TileSource.CENTER_AREA;
        v03 = ch.epfl.ajul.TileSource.COUNT;
        v20 = ch.epfl.ajul.TileSource.FACTORY_1;
        v20 = ch.epfl.ajul.TileSource.FACTORY_2;
        v20 = ch.epfl.ajul.TileSource.FACTORY_3;
        v20 = ch.epfl.ajul.TileSource.FACTORY_4;
        v20 = ch.epfl.ajul.TileSource.FACTORY_5;
        v20 = ch.epfl.ajul.TileSource.FACTORY_6;
        v20 = ch.epfl.ajul.TileSource.FACTORY_7;
        v20 = ch.epfl.ajul.TileSource.FACTORY_8;
        v20 = ch.epfl.ajul.TileSource.FACTORY_9;
        v03 = v20.index();
    }

    void checkTileSource_CenterArea() {
        v05 = (java.lang.Enum) v21;
        v05 = (ch.epfl.ajul.TileSource) v21;
        v21 = ch.epfl.ajul.TileSource.CenterArea.CENTER_AREA;
        v21 = ch.epfl.ajul.TileSource.CenterArea.valueOf(v09);
        v22 = ch.epfl.ajul.TileSource.CenterArea.values();
        v03 = v21.index();
    }

    void checkTileSource_Factory() {
        v05 = (java.lang.Enum) v23;
        v05 = (ch.epfl.ajul.TileSource) v23;
        v24 = ch.epfl.ajul.TileSource.Factory.ALL;
        v03 = ch.epfl.ajul.TileSource.Factory.COUNT;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_1;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_2;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_3;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_4;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_5;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_6;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_7;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_8;
        v23 = ch.epfl.ajul.TileSource.Factory.FACTORY_9;
        v03 = ch.epfl.ajul.TileSource.Factory.TILES_PER_FACTORY;
        v23 = ch.epfl.ajul.TileSource.Factory.valueOf(v09);
        v25 = ch.epfl.ajul.TileSource.Factory.values();
        v03 = v23.index();
    }

    void checkReadOnlyIntArray() {
        v03 = v26.get(v03);
        v27 = v26.immutable();
        v03 = v26.size();
        v28 = v26.toArray();
    }

    void checkAbstractIntArray() {
        v05 = (ch.epfl.ajul.intarray.ReadOnlyIntArray) v29;
        v03 = v29.get(v03);
        v27 = v29.immutable();
        v03 = v29.size();
        v28 = v29.toArray();
        v09 = v29.toString();
    }

    void checkImmutableIntArray() {
        v05 = (ch.epfl.ajul.intarray.AbstractIntArray) v27;
        v27 = ch.epfl.ajul.intarray.ImmutableIntArray.copyOf(v28);
        v27 = v27.immutable();
    }

    void checkMutableIntArray() {
        v05 = (ch.epfl.ajul.intarray.AbstractIntArray) v30;
        v30 = ch.epfl.ajul.intarray.MutableIntArray.wrapping(v28);
    }

    ch.epfl.ajul.TileKind v01;
    java.util.List<ch.epfl.ajul.TileKind> v02;
    int v03;
    ch.epfl.ajul.TileKind.Colored v04;
    java.lang.Object v05;
    java.util.List<ch.epfl.ajul.TileKind.Colored> v06;
    ch.epfl.ajul.TileKind.Colored[] v07;
    java.util.random.RandomGenerator v08;
    java.lang.String v09;
    ch.epfl.ajul.TileKind.FirstPlayerMarker v10;
    ch.epfl.ajul.TileKind.FirstPlayerMarker[] v11;
    java.util.List<ch.epfl.ajul.TileDestination> v12;
    ch.epfl.ajul.TileDestination v13;
    ch.epfl.ajul.TileDestination.Pattern v14;
    java.util.List<ch.epfl.ajul.TileDestination.Pattern> v15;
    ch.epfl.ajul.TileDestination.Pattern[] v16;
    ch.epfl.ajul.TileDestination.Floor v17;
    ch.epfl.ajul.TileDestination.Floor[] v18;
    java.util.List<ch.epfl.ajul.TileSource> v19;
    ch.epfl.ajul.TileSource v20;
    ch.epfl.ajul.TileSource.CenterArea v21;
    ch.epfl.ajul.TileSource.CenterArea[] v22;
    ch.epfl.ajul.TileSource.Factory v23;
    java.util.List<ch.epfl.ajul.TileSource.Factory> v24;
    ch.epfl.ajul.TileSource.Factory[] v25;
    ch.epfl.ajul.intarray.ReadOnlyIntArray v26;
    ch.epfl.ajul.intarray.ImmutableIntArray v27;
    int[] v28;
    ch.epfl.ajul.intarray.AbstractIntArray v29;
    ch.epfl.ajul.intarray.MutableIntArray v30;
}
