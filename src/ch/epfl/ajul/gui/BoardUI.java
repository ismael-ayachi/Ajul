package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javafx.geometry.Pos.*;

public final class BoardUI {

    private final Map<TileLocation, Node> anchors;
    private final ObservableValue<ImmutableGameState> observer;
    private final Node root;

    private BoardUI(Map<TileLocation, Node> anchors, ObservableValue<ImmutableGameState> observer, Node root){
        this.anchors = anchors;
        this.observer = observer;
        this.root = root;
    }

    //Create prend aussi 3 autres arguments à ajouter à la fin/en lien avec le drag&drop
    public static BoardUI create(Map<TileLocation, Node> anchors, ObservableValue<ImmutableGameState> observer){

        //Root
        HBox root = new HBox();

        //Source de Tuiles
        GridPane sourceGrid = new GridPane();
        sourceGrid.setId("tile-sources");
        root.getChildren().add(sourceGrid);

        GridPane playerBoardGrid = new GridPane();
        playerBoardGrid.setId("player-boards");
        root.getChildren().add(playerBoardGrid);

        //Fabriques
        for (TileSource factory : observer.getValue().game().factories()) {
            HBox factoryBox = new HBox();
            factoryBox.getStyleClass().addAll("tile-group", "tile-source");

            for (int i = 0; i < TileSource.Factory.TILES_PER_FACTORY; i++) {
                Node anchor = anchors.get(new TileLocation.OnSource(factory, i));
                factoryBox.getChildren().add(anchor);
            }
            int index = factory.index() - 1; // fabriques commencent à l'index 1
            sourceGrid.add(factoryBox, index % 2, index / 2);

        }

        //Zone centrale
        GridPane centerGrid = new GridPane();
        centerGrid.getStyleClass().addAll("tile-group", "tile-source");

        for (int i = 0; i < observer.getValue().game().centralAreaMaxSize(); i++) {
            Node anchor = anchors.get(new TileLocation.OnSource(TileSource.CENTER_AREA, i));
            centerGrid.add(anchor, i % 8, i / 8);
        }
        int centerRow = (observer.getValue().game().factoriesCount() + 1)/2;
        sourceGrid.add(centerGrid, 0, centerRow, 2, 1);

        //Plateaux des joueurs



        for (PlayerId playerId: observer.getValue().playerIds()){
            StackPane currentPlayerBoard = new StackPane();
            currentPlayerBoard.getStyleClass().addAll("player-board");

            VBox gridContent = new VBox();

            currentPlayerBoard.getChildren().add(gridContent);

            HBox floor = new HBox();
            floor.getStyleClass().addAll("floor", "tile-group", "tile-destination");

            gridContent.getChildren().add(floor);

            for (int i = 0; i < TileDestination.FLOOR.capacity(); i++){
                VBox floorPositions = new VBox();

                Node anchor = anchors.get(new TileLocation.OnFloor(playerId, i));
                floorPositions.getChildren().add(anchor);

                Text penalty = new Text(Integer.toString(Points.floorPenalty(i)));
                floorPositions.getChildren().add(penalty);


                floor.getChildren().add(floorPositions);

            }


            playerBoardGrid.add(currentPlayerBoard, playerId.ordinal()%2, playerId.ordinal()/2);

        }



        return new BoardUI(anchors, observer, root);

    }

    public Node root(){
        return root;
    }

    public void showBonusPoints(PlayerId playerId, Object bonusKey){

    }

}
