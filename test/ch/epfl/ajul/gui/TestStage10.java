package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

public final class TestStage10 extends Application {
    @Override
    public void start(Stage primaryStage) {
        int playersCount = 2;
        Game game = new Game(PlayerId.ALL.stream()
                .map(pId ->
                        new Game.PlayerDescription(pId, pId.name(), HUMAN))
                .limit(playersCount)
                .toList());
        ImmutableGameState initialGameState =
                ImmutableGameState.initial(game);

        ObjectProperty<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);

        Set<Move> validMoves = new HashSet<>();
        TileOverlayUI tileOverlayUI =
                TileOverlayUI.create(gameStateP,
                        tiles,
                        validMoves,
                        potentialMoves,
                        moveAccepted);

        Parent root = new StackPane(boardUI.root(),
                tileOverlayUI.root());
        Platform.runLater(() -> {
            MutableGameState gameState =
                    new MutableGameState(initialGameState);
            gameState.fillFactories(RandomGeneratorFactory
                    .getDefault()
                    .create(2026));
            ImmutableGameState immutableGameState =
                    gameState.immutable();
            // TODO remplir `validMoves` avec les coups valides.
            gameStateP.set(immutableGameState);
        });

        root.getStylesheets().add("ajul.css");
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(TestBoardUI.class.getSimpleName());
        primaryStage.show();
    }
}
