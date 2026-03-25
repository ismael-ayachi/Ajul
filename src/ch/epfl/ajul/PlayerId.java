package ch.epfl.ajul;

import java.util.List;

/// Enumération représentant l'identifiant d'un joueur dans une partie d'Ajul.
///
/// @author Ismaël Ayachi (393163)
public enum PlayerId {
    /// Identifiant du joueur 1.
    P1,
    /// Identifiant du joueur 2.
    P2,
    /// Identifiant du joueur 3.
    P3,
    /// Identifiant du joueur 4.
    P4;

    /// Liste immuable de tous les identifiants de joueurs, dans l'ordre de déclaration.
        public static final List<PlayerId> ALL = List.of(values());
}