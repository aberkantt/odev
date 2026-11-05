package hastane.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Bir çalışanın haftalık olarak tekrar eden müsaitlik zamanı.
 * Örn: "Her Pazartesi 09:00-12:00, Merkez Şube"
 * Hastalar sadece bu aralıklara randevu alabilir.
 */
public final class UygunlukAraligi {

    private final DayOfWeek gun;
    private final SaatAraligi saatAraligi;
    private final Sube sube;

    public UygunlukAraligi(DayOfWeek gun, SaatAraligi saatAraligi, Sube sube) {
        if (gun == null || saatAraligi == null || sube == null) {
            throw new IllegalArgumentException("Gün, saat aralığı ve şube boş olamaz.");
        }
        if (!sube.getCalismaSaatleri().kapsiyorMu(gun, saatAraligi)) {
            throw new IllegalArgumentException("Uygunluk aralığı şubenin çalışma saatleri dışında: "
                    + Bicim.gun(gun) + " " + saatAraligi + " (" + sube.getAd() + ")");
        }
        this.gun = gun;
        this.saatAraligi = saatAraligi;
        this.sube = sube;
    }

    /** Verilen şubede, verilen zaman dilimi bu uygunluk aralığının içinde mi? */
    public boolean kapsiyorMu(Sube hedefSube, LocalDateTime bas, LocalDateTime bit) {
        return sube == hedefSube
                && bas.getDayOfWeek() == gun
                && bas.toLocalDate().equals(bit.toLocalDate())
                && saatAraligi.kapsiyorMu(bas.toLocalTime(), bit.toLocalTime());
    }

    /** Aynı gün içinde saatleri çakışıyor mu? (Şube farklı olsa bile çalışan iki yerde olamaz.) */
    public boolean cakisiyorMu(UygunlukAraligi diger) {
        return gun == diger.gun && saatAraligi.cakisiyorMu(diger.saatAraligi);
    }

    public DayOfWeek getGun() {
        return gun;
    }

    public SaatAraligi getSaatAraligi() {
        return saatAraligi;
    }

    public Sube getSube() {
        return sube;
    }

    @Override
    public String toString() {
        return Bicim.gun(gun) + " " + saatAraligi + " @ " + sube.getAd();
    }
}
