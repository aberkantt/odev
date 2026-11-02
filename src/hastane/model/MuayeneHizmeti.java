package hastane.model;

import java.math.BigDecimal;

/** Doktor muayenesi (genel muayene, uzman muayenesi, kontrol ...). */
public class MuayeneHizmeti extends Hizmet {

    public MuayeneHizmeti(String ad, int sureDakika, BigDecimal ucret) {
        super(ad, sureDakika, ucret);
    }

    @Override
    public String getTur() {
        return "Muayene";
    }

    @Override
    public String getHazirlikBilgisi() {
        return "Kimliğinizi, varsa önceki tahlil sonuçlarınızı ve kullandığınız ilaçların listesini getiriniz.";
    }
}
