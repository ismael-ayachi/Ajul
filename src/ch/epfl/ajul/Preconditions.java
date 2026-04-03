package ch.epfl.ajul;

/// Classe utilitaire permettant de vérifier les préconditions des méthodes.
///
/// @author Ismaël Ayachi (393163)
public final class Preconditions {

    /// Lève une {@link IllegalArgumentException} si l'argument donné est faux.
    ///
    /// @param shouldBeTrue la condition qui doit être vraie
    /// @throws IllegalArgumentException si {@code shouldBeTrue} est faux
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}