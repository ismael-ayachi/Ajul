package ch.epfl.ajul.gui;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import javafx.animation.Animation;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class TileAnimator {

    private record Partition(
            List<TileLocation.OnWall> wall,
            List<TileLocation.OnPattern> pattern,
            List<TileLocation.OnFloor> floor,
            List<TileLocation.OnSource> source,
            List<TileLocation.OffBoard> offBoard
    ) {}

    public static Animation animateTiles(Function<TileLocation, Point2D> position,
                                         Map<TileKind, List<Node>> tiles,
                                         ReadOnlyGameState gameState) {


        Partition demand = new Partition(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());


        int sourceIndex = 0;
        for (int pkTileSource : gameState.pkTileSources().toArray()){
            sourceIndex++;
            for (TileKind tileKind: TileKind.ALL){
                int tilesCount = PkTileSet.countOf(pkTileSource, tileKind);
                TileSource tileSource = TileSource.ALL.get(sourceIndex);
                for (int j = 0;  j < tilesCount; j++){
                    demand.source.add(new TileLocation.OnSource(tileSource, j));
                }



            }

        }
    }


}
