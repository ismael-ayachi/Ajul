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
    private final Map<AbstractMap.SimpleEntry<PlayerId, Object>, Text> bonusMap;

    private static final String TILE_SOURCE_CLASS = "tile-source";
    private static final String TILE_GROUP_CLASS = "tile-group";
    private static final String TILE_DESTINATION_CLASS = "tile-destination";
    private static final String ACCEPTING_CLASS = "accepting";
    private static final String LINES_AND_WALL_CLASS = "lines-and-wall";
    private static final String WALL_BACKGROUND_CLASS = "wall-background";
    private static final String CURRENT_PLAYER_CLASS = "current-player";
    private static final String FLOOR_CLASS = "floor";
    private static final String PLAYER_BOARD_CLASS = "player-board";

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

        Game game = observer.getValue().game();

        //Root
        HBox root = new HBox();

        //Source de Tuiles
        GridPane sourceGrid = new GridPane();
        sourceGrid.setId("tile-sources");
        root.getChildren().add(sourceGrid);

        //Table associative pour la visibilité des points bonus
        Map<AbstractMap.SimpleEntry<PlayerId, Object>, Text> bonusMap = new HashMap<>();

        //Fabriques
        for (TileSource.Factory factory : game.factories()) {
            HBox factoryBox = new HBox();
            factoryBox.getStyleClass().addAll(TILE_GROUP_CLASS, TILE_SOURCE_CLASS);
            for (int i = 0; i < TileSource.Factory.TILES_PER_FACTORY; i++) {
                Node anchor = anchors.get(new TileLocation.OnSource(factory, i));
                factoryBox.getChildren().add(anchor);
            }
            int index = factory.index() - 1; // fabriques commencent à l'index 1
            sourceGrid.add(factoryBox, index % 2, index / 2);
        }

        //Zone centrale
        GridPane centerAreaGrid = new GridPane();
        centerAreaGrid.getStyleClass().addAll(TILE_GROUP_CLASS, TILE_SOURCE_CLASS);
        for (int i = 0; i < game.centralAreaMaxSize(); i++) {
            Node anchor = anchors.get(new TileLocation.OnSource(TileSource.CENTER_AREA, i));
            centerAreaGrid.add(anchor, i % 8, i / 8);
        }
        int centerAreaRow = (game.factoriesCount() + 1) / 2;
        sourceGrid.add(centerAreaGrid, 0, centerAreaRow, 2, 1);

        //Plateaux des joueurs

        GridPane playerBoardGrid = new GridPane();
        playerBoardGrid.setId("player-boards");
        root.getChildren().add(playerBoardGrid);

        Map<PlayerId, StackPane> playerBoards = new HashMap<>();

        for (PlayerId playerId: game.playerIds()){
            //Plateau du joueur courant
            StackPane currentPlayerBoard = new StackPane();
            currentPlayerBoard.getStyleClass().add(PLAYER_BOARD_CLASS);
            playerBoards.put(playerId, currentPlayerBoard);

            //Nom et points des joueurs
            ObservableValue<Integer> pointsObserver = observer.map(gameState ->
                    PkPlayerStates.points(gameState.pkPlayerStates(), playerId));
            Text identity = new Text();
            identity.textProperty().bind(
                    Bindings.format("%s\nPoints : %d",
                        game.playerDescriptions().get(playerId.ordinal()).name(),
                        pointsObserver));

            currentPlayerBoard.getChildren().add(identity);

            //Contenu du plateau du joueur courant
            VBox gridContent = new VBox();
            currentPlayerBoard.getChildren().add(gridContent);

            //Lignes de motif et mur
            GridPane patternWall = new GridPane();
            patternWall.getStyleClass().add(LINES_AND_WALL_CLASS);

            gridContent.getChildren().add(patternWall);

            for (int row = 0; row < PkWall.WALL_WIDTH; row++){

                HBox patternBox = new HBox();
                patternBox.getStyleClass().addAll(TILE_DESTINATION_CLASS , TILE_GROUP_CLASS);

                TileDestination.Pattern line = TileDestination.Pattern.ALL.get(row);

                //Gestion des événements
                patternBox.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, ( _ -> {
                    if (observer.getValue().currentPlayerId() != playerId) return;
                    boolean canAccept = potentialMoves.stream()
                            .anyMatch(move -> move.destination().equals(line));
                    if (canAccept)
                        patternBox.getStyleClass().add(ACCEPTING_CLASS);
                }));

                patternBox.addEventHandler(
                        MouseDragEvent.MOUSE_DRAG_EXITED,
                        _ -> patternBox.getStyleClass().remove(ACCEPTING_CLASS));

                patternBox.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, _ -> {
                    if (observer.getValue().currentPlayerId() != playerId) return;
                    potentialMoves.stream()
                            .filter(move -> move.destination().equals(line))
                            .findFirst()
                            .ifPresent(move -> {
                                if (moveQueue.offer(move)) {
                                    moveAccepted[0] = true;
                                    patternBox.getStyleClass().remove(ACCEPTING_CLASS);
                                }});
                });

                GridPane.setHalignment(patternBox, HPos.RIGHT);
                GridPane.setFillWidth(patternBox, false);
                for (int count = 0; count < row + 1; count++) {
                    Node anchor = anchors.get(new TileLocation.OnPattern(playerId, line, count));
                    patternBox.getChildren().add(anchor);
                }

                patternWall.add(patternBox, 0, row);
                Text fullColorBonus = new Text("+" + Points.FULL_COLOR_BONUS_POINTS); //Formatage ?
                fullColorBonus.setVisible(false);
                patternWall.add(fullColorBonus, 1, row);

                for (int col = 0; col < PkWall.WALL_WIDTH ; col++) {
                    TileKind.Colored color =  PkWall.colorAt(line, col);
                    bonusMap.put(new AbstractMap.SimpleEntry<>(playerId, color), fullColorBonus);
                    Node anchor = anchors.get(new TileLocation.OnWall(playerId, line, color));
                    anchor.getStyleClass().addAll(WALL_BACKGROUND_CLASS, color.toString());
                    patternWall.add(anchor, col + 2, row);
                }
                Text fullRowBonus = new Text("+" + Points.FULL_ROW_BONUS_POINTS); //Formatage ?
                fullRowBonus.setVisible(false);
                bonusMap.put(new AbstractMap.SimpleEntry<>(playerId, line), fullRowBonus);
                patternWall.add(fullRowBonus, PkWall.WALL_WIDTH + 2, row);
            }

            for (int col = 0; col < PkWall.WALL_WIDTH ; col++) {
                Text fullColBonus = new Text("+" + Points.FULL_COLUMN_BONUS_POINTS); //Formatage ?
                fullColBonus.setVisible(false);
                bonusMap.put(new AbstractMap.SimpleEntry<>(playerId, col) , fullColBonus);
                GridPane.setHalignment(fullColBonus, HPos.CENTER);
                patternWall.add(fullColBonus, col + 2, PkWall.WALL_HEIGHT);
            }

            //Ligne plancher
            HBox floor = new HBox();
            floor.getStyleClass().addAll(FLOOR_CLASS, TILE_GROUP_CLASS, TILE_DESTINATION_CLASS);
            gridContent.getChildren().add(floor);

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, _ -> {
                if (observer.getValue().currentPlayerId() != playerId) return;
                boolean canAccept = potentialMoves.stream()
                        .anyMatch(move -> move.destination().equals(TileDestination.FLOOR));
                if (canAccept)
                    floor.getStyleClass().add(ACCEPTING_CLASS);
            });

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, _ -> {
                floor.getStyleClass().remove(ACCEPTING_CLASS);
            });

            floor.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, _ -> {
                if (observer.getValue().currentPlayerId() != playerId) return;
                boolean canAccept = potentialMoves.stream()
                        .anyMatch(move -> move.destination().equals(TileDestination.FLOOR));
                Move moveFound = potentialMoves.stream()
                        .filter(move -> move.destination().equals(TileDestination.FLOOR))
                        .findFirst().orElse(null);
                if (canAccept && moveQueue.offer(Objects.requireNonNull(moveFound))) {
                    moveAccepted[0] = true;
                    floor.getStyleClass().remove(ACCEPTING_CLASS);
                }
            });
            for (int i = 0; i < TileDestination.FLOOR.capacity(); i++){
                floor.getChildren().add(new VBox(
                    anchors.get(new TileLocation.OnFloor(playerId, i)),
                    new Text("-" + Points.floorPenalty(i))));
            }
            playerBoardGrid.add(currentPlayerBoard, playerId.ordinal()%2, playerId.ordinal()/2);
        }

        //Mise à jour du bord du plateau du joueur courant
        observer.map(ImmutableGameState::currentPlayerId).subscribe(currentId -> {
            playerBoards.forEach((playerId, pane) -> {
                if (playerId == currentId) pane.getStyleClass().add(CURRENT_PLAYER_CLASS);
                else pane.getStyleClass().remove(CURRENT_PLAYER_CLASS);
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
