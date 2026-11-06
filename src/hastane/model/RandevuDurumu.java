package hastane.model;

/**
 * Randevunun yaşam döngüsü:
 *
 *   BEKLEMEDE --onayla--> ONAYLANDI --tamamla--> TAMAMLANDI
 *       |                     |
 *       +--reddet--> REDDEDILDI
 *       +--iptal---> IPTAL_EDILDI <--iptal--+
 */
public enum RandevuDurumu {
    BEKLEMEDE("Onay bekliyor"),
    ONAYLANDI("Onaylandı"),
    REDDEDILDI("Reddedildi"),
    IPTAL_EDILDI("İptal edildi"),
    TAMAMLANDI("Tamamlandı");

    private final String gorunenAd;

    RandevuDurumu(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    /** Aktif randevular zaman dilimini "meşgul" eder; çakışma kontrolüne girer. */
    public boolean aktifMi() {
        return this == BEKLEMEDE || this == ONAYLANDI;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }
}
