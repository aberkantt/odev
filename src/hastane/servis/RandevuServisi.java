package hastane.servis;

import hastane.depo.VeriDeposu;
import hastane.istisna.GecersizIslemException;
import hastane.istisna.HastaneException;
import hastane.istisna.RandevuCakismaException;
import hastane.istisna.YetkiException;
import hastane.model.Bicim;
import hastane.model.Calisan;
import hastane.model.Hasta;
import hastane.model.Hizmet;
import hastane.model.Kisi;
import hastane.model.Randevu;
import hastane.model.RandevuDurumu;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;
import hastane.model.UygunlukAraligi;
import hastane.model.Yonetici;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Randevu iş kurallarının merkezi:
 *  - randevu oluşturma ve ÇAKIŞMA KONTROLÜ
 *  - boş saatleri listeleme
 *  - ONAY MEKANİZMASI (onayla / reddet), iptal ve tamamlama
 */
public class RandevuServisi {

    /** Boş saat önerileri kaç dakikada bir üretilsin. */
    public static final int ADIM_DAKIKA = 15;

    private final VeriDeposu depo;

    public RandevuServisi(VeriDeposu depo) {
        this.depo = depo;
    }

    // =====================================================================
    //  RANDEVU OLUŞTURMA
    // =====================================================================

    public Randevu randevuOlustur(Hasta hasta, Calisan calisan, Hizmet hizmet, Sube sube, LocalDateTime baslangic) {
        HastaneException engel = engelBul(hasta, calisan, hizmet, sube, baslangic);
        if (engel != null) {
            throw engel;
        }
        Randevu randevu = new Randevu(hasta, calisan, hizmet, sube, baslangic);
        depo.randevuEkle(randevu);
        return randevu;
    }

    /**
     * Randevu alınmasına engel bir durum var mı? Varsa uygun hata nesnesini döndürür, yoksa null.
     * Kontrol sırası:
     *  1) geçmiş zaman  2) şube bu hizmeti veriyor mu  3) çalışan bu hizmeti veriyor mu
     *  4) şube açık mı  5) çalışan o saatte o şubede müsait mi
     *  6) ÇAKIŞMA: çalışanın veya hastanın aynı saatte başka aktif randevusu var mı
     */
    private HastaneException engelBul(Hasta hasta, Calisan calisan, Hizmet hizmet, Sube sube, LocalDateTime baslangic) {
        LocalDateTime bitis = baslangic.plusMinutes(hizmet.getSureDakika());

        if (!baslangic.isAfter(LocalDateTime.now())) {
            return new GecersizIslemException("Geçmiş bir zamana randevu alınamaz.");
        }
        if (!sube.hizmetSunuyorMu(hizmet)) {
            return new GecersizIslemException(sube + " şubesinde '" + hizmet.getAd() + "' hizmeti verilmiyor.");
        }
        if (!calisan.hizmetVerebilirMi(hizmet)) {
            return new GecersizIslemException(calisan + " '" + hizmet.getAd() + "' hizmetini vermiyor.");
        }
        if (!sube.getCalismaSaatleri().kapsiyorMu(baslangic, bitis)) {
            return new GecersizIslemException("Seçilen zaman şubenin çalışma saatleri dışında.");
        }
        if (!calisan.uygunMu(sube, baslangic, bitis)) {
            return new GecersizIslemException(calisan + " bu şubede bu saatte müsait değil.");
        }
        // --- Çakışma kontrolü: iki aralık [a1,a2) ve [b1,b2) çakışır <=> a1 < b2 && b1 < a2 ---
        for (Randevu r : depo.getRandevular()) {
            if (!r.aktifMi() || !r.cakisiyorMu(baslangic, bitis)) {
                continue;
            }
            if (r.getCalisan() == calisan) {
                return new RandevuCakismaException(calisan + " bu saatte dolu ("
                        + Bicim.saat(r.getBaslangic().toLocalTime()) + "-" + Bicim.saat(r.getBitis().toLocalTime()) + ").");
            }
            if (hasta != null && r.getHasta() == hasta) {
                return new RandevuCakismaException("Bu saatte zaten başka bir randevunuz var: " + r);
            }
        }
        return null;
    }

    public boolean alinabilirMi(Hasta hasta, Calisan calisan, Hizmet hizmet, Sube sube, LocalDateTime baslangic) {
        return engelBul(hasta, calisan, hizmet, sube, baslangic) == null;
    }

    /** Bir günde, çalışanın uygunluk aralıkları içindeki boş başlangıç saatlerini üretir. */
    public List<LocalDateTime> bosSaatler(Hasta hasta, Calisan calisan, Hizmet hizmet, Sube sube, LocalDate tarih) {
        List<LocalDateTime> sonuc = new ArrayList<>();
        int sure = hizmet.getSureDakika();
        for (UygunlukAraligi u : calisan.getUygunluklar(sube, tarih.getDayOfWeek())) {
            int dakika = u.getSaatAraligi().getBaslangic().toSecondOfDay() / 60;
            int sonDakika = u.getSaatAraligi().getBitis().toSecondOfDay() / 60;
            while (dakika + sure <= sonDakika) {
                LocalDateTime aday = tarih.atTime(LocalTime.ofSecondOfDay(dakika * 60L));
                if (alinabilirMi(hasta, calisan, hizmet, sube, aday)) {
                    sonuc.add(aday);
                }
                dakika += ADIM_DAKIKA;
            }
        }
        return sonuc;
    }

    /** Önümüzdeki 'gunSayisi' gün içinde en az bir boş saati olan günler ve o günlerin boş saatleri. */
    public Map<LocalDate, List<LocalDateTime>> uygunGunler(Hasta hasta, Calisan calisan, Hizmet hizmet,
                                                         Sube sube, int gunSayisi) {
        Map<LocalDate, List<LocalDateTime>> sonuc = new LinkedHashMap<>();
        LocalDate bugun = LocalDate.now();
        for (int i = 0; i < gunSayisi; i++) {
            LocalDate gun = bugun.plusDays(i);
            List<LocalDateTime> saatler = bosSaatler(hasta, calisan, hizmet, sube, gun);
            if (!saatler.isEmpty()) {
                sonuc.put(gun, saatler);
            }
        }
        return sonuc;
    }

    // =====================================================================
    //  ONAY MEKANİZMASI, İPTAL, TAMAMLAMA
    // =====================================================================

    public void onayla(Kisi islemYapan, Randevu randevu) {
        onayYetkisiKontrol(islemYapan, randevu);
        randevu.onayla();
    }

    public void reddet(Kisi islemYapan, Randevu randevu, String neden) {
        onayYetkisiKontrol(islemYapan, randevu);
        randevu.reddet(neden);
    }

    public void iptalEt(Kisi islemYapan, Randevu randevu, String neden) {
        boolean yetkili = islemYapan == randevu.getHasta()
                || islemYapan == randevu.getCalisan()
                || kurumYoneticisiMi(islemYapan, randevu);
        if (!yetkili) {
            throw new YetkiException("Bu randevuyu iptal etme yetkiniz yok.");
        }
        randevu.iptalEt(neden);
    }

    public void tamamla(Kisi islemYapan, Randevu randevu) {
        if (islemYapan != randevu.getCalisan() && !kurumYoneticisiMi(islemYapan, randevu)) {
            throw new YetkiException("Randevuyu sadece ilgili çalışan veya kurum yöneticisi tamamlayabilir.");
        }
        randevu.tamamla();
    }

    /** Onay/ret yetkisi: randevunun çalışanı ya da kurumun yöneticisi. */
    private void onayYetkisiKontrol(Kisi islemYapan, Randevu randevu) {
        if (islemYapan != randevu.getCalisan() && !kurumYoneticisiMi(islemYapan, randevu)) {
            throw new YetkiException("Bu randevuyu onaylama/reddetme yetkiniz yok.");
        }
    }

    private boolean kurumYoneticisiMi(Kisi kisi, Randevu randevu) {
        return kisi instanceof Yonetici yonetici && yonetici.yetkiliMi(randevu.getSube().getKurulus());
    }

    // =====================================================================
    //  SORGULAR
    // =====================================================================

    public List<Randevu> hastaRandevulari(Hasta hasta) {
        return filtrele(r -> r.getHasta() == hasta);
    }

    public List<Randevu> calisanRandevulari(Calisan calisan) {
        return filtrele(r -> r.getCalisan() == calisan);
    }

    public List<Randevu> onayBekleyenler(Calisan calisan) {
        return filtrele(r -> r.getCalisan() == calisan && r.getDurum() == RandevuDurumu.BEKLEMEDE);
    }

    public List<Randevu> kurulusRandevulari(SaglikKurulusu kurulus) {
        return filtrele(r -> r.getSube().getKurulus() == kurulus);
    }

    private List<Randevu> filtrele(Predicate<Randevu> kosul) {
        List<Randevu> sonuc = new ArrayList<>();
        for (Randevu r : depo.getRandevular()) {
            if (kosul.test(r)) {
                sonuc.add(r);
            }
        }
        sonuc.sort(Comparator.comparing(Randevu::getBaslangic));
        return sonuc;
    }
}
