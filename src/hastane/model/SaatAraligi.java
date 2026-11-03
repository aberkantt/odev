package hastane.model;

import java.time.LocalTime;

/**
 * Bir gün içindeki [baslangic, bitis) saat aralığı. Örn: 09:00 - 12:00
 * Değiştirilemez (immutable) bir değer nesnesidir: alanlar final, setter yok.
 */
public final class SaatAraligi {

    private final LocalTime baslangic;
    private final LocalTime bitis;

    public SaatAraligi(LocalTime baslangic, LocalTime bitis) {
        if (baslangic == null || bitis == null || !baslangic.isBefore(bitis)) {
            throw new IllegalArgumentException("Başlangıç saati bitiş saatinden önce olmalıdır.");
        }
        this.baslangic = baslangic;
        this.bitis = bitis;
    }

    /** Verilen [bas, bit) aralığı tamamen bu aralığın içinde mi? */
    public boolean kapsiyorMu(LocalTime bas, LocalTime bit) {
        return !bas.isBefore(baslangic) && !bit.isAfter(bitis);
    }

    /** İki aralık çakışıyor mu? (Bitişik aralıklar çakışmaz: 09-10 ve 10-11 gibi.) */
    public boolean cakisiyorMu(SaatAraligi diger) {
        return baslangic.isBefore(diger.bitis) && diger.baslangic.isBefore(bitis);
    }

    public LocalTime getBaslangic() {
        return baslangic;
    }

    public LocalTime getBitis() {
        return bitis;
    }

    @Override
    public String toString() {
        return Bicim.saat(baslangic) + "-" + Bicim.saat(bitis);
    }
}
