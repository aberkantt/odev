package hastane.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bir sağlık kuruluşunun şubesi. Tek şubeli bir klinikte de bir adet "Merkez" şube bulunur;
 * böylece tek şubeli ve çok şubeli kurumlar aynı modelle çalışır.
 */
public class Sube {

    private static int sonId = 0;

    private final int id;
    private final SaglikKurulusu kurulus;
    private final String ad;
    private final String adres;
    private final CalismaSaatleri calismaSaatleri;
    private final List<Bolum> bolumler = new ArrayList<>();

    /** Paket içi (package-private) constructor: şube sadece SaglikKurulusu.subeEkle() ile oluşturulur. */
    Sube(SaglikKurulusu kurulus, String ad, String adres, CalismaSaatleri calismaSaatleri) {
        this.id = ++sonId;
        this.kurulus = kurulus;
        this.ad = Dogrulama.metinZorunlu(ad, "Şube adı");
        this.adres = Dogrulama.metinZorunlu(adres, "Adres");
        this.calismaSaatleri = calismaSaatleri;
    }

    public Bolum bolumEkle(String bolumAdi) {
        if (bolumBul(bolumAdi) != null) {
            throw new IllegalArgumentException(bolumAdi + " bölümü bu şubede zaten var.");
        }
        Bolum bolum = new Bolum(bolumAdi);
        bolumler.add(bolum);
        return bolum;
    }

    public Bolum bolumBul(String bolumAdi) {
        for (Bolum b : bolumler) {
            if (b.getAd().equalsIgnoreCase(bolumAdi.trim())) {
                return b;
            }
        }
        return null;
    }

    /** Şubenin tüm bölümlerindeki hizmetler (tekrarsız). */
    public List<Hizmet> getSunulanHizmetler() {
        Set<Hizmet> hepsi = new LinkedHashSet<>();
        for (Bolum b : bolumler) {
            hepsi.addAll(b.getHizmetler());
        }
        return new ArrayList<>(hepsi);
    }

    public boolean hizmetSunuyorMu(Hizmet hizmet) {
        return getSunulanHizmetler().contains(hizmet);
    }

    /** Hizmetin şubedeki hangi bölümde verildiğini bulur. */
    public Bolum hizmetinBolumu(Hizmet hizmet) {
        for (Bolum b : bolumler) {
            if (b.getHizmetler().contains(hizmet)) {
                return b;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public SaglikKurulusu getKurulus() {
        return kurulus;
    }

    public String getAd() {
        return ad;
    }

    public String getAdres() {
        return adres;
    }

    public CalismaSaatleri getCalismaSaatleri() {
        return calismaSaatleri;
    }

    public List<Bolum> getBolumler() {
        return Collections.unmodifiableList(bolumler);
    }

    @Override
    public String toString() {
        return kurulus.getAd() + " - " + ad;
    }
}
