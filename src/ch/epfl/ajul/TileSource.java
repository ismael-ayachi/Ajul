package ch.epfl.ajul;

import java.util.List;

///
/// Source d'où proviennent les tuiles lors d'une prise (centre ou fabrique).
/// <p>
/// L'interface expose des constantes pratiques et des collections des valeurs
/// (centre + 9 fabriques).
///
/// @author Ismael Ayachi (393163)
///
public sealed interface TileSource {
    ///  La zone centrale.
    TileSource CENTER_AREA = CenterArea.CENTER_AREA;

    ///  Fabrique 1.
    TileSource FACTORY_1 = Factory.FACTORY_1;
    ///  Fabrique 2.
    TileSource FACTORY_2 = Factory.FACTORY_2;
    ///  Fabrique 3.
    TileSource FACTORY_3 = Factory.FACTORY_3;
    ///  Fabrique 4.
    TileSource FACTORY_4 = Factory.FACTORY_4;
    ///  Fabrique 5.
    TileSource FACTORY_5 = Factory.FACTORY_5;
    ///  Fabrique 6.
    TileSource FACTORY_6 = Factory.FACTORY_6;
    ///  Fabrique 7.
    TileSource FACTORY_7 = Factory.FACTORY_7;
    ///  Fabrique 8.
    TileSource FACTORY_8 = Factory.FACTORY_8;
    ///  Fabrique 9.
    TileSource FACTORY_9 = Factory.FACTORY_9;

    ///
    /// Retourne l'indice de cette source.
    ///
    /// @return l'indice de cette source
    ///
    int index();

    ///  Liste de toutes les sources (centre puis fabriques).
    List<TileSource> ALL = List.of(
            CENTER_AREA,
            FACTORY_1, FACTORY_2, FACTORY_3, FACTORY_4, FACTORY_5,
            FACTORY_6, FACTORY_7, FACTORY_8, FACTORY_9
    );

    ///  Nombre total de sources.
    int COUNT = ALL.size();

    ///
    /// La zone centrale.
    ///
    /// @author Ismael Ayachi (393163)
    ///
    enum CenterArea implements TileSource {
        ///  La zone centrale.
        CENTER_AREA;

        ///
        /// Retourne l'indice de la zone centrale (0).
        ///
        /// @return 0
        ///
        @Override
        public int index() {
            return 0;
        }
    }

    ///
    /// Les fabriques.
    ///
    /// @author Ismael Ayachi (393163)
    ///
    enum Factory implements TileSource {
        FACTORY_1, FACTORY_2, FACTORY_3, FACTORY_4, FACTORY_5, FACTORY_6, FACTORY_7, FACTORY_8, FACTORY_9;

        ///  Nombre de tuiles par fabrique.
        public static final int TILES_PER_FACTORY = 4;
        ///  Liste de toutes les fabriques.
        public static final List<Factory> ALL = List.of(values());
        ///  Nombre total de fabriques.
        public static final int COUNT = ALL.size();

        ///
        /// Retourne l'indice de cette fabrique (de 1 à 9).
        ///
        /// @return l'indice de cette fabrique
        ///
        @Override
        public int index() {
            return ordinal() + 1;
        }
    }
}
