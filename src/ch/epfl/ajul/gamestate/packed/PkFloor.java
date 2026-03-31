package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import java.util.ArrayList;

/// Classe utilitaire permettant de manipuler le contenu d'une ligne plancher empaqueté dans une valeur de type {@code int}.
///
/// Une ligne plancher empaquetée est un entier de type {@code int} dont les bits sont organisés comme suit :
/// - bits 0 à 2 (3 bits) : taille de la ligne plancher, comprise entre 0 et 7 (inclus)
/// - bits 3 à 5 (3 bits) : index de la première tuile, ou 0 si la ligne est vide
/// - bits 6 à 8 (3 bits) : index de la deuxième tuile, ou 0 si la ligne contient moins de 2 tuiles
/// - et ainsi de suite pour les 5 tuiles suivantes
/// - bits 24 à 31 (8 bits) : toujours 0
///
/// @author Ismaël Ayachi (393163)
public final class PkFloor {

    private static final int PATTERN_FLOOR_MASK = 0b111;
    private static final int FLOOR_BITS = 3;
    private static final int FLOOR_CAPACITY = TileDestination.FLOOR.capacity() ;

    /// Ligne plancher vide, ne contenant aucune tuile.
    public static final int EMPTY = 0;

    /// Retourne la taille de la ligne plancher empaquetée donnée, c'est-à-dire le nombre de tuiles qu'elle contient.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @return le nombre de tuiles contenues dans la ligne plancher
    public static int size(int pkFloor) {
        return pkFloor & PATTERN_FLOOR_MASK;

    }

    /// Retourne la sorte de tuile se trouvant à l'index {@code i} de la ligne plancher empaquetée donnée.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @param i
    ///        l'index de la tuile (doit être compris entre 0 inclus et la taille de la ligne excluse)
    /// @return la sorte de tuile à l'index donné
    public static TileKind tileAt(int pkFloor, int i) {
        assert isValid(pkFloor, i);
        int extractColor = (pkFloor >> (i + 1) * FLOOR_BITS) & PATTERN_FLOOR_MASK;
        return TileKind.ALL.get(extractColor);
    }

    /// Retourne une ligne plancher empaquetée identique à {@code pkFloor} mais avec les tuiles
    /// de l'ensemble empaqueté {@code pkTileSet} ajoutées par ordre de sorte (A, B, C, D, E,
    /// marqueur de premier joueur). Si la ligne n'a pas la capacité d'accueillir toutes ces tuiles,
    /// les excédentaires sont ignorées, sauf le marqueur de premier joueur qui est toujours ajouté,
    /// en remplaçant au besoin la dernière tuile.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @param pkTileSet
    ///        l'ensemble de tuiles empaqueté à ajouter
    /// @return la ligne plancher empaquetée avec les tuiles ajoutées
    public static int withAddedTiles(int pkFloor, int pkTileSet) {
        int pkFloorSize = size(pkFloor);
        int pkFloorUpdated = pkFloor;

        if (pkFloorSize == FLOOR_CAPACITY) {
            if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 0) {
                return pkFloorUpdated;
            } else {
                pkFloorUpdated &= ~(PATTERN_FLOOR_MASK << (FLOOR_BITS * FLOOR_CAPACITY));
                pkFloorUpdated += (TileKind.FIRST_PLAYER_MARKER.index() << (FLOOR_BITS * FLOOR_CAPACITY));
            }
            return pkFloorUpdated;
        }

        if (pkFloorSize < FLOOR_CAPACITY) {
            for (TileKind.Colored tileKind : TileKind.Colored.ALL) {
                int tileKindCountToAdd = Math.min(PkTileSet.countOf(pkTileSet, tileKind), FLOOR_CAPACITY - pkFloorSize);
                int targetSize = pkFloorSize + tileKindCountToAdd;
                for (int i = pkFloorSize; i < targetSize; i++) {
                    pkFloorUpdated += (tileKind.index() << (FLOOR_BITS * (i + 1)));
                    pkFloorUpdated++;
                    pkFloorSize++;
                }
            }
            if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) == 1) {
                if (pkFloorSize == FLOOR_CAPACITY) {
                    pkFloorUpdated &= ~(PATTERN_FLOOR_MASK << (FLOOR_BITS * FLOOR_CAPACITY));
                    pkFloorUpdated += (TileKind.FIRST_PLAYER_MARKER.index() << (FLOOR_BITS * FLOOR_CAPACITY));
                }
                else {
                    pkFloorUpdated += (TileKind.FIRST_PLAYER_MARKER.index() << (FLOOR_BITS * (pkFloorSize + 1)));
                    pkFloorUpdated++;
                }

            }
        }
        return pkFloorUpdated;
    }

    /// Retourne vrai si et seulement si la ligne plancher empaquetée donnée contient le marqueur de premier joueur.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @return {@code true} si la ligne contient le marqueur de premier joueur, {@code false} sinon
    public static boolean containsFirstPlayerMarker(int pkFloor) {
        boolean containsFirstPlayerMarker = false;
        for (int i = 0; i < size(pkFloor); i++) {
            containsFirstPlayerMarker = containsFirstPlayerMarker || (tileAt(pkFloor, i) == TileKind.FIRST_PLAYER_MARKER);
        }
        return containsFirstPlayerMarker;
    }

    /// Retourne l'ensemble de tuiles empaqueté constitué de toutes les tuiles se trouvant
    /// sur la ligne plancher empaquetée donnée.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @return l'ensemble de tuiles empaqueté correspondant au contenu de la ligne plancher
    public static int asPkTileSet(int pkFloor) {
        int newPkTileSet = PkTileSet.EMPTY;
        for (int i = 0; i < size(pkFloor); i++) {
            newPkTileSet = PkTileSet.add(newPkTileSet, tileAt(pkFloor, i));
        }
        return newPkTileSet;
    }

    /// Retourne la représentation textuelle de la ligne plancher empaquetée donnée.
    /// La représentation est constituée des noms des sortes des tuiles qu'elle contient,
    /// séparés par des virgules suivies d'espaces, et entourés de crochets.
    /// Par exemple : {@code [FIRST_PLAYER_MARKER, B, B]}.
    ///
    /// @param pkFloor
    ///        la ligne plancher empaquetée
    /// @return la représentation textuelle de la ligne plancher
    public static String toString(int pkFloor) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size(pkFloor); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tileAt(pkFloor, i));
        }
        return sb.append("]").toString();
    }

    private static boolean isValid(int pkFloor, int i) {
        return (i >= 0) && (i < size(pkFloor));
    }
}