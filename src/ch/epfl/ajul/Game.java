package ch.epfl.ajul;

import java.util.List;

import static java.util.Objects.requireNonNull;

/// Classe représentant une configuration de partie d'Ajul.
///
/// Une partie est définie par une liste de joueurs (entre 2 et 4 inclus), dont les identités
/// doivent correspondre à leur position dans la liste.
///
/// @author Ismaël Ayachi (393163)
public final class Game {

    private final List<PlayerDescription> playerDescriptions;
    private final List<TileSource.Factory> factories;
    private final int playerDescriptionSize;
    private final List<PlayerId> playerIds;
    private final List<TileSource> tileSources;

    /// Enregistrement décrivant un joueur participant à une partie.
    ///
    /// @param id
    ///        l'identité du joueur
    /// @param name
    ///        le nom du joueur
    /// @param kind
    ///        la sorte du joueur (humain ou IA)
    public record PlayerDescription(PlayerId id, String name, PlayerKind kind) {

        /// Type énuméré représentant la sorte d'un joueur.
        public enum PlayerKind {
            /// Joueur humain.
            HUMAN,
            /// Joueur contrôlé par une intelligence artificielle.
            AI
        }

        /// Constructeur compact vérifiant qu'aucun des attributs n'est {@code null}.
        ///
        /// @throws NullPointerException
        ///         si {@code id}, {@code name} ou {@code kind} est {@code null}
        public PlayerDescription {
            requireNonNull(id);
            requireNonNull(name);
            requireNonNull(kind);
        }
    }

    /// Construit une nouvelle configuration de partie pour les joueurs décrits par {@code playerDescriptions}.
    ///
    /// @param playerDescriptions
    ///        la liste des descriptions des joueurs (entre 2 et 4 inclus, dans l'ordre de leur identité)
    /// @throws IllegalArgumentException
    ///         si le nombre de joueurs n'est pas compris entre 2 et 4 inclus, ou si la position
    ///         d'un joueur dans la liste ne correspond pas à son identité
    public Game(List<PlayerDescription> playerDescriptions) {
        Preconditions.checkArgument((playerDescriptions.size() >= 2) && (playerDescriptions.size() <= 4));
        for (int i = 0; i < playerDescriptions.size(); i++) {
            Preconditions.checkArgument(playerDescriptions.get(i).id() == PlayerId.ALL.get(i));
        }
        this.playerDescriptions = List.copyOf(playerDescriptions);
        this.playerDescriptionSize = this.playerDescriptions.size();
        this.factories = List.copyOf(TileSource.Factory.ALL.subList(0, factoriesCount()));
        this.playerIds = List.copyOf(PlayerId.ALL.subList(0, playerDescriptionSize));
        this.tileSources = List.copyOf(TileSource.ALL.subList(0, tileSourcesCount()));
    }

    /// Retourne la liste immuable des descriptions des joueurs de la partie.
    ///
    /// @return la liste des descriptions des joueurs
    public List<PlayerDescription> playerDescriptions() {
        return playerDescriptions;
    }

    /// Retourne la liste immuable des identités des joueurs de la partie,
    /// qui est un préfixe de {@link PlayerId#ALL}.
    ///
    /// @return la liste des identités des joueurs
    public List<PlayerId> playerIds() {
        return playerIds;
    }

    /// Retourne le nombre de joueurs dans la partie.
    ///
    /// @return le nombre de joueurs
    public int playersCount() {
        return playerDescriptionSize;
    }

    /// Retourne la liste immuable des fabriques utilisées dans la partie,
    /// qui est un préfixe de {@link TileSource.Factory#ALL}.
    ///
    /// @return la liste des fabriques
    public List<TileSource.Factory> factories() {
        return factories;
    }

    /// Retourne le nombre de fabriques utilisées dans la partie,
    /// égal à {@code 2 * playersCount() + 1}.
    ///
    /// @return le nombre de fabriques
    public int factoriesCount() {
        return (2 * playerDescriptionSize) + 1 ;
    }

    /// Retourne la liste immuable des sources de tuiles utilisées dans la partie,
    /// qui est un préfixe de {@link TileSource#ALL}.
    ///
    /// @return la liste des sources de tuiles
    public List<TileSource> tileSources() {
        return tileSources;
    }

    /// Retourne le nombre de sources de tuiles dans la partie,
    /// égal à {@code factoriesCount() + 1}.
    ///
    /// @return le nombre de sources de tuiles
    public int tileSourcesCount() {
        return factoriesCount() + 1;
    }

    /// Retourne le nombre maximum de tuiles pouvant se trouver dans la zone centrale
    /// durant la partie, marqueur de premier joueur inclus.
    /// Ce nombre est égal à {@code 3 * factoriesCount() + 1}.
    ///
    /// @return le nombre maximum de tuiles dans la zone centrale
    public int centralAreaMaxSize() {
        return 3 * factoriesCount() + 1;
    }
}