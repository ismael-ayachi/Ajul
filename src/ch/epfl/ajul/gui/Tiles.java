package ch.epfl.ajul.gui;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.TileKind;
import javafx.scene.Node;

import java.util.List;
import java.util.Map;

public record Tiles(Map<TileLocation, Node> anchors, Map<TileKind, List<Node>> tiles) {
    public static final int TILE_WIDTH = 30;
    public static final int TILE_HEIGHT = 30;

    public static Tiles create(Game game) {

    }

    public static TileLocation location(Node node){
        return (TileLocation) node.getUserData();
    }

    public static void setLocation(Node node, TileLocation location){
        node.setUserData(location);
    }
}
