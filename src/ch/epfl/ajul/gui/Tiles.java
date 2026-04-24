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
        Map<TileLocation, Node> newAnchors = new HashMap<>();
        putInAnchor(newAnchors, game);
        Map<TileKind, List<Node>> newTiles = new HashMap<>();
        putInTiles(newTiles);
        return new Tiles(newAnchors, newTiles);
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
            StackPane markerTile = new StackPane(new Rectangle(TILE_WIDTH, TILE_HEIGHT), new Text("1"));
            markerTile.setId("first-player-marker");
            markerTile.getStyleClass().add("tile");
            return markerTile;
        }
    }

    private static Node createAnchor(TileLocation tileLocation) {
        Rectangle anchor = new Rectangle(TILE_WIDTH, TILE_HEIGHT);
        anchor.getStyleClass().add("tile");
        anchor.getStyleClass().add("anchor");
        setLocation(anchor, tileLocation);
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

        Stream<TileLocation> offBoardStream = TileKind.ALL.stream()
                .flatMap(tileKind -> IntStream.range(0, tileKind.tilesCount())
                        .mapToObj(i -> new TileLocation.OffBoard(tileKind, i)));

        Stream.of(sourceStream, patternStream, floorStream, wallStream, offBoardStream)
                .flatMap(s -> s)
                .forEach(loc -> anchors.put(loc, createAnchor(loc)));

     /*
        for (TileSource source : game.tileSources()){
            if (source instanceof TileSource.Factory){
                for (int i = 0; i < TileSource.Factory.TILES_PER_FACTORY; i++){
                    TileLocation location = new TileLocation.OnSource(source, i);
                    anchors.put(location, createAnchor(location));
                }

            }
            else {
                for (int i = 0; i < game.centralAreaMaxSize(); i++){
                    TileLocation location = new TileLocation.OnSource(source, i);
                    anchors.put(location, createAnchor(location));
                }
            }
        }


        for (TileDestination destination : TileDestination.ALL){
            if (destination instanceof TileDestination.Pattern){
                for (PlayerId playerId : game.playerIds()){
                    for (int capacity = 0; capacity < destination.capacity(); capacity++){
                        TileLocation location =
                                new TileLocation.OnPattern(playerId, (TileDestination.Pattern) destination, capacity);
                        anchors.put(location, createAnchor(location));
                    }

                }
            }
            else {
                for (PlayerId playerId : game.playerIds()){
                    for (int i = 0; i < destination.capacity(); i++){
                        TileLocation location = new TileLocation.OnFloor(playerId, i);
                        anchors.put(location, createAnchor(location));
                    }
                }
            }
        }

        for (TileDestination.Pattern wallDestination : TileDestination.Pattern.ALL){
            for (PlayerId playerId: game.playerIds()){
                for (int col = 0; col < PkWall.WALL_WIDTH; col++){
                    TileLocation location = new TileLocation.OnWall(playerId, wallDestination, PkWall.colorAt(wallDestination, col));
                    anchors.put(location, createAnchor(location));
                }
            }
        }

        for (TileKind tileKind : TileKind.ALL){
            for (int i = 0; i < tileKind.tilesCount(); i++) {
                TileLocation location = new TileLocation.OffBoard(tileKind, i);
                anchors.put(location, createAnchor(location));
            }
        }
    }


      */

    }
}
