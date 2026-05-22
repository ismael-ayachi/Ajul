package ch.epfl.ajul.gui;

import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

/// Animation déplaçant progressivement un nœud JavaFX de sa position courante
/// vers une position finale donnée.
///
/// @author Ismaël Ayachi (393163)
public final class RelocationTransition extends Transition {

    private final Node node;
    private final Point2D endPos;
    private final Point2D startPos;

    /// Construit une animation de déplacement du nœud {@code node} vers {@code endPos}.
    /// La position de départ est celle qu'occupe le nœud au moment de la construction.
    ///
    /// @param node     le nœud à déplacer
    /// @param endPos   la position finale, exprimée dans le repère du parent du nœud
    /// @param duration la durée de l'animation
    public RelocationTransition(Node node, Point2D endPos, Duration duration){
        this.node = node;
        this.startPos = node.localToParent(Point2D.ZERO);
        this.endPos = endPos;
        setCycleDuration(duration);
    }

    /// Place le nœud à la position interpolée entre départ et arrivée selon {@code frac}.
    ///
    /// @param frac la fraction d'avancement de l'animation (entre 0 et 1 inclus)
    @Override
    protected void interpolate(double frac) {
        Point2D interpolatedPos = startPos.interpolate(endPos, frac);
        node.relocate(interpolatedPos.getX(), interpolatedPos.getY());
    }
}
