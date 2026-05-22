package ch.epfl.ajul.gui;

import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/// Emplacement possible d'une tuile dans l'interface graphique.
/// <p>
/// Interface scellée dont les cinq implémentations couvrent tous les endroits où
/// une tuile peut se trouver : hors du plateau, sur une source, sur une ligne de
/// motif, sur le mur ou sur le plancher d'un joueur.
///
/// @author Ismaël Ayachi (393163)
public sealed interface TileLocation {

    /// Emplacement d'une tuile hors du plateau (dans le sac ou défaussée).
    ///
    /// @param tileKind la sorte de la tuile
    /// @param index    l'index distinguant les tuiles identiques hors plateau
    record OffBoard(TileKind tileKind, int index) implements TileLocation {}

    /// Emplacement d'une tuile sur une source (fabrique ou zone centrale).
    ///
    /// @param tileSource la source contenant la tuile
    /// @param index      la position de la tuile au sein de la source
    record OnSource(TileSource tileSource, int index) implements TileLocation {}

    /// Emplacement d'une tuile sur une ligne de motif d'un joueur.
    ///
    /// @param playerId le joueur propriétaire de la ligne
    /// @param pattern  la ligne de motif
    /// @param index    la position de la tuile sur la ligne
    record OnPattern(PlayerId playerId, TileDestination.Pattern pattern, int index) implements TileLocation {}

    /// Emplacement d'une tuile sur le mur d'un joueur.
    ///
    /// @param playerId le joueur propriétaire du mur
    /// @param pattern  la ligne du mur
    /// @param tileKind la couleur de la case, qui détermine sa colonne
    record OnWall(PlayerId playerId, TileDestination.Pattern pattern, TileKind.Colored tileKind)
            implements TileLocation {}

    /// Emplacement d'une tuile sur le plancher d'un joueur.
    ///
    /// @param playerId le joueur propriétaire du plancher
    /// @param index    la position de la tuile sur le plancher (entre 0 et 6 inclus)
    record OnFloor(PlayerId playerId, int index) implements TileLocation {}

}
