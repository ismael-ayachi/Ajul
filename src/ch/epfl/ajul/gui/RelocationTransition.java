package ch.epfl.ajul.gui;

import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

public final class RelocationTransition extends Transition {

    private final Node node;
    private final Point2D endPos;
    private final Point2D startPos;

    public RelocationTransition(Node node, Point2D endPos, Duration duration){
        this.node = node;
        this.startPos = node.localToParent(Point2D.ZERO);
        this.endPos = endPos;
        setCycleDuration(duration);
    }

    @Override
    protected void interpolate(double frac) {
        Point2D interpolatedPos = startPos.interpolate(endPos, frac);
        node.relocate(interpolatedPos.getX(), interpolatedPos.getY());
    }
}
