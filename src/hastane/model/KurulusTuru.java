package hastane.model;

/** Sağlık kuruluşunun türü. */
public enum KurulusTuru {
    HASTANE("Hastane"),
    KLINIK("Klinik"),
    TIP_MERKEZI("Tıp Merkezi");

    private final String gorunenAd;

    KurulusTuru(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    public String getGorunenAd() {
        return gorunenAd;
    }
}
