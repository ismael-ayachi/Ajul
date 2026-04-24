package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;

/// Classe utilitaire permettant de manipuler le contenu des lignes de motif d'un joueur,
/// empaqueté dans une valeur de type {@code int}.
///
/// Les lignes de motif empaquetées sont un entier de type {@code int} dont les bits sont organisés
/// comme suit, pour chacune des 5 lignes de motif :
/// - 3 bits contenant le nombre de tuiles présentes sur la ligne (0 à capacité de la ligne)
/// - 3 bits contenant l'index de la couleur des tuiles, ou 0 si la ligne est vide
/// Les 2 bits de poids fort valent toujours 0.
///
/// @author Ismaël Ayachi (393163)
public final class PkPatterns {

    /// Lignes de motif vides, ne contenant aucune tuile sur aucune ligne.
    public static final int EMPTY = 0;
    private static final int PATTERN_LINE_MASK = 0b111;
    private static final int PATTERN_MASK = (PATTERN_LINE_MASK << 3) | PATTERN_LINE_MASK;
    private static final int PATTERN_LINE_OFFSET = TileDestination.Pattern.COUNT + 1 ;
    private static final int PATTERN_COLOR_OFFSET = PATTERN_LINE_OFFSET / 2;

    /// Retourne le nombre de tuiles présentes sur la ligne de motif {@code line}
    /// des lignes de motif empaquetées {@code pkPatterns}.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif dont on veut connaître le nombre de tuiles
    /// @return le nombre de tuiles sur la ligne donnée
    public static int size(int pkPatterns, TileDestination.Pattern line) {
        return (pkPatterns >> (line.index() * PATTERN_LINE_OFFSET)) & PATTERN_LINE_MASK;
    }

    /// Retourne la couleur des tuiles présentes sur la ligne de motif {@code line}
    /// des lignes de motif empaquetées {@code pkPatterns}.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif dont on veut connaître la couleur (ne doit pas être vide)
    /// @return la couleur des tuiles sur la ligne donnée
    public static TileKind.Colored color(int pkPatterns, TileDestination.Pattern line) {
        assert isSizeValid(pkPatterns, line);
        int rawColorBits = (pkPatterns >> (PATTERN_COLOR_OFFSET + line.index() * PATTERN_LINE_OFFSET));
        return TileKind.Colored.ALL.get(rawColorBits & PATTERN_LINE_MASK);
    }

    /// Retourne vrai si et seulement si la ligne de motif {@code line} des lignes de motif
    /// empaquetées {@code pkPatterns} est pleine, c'est-à-dire contient le nombre maximum
    /// de tuiles qu'elle peut contenir.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif à vérifier
    /// @return {@code true} si la ligne est pleine, {@code false} sinon
    public static boolean isFull(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) == line.capacity();
    }

    /// Retourne vrai si et seulement si la ligne de motif {@code line} des lignes de motif
    /// empaquetées {@code pkPatterns} peut contenir des tuiles de couleur {@code color},
    /// c'est-à-dire si elle est vide ou si elle contient déjà des tuiles de cette couleur,
    /// indépendamment du fait qu'elle soit pleine ou non.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif à vérifier
    /// @param color la couleur des tuiles à ajouter
    /// @return {@code true} si la ligne peut contenir des tuiles de la couleur donnée, {@code false} sinon
    public static boolean canContain(int pkPatterns, TileDestination.Pattern line, TileKind.Colored color) {
        return (size(pkPatterns, line) == EMPTY) || ((color == color(pkPatterns, line)));
    }

    /// Retourne des lignes de motif empaquetées identiques à {@code pkPatterns} mais avec
    /// {@code tileCount} tuiles de couleur {@code color} ajoutées à la ligne {@code line}.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif à laquelle ajouter les tuiles (doit pouvoir contenir la couleur donnée
    ///             et avoir suffisamment de place)
    /// @param tileCount le nombre de tuiles à ajouter
    /// @param color la couleur des tuiles à ajouter
    /// @return les lignes de motif empaquetées avec les tuiles ajoutées
    public static int withAddedTiles(int pkPatterns, TileDestination.Pattern line, int tileCount, TileKind.Colored color) {
        if (tileCount == 0) return pkPatterns;
        assert isTileValid(pkPatterns, line, tileCount, color);
        if (size(pkPatterns, line) == EMPTY) {
            return pkPatterns
                    + (tileCount << (line.index() * PATTERN_LINE_OFFSET))
                    + (color.index() << (PATTERN_COLOR_OFFSET + line.index() * PATTERN_LINE_OFFSET));
        }
        else if (canContain(pkPatterns, line, color)) {
            return pkPatterns + (tileCount << (line.index() * PATTERN_LINE_OFFSET));
        }
        return pkPatterns;
    }

    /// Retourne des lignes de motif empaquetées identiques à {@code pkPatterns} mais avec
    /// la ligne {@code line} vidée.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @param line la ligne de motif à vider
    /// @return les lignes de motif empaquetées avec la ligne donnée vide
    public static int withEmptyLine(int pkPatterns, TileDestination.Pattern line) {
        return pkPatterns & ~(PATTERN_MASK << (line.index() * PATTERN_LINE_OFFSET));
    }

    /// Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles se trouvant
    /// sur les lignes de motif empaquetées {@code pkPatterns}.
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @return l'ensemble de tuiles empaqueté correspondant au contenu de toutes les lignes
    public static int asPkTileSet(int pkPatterns) {
        int pkTileSet = PkTileSet.EMPTY;
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            if (size(pkPatterns, line) > 0) {
                TileKind.Colored lineColor = color(pkPatterns, line);
                int numberOfColor = size(pkPatterns, line);
                pkTileSet = PkTileSet.union(pkTileSet,PkTileSet.of(numberOfColor, lineColor));
            }
        }
        return pkTileSet;
    }

    /// Retourne la représentation textuelle des lignes de motif empaquetées {@code pkPatterns}.
    /// La représentation est constituée de cinq éléments séparés par des virgules suivies d'espaces,
    /// entourés de crochets. Chaque élément commence par la lettre de la couleur répétée autant de fois
    /// qu'il y a de tuiles, suivie de points jusqu'à atteindre la capacité de la ligne.
    /// Par exemple : {@code [C, AA, AAA, EEE., .....]}
    ///
    /// @param pkPatterns les lignes de motif empaquetées
    /// @return la représentation textuelle des lignes de motif
    public static String toString(int pkPatterns) {
        StringBuilder sb = new StringBuilder("[");
        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
            if (sb.length() > 1) sb.append(", ");
            int repeatPatternLine = size(pkPatterns, line);
            if (repeatPatternLine == EMPTY)
                sb.append(".".repeat(line.capacity()));
             else {
                TileKind.Colored pkPatternsColorLine = color(pkPatterns, line);
                sb.append(pkPatternsColorLine.toString().repeat(repeatPatternLine));
                if (repeatPatternLine < line.capacity())
                    sb.append(".".repeat(line.capacity() - repeatPatternLine));
            }
        }
        return sb.append("]").toString();
    }

    private static boolean isSizeValid(int pkPatterns, TileDestination.Pattern line) {
        return size(pkPatterns, line) > 0;
    }

    private static boolean isTileValid(int pkPatterns, TileDestination.Pattern line, int tileCount, TileKind.Colored color) {
        boolean tilesCountValid = tileCount <= (line.capacity() - size(pkPatterns, line));
        return canContain(pkPatterns, line, color) && tilesCountValid;
    }
}