package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.security.spec.PKCS8EncodedKeySpec;
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

        GridPane playerBoardGrid = new GridPane();
        playerBoardGrid.setId("player-boards");
        root.getChildren().add(playerBoardGrid);

        Map<PlayerId, StackPane> playerBoards = new HashMap<>();

        for (PlayerId playerId: observer.getValue().playerIds()){
            //Plateau du joueur courant
            StackPane currentPlayerBoard = new StackPane();
            currentPlayerBoard.getStyleClass().addAll("player-board");

            playerBoards.put(playerId, currentPlayerBoard);
            //Contenu du plateau du joueur courant
            VBox gridContent = new VBox();
            currentPlayerBoard.getChildren().add(gridContent);


            //Lignes de motif et mur

            GridPane patternWall = new GridPane();
            patternWall.getStyleClass().add("lines-and-wall");

            gridContent.getChildren().add(patternWall);

            for (int row = 0; row < PkWall.WALL_WIDTH; row++){

                HBox patternBox = new HBox();

                patternBox.getStyleClass().addAll("tile-destination" , "tile-group");
                for (int count = 0; count < row + 1; count++) {
                    Node anchor =
                            anchors.get(new TileLocation.OnPattern(playerId,TileDestination.Pattern.ALL.get(row), count));
                    GridPane.setHalignment(patternBox, HPos.RIGHT);
                    GridPane.setFillWidth(patternBox, false);
                    patternBox.getChildren().add(anchor);

                }
                patternWall.add(patternBox, 0, row);

                Text fullColorBonus = new Text("+10");
                patternWall.add(fullColorBonus, 1, row);


                for (int col = 2; col < 7 ; col++) {

                    Node anchor = anchors.get(
                            new TileLocation.OnWall(
                                    playerId,
                                    TileDestination.Pattern.ALL.get(row),
                                    PkWall.colorAt(TileDestination.Pattern.ALL.get(row), col-2)));
                    anchor.getStyleClass().addAll("wall-background",
                            PkWall.colorAt(TileDestination.Pattern.ALL.get(row), col-2).toString());

                    patternWall.add(anchor, col, row);

                }

                Text fullRowBonus = new Text("+2");
                patternWall.add(fullRowBonus, 7, row);
            }

            for (int col = 2; col < 7 ; col++) {
                Text fullColBonus = new Text("+7");
                GridPane.setHalignment(fullColBonus, HPos.CENTER);
                patternWall.add(fullColBonus, col, 5);
            }

            //Ligne plancher
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
        //Mise à jour du bord du pleteau du joueur courant
        observer.map(ImmutableGameState::currentPlayerId).subscribe(currentId -> {
            playerBoards.forEach((playerId, pane) -> {
                if (playerId == currentId)
                    pane.getStyleClass().add("current-player");
                else
                    pane.getStyleClass().remove("current-player");
            });
        });



        return new BoardUI(anchors, observer, root);

    }

    public Node root(){
        return root;
    }

    public void showBonusPoints(PlayerId playerId, Object bonusKey){

    }

}
