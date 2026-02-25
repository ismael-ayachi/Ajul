package ch.epfl.ajul.intarray;

///
/// Tableau d'entiers mutable, basé sur l'enveloppement d'un tableau Java.
/// <p>
/// La méthode {@link #wrapping(int[])} ne copie pas le tableau fourni: toute
/// modification du tableau passé en argument se reflète donc dans l'instance,
/// et inversement.
///
/// @author Ismael Ayachi (393163)
///
public final class MutableIntArray extends AbstractIntArray {
    ///
    /// Construit un tableau mutable enveloppant le tableau donné.
    ///
    /// @param tab le tableau sous-jacent (non copié)
    ///
    private MutableIntArray(int[] tab) {
        super(tab);
    }

    ///
    /// Retourne un {@code MutableIntArray} qui enveloppe le tableau donné.
    ///
    /// @param tab le tableau à envelopper (non copié)
    /// @return un tableau mutable enveloppant {@code tab}
    ///
    public static MutableIntArray wrapping(int[] tab) {
        return new MutableIntArray(tab);
    }
}
