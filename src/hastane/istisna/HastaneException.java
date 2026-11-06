package hastane.istisna;

/**
 * Uygulamaya özgü tüm hataların ortak atası.
 * Arayüz katmanı bu tipi yakalayarak kullanıcıya anlaşılır bir mesaj gösterir.
 */
public class HastaneException extends RuntimeException {

    public HastaneException(String mesaj) {
        super(mesaj);
    }
}
