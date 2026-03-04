package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;

import java.util.Objects;

/// Enregistrement représentant un coup joué dans une partie d'Ajul.
///
/// Un coup est défini par une source, une couleur de tuile et une destination.
/// Aucun des attributs ne peut être {@code null}.
///
/// @param source
///        la source depuis laquelle les tuiles jouées sont obtenues
/// @param tileColor
///        la couleur des tuiles jouées
/// @param destination
///        la destination sur laquelle les tuiles jouées sont placées
/// @author Ismaël Ayachi (393163)
public record Move(TileSource source, TileKind.Colored tileColor, TileDestination destination) {

    /// Le nombre maximum de coups entre lesquels un joueur peut avoir à choisir,
    /// calculé comme le produit du nombre de fabriques, du nombre de couleurs de tuiles
    /// et du nombre de destinations.
    public static final int MAX_MOVES = TileSource.Factory.COUNT * (TileSource.Factory.TILES_PER_FACTORY) * TileDestination.COUNT;

    /// Constructeur compact vérifiant qu'aucun des attributs n'est {@code null}.
    ///
    /// @throws NullPointerException
    ///         si {@code source}, {@code tileColor} ou {@code destination} est {@code null}
    public Move {
        Objects.requireNonNull(source);
        Objects.requireNonNull(tileColor);
        Objects.requireNonNull(destination);
    }

    /// Retourne le coup correspondant au coup empaqueté donné.
    ///
    /// @param pkMove
    ///        le coup empaqueté
    /// @return le coup correspondant au coup empaqueté donné
    public static Move ofPacked(short pkMove) {
        TileSource pkMoveSource = PkMove.source(pkMove);
        TileKind.Colored pkMoveColor = PkMove.color(pkMove);
        TileDestination pkMoveDestination = PkMove.destination(pkMove);
        return new Move(pkMoveSource, pkMoveColor, pkMoveDestination);
    }

    /// Retourne le coup empaqueté correspondant à ce coup.
    ///
    /// @return le coup empaqueté correspondant au récepteur
    public short packed() {
        return PkMove.pack(source, tileColor, destination);
    }
}