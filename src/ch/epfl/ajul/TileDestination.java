package ch.epfl.ajul;

import java.util.List;

///
/// Destination d'une tuile, c'est-à-dire l'endroit où elle peut être placée.
/// <p>
/// Les destinations possibles sont les cinq lignes de motif (patterns) et le
/// plancher (floor). L'interface fournit également des constantes pratiques et
/// des collections des valeurs.
///
/// @author Ismaël Ayachi (393163)
///
public sealed interface TileDestination {
    ///  Destination correspondant à la première ligne de motif.
    TileDestination PATTERN_1 = Pattern.PATTERN_1;
    ///  Destination correspondant à la deuxième ligne de motif.
    TileDestination PATTERN_2 = Pattern.PATTERN_2;
    ///  Destination correspondant à la troisième ligne de motif.
    TileDestination PATTERN_3 = Pattern.PATTERN_3;
    ///  Destination correspondant à la quatrième ligne de motif.
    TileDestination PATTERN_4 = Pattern.PATTERN_4;
    ///  Destination correspondant à la cinquième ligne de motif.
    TileDestination PATTERN_5 = Pattern.PATTERN_5;
    ///  Destination correspondant au plancher.
    TileDestination FLOOR = Floor.FLOOR;

    ///  Liste de toutes les destinations possibles.
    List<TileDestination> ALL = List.of(PATTERN_1, PATTERN_2, PATTERN_3, PATTERN_4, PATTERN_5, FLOOR);
    ///  Nombre total de destinations.
    int COUNT = ALL.size();

    ///
    /// Retourne l'indice de cette destination.
    ///
    /// @return l'indice de cette destination
    ///
    int index();

    ///
    /// Retourne la capacité (nombre maximal de tuiles) de cette destination.
    ///
    /// @return la capacité de cette destination
    ///
    int capacity();

    ///
    /// Destination correspondant à une ligne de motif (pattern).
    ///
    /// @author Ismaël Ayachi (393163)
    ///
    enum Pattern implements TileDestination {
        ///  Première ligne de motif.
        PATTERN_1,
        ///  Deuxième ligne de motif.
        PATTERN_2,
        ///  Troisième ligne de motif.
        PATTERN_3,
        ///  Quatrième ligne de motif.
        PATTERN_4,
        ///  Cinquième ligne de motif.
        PATTERN_5;

        ///  Liste de toutes les lignes de motif.
        public static final List<Pattern> ALL = List.of(values());
        ///  Nombre total de lignes de motif.
        public static final int COUNT = ALL.size();

        ///
        /// Retourne l'indice de cette ligne (de 0 à 4).
        ///
        /// @return l'indice de cette ligne
        ///
        @Override
        public int index() {
            return ordinal();
        }

        ///
        /// Retourne la capacité de cette ligne, égale à {@code index() + 1}.
        ///
        /// @return la capacité de cette ligne
        ///
        @Override
        public int capacity() {
            return ordinal() + 1;
        }
    }

    ///
    /// Destination correspondant au plancher (floor).
    ///
    /// @author Ismaël Ayachi (393163)
    ///
    enum Floor implements TileDestination {
        ///  Le plancher.
        FLOOR;

        ///
        /// Retourne l'indice du plancher.
        ///
        /// @return l'indice du plancher
        ///
        @Override
        public int index() {
            return ordinal() + 5;
        }

        ///
        /// Retourne la capacité du plancher.
        ///
        /// @return la capacité du plancher
        ///
        @Override
        public int capacity() {
            return 7;
        }
    }
}
