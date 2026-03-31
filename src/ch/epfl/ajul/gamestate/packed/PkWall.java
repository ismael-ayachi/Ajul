package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

import java.util.ArrayList;
import java.util.List;

/// Représentation compacte du mur d'un joueur, empaqueté dans un {@code int}
/// interprété comme un {@link PkIntSet32} : le bit {@code i} vaut 1 si et
/// seulement si la case d'index {@code i} du mur contient une tuile.
/// Les cases sont numérotées de gauche à droite et de haut en bas (de 0 à 24).
/// Les bits 25 à 31 valent toujours 0.
///
/// @author Ismaël Ayachi (393163)
public final class PkWall {

    /// Le mur vide, dont la valeur empaquetée est 0.
    public static final int EMPTY = PkIntSet32.EMPTY;

    /// Le nombre de colonnes du mur, égal au nombre de couleurs de tuiles.
    public static final int WALL_WIDTH = TileKind.Colored.COUNT;

    /// Le nombre de lignes du mur, égal au nombre de lignes de motif.
    public static final int WALL_HEIGHT = TileDestination.Pattern.COUNT;

    private static final int COLUMN_MASK = 0b1;
    private static final int ROW0_MASK = 0b00000_00000_00000_00000_11111;
    private static final int COLOR_A_MASK = 0b10000_01000_00100_00010_00001;
    private static final int COLOR_B_MASK = 0b00001_10000_01000_00100_00010;
    private static final int COLOR_C_MASK = 0b00010_00001_10000_01000_00100;
    private static final int COLOR_D_MASK = 0b00100_00010_00001_10000_01000;
    private static final int COLOR_E_MASK = 0b01000_00100_00010_00001_10000;
    private static final ArrayList<Integer> COLOR_MASK_LIST = new ArrayList<>(
            List.of(COLOR_A_MASK, COLOR_B_MASK, COLOR_C_MASK, COLOR_D_MASK, COLOR_E_MASK));

    /// Retourne l'index (entre 0 et 24 inclus) de la case du mur correspondant
    /// à la ligne {@code line} et à la couleur {@code color}.
    ///
    /// @param line  la ligne de motif
    /// @param color la couleur de la tuile
    /// @return l'index de la case dans le mur empaqueté
    public static int indexOf(TileDestination.Pattern line, TileKind.Colored color) {
        return (line.index() * WALL_WIDTH) + column(line, color);
    }

    /// Retourne la colonne (entre 0 et 4 inclus) à laquelle se trouve la couleur
    /// {@code color} sur la ligne {@code line}.
    /// Les couleurs tournent d'une ligne à l'autre selon la règle du jeu.
    ///
    /// @param line  la ligne de motif
    /// @param color la couleur de la tuile
    /// @return la colonne de cette couleur sur cette ligne
    public static int column(TileDestination.Pattern line, TileKind.Colored color) {
        return (color.index() + line.index()) % WALL_WIDTH;
    }

    /// Retourne la couleur de la tuile à placer à la colonne {@code column}
    /// sur la ligne {@code line}.
    ///
    /// @param line   la ligne de motif
    /// @param column la colonne (entre 0 et 4 inclus)
    /// @return la couleur attendue à cette position
    public static TileKind.Colored colorAt(TileDestination.Pattern line, int column) {
        return TileKind.Colored.ALL.get((line.index() * (WALL_WIDTH - 1) + column) % WALL_WIDTH);
    }

    /// Retourne le mur empaqueté obtenu en ajoutant une tuile de couleur {@code color}
    /// sur la ligne {@code line} du mur {@code pkWall}.
    ///
    /// @param pkWall le mur empaqueté
    /// @param line   la ligne de motif cible
    /// @param color  la couleur de la tuile à placer
    /// @return le mur empaqueté avec la tuile ajoutée
    public static int withTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert !hasTileAt(pkWall, line, color) && isPkWallValid(pkWall);
        return PkIntSet32.add(pkWall, indexOf(line, color));
    }

    /// Retourne {@code true} si le mur empaqueté {@code pkWall} contient une tuile
    /// de couleur {@code color} sur la ligne {@code line}.
    ///
    /// @param pkWall le mur empaqueté
    /// @param line   la ligne de motif
    /// @param color  la couleur de la tuile
    /// @return {@code true} si la case est occupée
    public static boolean hasTileAt(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert isPkWallValid(pkWall);
        return PkIntSet32.contains(pkWall, indexOf(line, color));
    }

    /// Retourne la taille du groupe horizontal de tuiles consécutives contenant
    /// la tuile de couleur {@code color} sur la ligne {@code line} du mur {@code pkWall}.
    /// La tuile elle-même est incluse dans le groupe.
    ///
    /// @param pkWall le mur empaqueté
    /// @param line   la ligne de motif
    /// @param color  la couleur de la tuile de référence
    /// @return le nombre de tuiles consécutives horizontalement autour de cette tuile
    public static int hGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert isPkWallValid(pkWall);
        int groupSize = 1;
        int indexRight = 1;
        int indexLeft = 1;
        int col = column(line, color);

        while ((col + indexRight < WALL_WIDTH) && hasTileAt(pkWall, line, colorAt(line, col + indexRight))) {
            groupSize++;
            indexRight++;
        }
        while ((col - indexLeft >= 0) && hasTileAt(pkWall, line, colorAt(line, col - indexLeft))) {
            groupSize++;
            indexLeft++;
        }
        return groupSize;
    }

    /// Retourne la taille du groupe vertical de tuiles consécutives contenant
    /// la tuile de couleur {@code color} sur la ligne {@code line} du mur {@code pkWall}.
    /// La tuile elle-même est incluse dans le groupe.
    ///
    /// @param pkWall le mur empaqueté
    /// @param line   la ligne de motif
    /// @param color  la couleur de la tuile de référence
    /// @return le nombre de tuiles consécutives verticalement autour de cette tuile
    public static int vGroupSize(int pkWall, TileDestination.Pattern line, TileKind.Colored color) {
        assert isPkWallValid(pkWall);
        int groupSize = 1;
        int indexUp = 1;
        int indexDown = 1;
        int col = column(line, color);

        while ((line.index() + indexUp < WALL_HEIGHT) &&
                hasTileAt(pkWall, TileDestination.Pattern.ALL.get(line.index() + indexUp),
                        colorAt(TileDestination.Pattern.ALL.get(line.index() + indexUp), col))) {
            groupSize++;
            indexUp++;
        }

        while ((line.index() - indexDown >= 0) &&
                hasTileAt(pkWall, TileDestination.Pattern.ALL.get(line.index() - indexDown),
                        colorAt(TileDestination.Pattern.ALL.get(line.index() - indexDown), col))) {
            groupSize++;
            indexDown++;
        }

        return groupSize;
    }

    /// Retourne {@code true} si au moins une ligne du mur empaqueté {@code pkWall}
    /// est complète, c'est-à-dire contient une tuile dans chacune de ses 5 cases.
    ///
    /// @param pkWall le mur empaqueté
    /// @return {@code true} si au moins une ligne est complète
    public static boolean hasFullRow(int pkWall) {
        assert isPkWallValid(pkWall);
        boolean fullRow = false;
        for (int i = 0; i < WALL_HEIGHT; i++) {
            fullRow = fullRow || (((pkWall >> (WALL_WIDTH * i)) & ROW0_MASK) == ROW0_MASK);
        }
        return fullRow;
    }

    /// Retourne {@code true} si la ligne {@code line} du mur empaqueté {@code pkWall}
    /// est complète.
    ///
    /// @param pkWall le mur empaqueté
    /// @param line   la ligne de motif à tester
    /// @return {@code true} si la ligne est complète
    public static boolean isRowFull(int pkWall, TileDestination.Pattern line) {
        assert isPkWallValid(pkWall);
        return ((pkWall >> (WALL_WIDTH * line.index())) & ROW0_MASK) == ROW0_MASK;
    }

    /// Retourne {@code true} si la colonne {@code column} du mur empaqueté {@code pkWall}
    /// est complète, c'est-à-dire contient une tuile sur chacune des 5 lignes.
    ///
    /// @param pkWall le mur empaqueté
    /// @param column la colonne à tester (entre 0 et 4 inclus)
    /// @return {@code true} si la colonne est complète
    public static boolean isColumnFull(int pkWall, int column) {
        assert isPkWallValid(pkWall);
        return columnSize(pkWall, column) == WALL_HEIGHT;
    }

    /// Retourne le nombre de tuiles présentes dans la colonne {@code column}
    /// du mur empaqueté {@code pkWall}.
    ///
    /// @param pkWall le mur empaqueté
    /// @param column la colonne (entre 0 et 4 inclus)
    /// @return le nombre de cases occupées dans cette colonne
    private static int columnSize(int pkWall, int column) {
        assert isPkWallValid(pkWall);
        int size = 0;
        for (int i = 0; i < WALL_HEIGHT; i++) {
            size += ((pkWall >> (column + (i * WALL_WIDTH))) & COLUMN_MASK);
        }
        return size;
    }

    /// Retourne {@code true} si la couleur {@code color} est complète dans le mur
    /// empaqueté {@code pkWall}, c'est-à-dire si une tuile de cette couleur est
    /// présente sur chacune des 5 lignes.
    ///
    /// @param pkWall le mur empaqueté
    /// @param color  la couleur à tester
    /// @return {@code true} si la couleur est complète
    public static boolean isColorFull(int pkWall, TileKind.Colored color) {
        assert isPkWallValid(pkWall);
        return PkIntSet32.containsAll(pkWall, COLOR_MASK_LIST.get(color.index()));
    }

    /// Retourne le {@link PkTileSet} correspondant à l'ensemble des tuiles
    /// présentes dans le mur empaqueté {@code pkWall}.
    ///
    /// @param pkWall le mur empaqueté
    /// @return l'ensemble des tuiles du mur sous forme de {@code PkTileSet}
    public static int asPkTileSet(int pkWall) {
        assert isPkWallValid(pkWall);
        int newPkTileSet = PkTileSet.EMPTY;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int tileKindCount = 0;
            for (int i = 0; i < WALL_WIDTH; i++) {
                int rowMask = ROW0_MASK << (WALL_WIDTH * i);
                int colorMask = COLOR_MASK_LIST.get(tileKind.index());
                tileKindCount += Integer.bitCount((pkWall & rowMask) & colorMask);
            }
            newPkTileSet = PkTileSet.union(newPkTileSet, PkTileSet.of(tileKindCount, tileKind));
        }
        return newPkTileSet;
    }

    /// Retourne une représentation textuelle du mur empaqueté {@code pkWall}.
    /// Chaque ligne est représentée par une séquence de 5 caractères : une lettre
    /// majuscule si la case est occupée, minuscule sinon. Les lignes sont séparées
    /// par {@code ", "} et l'ensemble est encadré par des crochets.
    ///
    /// @param pkWall le mur empaqueté
    /// @return la représentation textuelle du mur
    public static String toString(int pkWall) {
        StringBuilder b = new StringBuilder().append("[");
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            for (int i = 0; i < WALL_HEIGHT; i++) {
                TileKind.Colored color = colorAt(line, i);
                if (hasTileAt(pkWall, line, color)) {
                    b.append(color.toString().toUpperCase());
                } else {
                    b.append(color.toString().toLowerCase());
                }
            }
            if (line != TileDestination.Pattern.PATTERN_5) {
                b.append(", ");
            }
        }
        return b.append("]").toString();
    }

    /// Retourne {@code true} si la valeur empaquetée {@code pkWall} est valide,
    /// c'est-à-dire si les bits 25 à 31 sont tous nuls.
    ///
    /// @param pkWall la valeur à vérifier
    /// @return {@code true} si {@code pkWall} est un mur empaqueté valide
    private static boolean isPkWallValid(int pkWall) {
        return (pkWall >> (WALL_HEIGHT * WALL_HEIGHT)) == EMPTY;
    }
}