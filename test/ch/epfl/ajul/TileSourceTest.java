package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.ajul.TileSource.CenterArea;
import static ch.epfl.ajul.TileSource.Factory;
import static org.junit.jupiter.api.Assertions.*;

class TileSourceTest {
    @Test
    void tileSourceFieldsAreEnumConstants() {
        assertSame(TileSource.CENTER_AREA, CenterArea.CENTER_AREA);
        assertSame(TileSource.FACTORY_1, Factory.FACTORY_1);
        assertSame(TileSource.FACTORY_2, Factory.FACTORY_2);
        assertSame(TileSource.FACTORY_3, Factory.FACTORY_3);
        assertSame(TileSource.FACTORY_4, Factory.FACTORY_4);
        assertSame(TileSource.FACTORY_5, Factory.FACTORY_5);
        assertSame(TileSource.FACTORY_6, Factory.FACTORY_6);
        assertSame(TileSource.FACTORY_7, Factory.FACTORY_7);
        assertSame(TileSource.FACTORY_8, Factory.FACTORY_8);
        assertSame(TileSource.FACTORY_9, Factory.FACTORY_9);
    }

    @Test
    void tileSourceAllIsCorrectlyDefined() {
        var expected = List.of(
                TileSource.CENTER_AREA,
                TileSource.FACTORY_1,
                TileSource.FACTORY_2,
                TileSource.FACTORY_3,
                TileSource.FACTORY_4,
                TileSource.FACTORY_5,
                TileSource.FACTORY_6,
                TileSource.FACTORY_7,
                TileSource.FACTORY_8,
                TileSource.FACTORY_9);
        var actual = TileSource.ALL;
        assertEquals(expected, actual);
    }

    @Test
    void tileSourceAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            TileSource.ALL.clear();
        });
    }

    @Test
    void tileSourceCountIsCorrectlyDefined() {
        assertEquals(10, TileSource.COUNT);
    }

    @Test
    void tileSourceIndexAndAllGetAreInverse() {
        for (var i = 0; i < TileSource.COUNT; i += 1)
            assertEquals(i, TileSource.ALL.get(i).index());
    }

    @Test
    void factoryAllIsCorrectlyDefined() {
        assertEquals(List.of(Factory.FACTORY_1,
                        Factory.FACTORY_2,
                        Factory.FACTORY_3,
                        Factory.FACTORY_4,
                        Factory.FACTORY_5,
                        Factory.FACTORY_6,
                        Factory.FACTORY_7,
                        Factory.FACTORY_8,
                        Factory.FACTORY_9),
                Factory.ALL);
    }

    @Test
    void factoryAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            Factory.ALL.clear();
        });
    }

    @Test
    void factoryCountIsCorrectlyDefined() {
        assertEquals(9, Factory.COUNT);
    }

}