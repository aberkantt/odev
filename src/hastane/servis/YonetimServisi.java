package hastane.servis;

import hastane.depo.VeriDeposu;
import hastane.istisna.GecersizIslemException;
import hastane.istisna.YetkiException;
import hastane.model.Bolum;
import hastane.model.Calisan;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.Hizmet;
import hastane.model.Kisi;
import hastane.model.KurulusTuru;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.SaglikPersoneli;
import hastane.model.Sube;
import hastane.model.UygunlukAraligi;
import hastane.model.Yonetici;

import java.time.DayOfWeek;

/**
 * Kuruluş, şube, bölüm, hizmet ve çalışan tanımlamaları.
 * Her işlemden önce YETKİ KONTROLÜ yapılır: yönetici sadece kendi kuruluşunu yönetebilir.
 */
public class YonetimServisi {

    private final VeriDeposu depo;

    public YonetimServisi(VeriDeposu depo) {
        this.depo = depo;
    }

    // ---------- Kuruluş / şube / bölüm / hizmet ----------

    public SaglikKurulusu kurulusOlustur(Yonetici yonetici, String ad, KurulusTuru tur) {
        SaglikKurulusu kurulus = new SaglikKurulusu(ad, tur);
        depo.kurulusEkle(kurulus);
        yonetici.kurulusYetkisiVer(kurulus);
        return kurulus;
    }

    public Sube subeEkle(Yonetici yonetici, SaglikKurulusu kurulus, String ad, String adres,
                         CalismaSaatleri calismaSaatleri) {
        yetkiKontrol(yonetici, kurulus);
        return kurulus.subeEkle(ad, adres, calismaSaatleri);
    }

    /**
     * Şubenin bir gününün çalışma saatini değiştirir. aralik == null ise o gün kapalı olur.
     * O gün o şubede çalışanların uygunluk saatleri yeni saatlerin dışında kalıyorsa işlem reddedilir.
     */
    public void calismaSaatiAyarla(Yonetici yonetici, Sube sube, DayOfWeek gun, SaatAraligi aralik) {
        yetkiKontrol(yonetici, sube.getKurulus());
        for (Calisan c : depo.calisanlariGetir(sube.getKurulus())) {
            for (UygunlukAraligi u : c.getUygunluklar(sube, gun)) {
                SaatAraligi s = u.getSaatAraligi();
                if (aralik == null || !aralik.kapsiyorMu(s.getBaslangic(), s.getBitis())) {
                    throw new GecersizIslemException(c + " bu gün " + s + " saatlerinde çalışıyor; "
                            + "önce çalışanın uygunluk saatleri düzenlenmeli.");
                }
            }
        }
        if (aralik == null) {
            sube.getCalismaSaatleri().kapaliYap(gun);
        } else {
            sube.getCalismaSaatleri().gunAyarla(gun, aralik.getBaslangic(), aralik.getBitis());
        }
    }

    public Bolum bolumEkle(Yonetici yonetici, Sube sube, String bolumAdi) {
        yetkiKontrol(yonetici, sube.getKurulus());
        return sube.bolumEkle(bolumAdi);
    }

    public void hizmetEkle(Yonetici yonetici, Sube sube, Bolum bolum, Hizmet hizmet) {
        yetkiKontrol(yonetici, sube.getKurulus());
        if (!sube.getBolumler().contains(bolum)) {
            throw new GecersizIslemException(bolum + " bölümü " + sube + " şubesine ait değil.");
        }
        bolum.hizmetEkle(hizmet);
    }

    // ---------- Çalışan yönetimi ----------

    public Doktor doktorKaydet(Yonetici yonetici, SaglikKurulusu kurulus, String ad, String soyad, String telefon,
                               String eposta, String sifre, String unvan, String uzmanlik) {
        yetkiKontrol(yonetici, kurulus);
        Doktor doktor = new Doktor(ad, soyad, telefon, eposta, sifre, kurulus, unvan, uzmanlik);
        depo.kisiEkle(doktor);
        return doktor;
    }

    public SaglikPersoneli personelKaydet(Yonetici yonetici, SaglikKurulusu kurulus, String ad, String soyad,
                                          String telefon, String eposta, String sifre, String gorev) {
        yetkiKontrol(yonetici, kurulus);
        SaglikPersoneli personel = new SaglikPersoneli(ad, soyad, telefon, eposta, sifre, kurulus, gorev);
        depo.kisiEkle(personel);
        return personel;
    }

    public void calisanaHizmetAta(Yonetici yonetici, Calisan calisan, Hizmet hizmet) {
        yetkiKontrol(yonetici, calisan.getKurulus());
        calisan.hizmetEkle(hizmet);
    }

    /** Uygunluk zamanını çalışan kendisi ya da kurumunun yöneticisi ekleyebilir. */
    public void uygunlukEkle(Kisi islemYapan, Calisan calisan, Sube sube, DayOfWeek gun, SaatAraligi aralik) {
        boolean kendisi = islemYapan == calisan;
        boolean yonetici = islemYapan instanceof Yonetici y && y.yetkiliMi(calisan.getKurulus());
        if (!kendisi && !yonetici) {
            throw new YetkiException("Bu çalışanın uygunluk zamanlarını düzenleme yetkiniz yok.");
        }
        calisan.uygunlukEkle(new UygunlukAraligi(gun, aralik, sube));
    }

    /** Yönetici bu kuruluşta yetkili değilse YetkiException fırlatır. */
    public void yetkiKontrol(Yonetici yonetici, SaglikKurulusu kurulus) {
        if (!yonetici.yetkiliMi(kurulus)) {
            throw new YetkiException(yonetici.getAdSoyad() + ", " + kurulus.getAd() + " üzerinde yetkili değil.");
        }
    }
}
