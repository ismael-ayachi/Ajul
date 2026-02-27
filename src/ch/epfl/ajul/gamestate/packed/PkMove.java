package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;

/// Classe utilitaire permettant de manipuler des coups empaquetés dans des valeurs de type {@code short}.
///
/// Un coup empaqueté est un entier de type {@code short} dont les bits sont organisés comme suit :
/// - bits 0 à 3 (4 bits) : index de la source
/// - bits 4 à 6 (3 bits) : index de la couleur
/// - bits 7 à 9 (3 bits) : index de la destination
/// - bits 10 à 15 (6 bits) : toujours 0
///
/// Cette classe n'est pas instanciable.
///
/// @author Ismaël Ayachi (393163)
public final class PkMove {
    private static final int SOURCE_OFFSET = 0;
    private static final int SOURCE_BITS = 4;
    private static final int SOURCE_MASK = (1 << SOURCE_BITS) - 1;

    private static final int COLOR_OFFSET = SOURCE_OFFSET + SOURCE_BITS;
    private static final int COLOR_BITS  = 3;
    private static final int COLOR_MASK = (1 << COLOR_BITS) - 1;

    private static final int DESTINATION_OFFSET = COLOR_OFFSET + COLOR_BITS;
    private static final int DESTINATION_BITS = 3;
    private static final int DESTINATION_MASK = (1 << DESTINATION_BITS) - 1;

    /// Empaquète un coup dans un entier de type {@code short}.
    ///
    /// @param source
    ///        la source depuis laquelle les tuiles jouées sont obtenues
    /// @param color
    ///        la couleur des tuiles jouées
    /// @param destination
    ///        la destination sur laquelle les tuiles jouées sont placées
    /// @return le coup empaqueté correspondant aux paramètres donnés
    public static short pack(TileSource source, TileKind.Colored color, TileDestination destination) {
        short pkMove = (short) ((TileSource.ALL.indexOf(source) & SOURCE_MASK) << SOURCE_OFFSET
                | ((TileKind.Colored.ALL.indexOf(color) & COLOR_MASK) << COLOR_OFFSET)
                | ((TileDestination.ALL.indexOf(destination) & DESTINATION_MASK) << DESTINATION_OFFSET));
        return pkMove;
    }

    /// Retourne la source du coup empaqueté donné.
    ///
    /// @param pkMove
    ///        le coup empaqueté
    /// @return la source correspondant au coup empaqueté
    public static TileSource source(short pkMove) {
        int sourceIndex = (pkMove >> SOURCE_OFFSET) & SOURCE_MASK;
        return TileSource.ALL.get(sourceIndex);
    }

    /// Retourne la couleur des tuiles du coup empaqueté donné.
    ///
    /// @param pkMove
    ///        le coup empaqueté
    /// @return la couleur correspondant au coup empaqueté
    public static TileKind.Colored color(short pkMove) {
        int colorIndex = (pkMove >> COLOR_OFFSET) & COLOR_MASK;
        return TileKind.Colored.ALL.get(colorIndex);
    }

    /// Retourne la destination du coup empaqueté donné.
    ///
    /// @param pkMove
    ///        le coup empaqueté
    /// @return la destination correspondant au coup empaqueté
    public static TileDestination destination(short pkMove) {
        int destinationIndex = (pkMove >> DESTINATION_OFFSET) & DESTINATION_MASK;
        return TileDestination.ALL.get(destinationIndex);
    }
}