package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class TileOverlayUI {


    private final Pane root;
    private final Tiles tiles;
    private final ObservableValue<ImmutableGameState> observer;

    private TileOverlayUI(Pane root, Tiles tiles, ObservableValue<ImmutableGameState> observer){
        this.root = root;
        this.tiles = tiles;
        this.observer = observer;

    }

    public static TileOverlayUI create(ObservableValue<ImmutableGameState> observer,
                          Tiles tiles,
                          Set<Move> validMoves,
                          Set<Move> potentialMoves,
                          boolean[] moveAccepted){

        Pane root = new Pane();


        Function<TileLocation, Point2D> position = loc -> {
            if (loc instanceof TileLocation.OffBoard) {
                return new Point2D(-30, -30);
            }
            Node anchor = tiles.anchors().get(loc);
            Point2D sceneCoords = anchor.localToScene(0, 0);
            return root.sceneToLocal(sceneCoords);
        };


        Platform.runLater(() -> observer.subscribe(gameState -> {
            Animation animation = TileAnimator.animateTiles(position, tiles.tiles(), gameState);
            animation.play();
        }));

        for (TileKind tileKind : TileKind.ALL){
            int i = 0;

            for (Node node : tiles.tiles().get(tileKind)) {
                node.setViewOrder(0);
                root.getChildren().add(node);
                Tiles.setLocation(node, new TileLocation.OffBoard(tileKind, i++));
                node.relocate(-30, -30);
                if (tileKind instanceof TileKind.Colored){
                    node.setOnMousePressed(e -> e.setDragDetect(true));
                    node.setOnDragDetected(e -> {
                        TileLocation loc = Tiles.location(node);
                        if (!(loc instanceof TileLocation.OnSource) || validMoves.isEmpty()) return;
                        TileSource source = ((TileLocation.OnSource) loc).tileSource();
                        potentialMoves.clear();
                        for (Move move: validMoves) {
                            if (move.source().equals(source) && move.tileColor().equals(tileKind))
                                potentialMoves.add(move);
                        }
                        node.startFullDrag();
                        root.setMouseTransparent(true);
                        List<Node> nodesToMove = tiles.tiles().get(tileKind).stream()
                                .filter(n -> {
                                    TileLocation currentLoc = Tiles.location(n);
                                    return currentLoc instanceof TileLocation.OnSource onSource
                                            && onSource.tileSource().equals(source);
                                })
                                .toList();
                        for (Node nodeMove: nodesToMove){
                            nodeMove.setViewOrder(-1);
                        }

                        double initPosX = e.getSceneX();
                        double initPosY = e.getSceneY();
                        node.setOnMouseDragged(de -> {
                            double dx = de.getSceneX() - initPosX;
                            double dy = de.getSceneY() - initPosY;
                            for (Node nodeToMove: nodesToMove){
                                nodeToMove.setTranslateX(dx);
                                nodeToMove.setTranslateY(dy);
                                }
                            });

                        node.setOnMouseReleased( _ -> {
                            for (Node nodeToMove : nodesToMove){

                                if (moveAccepted[0]){
                                    nodeToMove.setLayoutX(nodeToMove.getLayoutX() + nodeToMove.getTranslateX());
                                    nodeToMove.setLayoutY(nodeToMove.getLayoutY() + nodeToMove.getTranslateY());
                                    nodeToMove.setTranslateX(0);
                                    nodeToMove.setTranslateY(0);
                                }
                                else {
                                    TranslateTransition transition =
                                            new TranslateTransition(Duration.millis(125), nodeToMove);
                                    transition.setToX(0);
                                    transition.setToY(0);
                                    transition.play();
                                }
                            }
                            node.setOnMouseReleased(null);
                            node.setOnMouseDragged(null);
                            for (Node nodeToMove : nodesToMove) {
                                nodeToMove.setViewOrder(0);
                            }

                            moveAccepted[0] = false;
                            root.setMouseTransparent(false);

                        });
                    });
                }
            }
        }

        return new TileOverlayUI(root, tiles, observer);
    }


    public Node root() {
        return root;
    }

    public void showTilePoints(TileLocation.OnWall wall, int points) {
        Platform.runLater(() -> {
            Text pointsText = new Text(Integer.toString(points));
            pointsText.setViewOrder(-2);

            // Position de l'ancre du mur
            Node anchor = tiles.anchors().get(wall);
            Point2D sceneCoords = anchor.localToScene(0, 0);
            Point2D paneCoords = root.sceneToLocal(sceneCoords);

            // Centrer le texte sur la tuile
            Bounds textSize = pointsText.getBoundsInLocal();
            double centerX = paneCoords.getX() + (Tiles.TILE_WIDTH - textSize.getWidth()) / 2;
            double centerY = paneCoords.getY() + (Tiles.TILE_HEIGHT + textSize.getHeight()) / 2;
            pointsText.setX(centerX);
            pointsText.setY(centerY);

            root.getChildren().add(pointsText);
        });
    }


}
