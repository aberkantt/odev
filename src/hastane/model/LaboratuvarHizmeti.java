package hastane.model;

import java.math.BigDecimal;

/** Laboratuvar tetkiki (kan tahlili, idrar tahlili ...). */
public class LaboratuvarHizmeti extends Hizmet {

    private final int aclikSaati;

    public LaboratuvarHizmeti(String ad, int sureDakika, BigDecimal ucret, int aclikSaati) {
        super(ad, sureDakika, ucret);
        if (aclikSaati < 0) {
            throw new IllegalArgumentException("Açlık süresi negatif olamaz.");
        }
        this.aclikSaati = aclikSaati;
    }

    public int getAclikSaati() {
        return aclikSaati;
    }

    @Override
    public String getTur() {
        return "Laboratuvar";
    }

    @Override
    public String getHazirlikBilgisi() {
        if (aclikSaati > 0) {
            return "Tahlilden önce en az " + aclikSaati + " saat aç olmanız gerekir (su içebilirsiniz).";
        }
        return "Özel bir hazırlık gerekmez.";
    }
}
