package hastane.model;

import java.math.BigDecimal;

/** Radyolojik görüntüleme hizmeti (röntgen, ultrason, MR ...). */
public class GoruntulemeHizmeti extends Hizmet {

    private final String cihaz;
    private final boolean kontrastMaddeli;

    public GoruntulemeHizmeti(String ad, int sureDakika, BigDecimal ucret, String cihaz, boolean kontrastMaddeli) {
        super(ad, sureDakika, ucret);
        this.cihaz = Dogrulama.metinZorunlu(cihaz, "Cihaz");
        this.kontrastMaddeli = kontrastMaddeli;
    }

    public String getCihaz() {
        return cihaz;
    }

    public boolean isKontrastMaddeli() {
        return kontrastMaddeli;
    }

    @Override
    public String getTur() {
        return "Görüntüleme (" + cihaz + ")";
    }

    @Override
    public String getHazirlikBilgisi() {
        String bilgi = "Takı, saat gibi metal eşyalarınızı çıkarınız.";
        if (kontrastMaddeli) {
            bilgi += " Kontrast madde verileceği için çekimden önce 4 saat bir şey yemeyiniz.";
        }
        return bilgi;
    }
}
