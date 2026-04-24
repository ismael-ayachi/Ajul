package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.Player;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ImmutableGameState;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        HBox hbox = new HBox();
        GridPane grid = new GridPane();
        grid.setId("tile-sources");
        hbox.getChildren().add(grid);

        Tiles tiles = Tiles.create(observer.getValue().game());

        HBox factoryBox = new HBox();
        factoryBox.getStyleClass().addAll("tile-group", "tile-source");

        List<TileLocation.OnSource> sourcesLocation =
                IntStream.range(1, TileSource.Factory.COUNT + 1)
                        .mapToObj(i -> new TileLocation.OnSource(TileSource.Factory.FACTORY_1, i)).toList();

        sourcesLocation.forEach(v -> factoryBox.getChildren().add(tiles.anchors().get(v)));

        GridPane centerAreaGrid = new GridPane();
        centerAreaGrid.getStyleClass().addAll("tile-group", "tile-source");

        TileLocation centerArea = new TileLocation.OnSource(TileSource.CenterArea.CENTER_AREA, 0);
        centerAreaGrid.getChildren().add(tiles.anchors().get(centerArea));

        return new BoardUI(anchors, observer, hbox);

    }

    public Node root(){
        return root;
    }

    public void showBonusPoints(PlayerId playerId, Object bonusKey){

    }

}
