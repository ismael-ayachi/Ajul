package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;

public final class TileOverlayUI {

    private final Node root;
    private final ObservableValue<ImmutableGameState> observer;

    private TileOverlayUI(Node root, ObservableValue<ImmutableGameState> observer){
        this.root = root;
        this.observer = observer;

    }

    public static TileOverlayUI create(ObservableValue<ImmutableGameState> observer,
                          Tiles tiles,
                          Set<Move> validMoves,
                          Set<Move> potentialMoves,
                          boolean[] moveAccepted){


        Pane root = new Pane();
        for (TileKind tileKind : TileKind.ALL){
            int i = 0;

            for (Node node : tiles.tiles().get(tileKind)) {
                node.setViewOrder(0);
                root.getChildren().add(node);

                Tiles.setLocation(node, new TileLocation.OffBoard(tileKind, i++));
                node.relocate(-30, -30);
                if (tileKind instanceof TileKind.Colored){
                    node.setOnMousePressed(e -> e.setDragDetect(true));
                    node.setOnDragDetected(_ -> {
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
                        node.setOnDragDetected(e -> {
                            double initPosX = e.getX();
                            double initPosY = e.getY();
                            node.setOnMouseDragged(de -> {
                                double dx = de.getSceneX() - initPosX;
                                double dy = de.getSceneY() - initPosY;

                                for (Node nodeToMove: nodesToMove){
                                    nodeToMove.setTranslateX(dx);
                                    nodeToMove.setTranslateY(dy);
                                }
                            });
                        });

                        node.setOnMouseDragReleased( _ -> {
                            for (Node nodeToMove : nodesToMove){

                                if (moveAccepted[0]){
                                    nodeToMove.setTranslateX(nodeToMove.getLayoutX());
                                    nodeToMove.setTranslateY(nodeToMove.getLayoutY());
                                }
                                else {
                                    TranslateTransition transition =
                                            new TranslateTransition(Duration.millis(125), node);
                                    transition.setToX(0);
                                    transition.setToY(0);
                                    transition.play();
                                }
                            }
                            node.setOnMouseDragReleased(null);
                            for (Node nodeToMove : nodesToMove) {
                                nodeToMove.setViewOrder(0);
                            }
                            root.setMouseTransparent(false);

                        });


                    });



                }
            }
        }

        return new TileOverlayUI(root, observer);
    }


    public Node root() {
        return root;
    }

    public void showTilePoints(TileLocation.OnWall wall, int points){
        Text pointsText = new Text(Integer.toString(points));

    }


}
