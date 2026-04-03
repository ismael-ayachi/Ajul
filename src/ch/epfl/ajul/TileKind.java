package ch.epfl.ajul;

import java.util.List;
import java.util.random.RandomGenerator;

///
/// Type de tuile (couleur ou marqueur de premier joueur).
/// <p>
/// L'interface expose des constantes pratiques, ainsi que des méthodes
/// permettant d'obtenir l'indice du type et le nombre de tuiles disponibles.
///
/// @author Ismaël Ayachi (393163)
///
public sealed interface TileKind {
    ///  Tuile de type A.
    TileKind A = Colored.A;
    ///  Tuile de type B.
    TileKind B = Colored.B;
    ///  Tuile de type C.
    TileKind C = Colored.C;
    ///  Tuile de type D.
    TileKind D = Colored.D;
    ///  Tuile de type E.
    TileKind E = Colored.E;
    ///  Marqueur de premier joueur.
    TileKind FIRST_PLAYER_MARKER = FirstPlayerMarker.FIRST_PLAYER_MARKER;

    ///  Liste de tous les types de tuiles.
    List<TileKind> ALL = List.of(A, B, C, D, E, FIRST_PLAYER_MARKER);
    ///  Nombre total de types de tuiles.
    int COUNT = ALL.size();

    ///
    /// Retourne l'indice de ce type de tuile.
    ///
    /// @return l'indice de ce type
    ///
    int index();

    ///
    /// Retourne le nombre de tuiles de ce type dans le jeu.
    ///
    /// @return le nombre de tuiles de ce type
    ///
    int tilesCount();

    ///
    /// Types de tuiles colorées.
    ///
    /// @author Ismaël Ayachi (393163)
    ///
    enum Colored implements TileKind {
        A, B, C, D, E;

        ///  Liste de toutes les couleurs.
        public static final List<Colored> ALL = List.of(values());
        ///  Nombre total de couleurs.
        public static final int COUNT = ALL.size();

        ///
        /// Retourne l'indice de cette couleur (de 0 à 4).
        ///
        /// @return l'indice de cette couleur
        ///
        @Override
        public int index() {
            return ordinal();
        }

        ///
        /// Retourne le nombre de tuiles de cette couleur dans le jeu.
        ///
        /// @return 20
        ///
        @Override
        public int tilesCount() {
            return 20;
        }

        ///
        /// Mélange en place le tableau de tuiles colorées donné en utilisant le
        /// générateur aléatoire fourni (algorithme de Fisher-Yates).
        ///
        /// @param tiles le tableau à mélanger
        /// @param randomGenerator le générateur aléatoire à utiliser
        ///
        public static void shuffle(Colored[] tiles, RandomGenerator randomGenerator) {
            for (int i = 0; i <= tiles.length - 2; i++) {
                int j = randomGenerator.nextInt(i, tiles.length);
                Colored tmp = tiles[i];
                tiles[i] = tiles[j];
                tiles[j] = tmp;
            }
        }
    }

    ///
    /// Marqueur de premier joueur.
    ///
    /// @author Ismaël Ayachi (393163)
    ///
    enum FirstPlayerMarker implements TileKind {
        ///  Le marqueur de premier joueur.
        FIRST_PLAYER_MARKER;

        ///
        /// Retourne l'indice du marqueur.
        ///
        /// @return l'indice du marqueur
        ///
        @Override
        public int index() {
            return ordinal() + 5;
        }

        ///
        /// Retourne le nombre de marqueurs dans le jeu.
        ///
        /// @return 1
        ///
        @Override
        public int tilesCount() {
            return 1;
        }
    }
}
