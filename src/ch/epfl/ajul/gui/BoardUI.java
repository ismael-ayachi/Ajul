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

/// Interface graphique du plateau de jeu : les sources de tuiles d'une part,
/// et les plateaux individuels des joueurs (lignes de motif, mur, plancher) d'autre part.
///
/// @author Ismaël Ayachi (393163)
public final class BoardUI {

    private final Node root;
    private final Map<BonusKey, Text> bonusMap;

    private static final String TILE_SOURCE_CLASS = "tile-source";
    private static final String TILE_GROUP_CLASS = "tile-group";
    private static final String TILE_DESTINATION_CLASS = "tile-destination";
    private static final String ACCEPTING_CLASS = "accepting";
    private static final String LINES_AND_WALL_CLASS = "lines-and-wall";
    private static final String WALL_BACKGROUND_CLASS = "wall-background";
    private static final String CURRENT_PLAYER_CLASS = "current-player";
    private static final String FLOOR_CLASS = "floor";
    private static final String PLAYER_BOARD_CLASS = "player-board";

    private BoardUI(Node root, Map<BonusKey, Text> bonusMap){
        this.root = root;
        this.bonusMap = bonusMap;
    }

    /// Construit l'interface graphique complète du plateau de jeu.
    ///
    /// @param anchors        la table associant chaque emplacement de tuile à son ancre
    /// @param observer       l'état observable de la partie
    /// @param potentialMoves l'ensemble des coups correspondant à la tuile en cours de glissement
    /// @param moveAccepted   drapeau (tableau d'une case) indiquant si le coup glissé a été accepté
    /// @param moveQueue       la file bloquante recevant le coup joué par un humain
    /// @return l'interface graphique du plateau
    public static BoardUI create(Map<TileLocation, Node> anchors,
                                 ObservableValue<ImmutableGameState> observer,
                                 Set<Move> potentialMoves,
                                 boolean[] moveAccepted,
                                 BlockingQueue<Move> moveQueue) {

        Game game = observer.getValue().game();
        HBox root = new HBox();
        Map<BonusKey, Text> bonusMap = new HashMap<>();

        //Grille des sources de tuiles
        GridPane sourceGrid = new GridPane();
        sourceGrid.setId("tile-sources");
        root.getChildren().add(sourceGrid);

        //Fabriques
        for (TileSource.Factory factory : game.factories()) {
            HBox factoryBox = new HBox();
            factoryBox.getStyleClass().addAll(TILE_GROUP_CLASS, TILE_SOURCE_CLASS);
            for (int i = 0; i < TileSource.Factory.TILES_PER_FACTORY; i++) {
                Node anchor = anchors.get(new TileLocation.OnSource(factory, i));
                factoryBox.getChildren().add(anchor);
            }
            int index = factory.index() - 1;
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

                //Cases de la ligne de motif
                GridPane.setHalignment(patternBox, HPos.RIGHT);
                GridPane.setFillWidth(patternBox, false);
                for (int count = 0; count < row + 1; count++) {
                    Node anchor = anchors.get(new TileLocation.OnPattern(playerId, line, count));
                    patternBox.getChildren().add(anchor);
                }
                patternWall.add(patternBox, 0, row);

                //Texte du bonus associé à une couleur complète
                Text fullColorBonus = new Text("+" + Points.FULL_COLOR_BONUS_POINTS);
                fullColorBonus.setVisible(false);
                patternWall.add(fullColorBonus, 1, row);

                //Cases du mur
                for (int col = 0; col < PkWall.WALL_WIDTH ; col++) {
                    TileKind.Colored color =  PkWall.colorAt(line, col);
                    bonusMap.put(new BonusKey(playerId, color), fullColorBonus);
                    Node anchor = anchors.get(new TileLocation.OnWall(playerId, line, color));
                    anchor.getStyleClass().addAll(WALL_BACKGROUND_CLASS, color.toString());
                    patternWall.add(anchor, col + 2, row);
                }

                //Texte du bonus associé à une ligne complète
                Text fullRowBonus = new Text("+" + Points.FULL_ROW_BONUS_POINTS);
                fullRowBonus.setVisible(false);
                bonusMap.put(new BonusKey(playerId, line), fullRowBonus);
                patternWall.add(fullRowBonus, PkWall.WALL_WIDTH + 2, row);
            }

            //Texte de bonus associé à une colonne complète
            for (int col = 0; col < PkWall.WALL_WIDTH ; col++) {
                Text fullColBonus = new Text("+" + Points.FULL_COLUMN_BONUS_POINTS);
                fullColBonus.setVisible(false);
                bonusMap.put(new BonusKey(playerId, col) , fullColBonus);
                GridPane.setHalignment(fullColBonus, HPos.CENTER);
                patternWall.add(fullColBonus, col + 2, PkWall.WALL_HEIGHT);
            }

            //Ligne plancher
            HBox floor = new HBox();
            floor.getStyleClass().addAll(FLOOR_CLASS, TILE_GROUP_CLASS, TILE_DESTINATION_CLASS);
            gridContent.getChildren().add(floor);

            //Gestion des événements
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

            //Cases de la ligne plancher avec leur pénalité
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
    /// Retourne la racine du graphe de scène du plateau.
    ///
    /// @return la racine du plateau
    public Node root(){
        return root;
    }

    /// Rend visible le texte de points bonus associé au joueur {@code playerId} et à la
    /// clé {@code bonusKey}, appelé en fin de partie lorsqu'un bonus est obtenu.
    ///
    /// @param playerId le joueur concerné
    /// @param bonusKey la clé du bonus : une ligne ({@code Pattern}), une colonne
    ///                 ({@code Integer}) ou une couleur ({@code TileKind.Colored})
    public void showBonusPoints(PlayerId playerId, Object bonusKey){
        bonusMap.get(new BonusKey(playerId, bonusKey)).setVisible(true);
    }

    /// Clé identifiant un texte de points bonus, combinant un joueur et un identifiant
    /// de bonus (ligne, colonne ou couleur).
    ///
    /// @param playerId le joueur concerné
    /// @param bonusKey l'identifiant du bonus
    private record BonusKey(PlayerId playerId, Object bonusKey) {}
}
