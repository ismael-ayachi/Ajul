package ch.epfl.ajul.intarray;

///
/// Tableau d'entiers immuable.
/// <p>
/// Les instances ne peuvent pas être modifiées après création; la méthode
/// {@link #copyOf(int[])} effectue une copie défensive du tableau fourni.
///
/// @author Ismaël Ayachi (393163)
///
public final class ImmutableIntArray extends AbstractIntArray implements ReadOnlyIntArray {
    ///
    /// Construit un tableau immuable à partir du tableau donné.
    /// <p>
    /// Le constructeur est privé afin de forcer l'utilisation de
    /// {@link #copyOf(int[])} qui réalise une copie.
    ///
    /// @param tab le tableau sous-jacent
    ///
    private ImmutableIntArray(int[] tab) {
        super(tab);
    }

    ///
    /// Crée un {@code ImmutableIntArray} contenant une copie du tableau donné.
    ///
    /// @param tab le tableau à copier
    /// @return un tableau immuable contenant les mêmes valeurs
    ///
    public static ImmutableIntArray copyOf(int[] tab) {
        return new ImmutableIntArray(tab.clone());
    }

    ///
    /// Retourne ce tableau lui-même (déjà immuable).
    ///
    /// @return {@code this}
    ///
    @Override
    public ImmutableIntArray immutable() {
        return this;
    }
}
