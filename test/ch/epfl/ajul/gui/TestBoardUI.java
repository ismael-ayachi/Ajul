package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;

public final class TestBoardUI extends Application {

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

        ObservableValue<ImmutableGameState> gameStateP =
                new SimpleObjectProperty<>(initialGameState);
        Set<Move> potentialMoves = new HashSet<>();
        boolean[] moveAccepted = new boolean[]{false};
        BlockingQueue<Move> moveQueue = new SynchronousQueue<>();

        Tiles tiles = Tiles.create(game);
        BoardUI boardUI = BoardUI.create(tiles.anchors(),
                gameStateP);

                // ,potentialMoves,
               // moveAccepted,
                //moveQueue);
        Parent root = new StackPane(boardUI.root());
        root.getStylesheets().add("ajul.css");

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(TestBoardUI.class.getSimpleName());
        primaryStage.show();
    }
}
