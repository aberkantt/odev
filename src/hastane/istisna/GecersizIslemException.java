package hastane.istisna;

/** İş kuralına aykırı işlem: geçmiş tarihe randevu, onaylanmış randevuyu tekrar onaylama vb. */
public class GecersizIslemException extends HastaneException {

    public GecersizIslemException(String mesaj) {
        super(mesaj);
    }
}
