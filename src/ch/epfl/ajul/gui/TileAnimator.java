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

    /// Calcule les animations déplaçant les tuiles vers leur nouvel emplacement lors
    /// d'un changement d'état, d'une manière conforme aux règles du jeu et agréable à l'œil.
    ///
    /// @author Ismaël Ayachi (393163)
    public final class TileAnimator {

        private static final Duration TRANSITION_DURATION = Duration.seconds(0.5);

        /// Répartition d'emplacements de tuiles par type de destination, utilisée pour
        /// distinguer l'offre (tuiles à déplacer) de la demande (emplacements à pourvoir).
        ///
        /// @param wall     les emplacements sur le mur
        /// @param pattern  les emplacements sur les lignes de motif
        /// @param floor    les emplacements sur le plancher
        /// @param source   les emplacements sur les sources
        /// @param offBoard les emplacements hors plateau
        private record Partition(
                List<TileLocation> wall,
                List<TileLocation> pattern,
                List<TileLocation> floor,
                List<TileLocation> source,
                List<TileLocation> offBoard) {

            /// Retourne une partition vide, dont chaque catégorie est une liste modifiable vide.
            ///
            /// @return une partition vide
            static Partition empty() {
                return new Partition(new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }

            /// Retourne tous les emplacements de cette partition réunis en une seule liste.
            ///
            /// @return la concaténation de toutes les catégories
            List<TileLocation> toList() {
                List<TileLocation> all = new ArrayList<>();
                all.addAll(wall()); all.addAll(pattern());
                all.addAll(floor()); all.addAll(source()); all.addAll(offBoard());
                return all;
            }
        }

        /// Retourne une animation déplaçant chaque tuile de sa position actuelle vers la
        /// position qu'elle doit occuper dans l'état {@code gameState}, en appariant les
        /// tuiles disponibles (offre) aux emplacements à pourvoir (demande).
        ///
        /// @param position fonction donnant la position à l'écran d'un emplacement de tuile
        /// @param tiles    la table associant chaque sorte de tuile à ses nœuds représentants
        /// @param gameState l'état de la partie vers lequel animer les tuiles
        /// @return l'animation parallèle déplaçant les tuiles
        public static Animation animateTiles(Function<TileLocation, Point2D> position,
                                             Map<TileKind, List<Node>> tiles,
                                             ReadOnlyGameState gameState) {

            ParallelTransition parallelTransition = new ParallelTransition();

            tiles.forEach((tileKind, nodes) -> {

                Partition demand = Partition.empty();
                Partition supply = Partition.empty();

                // Calcul de la demande
                int sourceIndex = 0;
                for (int pkTileSource : gameState.pkTileSources().toArray()) {
                    int tilesCount = PkTileSet.countOf(pkTileSource, tileKind);
                    TileSource tileSource = TileSource.ALL.get(sourceIndex);
                    int offset = 0;
                    for (TileKind tileKind1 : TileKind.ALL){
                        if (tileKind1 == tileKind) break;
                        offset += PkTileSet.countOf(pkTileSource, tileKind1);
                    }
                    for (int j = 0; j < tilesCount; j++) {
                        demand.source().add(new TileLocation.OnSource(tileSource, offset + j));
                    }
                    sourceIndex++;
                }

                // Tuiles des joueurs
                for (PlayerId playerId : gameState.playerIds()) {
                    // Plancher : uniquement les tuiles du tileKind actuel
                    int floor = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), playerId);
                    for (int i = 0; i < PkFloor.size(floor); i++) {
                        if (PkFloor.tileAt(floor, i) == tileKind) {
                            demand.floor().add(new TileLocation.OnFloor(playerId, i));
                        }
                    }

                    if (tileKind instanceof TileKind.Colored colored) {
                        // Lignes de motifs : uniquement si la couleur correspond
                        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), playerId);
                        for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                            if (PkPatterns.canContain(pattern, line, colored))
                                for (int i = 0; i < PkPatterns.size(pattern, line); i++)
                                    demand.pattern().add(new TileLocation.OnPattern(playerId, line, i));
                        }

                        // Mur : uniquement la case de cette couleur
                        int wall = PkPlayerStates.pkWall(gameState.pkPlayerStates(), playerId);
                        for (TileDestination.Pattern line : TileDestination.Pattern.ALL)
                            if (PkWall.hasTileAt(wall, line, colored))
                                demand.wall().add(new TileLocation.OnWall(playerId, line, colored));
                    }
                }

                // OffBoard : uniquement les tuiles du tileKind courant
                int pkOffBoard = PkTileSet.union(gameState.pkTileBag(), gameState.pkDiscardedTiles());
                int tilesCount = PkTileSet.countOf(pkOffBoard, tileKind);
                for (int i = 0; i < tilesCount; i++) {
                    demand.offBoard().add(new TileLocation.OffBoard(tileKind, i));
                }

                // Calcul de l'offre pour le tileKind actuel
                Map<TileLocation, Node> nodeAtLocation = new HashMap<>();
                for (Node n : nodes) nodeAtLocation.put(Tiles.location(n), n);
                for (Node node : nodes) {
                    TileLocation loc = Tiles.location(node);
                    switch (loc) {
                        case TileLocation.OnWall onWall ->
                        { if (!demand.wall().remove(onWall)) supply.wall().add(onWall); }
                        case TileLocation.OnPattern onPattern ->
                        { if (!demand.pattern().remove(onPattern)) supply.pattern().add(onPattern); }
                        case TileLocation.OnFloor onFloor ->
                        { if (!demand.floor().remove(onFloor)) supply.floor().add(onFloor); }
                        case TileLocation.OnSource onSource ->
                        { if (!demand.source().remove(onSource)) supply.source().add(onSource); }
                        case TileLocation.OffBoard offBoard ->
                        { if (!demand.offBoard().remove(offBoard)) supply.offBoard().add(offBoard); }
                        default -> {}
                    }
                }

                // Algorithme d'appariement
                Map<TileLocation, TileLocation> pairings = new HashMap<>();

                // 1. Appariement mur → ligne de motif
                for (TileLocation loc : new ArrayList<>(demand.wall())) {
                    TileLocation.OnWall wallDemand = (TileLocation.OnWall) loc;
                    TileLocation.OnPattern match = supply.pattern().stream()
                            .map(p -> (TileLocation.OnPattern) p)
                            .filter(p -> p.playerId() == wallDemand.playerId()
                                    && p.pattern() == wallDemand.pattern())
                            .min(Comparator.comparingInt(TileLocation.OnPattern::index))
                            .orElseThrow();
                    pairings.put(match, wallDemand);
                    supply.pattern().remove(match);
                    demand.wall().remove(loc);
                }

                // Trier l'offre source
                supply.source().sort(
                        Comparator.comparingInt((TileLocation s) -> ((TileLocation.OnSource) s).tileSource().index())
                                .reversed()
                                .thenComparingInt(s -> ((TileLocation.OnSource) s).index()));

                // 2–5. Appariements par priorité décroissante
                pairings.putAll(matchSupplyToDemand(supply.source(), demand.floor()));
                pairings.putAll(matchSupplyToDemand(supply.source(), demand.pattern()));
                pairings.putAll(matchSupplyToDemand(supply.floor(),  demand.offBoard()));
                pairings.putAll(matchSupplyToDemand(supply.source(), demand.offBoard()));

                // 6. Appariement quelconque du reste
                List<TileLocation> remainingSupply = supply.toList();
                List<TileLocation> remainingDemand = demand.toList();

                for (int i = 0; i < remainingSupply.size(); i++) {
                    pairings.put(remainingSupply.get(i), remainingDemand.get(i));
                }

                // Ensuite, crée les animations et mets à jour les locations
                for (Map.Entry<TileLocation, TileLocation> entry : pairings.entrySet()) {
                    TileLocation supplyLoc = entry.getKey();
                    TileLocation demandLoc = entry.getValue();

                    Node node = nodeAtLocation.get(supplyLoc);
                    Point2D destination = position.apply(demandLoc);

                    // Mettre à jour la location
                    Tiles.setLocation(node, demandLoc);

                    // Créer et ajouter l'animation
                    parallelTransition.getChildren().add(
                            new RelocationTransition(node, destination, TRANSITION_DURATION));
                }
            });
            return parallelTransition;
    }

        /// Apparie au maximum {@code min(supply.size(), demand.size())} éléments entre
        /// {@code supply} et {@code demand}, en retirant les éléments consommés des deux listes,
        /// et retourne les paires formées (clé = offre, valeur = demande).
        private static Map<TileLocation, TileLocation> matchSupplyToDemand(
                List<TileLocation> supply, List<TileLocation> demand) {
            Map<TileLocation, TileLocation> pairs = new HashMap<>();
            int count = Math.min(supply.size(), demand.size());
            for (int i = 0; i < count; i++)
                pairs.put(supply.removeFirst(), demand.removeFirst());
            return pairs;
        }
}