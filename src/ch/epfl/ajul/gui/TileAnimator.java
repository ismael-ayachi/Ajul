package ch.epfl.ajul.gui;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.*;
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


            ParallelTransition parallelTransition = new ParallelTransition();

            for (TileKind tileKind : TileKind.ALL) {

                Partition demand = new Partition(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>());

                Partition supply = new Partition(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>());

                // Calcul de la demande pour ce tileKind uniquement
                int sourceIndex = 0;
                for (int pkTileSource : gameState.pkTileSources().toArray()) {
                    int tilesCount = PkTileSet.countOf(pkTileSource, tileKind);
                    TileSource tileSource = TileSource.ALL.get(sourceIndex);
                    for (int j = 0; j < tilesCount; j++) {
                        demand.source().add(new TileLocation.OnSource(tileSource, j));
                    }
                    sourceIndex++;
                }

                for (PlayerId playerId : gameState.playerIds()) {
                    for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), playerId);

                        if (tileKind instanceof TileKind.Colored colored) {
                            // Ligne de motif : uniquement si la couleur correspond
                            if (PkPatterns.size(pattern, line) > 0
                                    && PkPatterns.color(pattern, line).equals(colored)) {
                                for (int i = 0; i < PkPatterns.size(pattern, line); i++) {
                                    demand.pattern().add(new TileLocation.OnPattern(playerId, line, i));
                                }
                            }

                            // Mur : uniquement la case de cette couleur
                            int wall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
                            if (PkWall.hasTileAt(wall, line, colored)) {
                                demand.wall().add(new TileLocation.OnWall(playerId, line, colored));
                            }
                        }
                    }

                    // Plancher : uniquement les tuiles du tileKind courant
                    int floor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), playerId);
                    for (int i = 0; i < PkFloor.size(floor); i++) {
                        if (PkFloor.tileAt(floor, i).equals(tileKind)) {
                            demand.floor().add(new TileLocation.OnFloor(playerId, i));
                        }
                    }
                }

                // OffBoard : uniquement les tuiles du tileKind courant
                int pkOffBoard = PkTileSet.union(gameState.pkTileBag(), gameState.pkDiscardedTiles());
                int tilesCount = PkTileSet.countOf(pkOffBoard, tileKind);
                for (int i = 0; i < tilesCount; i++) {
                    demand.offBoard().add(new TileLocation.OffBoard(tileKind, i));
                }

                // Calcul de l'offre pour ce tileKind uniquement
                for (Node node : tiles.get(tileKind)) {
                    TileLocation loc = Tiles.location(node);
                    switch (loc) {
                        case TileLocation.OnWall onWall -> {
                            if (demand.wall().contains(onWall)) demand.wall().remove(onWall);
                            else supply.wall().add(onWall);
                        }
                        case TileLocation.OnPattern onPattern -> {
                            if (demand.pattern().contains(onPattern)) demand.pattern().remove(onPattern);
                            else supply.pattern().add(onPattern);
                        }
                        case TileLocation.OnFloor onFloor -> {
                            if (demand.floor().contains(onFloor)) demand.floor().remove(onFloor);
                            else supply.floor().add(onFloor);
                        }
                        case TileLocation.OnSource onSource -> {
                            if (demand.source().contains(onSource)) demand.source().remove(onSource);
                            else supply.source().add(onSource);
                        }
                        case TileLocation.OffBoard offBoard -> {
                            if (demand.offBoard().contains(offBoard)) demand.offBoard().remove(offBoard);
                            else supply.offBoard().add(offBoard);
                        }
                    }
                }

                // Algorithme d'appariement
                Map<TileLocation, TileLocation> pairings = new HashMap<>();

                // 1. Appariement mur → ligne de motif
                for (TileLocation.OnWall wallDemand : new ArrayList<>(demand.wall())) {
                    TileLocation.OnPattern match = supply.pattern().stream()
                            .filter(p -> p.playerId().equals(wallDemand.playerId())
                                    && p.pattern().equals(wallDemand.pattern()))
                            .min(Comparator.comparingInt(TileLocation.OnPattern::index))
                            .orElseThrow();
                    pairings.put(match, wallDemand);
                    supply.pattern().remove(match);
                    demand.wall().remove(wallDemand);
                }

                // Trier l'offre source
                supply.source().sort(
                        Comparator.comparingInt(
                                        (TileLocation.OnSource s) -> s.tileSource().index()).reversed()
                                .thenComparingInt(TileLocation.OnSource::index));

                // 2. Appariement plancher/source
                int count2 = Math.min(demand.floor().size(), supply.source().size());
                for (int i = 0; i < count2; i++) {
                    TileLocation.OnFloor floorDemand = demand.floor().removeFirst();
                    TileLocation.OnSource match = supply.source().removeFirst();
                    pairings.put(match, floorDemand);
                }

                // 3. Appariement ligne de motif/source
                int count3 = Math.min(demand.pattern().size(), supply.source().size());
                for (int i = 0; i < count3; i++) {
                    TileLocation.OnPattern patternDemand = demand.pattern().removeFirst();
                    TileLocation.OnSource match = supply.source().removeFirst();
                    pairings.put(match, patternDemand);
                }

                // 4. Appariement OffBoard/plancher restant
                int count4 = Math.min(demand.offBoard().size(), supply.floor().size());
                for (int i = 0; i < count4; i++) {
                    TileLocation.OffBoard offBoardDemand = demand.offBoard().removeFirst();
                    TileLocation.OnFloor match = supply.floor().removeFirst();
                    pairings.put(match, offBoardDemand);
                }

                // 5. Appariement OffBoard/source restante
                int count5 = Math.min(demand.offBoard().size(), supply.source().size());
                for (int i = 0; i < count5; i++) {
                    TileLocation.OffBoard offBoardDemand = demand.offBoard().removeFirst();
                    TileLocation.OnSource match = supply.source().removeFirst();
                    pairings.put(match, offBoardDemand);
                }

                // 6. Appariement quelconque du reste
                List<TileLocation> remainingSupply = new ArrayList<>();
                remainingSupply.addAll(supply.wall());
                remainingSupply.addAll(supply.pattern());
                remainingSupply.addAll(supply.floor());
                remainingSupply.addAll(supply.source());
                remainingSupply.addAll(supply.offBoard());

                List<TileLocation> remainingDemand = new ArrayList<>();
                remainingDemand.addAll(demand.wall());
                remainingDemand.addAll(demand.pattern());
                remainingDemand.addAll(demand.floor());
                remainingDemand.addAll(demand.source());
                remainingDemand.addAll(demand.offBoard());

                for (int i = 0; i < remainingSupply.size(); i++) {
                    pairings.put(remainingSupply.get(i), remainingDemand.get(i));
                }

                // Construction de l'animation
                for (Map.Entry<TileLocation, TileLocation> entry : pairings.entrySet()) {
                    TileLocation supplyLoc = entry.getKey();
                    TileLocation demandLoc = entry.getValue();

                    Node node = tiles.get(tileKind).stream()
                            .filter(n -> Tiles.location(n).equals(supplyLoc))
                            .findFirst()
                            .orElseThrow();

                    Point2D destination = position.apply(demandLoc);
                    parallelTransition.getChildren().add(
                            new RelocationTransition(node, destination, Duration.millis(500)));
                    Tiles.setLocation(node, demandLoc);
                }
            }

            return parallelTransition;

    }
}
