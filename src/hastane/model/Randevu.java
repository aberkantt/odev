package hastane.model;

import hastane.istisna.GecersizIslemException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bir hastanın, bir çalışandan, belirli bir şubede, belirli bir hizmet için aldığı randevu.
 *
 * KAPSÜLLEME: durum alanı dışarıdan doğrudan değiştirilemez (setter yok).
 * Durum sadece onayla() / reddet() / iptalEt() / tamamla() metotlarıyla, kurallara
 * uygun şekilde değişir. Böylece "iptal edilmiş randevuyu onaylamak" gibi hatalar engellenir.
 */
public class Randevu {

    private static int sonId = 0;

    private final int id;
    private final Hasta hasta;
    private final Calisan calisan;
    private final Hizmet hizmet;
    private final Sube sube;
    private final LocalDateTime baslangic;
    private final LocalDateTime bitis;
    // Süre ve ücret randevu anında kopyalanır: hizmetin fiyatı sonradan değişse de
    // alınmış randevunun ücreti değişmez.
    private final int sureDakika;
    private final BigDecimal ucret;
    private final LocalDateTime olusturulmaZamani;

    private RandevuDurumu durum;
    private String durumAciklamasi;

    public Randevu(Hasta hasta, Calisan calisan, Hizmet hizmet, Sube sube, LocalDateTime baslangic) {
        if (hasta == null || calisan == null || hizmet == null || sube == null || baslangic == null) {
            throw new IllegalArgumentException("Randevu bilgileri eksik.");
        }
        this.id = ++sonId;
        this.hasta = hasta;
        this.calisan = calisan;
        this.hizmet = hizmet;
        this.sube = sube;
        this.baslangic = baslangic;
        this.sureDakika = hizmet.getSureDakika();
        this.bitis = baslangic.plusMinutes(sureDakika);
        this.ucret = hizmet.getUcret();
        this.olusturulmaZamani = LocalDateTime.now();
        this.durum = RandevuDurumu.BEKLEMEDE;
    }

    // ---------- Onay mekanizması (durum geçişleri) ----------

    public void onayla() {
        durumOlmali(RandevuDurumu.BEKLEMEDE, "onaylanamaz");
        durum = RandevuDurumu.ONAYLANDI;
        durumAciklamasi = null;
    }

    public void reddet(String neden) {
        durumOlmali(RandevuDurumu.BEKLEMEDE, "reddedilemez");
        durum = RandevuDurumu.REDDEDILDI;
        durumAciklamasi = neden;
    }

    public void iptalEt(String neden) {
        if (!durum.aktifMi()) {
            throw new GecersizIslemException("Randevu '" + durum.getGorunenAd() + "' durumunda, iptal edilemez.");
        }
        durum = RandevuDurumu.IPTAL_EDILDI;
        durumAciklamasi = neden;
    }

    public void tamamla() {
        durumOlmali(RandevuDurumu.ONAYLANDI, "tamamlanamaz");
        if (baslangic.isAfter(LocalDateTime.now())) {
            throw new GecersizIslemException("Randevu saati henüz gelmedi, tamamlandı olarak işaretlenemez.");
        }
        durum = RandevuDurumu.TAMAMLANDI;
    }

    private void durumOlmali(RandevuDurumu beklenen, String islem) {
        if (durum != beklenen) {
            throw new GecersizIslemException("Randevu '" + durum.getGorunenAd() + "' durumunda, " + islem + ".");
        }
    }

    /** Bu randevu verilen [bas, bit) zaman dilimiyle üst üste biniyor mu? */
    public boolean cakisiyorMu(LocalDateTime bas, LocalDateTime bit) {
        return baslangic.isBefore(bit) && bas.isBefore(bitis);
    }

    public boolean aktifMi() {
        return durum.aktifMi();
    }

    // ---------- getter'lar ----------

    public int getId() {
        return id;
    }

    public Hasta getHasta() {
        return hasta;
    }

    public Calisan getCalisan() {
        return calisan;
    }

    public Hizmet getHizmet() {
        return hizmet;
    }

    public Sube getSube() {
        return sube;
    }

    public LocalDateTime getBaslangic() {
        return baslangic;
    }

    public LocalDateTime getBitis() {
        return bitis;
    }

    public int getSureDakika() {
        return sureDakika;
    }

    public BigDecimal getUcret() {
        return ucret;
    }

    public LocalDateTime getOlusturulmaZamani() {
        return olusturulmaZamani;
    }

    public RandevuDurumu getDurum() {
        return durum;
    }

    public String getDurumAciklamasi() {
        return durumAciklamasi;
    }

    /** Tek satırlık özet (listelerde kullanılır). */
    @Override
    public String toString() {
        return "#" + id + " " + Bicim.tarihSaat(baslangic) + "-" + Bicim.saat(bitis.toLocalTime())
                + " | " + hizmet.getAd() + " | " + calisan + " | " + sube
                + " | " + durum.getGorunenAd();
    }

    /** Tüm randevu detayları (işlem, süre, ücret, çalışan, tarih/saat, durum). */
    public String detay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Randevu No   : #").append(id).append('\n');
        sb.append("Hasta        : ").append(hasta.getAdSoyad())
                .append(" (TC ").append(hasta.getMaskeliTcKimlikNo()).append(")\n");
        sb.append("İşlem        : ").append(hizmet.getAd()).append(" [").append(hizmet.getTur()).append("]\n");
        sb.append("Çalışan      : ").append(calisan).append('\n');
        sb.append("Yer          : ").append(sube).append(" / ").append(sube.getAdres()).append('\n');
        sb.append("Tarih / Saat : ").append(Bicim.tarih(baslangic.toLocalDate())).append(", ")
                .append(Bicim.saat(baslangic.toLocalTime())).append(" - ")
                .append(Bicim.saat(bitis.toLocalTime())).append('\n');
        sb.append("Süre         : ").append(sureDakika).append(" dakika\n");
        sb.append("Ücret        : ").append(Bicim.para(ucret)).append('\n');
        sb.append("Durum        : ").append(durum.getGorunenAd());
        if (durumAciklamasi != null && !durumAciklamasi.isBlank()) {
            sb.append(" (").append(durumAciklamasi).append(')');
        }
        sb.append('\n');
        sb.append("Hazırlık     : ").append(hizmet.getHazirlikBilgisi());
        return sb.toString();
    }
}
