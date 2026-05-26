package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.mcts.MctsPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.AI;
import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

/// Programme principal de l'application Ajul : hérite de {@link Application} et met en
/// œuvre sa méthode {@code start}, qui analyse les arguments passés au programme,
/// construit l'interface graphique et démarre le fil de gestion de la partie.
///
/// @author Ismaël Ayachi (393163)
public final class Main extends Application {

    private static final int ITERATION_COUNT = 100000;

    /// Lance l'application JavaFX.
    ///
    /// @param args les arguments passés au programme : les noms des joueurs (préfixés
    ///             de {@code _} pour une IA) et éventuellement une graine pour le
    ///             générateur aléatoire via {@code --seed=...}
    public static void main(String[] args) {
        launch(args);
    }

    /// Analyse les arguments passés au programme, construit l'interface graphique
    /// puis démarre le fil chargé de la gestion de la partie.
    ///
    /// @param primaryStage la fenêtre principale fournie par JavaFX
    @Override
    public void start(Stage primaryStage) {

        //Analyse des arguments
        Parameters parameters = getParameters();
        List<Game.PlayerDescription> playerDescriptions = new ArrayList<>();
        List<String> names = parameters.getUnnamed();
        for (int i = 0; i < names.size(); i++) {
            String pName = names.get(i);
            PlayerId playerId = PlayerId.ALL.get(i);
            if (pName.startsWith("_")) {
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
        Game game = new Game(playerDescriptions);
        ImmutableGameState initialGameState = ImmutableGameState.initial(game);
        ObjectProperty<ImmutableGameState> gameStateP = new SimpleObjectProperty<>(initialGameState);
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<>());
        BlockingQueue<Move> moveQueue = new SynchronousQueue<>();

        Tiles tiles = Tiles.create(game);
        TileOverlayUI tileOverlayUI = TileOverlayUI.create(gameStateP, tiles, validMoves, potentialMoves, moveAccepted);
        BoardUI boardUI = BoardUI.create(tiles.anchors(), gameStateP, potentialMoves, moveAccepted, moveQueue);
        Parent root = new StackPane(boardUI.root(), tileOverlayUI.root());

        root.getStylesheets().add("ajul.css");
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Ajul");
        primaryStage.setResizable(false);
        primaryStage.show();

        //Fil gérant l'exécution d'une partie complète
        Thread.startVirtualThread(() -> {
            PointsObserver pointsObserver = createPointsObserver(tileOverlayUI, boardUI);
            MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game), pointsObserver);
            gameState.fillFactories(rng);
            updateGameStateP(gameStateP, gameState.immutable());
            // Créer les instances MctsPlayer pour les joueurs IA
            int playersCount = parameters.getUnnamed().size();
            Map<PlayerId, Player> aiPlayers = new HashMap<>();
            for (int i = 0; i < playersCount; i++) {
                if (playerDescriptions.get(i).kind() == Game.PlayerDescription.PlayerKind.AI) {
                    aiPlayers.put(
                            PlayerId.ALL.get(i),
                            new MctsPlayer(RandomGeneratorFactory.getDefault(), ITERATION_COUNT));
                }
            }

            //Boucle principale qui gère le déroulement d'une partie
            while (!gameState.isGameOver()) {
                PlayerId current = gameState.currentPlayerId();
                Move move = game.playerDescriptions().get(current.ordinal()).kind() == HUMAN
                        ? playHumanMove(gameState, validMoves, moveQueue)
                        : aiPlayers.get(current).nextMove(gameState);
                gameState.registerMove(move.packed());
                updateGameStateP(gameStateP, gameState.immutable());
                if (gameState.isRoundOver()) {
                    pause(1);
                    gameState.endRound();
                    if (!gameState.isGameOver()) {
                        updateGameStateP(gameStateP, gameState.immutable());
                        pause(0.7);
                        gameState.fillFactories(rng);
                    }
                    updateGameStateP(gameStateP, gameState.immutable());
                }
            }
            gameState.endGame();
        });

    }

    private static Move playHumanMove(ReadOnlyGameState gameState,
                                      Set<Move> validMoves, BlockingQueue<Move> moveQueue) {
        validMoves.clear();
        short[] validMovesArray = new short[Move.MAX_MOVES];
        int count = gameState.validMoves(validMovesArray);
        for (int i = 0; i < count; i++)
            validMoves.add(Move.ofPacked(validMovesArray[i]));
        try {
            Move move = moveQueue.take();
            validMoves.clear();
            return move;
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private static void pause(double duration){
        try {
            Thread.sleep((long) (duration * 1e3));
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private static void updateGameStateP(ObjectProperty<ImmutableGameState> gameStateP,
                                         ImmutableGameState immutableGameState) {
        Platform.runLater(() -> gameStateP.set(immutableGameState));
    }

    private static PointsObserver createPointsObserver(TileOverlayUI tileOverlayUI, BoardUI boardUI){
        return new PointsObserver() {
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

    }
}