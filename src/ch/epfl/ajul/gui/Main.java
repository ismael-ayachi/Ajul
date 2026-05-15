package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkWall;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.AI;
import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

public final class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //Analyse des arguments
        Parameters parameters = getParameters();
        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        List<String> names = parameters.getUnnamed();
        for (int i = 0; i < names.size(); i++) {
            String pName = names.get(i);
            PlayerId playerId = PlayerId.ALL.get(i);
            if (pName.charAt(0) == '_') {
                playerDescriptions.add(new Game.PlayerDescription(playerId, pName.substring(1), AI));
            } else {
                playerDescriptions.add(new Game.PlayerDescription(playerId, pName, HUMAN));
            }
        }

        //Génération de la seed
        String seedString = parameters.getNamed().get("seed");
        byte[] seedBytes = seedString != null
                ? seedString.getBytes(StandardCharsets.UTF_8)
                : SecureRandom.getSeed(8);

        RandomGenerator rng = RandomGeneratorFactory.getDefault().create(seedBytes);

        //Construction de l'interface graphique
        int playersCount = parameters.getUnnamed().size();
        Game game = new Game(playerDescriptions);

        ImmutableGameState initialGameState = ImmutableGameState.initial(game);

        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<>());
        BlockingQueue<Move> moveQueue = new SynchronousQueue<>();
        Tiles tiles = Tiles.create(game);

        TileOverlayUI tileOverlayUI =
                TileOverlayUI.create(gameStateP,
                        tiles,
                        validMoves,
                        potentialMoves,
                        moveAccepted);

        BoardUI boardUI = BoardUI.create(tiles.anchors(),
                gameStateP,
                potentialMoves,
                moveAccepted,
                moveQueue);

        Parent root = new StackPane(boardUI.root(),
                tileOverlayUI.root());

        Thread.startVirtualThread(() -> {
            PointsObserver pointsObserver = new PointsObserver() {
                @Override
                public void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                                        TileKind.Colored color, int points) {
                    tileOverlayUI.showTilePoints(new TileLocation.OnWall(playerId, line, color), points);
                }

                @Override
                public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
                    Platform.runLater(() -> boardUI.showBonusPoints(playerId, line));
                }

                @Override
                public void fullColumn(PlayerId playerId, int column, int points) {
                    Platform.runLater(() -> boardUI.showBonusPoints(playerId, column));
                }

                @Override
                public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
                    Platform.runLater(() -> boardUI.showBonusPoints(playerId, color));
                }
            };

            MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game), pointsObserver);
            gameState.fillFactories(rng);
            ImmutableGameState immutableGameState = gameState.immutable();
            Platform.runLater(() -> gameStateP.set(immutableGameState));


            // Créer les instances MctsPlayer pour les joueurs IA
            Map<PlayerId, Player> aiPlayers = new HashMap<>();
            for (int i = 0; i < playersCount; i++) {
                if (playerDescriptions.get(i).kind() == Game.PlayerDescription.PlayerKind.AI) {
                    aiPlayers.put(
                            PlayerId.ALL.get(i),
                            new MctsPlayer(RandomGeneratorFactory.getDefault(), 10000));
                }
            }


            while (!gameState.isGameOver()) {
                PlayerId current = gameState.currentPlayerId();
                Game.PlayerDescription.PlayerKind kind = playerDescriptions.get(current.ordinal()).kind();
                Move move;
                if (kind == HUMAN) {
                    validMoves.clear();
                    //Calcul et remplissage de validMoves
                    short[] validMovesArray = new short[Move.MAX_MOVES];
                    int count = gameState.immutable().validMoves(validMovesArray);
                    for (int i = 0; i < count; i++) {
                        validMoves.add(Move.ofPacked(validMovesArray[i]));
                    }

                    try {
                        move = moveQueue.take();
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }

                   validMoves.clear();


                } else {
                    Player aiPlayer = aiPlayers.get(current);
                    move = aiPlayer.nextMove(gameState);
                }
                gameState.registerMove(move.packed());

                ImmutableGameState stateBeforeEnd = gameState.immutable();
                Platform.runLater(() -> gameStateP.set(stateBeforeEnd));
                if (gameState.isRoundOver()) {

                     //Sleep
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new Error(e);
                    }


                    gameState.endRound();

                    ImmutableGameState stateAfterEnd = gameState.immutable();
                    Platform.runLater(() -> gameStateP.set(stateAfterEnd));

                    if (!gameState.isGameOver()) {
                        //Sleep
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            throw new Error(e);
                        }

                        gameState.fillFactories(rng);

                        ImmutableGameState stateNewRound = gameState.immutable();
                        Platform.runLater(() -> gameStateP.set(stateNewRound));
                    }
                }
            }

            gameState.endGame();
        });

        root.getStylesheets().add("ajul.css");
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);
        primaryStage.show();

    }

}


