package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.PkWall;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Tiles(Map<TileLocation, Node> anchors, Map<TileKind, List<Node>> tiles) {
    public static final int TILE_WIDTH = 30;
    public static final int TILE_HEIGHT = 30;

    public static Tiles create(Game game) {
        Map<TileLocation, Node> anchors = new HashMap<>(); //Map immuable à ajouter ?
        putInAnchor(anchors, game);
        Map<TileKind, List<Node>> tiles = new HashMap<>(); //Map immuable à ajouter ?
        putInTiles(tiles);
        return new Tiles(anchors, tiles);
    }

    public static TileLocation location(Node node){
        return (TileLocation) node.getUserData();
    }

    public static void setLocation(Node node, TileLocation location){
        node.setUserData(location);
    }

    private static Node createTile(TileKind tileKind){
        if (tileKind instanceof TileKind.Colored){
            Rectangle coloredTile = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
            coloredTile.getStyleClass().add("tile");
            coloredTile.getStyleClass().add(tileKind.toString());
            return coloredTile;
        }
        else {
            Rectangle marker = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
            marker.getStyleClass().add("tile");
            StackPane markerTile = new StackPane(marker, new Text("1"));
            markerTile.setId("first-player-marker");
            return markerTile;
        }
    }

    private static Node createAnchor() {
        Rectangle anchor = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
        anchor.getStyleClass().addAll("tile", "anchor");
        return anchor;
    }

    private static void putInTiles(Map<TileKind, List<Node>> tiles){
        for (TileKind tileKind : TileKind.ALL){
            List<Node> tileNodes = new ArrayList<>();
            for (int i = 0; i < tileKind.tilesCount(); i++){
                tileNodes.add(createTile(tileKind));
            }
            tiles.put(tileKind, tileNodes);
        }
    }

    private static void putInAnchor(Map<TileLocation, Node> anchors, Game game) {

        Stream<TileLocation> sourceStream = game.tileSources().stream()
                .flatMap(s -> IntStream.range(0, s instanceof TileSource.Factory
                                ? TileSource.Factory.TILES_PER_FACTORY
                                : game.centralAreaMaxSize())
                        .mapToObj(i -> new TileLocation.OnSource(s, i)));

        Stream<TileLocation> patternStream = game.playerIds().stream()
                .flatMap(p -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, line.capacity())
                                .mapToObj(i -> new TileLocation.OnPattern(p, line, i))));

        Stream<TileLocation> floorStream = game.playerIds().stream()
                .flatMap(p -> IntStream.range(0, TileDestination.FLOOR.capacity())
                        .mapToObj(i -> new TileLocation.OnFloor(p, i)));

        Stream<TileLocation> wallStream = game.playerIds().stream()
                .flatMap(p -> TileDestination.Pattern.ALL.stream()
                        .flatMap(line -> IntStream.range(0, PkWall.WALL_WIDTH)
                                .mapToObj(col -> new TileLocation.OnWall(p, line, PkWall.colorAt(line, col)))));

        Stream.of(sourceStream, patternStream, floorStream, wallStream)
                .flatMap(s -> s)
                .forEach(loc -> anchors.put(loc, createAnchor()));

    }
}
