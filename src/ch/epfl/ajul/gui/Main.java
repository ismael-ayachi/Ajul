package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
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
import static java.lang.IO.println;

public final class Main extends Application {

    static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {

        //Analyse des arguments
        Parameters parameters = getParameters();
        Map<PlayerId, Game.PlayerDescription> playerDescriptionMap = new HashMap<>();

        for (String pName : parameters.getUnnamed()){
            int index = parameters.getUnnamed().indexOf(pName);
            PlayerId playerId = PlayerId.ALL.get(index);
            Game.PlayerDescription playerDescription;
            if (pName.charAt(0) == '_'){
                playerDescription = new Game.PlayerDescription(playerId, pName.substring(1), AI);
            }
            else {
                playerDescription = new Game.PlayerDescription(playerId, pName, HUMAN);
            }
            playerDescriptionMap.put(playerId, playerDescription);
        }

        //Génération de la seed
        String seedString = parameters.getNamed().get("seed");
        byte[] seedBytes = seedString != null
                ? seedString.getBytes(StandardCharsets.UTF_8)
                : SecureRandom.getSeed(8);

        RandomGenerator rng = RandomGeneratorFactory.getDefault().create(seedBytes);

        //Construction de l'interface graphique
        int playersCount = parameters.getUnnamed().size();
        Game game = new Game(playerDescriptionMap.values().stream().toList());

        ImmutableGameState initialGameState = ImmutableGameState.initial(game);

        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        Set<Move> validMoves = new HashSet<>();
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
            MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game));
            gameState.fillFactories(rng);
            ImmutableGameState immutableGameState =
                    gameState.immutable();
            Platform.runLater(() -> gameStateP.set(immutableGameState));

            // Créer les instances MctsPlayer pour les joueurs IA
            Map<PlayerId, Player> aiPlayers = new HashMap<>();
            for (int i = 0; i < playersCount; i++) {
                if (playerDescriptionMap.get(PlayerId.ALL.get(i)).kind() == Game.PlayerDescription.PlayerKind.AI) {
                    aiPlayers.put(
                            PlayerId.ALL.get(i),
                            new MctsPlayer(RandomGeneratorFactory.getDefault(), 100000));
                }
            }

            while (!gameState.isGameOver()) {
                PlayerId current = gameState.currentPlayerId();
                Game.PlayerDescription.PlayerKind kind = playerDescriptionMap.get(current).kind();

                Move move;
                if (kind == Game.PlayerDescription.PlayerKind.HUMAN) {
                   // move = MctsPlayer(playerName, gameState);
                } else {
                   // move = queryAiMove(playerName, gameState, aiPlayers);
                }

                gameState.registerMove(move.packed());
                if (gameState.isRoundOver()) {
                    //Sleep
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    gameState.endRound();
                    if (!gameState.isGameOver()) {
                        //Sleep
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        gameState.fillFactories(rng);
                    }
                }
            }

            gameState.endGame();

        });

        /*Platform.runLater(() -> {
            MutableGameState gameState =
                    new MutableGameState(initialGameState);
            gameState.fillFactories(rng);
            ImmutableGameState immutableGameState =
                    gameState.immutable();

            short[] validMovesArray = new short[Move.MAX_MOVES];
            int count = immutableGameState.validMoves(validMovesArray);
            for (int i = 0; i < count; i++) {
                validMoves.add(Move.ofPacked(validMovesArray[i]));
            }

            gameStateP.set(immutableGameState);
        });

         */

        root.getStylesheets().add("ajul.css");
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);
        primaryStage.show();


    }


}


