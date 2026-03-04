package ch.epfl.ajul;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class Game {
    private final List<PlayerDescription> playerDescriptions;


    public record PlayerDescription(PlayerId id, String name, PlayerKind kind){
        public enum PlayerKind{
            HUMAN, AI;
        }

        public PlayerDescription {
            requireNonNull(id);
            requireNonNull(name);
            requireNonNull(kind);
        }
    }


    public Game(List<PlayerDescription> playerDescriptions){
        Preconditions.checkArgument((playerDescriptions.size() >= 2) && (playerDescriptions.size() <= 4));
        for (int i=0; i < playerDescriptions.size(); i++) {
            Preconditions.checkArgument(playerDescriptions.get(i).id == PlayerId.ALL.get(i) );
        }
        this.playerDescriptions = List.copyOf(playerDescriptions);

    }

    public List<PlayerDescription> playerDescriptions(){
        return playerDescriptions;
    }

    public List<PlayerId> playerIds() {
        int playerDescriptionSize = playerDescriptions.size();
        return List.copyOf(PlayerId.ALL.subList(0, playerDescriptionSize));
    }

    public int playersCount() {
        return playerDescriptions.size();
    }

    public List<TileSource.Factory> factories() {
        return List.copyOf(TileSource.Factory.ALL.subList(0, factoriesCount()));
    }

    public int factoriesCount() {
        return 2*playerDescriptions.size() + 1;
    }

    public List<TileSource> tileSources() {
        return List.copyOf(TileSource.ALL.subList(0, factoriesCount() + 1));

    }
    public int tileSourcesCount() {
        return factoriesCount() + 1;
    }

    public int centralAreaMaxSize() {
        return 3*factoriesCount() + 1;
    }



}
