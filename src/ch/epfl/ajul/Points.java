package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.packed.PkWall;

/// Classe utilitaire regroupant les calculs de points du jeu Azul.
/// Fournit des méthodes pour calculer les points obtenus lors du placement
/// d'une tuile sur le mur, les pénalités de la ligne de plancher,
/// ainsi que les bonus de fin de partie.
///
/// @author Ismaël Ayachi (393163)
public final class Points {

    /// Points bonus pour chaque ligne complète en fin de partie.
    public static final int FULL_ROW_BONUS_POINTS = 2;

    /// Points bonus pour chaque colonne complète en fin de partie.
    public static final int FULL_COLUMN_BONUS_POINTS = 7;

    /// Points bonus pour chaque couleur complète (5 tuiles) en fin de partie.
    public static final int FULL_COLOR_BONUS_POINTS = 10;

    private static final int FLOOR_TILES = 7;
    private static final int FLOOR_PENALTY_BITS = 4;
    private static final int FLOOR_PENALTY = 0x3322211;
    private static final int FLOOR_PENALTY_MASK = 0b1111;
    private static final int TOTAL_FLOOR_PENALTY = (FLOOR_PENALTY * 0x1111111) << 4;

    /// Retourne le nombre de points obtenus lors du placement d'une tuile sur le mur,
    /// en fonction de la taille du groupe horizontal {@code hGroupSize} et de la taille
    /// du groupe vertical {@code vGroupSize} auxquels elle appartient.
    /// Si l'un des groupes est de taille 1, seule la taille de l'autre groupe est comptée.
    /// Sinon, la somme des deux tailles est retournée.
    ///
    /// @param hGroupSize la taille du groupe horizontal (entre 1 et {@link PkWall#WALL_WIDTH})
    /// @param vGroupSize la taille du groupe vertical (entre 1 et {@link PkWall#WALL_HEIGHT})
    /// @return le nombre de points obtenus pour ce placement
    public static int newWallTilePoints(int hGroupSize, int vGroupSize) {
        assert (hGroupSize > 0 && hGroupSize <= PkWall.WALL_WIDTH)
                && (vGroupSize > 0 && vGroupSize <= PkWall.WALL_HEIGHT);
        if (vGroupSize == 1) {
            return hGroupSize;
        }
        else if (hGroupSize == 1) {
            return vGroupSize;
        }
        return hGroupSize + vGroupSize;
    }

    /// Retourne la pénalité associée à la tuile se trouvant à l'index {@code tileIndex}
    /// sur la ligne de plancher (entre 0 et 6 inclus).
    /// Les pénalités sont : 1, 1, 2, 2, 2, 3, 3.
    ///
    /// @param tileIndex l'index de la tuile sur le plancher (entre 0 et 6 inclus)
    /// @return la pénalité de cette tuile
    public static int floorPenalty(int tileIndex) {
        assert (tileIndex >= 0 && tileIndex < FLOOR_TILES);
        return (FLOOR_PENALTY >> (FLOOR_PENALTY_BITS * tileIndex)) & FLOOR_PENALTY_MASK;
    }

    /// Retourne la pénalité totale pour {@code tilesCount} tuiles sur le plancher
    /// (entre 0 et 7 inclus). La pénalité totale est la somme des pénalités
    /// individuelles des {@code tilesCount} premières cases.
    ///
    /// @param tilesCount le nombre de tuiles sur le plancher (entre 0 et 7 inclus)
    /// @return la pénalité totale
    public static int totalFloorPenalty(int tilesCount) {
        assert tilesCount >= 0 && tilesCount <= FLOOR_TILES;
        return (TOTAL_FLOOR_PENALTY >> (FLOOR_PENALTY_BITS * tilesCount)) & FLOOR_PENALTY_MASK;
    }
}