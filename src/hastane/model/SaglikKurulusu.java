package hastane.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Hastane, klinik veya tıp merkezi. Bir veya birden çok şubesi olabilir. */
public class SaglikKurulusu {

    private static int sonId = 0;

    private final int id;
    private final String ad;
    private final KurulusTuru tur;
    private final List<Sube> subeler = new ArrayList<>();

    public SaglikKurulusu(String ad, KurulusTuru tur) {
        this.id = ++sonId;
        this.ad = Dogrulama.metinZorunlu(ad, "Kuruluş adı");
        if (tur == null) {
            throw new IllegalArgumentException("Kuruluş türü seçilmelidir.");
        }
        this.tur = tur;
    }

    /** Yeni şube oluşturur. Şube nesnesi her zaman bir kuruluşa bağlı olarak doğar. */
    public Sube subeEkle(String subeAdi, String adres, CalismaSaatleri calismaSaatleri) {
        for (Sube s : subeler) {
            if (s.getAd().equalsIgnoreCase(subeAdi.trim())) {
                throw new IllegalArgumentException(subeAdi + " adında bir şube zaten var.");
            }
        }
        Sube sube = new Sube(this, subeAdi, adres, calismaSaatleri);
        subeler.add(sube);
        return sube;
    }

    public boolean cokSubeliMi() {
        return subeler.size() > 1;
    }

    /** Kuruluşun tüm şubelerinde sunulan hizmetler (tekrarsız). */
    public List<Hizmet> getTumHizmetler() {
        Set<Hizmet> hepsi = new LinkedHashSet<>();
        for (Sube s : subeler) {
            hepsi.addAll(s.getSunulanHizmetler());
        }
        return new ArrayList<>(hepsi);
    }

    public boolean hizmetSunuyorMu(Hizmet hizmet) {
        return getTumHizmetler().contains(hizmet);
    }

    public int getId() {
        return id;
    }

    public String getAd() {
        return ad;
    }

    public KurulusTuru getTur() {
        return tur;
    }

    public List<Sube> getSubeler() {
        return Collections.unmodifiableList(subeler);
    }

    @Override
    public String toString() {
        return ad + " (" + tur.getGorunenAd() + ", " + subeler.size() + " şube)";
    }
}
