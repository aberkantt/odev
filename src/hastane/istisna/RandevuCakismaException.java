package hastane.istisna;

/** Aynı çalışanın ya da aynı hastanın üst üste binen iki aktif randevusu olamaz. */
public class RandevuCakismaException extends HastaneException {

    public RandevuCakismaException(String mesaj) {
        super(mesaj);
    }
}
