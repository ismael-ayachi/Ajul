package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Set;

public final class TileOverlayUI {


    private TileOverlayUI(){

    }

    public static TileOverlayUI create(ObservableValue<ImmutableGameState> observer,
                          Tiles tiles,
                          Set<Move> validMoves,
                          Set<Move> potentialMoves,
                          boolean[] moveAccepted){


        Pane tilesPane = new Pane();
        for (TileKind tileKind : TileKind.ALL){
            int i = 0;

            for (Node node : tiles.tiles().get(tileKind)) {
                node.setViewOrder(0);
                tilesPane.getChildren().add(node);

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
                        tilesPane.setMouseTransparent(true);
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

                    });

                }

            }

        }
        return new TileOverlayUI();


    }
}
