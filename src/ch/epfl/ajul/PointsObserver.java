package ch.epfl.ajul;

/// Interface représentant un observateur de points, c'est-à-dire un objet informé
/// chaque fois que les points d'un joueur changent au cours d'une partie d'Ajul.
/// Toutes les méthodes sont des méthodes par défaut dont le corps est vide, ce qui
/// permet à un implémenteur de ne redéfinir que les méthodes qui l'intéressent.
///
/// @author Ismaël Ayachi (393163)
public interface PointsObserver {

    /// Un observateur de points vide dont aucune méthode ne fait quoi que ce soit.
    /// Principalement destiné à l'intelligence artificielle, qui n'a pas besoin de
    /// connaître le détail des gains et pertes de points.
    PointsObserver EMPTY = new PointsObserver() {};

    /// Appelée lorsque le joueur {@code playerId} a placé une tuile de couleur
    /// {@code color} sur la ligne {@code line} de son mur et remporté {@code points} points.
    ///
    /// @param playerId l'identité du joueur
    /// @param line     la ligne du mur sur laquelle la tuile a été placée
    /// @param color    la couleur de la tuile placée
    /// @param points   le nombre de points remportés
    default void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                             TileKind.Colored color, int points) {}

    /// Appelée à la fin d'une manche lorsque le joueur {@code playerId} a perdu
    /// {@code penalty} points en raison de la présence de tuiles sur sa ligne plancher.
    ///
    /// @param playerId l'identité du joueur
    /// @param penalty  le nombre de points perdus (strictement positif)
    default void floor(PlayerId playerId, int penalty) {}

    /// Appelée à la fin de la partie lorsque le joueur {@code playerId} a remporté
    /// {@code points} points de bonus car la ligne {@code line} de son mur est complète.
    ///
    /// @param playerId l'identité du joueur
    /// @param line     la ligne complète du mur
    /// @param points   le nombre de points bonus (égal à {@link Points#FULL_ROW_BONUS_POINTS})
    default void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {}

    /// Appelée à la fin de la partie lorsque le joueur {@code playerId} a remporté
    /// {@code points} points de bonus car la colonne {@code column} de son mur est complète.
    ///
    /// @param playerId l'identité du joueur
    /// @param column   l'index de la colonne complète (entre 0 et 4 inclus)
    /// @param points   le nombre de points bonus (égal à {@link Points#FULL_COLUMN_BONUS_POINTS})
    default void fullColumn(PlayerId playerId, int column, int points) {}

    /// Appelée à la fin de la partie lorsque le joueur {@code playerId} a remporté
    /// {@code points} points de bonus car la couleur {@code color} est complète dans son mur.
    ///
    /// @param playerId l'identité du joueur
    /// @param color    la couleur complète
    /// @param points   le nombre de points bonus (égal à {@link Points#FULL_COLOR_BONUS_POINTS})
    default void fullColor(PlayerId playerId, TileKind.Colored color, int points) {}
}