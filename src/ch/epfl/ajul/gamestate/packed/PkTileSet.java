package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.random.RandomGenerator;

/// Classe utilitaire permettant de manipuler des ensembles de tuiles empaquetés dans des valeurs de type {@code int}.
///
/// Un ensemble de tuiles empaqueté est un entier de type {@code int} dont les bits sont organisés comme suit :
/// - bits 0 à 4 (5 bits) : nombre de tuiles de couleur A (0 à 20)
/// - bit 5 : bit de garde, vaut toujours 0
/// - bits COLOR_BITS + 1 à 10 (5 bits) : nombre de tuiles de couleur B (0 à 20)
/// - bit 11 : bit de garde, vaut toujours 0
/// - bits 12 à 1COLOR_BITS + 1 (5 bits) : nombre de tuiles de couleur C (0 à 20)
/// - bit 17 : bit de garde, vaut toujours 0
/// - bits 18 à 22 (5 bits) : nombre de tuiles de couleur D (0 à 20)
/// - bit 23 : bit de garde, vaut toujours 0
/// - bits 24 à 28 (5 bits) : nombre de tuiles de couleur E (0 à 20)
/// - bit 29 : bit de garde, vaut toujours 0
/// - bit 30 : nombre de marqueurs de premier joueur (0 ou 1)
/// - bit 31 : vaut toujours 0
///
/// Cette classe n'est pas instanciable.
///
/// @author Ismaël Ayachi (3931COLOR_BITS + 13)
public final class PkTileSet {

    private static final int COLOR_BITS = 5;
    private static final int COLOR_MASK = (1 << COLOR_BITS) - 1;

    private static final int COLOR_OFFSET_A = 0;
    private static final int COLOR_OFFSET_B = COLOR_OFFSET_A + COLOR_BITS + 1;
    private static final int COLOR_OFFSET_C = COLOR_OFFSET_B + COLOR_BITS + 1;
    private static final int COLOR_OFFSET_D = COLOR_OFFSET_C + COLOR_BITS + 1;
    private static final int COLOR_OFFSET_E = COLOR_OFFSET_D + COLOR_BITS + 1;
    private static final int COLOR_OFFSET_FIRST_PLAYER_MARKER = COLOR_OFFSET_E + COLOR_BITS + 1;

    private static final int COLOR_BITS_FIRST_PLAYER_MARKER = 1;
    private static final int COLOR_MASK_FIRST_PLAYER_MARKER = (1 << COLOR_BITS_FIRST_PLAYER_MARKER) - 1;

    /// Ensemble de tuiles vide, ne contenant aucune tuile d'aucune couleur et aucun marqueur.
    public static final int EMPTY = 0;

    /// Ensemble de tuiles plein, contenant 20 tuiles de chacune des 5 couleurs et le marqueur de premier joueur.
    public static final int FULL = computeFull() ;

    /// Ensemble de tuiles plein sans marqueur de premier joueur, contenant 20 tuiles de chacune des 5 couleurs.
    public static final int FULL_COLORED = computeFullColored();



    /// Retourne un ensemble de tuiles empaqueté ne contenant que {@code count} tuiles de la sorte {@code tileKind}.
    ///
    /// @param count
    ///        le nombre de tuiles (doit être compris entre 0 et 20 inclus)
    /// @param tileKind
    ///        la sorte de tuile
    /// @return l'ensemble empaqueté contenant uniquement {@code count} tuiles de la sorte {@code tileKind}
    public static int of(int count, TileKind tileKind) {
        return count << (tileKind.index() * (COLOR_BITS + 1));
    }

    /// Retourne vrai si et seulement si l'ensemble de tuiles empaqueté donné est vide.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @return {@code true} si l'ensemble est vide, {@code false} sinon
    public static boolean isEmpty(int pkTileSet) {
        return pkTileSet == EMPTY;
    }

    /// Retourne la taille de l'ensemble de tuiles empaqueté, c'est-à-dire le nombre total de tuiles qu'il contient.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @return le nombre total de tuiles contenues dans l'ensemble
    public static int size(int pkTileSet) {
        int extract = pkTileSet + (pkTileSet >> (COLOR_BITS + 1));
        int extractAB = extract & ((COLOR_MASK << 1) | 1);
        int extractCD = (extract >> COLOR_OFFSET_C) & ((COLOR_MASK << 1) | 1);
        int extractEM = (extract >> COLOR_OFFSET_E) & ((COLOR_MASK << 1) | 1);
        return extractAB + extractCD + extractEM ;
    }

    /// Retourne le nombre de tuiles de la sorte {@code tileKind} que contient l'ensemble empaqueté {@code pkTileSet}.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @param tileKind
    ///        la sorte de tuile dont on veut connaître le nombre
    /// @return le nombre de tuiles de la sorte donnée dans l'ensemble
    public static int countOf(int pkTileSet, TileKind tileKind) {
        return (pkTileSet >> (tileKind.index() * (COLOR_BITS + 1))) & COLOR_MASK;
    }

    /// Retourne le sous-ensemble de l'ensemble empaqueté {@code pkTileSet} constitué de toutes les tuiles
    /// de la sorte {@code tileKind} qu'il contient.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @param tileKind
    ///        la sorte de tuile à extraire
    /// @return un ensemble empaqueté ne contenant que les tuiles de la sorte donnée
    public static int subsetOf(int pkTileSet, TileKind tileKind) {
        return pkTileSet & (COLOR_MASK << (tileKind.index() * (COLOR_BITS + 1)));
    }

    /// Retourne un ensemble de tuiles empaqueté égal à {@code pkTileSet} si ce n'est qu'il contient
    /// exactement une tuile de la sorte {@code tileKind} en plus.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @param tileKind
    ///        la sorte de tuile à ajouter
    /// @return l'ensemble empaqueté avec une tuile supplémentaire de la sorte donnée
    public static int add(int pkTileSet, TileKind tileKind) {
        return pkTileSet + (1 << (tileKind.index() * (COLOR_BITS + 1)));
    }

    /// Retourne un ensemble de tuiles empaqueté égal à {@code pkTileSet} si ce n'est qu'il contient
    /// exactement une tuile de la sorte {@code tileKind} en moins.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @param tileKind
    ///        la sorte de tuile à retirer
    /// @return l'ensemble empaqueté avec une tuile en moins de la sorte donnée
    public static int remove(int pkTileSet, TileKind tileKind) {
        return pkTileSet - (1 << (tileKind.index() * (COLOR_BITS + 1)));
    }

    /// Retourne l'union empaquetée des deux ensembles de tuiles empaquetés donnés.
    ///
    /// @param pkTileSet1
    ///        le premier ensemble de tuiles empaqueté
    /// @param pkTileSet2
    ///        le second ensemble de tuiles empaqueté
    /// @return l'union des deux ensembles empaquetés
    public static int union(int pkTileSet1, int pkTileSet2) {
        return pkTileSet1 + pkTileSet2;
    }

    /// Retourne la différence empaquetée des deux ensembles de tuiles empaquetés donnés.
    /// {@code pkTileSet2} doit être un sous-ensemble de {@code pkTileSet1}.
    ///
    /// @param pkTileSet1
    ///        l'ensemble de tuiles empaqueté dont on soustrait
    /// @param pkTileSet2
    ///        l'ensemble de tuiles empaqueté à soustraire (doit être un sous-ensemble de {@code pkTileSet1})
    /// @return la différence des deux ensembles empaquetés
    public static int difference(int pkTileSet1, int pkTileSet2) {
        int difference = pkTileSet1 - pkTileSet2;
        assert isValid(difference);
        return difference;
    }

    /// Copie dans le tableau {@code destination} les tuiles colorées de l'ensemble empaqueté {@code pkTileSet},
    /// ordonnées par couleur. Retourne l'index dans {@code destination} de l'élément qui suit le dernier écrit.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté dont on copie les tuiles
    /// @param destination
    ///        le tableau dans lequel les tuiles sont copiées
    /// @return l'index dans {@code destination} de l'élément qui suit le dernier écrit
    public static int copyColoredInto(int pkTileSet, TileKind.Colored[] destination) {
        int count = 0;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int numberOfTiles = countOf(pkTileSet, tileKind);
            Arrays.fill(destination, count, count + numberOfTiles, tileKind);
            count += numberOfTiles;
        }
        return count;
    }

    /// Obtient un échantillon aléatoire de l'ensemble {@code pkTileSet} et le place dans le tableau
    /// {@code destination} à partir de l'index {@code offset}, en utilisant l'algorithme
    /// d'échantillonnage par réservoir. Retourne la somme de {@code offset} et de la taille de l'ensemble.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté depuis lequel on échantillonne
    /// @param destination
    ///        le tableau dans lequel la tuile échantillonnée est placée
    /// @param offset
    ///        l'index dans {@code destination} à partir duquel on écrit
    /// @param randomGenerator
    ///        le générateur de nombres aléatoires utilisé pour l'échantillonnage
    /// @return la somme de {@code offset} et de la taille de l'ensemble
    public static int sampleColoredInto(int pkTileSet, TileKind.Colored[] destination, int offset, RandomGenerator randomGenerator) {
        int i = offset;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int numberOfTiles = countOf(pkTileSet, tileKind);
            for (int y = 0; y < numberOfTiles; y++) {
                int slot = i - offset;
                if (slot < destination.length - offset) {
                    destination[offset + slot] = tileKind;
                }
                else {
                    int j = randomGenerator.nextInt(0, i - offset + 1);
                    if (j < destination.length - offset) {
                        destination[offset + j] = tileKind;
                    }
                }
                i++;
            }
        }
        return i;
    }

    /// Retourne la représentation textuelle de l'ensemble de tuiles empaqueté donné.
    /// La représentation est composée des éléments dont le compteur est strictement positif,
    /// chacun sous la forme {@code n*COULEUR}, séparés par des virgules et entourés d'accolades.
    /// Par exemple : {@code {20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}}.
    ///
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté
    /// @return la représentation textuelle de l'ensemble
    public static String toString(int pkTileSet) {
        StringJoiner j = new StringJoiner(",", "{", "}");
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int numberOfTiles = countOf(pkTileSet, tileKind);
            if (numberOfTiles > 0) {
                j.add(numberOfTiles + "*" + tileKind.name());
            }
        }
        int numberOfTileM = countOf(pkTileSet, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER);
        if (numberOfTileM > 0) {
            j.add(numberOfTileM + "*" + TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER.name());
        }
        return j.toString();
    }

    private static int computeFull() {
        int pkTileSetFull = PkTileSet.EMPTY;
        for (TileKind tileKind : TileKind.ALL) {
            pkTileSetFull = union(pkTileSetFull, of(tileKind.tilesCount(), tileKind));
        }
        return pkTileSetFull;
    }

    private static int computeFullColored() {

        int pkTileSetFullColored = PkTileSet.EMPTY;
        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            pkTileSetFullColored = union(pkTileSetFullColored, of(tileKind.tilesCount(), tileKind));
        }
        return pkTileSetFullColored;
    }

    private static boolean isValid(int pkTileSet) {

        for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
            int extract = (pkTileSet >> (tileKind.index() * (COLOR_BITS + 1))) & ((COLOR_MASK << 1) | 1);
            if (extract > tileKind.tilesCount()) {
                return false;
            }
        }
        int extractFirstPlayerMarker =
                (pkTileSet >> COLOR_OFFSET_FIRST_PLAYER_MARKER) & ((COLOR_MASK_FIRST_PLAYER_MARKER << 1) | 1);
        return extractFirstPlayerMarker <= 1;

    }
}