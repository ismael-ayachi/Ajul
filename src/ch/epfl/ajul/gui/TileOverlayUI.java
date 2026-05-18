package ch.epfl.ajul.gui;
import ch.epfl.ajul.Points;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class TileOverlayUI {


    private final Pane root;
    private final Map<TileLocation, Node> anchors;
    private static final Point2D OFFBOARD_POSITION = new Point2D(-Tiles.TILE_WIDTH, -Tiles.TILE_HEIGHT);

    private enum Layer {
        STILL, MOVING, POINTS;
        public void order(Node node) {
            node.setViewOrder(-ordinal());
        }
    }

    private TileOverlayUI(Pane root,  Map<TileLocation, Node> anchors){
        this.root = root;
        this.anchors = anchors;
    }

    public Node root() {
        return root;
    }

    public static TileOverlayUI create(ObservableValue<ImmutableGameState> observer,
                          Tiles tiles,
                          Set<Move> validMoves,
                          Set<Move> potentialMoves,
                          boolean[] moveAccepted){

        Pane root = new Pane();

        //Méthode auxiliaire => pas forcément nécessaire
        Function<TileLocation, Point2D> position = loc -> {
            Node anchor = tiles.anchors().get(loc);
            return anchor != null
                    ? root.sceneToLocal(anchor.localToScene(Point2D.ZERO))
                    : OFFBOARD_POSITION;
        };

        Platform.runLater(() -> observer.subscribe(gameState -> {
            Animation animation = TileAnimator.animateTiles(position, tiles.tiles(), gameState);
            animation.play();
        }));

        tiles.tiles().forEach((tileKind, nodes) -> {

            root.getChildren().addAll(nodes);
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);

               // node.setViewOrder(0); // À supprimer ?
                Tiles.setLocation(node, new TileLocation.OffBoard(tileKind, i));
                node.relocate(OFFBOARD_POSITION.getX(), OFFBOARD_POSITION.getY());
                if (tileKind instanceof TileKind.Colored){
                    node.setOnMousePressed(e -> e.setDragDetect(true));
                    node.setOnDragDetected(e -> {
                        TileLocation loc = Tiles.location(node);
                        if (validMoves.isEmpty() || !(loc instanceof TileLocation.OnSource) ) return;

                        TileSource source = ((TileLocation.OnSource) loc).tileSource();
                        potentialMoves.clear();
                        //Assert potentialMoves ?
                        for (Move move: validMoves) { //Utiliser un stream ?
                            if (move.source().equals(source) && move.tileColor().equals(tileKind))
                                potentialMoves.add(move);
                        }
                        //Assert potentialMoves ?
                        node.startFullDrag();
                        root.setMouseTransparent(true);

                        List<Node> nodesToMove = nodes.stream()
                                .filter(n -> {
                                    TileLocation currentLoc = Tiles.location(n);
                                    return currentLoc instanceof TileLocation.OnSource onSource
                                            && onSource.tileSource().equals(source);
                                })
                                .toList();

                        for (Node nodeToMove: nodesToMove) Layer.MOVING.order(nodeToMove);

                        node.setOnMouseDragged(de -> {
                            double dx = de.getSceneX() - e.getSceneX();
                            double dy = de.getSceneY() - e.getSceneY();
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
                            node.setOnMouseDragReleased(null);
                            node.setOnMouseDragged(null);
                            for (Node nodeToMove : nodesToMove) {
                                Layer.STILL.order(nodeToMove);
                            }
                            moveAccepted[0] = false;
                            root.setMouseTransparent(false);

                        });
                    });
                }
            }
        });

        return new TileOverlayUI(root, tiles.anchors());
    }


    public void showTilePoints(TileLocation.OnWall wall, int points) {
        Platform.runLater(() -> {
            Text pointsText = new Text("+" + points);
            Layer.POINTS.order(pointsText);

            Bounds textSize = pointsText.getBoundsInLocal();
            double centerX =  (Tiles.TILE_WIDTH - textSize.getWidth()) / 2;
            double centerY = (Tiles.TILE_HEIGHT - textSize.getHeight()) / 2;
            Point2D anchorPos = root.sceneToLocal(anchors.get(wall).localToScene(Point2D.ZERO));
            pointsText.relocate(anchorPos.getX() + centerX, anchorPos.getY() + centerY);

            Platform.runLater(() -> root.getChildren().add(pointsText));
        });
    }


}
