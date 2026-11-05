package hastane.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Hizmet veren tüm çalışanların (doktor, sağlık personeli) ortak atası.
 *
 * KALITIM: Kisi -> Calisan -> Doktor / SaglikPersoneli
 * Ortak davranışlar (hizmet listesi, uygunluk zamanları) burada bir kez yazılır,
 * alt sınıflar sadece kendilerine özgü kısmı ekler.
 */
public abstract class Calisan extends Kisi {

    private final SaglikKurulusu kurulus;
    private final List<Hizmet> verdigiHizmetler = new ArrayList<>();
    private final List<UygunlukAraligi> uygunluklar = new ArrayList<>();

    protected Calisan(String ad, String soyad, String telefon, String eposta, String sifre,
                      SaglikKurulusu kurulus) {
        super(ad, soyad, telefon, eposta, sifre);
        if (kurulus == null) {
            throw new IllegalArgumentException("Çalışanın bağlı olduğu kuruluş belirtilmelidir.");
        }
        this.kurulus = kurulus;
    }

    /** Çalışanın unvanı/görevi. Doktorda "Uzm. Dr.", personelde "Laborant" gibi. */
    public abstract String getUnvan();

    public void hizmetEkle(Hizmet hizmet) {
        if (!kurulus.hizmetSunuyorMu(hizmet)) {
            throw new IllegalArgumentException(hizmet.getAd() + " hizmeti " + kurulus.getAd() + " bünyesinde tanımlı değil.");
        }
        if (verdigiHizmetler.contains(hizmet)) {
            throw new IllegalArgumentException(getAdSoyad() + " zaten bu hizmeti veriyor.");
        }
        verdigiHizmetler.add(hizmet);
    }

    public boolean hizmetVerebilirMi(Hizmet hizmet) {
        return verdigiHizmetler.contains(hizmet);
    }

    public void uygunlukEkle(UygunlukAraligi yeni) {
        if (yeni.getSube().getKurulus() != kurulus) {
            throw new IllegalArgumentException("Çalışan sadece kendi kuruluşunun şubelerinde çalışabilir.");
        }
        for (UygunlukAraligi mevcut : uygunluklar) {
            if (mevcut.cakisiyorMu(yeni)) {
                throw new IllegalArgumentException("Bu aralık mevcut uygunlukla çakışıyor: " + mevcut);
            }
        }
        uygunluklar.add(yeni);
        uygunluklar.sort(Comparator.comparing(UygunlukAraligi::getGun)
                .thenComparing(u -> u.getSaatAraligi().getBaslangic()));
    }

    /** Çalışan, verilen şubede verilen zaman diliminde müsait mi? */
    public boolean uygunMu(Sube sube, LocalDateTime bas, LocalDateTime bit) {
        for (UygunlukAraligi u : uygunluklar) {
            if (u.kapsiyorMu(sube, bas, bit)) {
                return true;
            }
        }
        return false;
    }

    /** Belirli bir günde, belirli bir şubedeki uygunluk aralıkları. */
    public List<UygunlukAraligi> getUygunluklar(Sube sube, DayOfWeek gun) {
        List<UygunlukAraligi> sonuc = new ArrayList<>();
        for (UygunlukAraligi u : uygunluklar) {
            if (u.getSube() == sube && u.getGun() == gun) {
                sonuc.add(u);
            }
        }
        return sonuc;
    }

    /** Çalışanın bu şubede hiç uygunluk zamanı var mı? */
    public boolean subedeCalisiyorMu(Sube sube) {
        for (UygunlukAraligi u : uygunluklar) {
            if (u.getSube() == sube) {
                return true;
            }
        }
        return false;
    }

    public SaglikKurulusu getKurulus() {
        return kurulus;
    }

    public List<Hizmet> getVerdigiHizmetler() {
        return Collections.unmodifiableList(verdigiHizmetler);
    }

    public List<UygunlukAraligi> getUygunluklar() {
        return Collections.unmodifiableList(uygunluklar);
    }

    @Override
    public String toString() {
        return getUnvan() + " " + getAdSoyad();
    }
}
