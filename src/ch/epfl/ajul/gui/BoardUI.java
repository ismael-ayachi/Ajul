package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.*;
import java.util.concurrent.BlockingQueue;


public final class BoardUI {


    private final Node root;
    Map<AbstractMap.SimpleEntry<PlayerId, Object>, Text> bonusMap;

    private BoardUI(Node root, Map<AbstractMap.SimpleEntry<PlayerId, Object>, Text> bonusMap){
        this.root = root;
        this.bonusMap = bonusMap;
    }

    //Create prend aussi 3 autres arguments à ajouter à la fin/en lien avec le drag&drop
    public static BoardUI create(Map<TileLocation, Node> anchors,
                                 ObservableValue<ImmutableGameState> observer,
                                 Set<Move> potentialMoves,
                                 boolean[] moveAccepted,
                                 BlockingQueue<Move> moveQueue) {

        //Définit une variable GameState pour à partie statique ???

        //Root
        HBox root = new HBox();

        //Source de Tuiles
        GridPane sourceGrid = new GridPane();
        sourceGrid.setId("tile-sources");
        root.getChildren().add(sourceGrid);

        //Table associative pour la visibilité des points bonus
        Map<AbstractMap.SimpleEntry<PlayerId, Object>, Text> bonusMap = new HashMap<>();



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

            //Nom et points des joueurs

            ObservableValue<Integer> pointsObserver = observer.map( gameState ->
                    PkPlayerStates.points(gameState.pkPlayerStates(), playerId));
            Text identity = new Text();
            identity.textProperty().bind(
                    Bindings.format("%s\nPoints : %d",
                        observer.getValue().game().playerDescriptions().get(playerId.ordinal()).name(),
                        observer.map(gs -> PkPlayerStates.points(gs.pkPlayerStates(), playerId))));


            currentPlayerBoard.getChildren().add(identity);

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

                TileDestination.Pattern line = TileDestination.Pattern.ALL.get(row);

                //Gestion des événements
                patternBox.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, ( _ -> {
                    boolean canAccept = potentialMoves.stream()
                            .anyMatch(move -> move.destination().equals(line));
                    if (canAccept)
                        patternBox.getStyleClass().add("accepting");
                }));

                patternBox.addEventHandler(
                        MouseDragEvent.MOUSE_DRAG_EXITED,
                        _ -> patternBox.getStyleClass().remove("accepting"));

                patternBox.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, _ -> {
                    boolean canAccept = potentialMoves.stream()
                            .anyMatch(move -> move.destination().equals(line));
                    Move moveFound = potentialMoves.stream().filter(
                            move -> move.destination().equals(line)).findFirst().orElse(null);
                    if (canAccept && moveQueue.offer(Objects.requireNonNull(moveFound))){
                        moveAccepted[0] = true;
                        patternBox.getStyleClass().remove("accepting");
                    }
                });



                for (int count = 0; count < row + 1; count++) {
                    Node anchor =
                            anchors.get(new TileLocation.OnPattern(playerId,TileDestination.Pattern.ALL.get(row), count));
                    GridPane.setHalignment(patternBox, HPos.RIGHT);
                    GridPane.setFillWidth(patternBox, false);
                    patternBox.getChildren().add(anchor);

                }
                patternWall.add(patternBox, 0, row);

                Text fullColorBonus = new Text("+10");
                fullColorBonus.setVisible(false);

                patternWall.add(fullColorBonus, 1, row);


                for (int col = 2; col < 7 ; col++) {

                    bonusMap.put(new AbstractMap.SimpleEntry<>(
                            playerId,
                            PkWall.colorAt(TileDestination.Pattern.ALL.get(row), col - 2)), fullColorBonus);

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
                fullRowBonus.setVisible(false);
                bonusMap.put(new AbstractMap.SimpleEntry<>(playerId, TileDestination.Pattern.ALL.get(row)), fullRowBonus);
                patternWall.add(fullRowBonus, 7, row);
            }

            for (int col = 2; col < 7 ; col++) {
                Text fullColBonus = new Text("+7");
                fullColBonus.setVisible(false);
                bonusMap.put(new AbstractMap.SimpleEntry<>(playerId, col - 2) , fullColBonus);
                GridPane.setHalignment(fullColBonus, HPos.CENTER);
                patternWall.add(fullColBonus, col, 5);
            }

            //Ligne plancher
            HBox floor = new HBox();
            floor.getStyleClass().addAll("floor", "tile-group", "tile-destination");
            gridContent.getChildren().add(floor);

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, _ -> {
                boolean canAccept = potentialMoves.stream()
                        .anyMatch(move -> move.destination().equals(TileDestination.FLOOR));
                if (canAccept)
                    floor.getStyleClass().add("accepting");
            });

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED,
                    _ -> floor.getStyleClass().remove("accepting"));

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, _ -> {
                boolean canAccept = potentialMoves.stream()
                        .anyMatch(move -> move.destination().equals(TileDestination.FLOOR));
                Move moveFound = potentialMoves.stream()
                        .filter(move -> move.destination().equals(TileDestination.FLOOR))
                        .findFirst().orElse(null);
                if (canAccept && moveQueue.offer(Objects.requireNonNull(moveFound))) {
                    moveAccepted[0] = true;
                    floor.getStyleClass().remove("accepting");
                }
            });



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

        return new BoardUI(root, bonusMap);

    }

    public Node root(){
        return root;
    }

    public void showBonusPoints(PlayerId playerId, Object bonusKey){
        bonusMap.get(new AbstractMap.SimpleEntry<>(playerId, bonusKey)).setVisible(true);
    }

}
