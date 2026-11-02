package hastane.model;

import java.math.BigDecimal;

/**
 * Kurumun sunduğu sağlık hizmeti (muayene, görüntüleme, laboratuvar ...).
 *
 * SOYUTLAMA: "Hizmet" tek başına anlamlı değildir; türü belli olan alt sınıflar kullanılır.
 * ÇOK BİÇİMLİLİK: getTur() ve getHazirlikBilgisi() her alt sınıfta farklı davranır.
 */
public abstract class Hizmet {

    private static int sonId = 0;

    private final int id;
    private String ad;
    private int sureDakika;
    private BigDecimal ucret;

    protected Hizmet(String ad, int sureDakika, BigDecimal ucret) {
        this.id = ++sonId;
        setAd(ad);
        setSureDakika(sureDakika);
        setUcret(ucret);
    }

    /** Hizmetin türünün okunabilir adı. */
    public abstract String getTur();

    /** Hastanın randevudan önce bilmesi gereken hazırlık talimatı. */
    public abstract String getHazirlikBilgisi();

    public int getId() {
        return id;
    }

    public String getAd() {
        return ad;
    }

    public int getSureDakika() {
        return sureDakika;
    }

    public BigDecimal getUcret() {
        return ucret;
    }

    public final void setAd(String ad) {
        this.ad = Dogrulama.metinZorunlu(ad, "Hizmet adı");
    }

    public final void setSureDakika(int sureDakika) {
        Dogrulama.pozitif(sureDakika, "Hizmet süresi");
        if (sureDakika > 480) {
            throw new IllegalArgumentException("Hizmet süresi 8 saati geçemez.");
        }
        this.sureDakika = sureDakika;
    }

    public final void setUcret(BigDecimal ucret) {
        this.ucret = Dogrulama.negatifOlamaz(ucret, "Ücret");
    }

    @Override
    public String toString() {
        return ad + " [" + getTur() + "] - " + sureDakika + " dk - " + Bicim.para(ucret);
    }
}
