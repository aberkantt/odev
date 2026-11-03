package hastane.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Şube içindeki bölüm (Dahiliye, Radyoloji, Laboratuvar ...) ve sunduğu hizmetler. */
public class Bolum {

    private final String ad;
    private final List<Hizmet> hizmetler = new ArrayList<>();

    public Bolum(String ad) {
        this.ad = Dogrulama.metinZorunlu(ad, "Bölüm adı");
    }

    public void hizmetEkle(Hizmet hizmet) {
        if (hizmetler.contains(hizmet)) {
            throw new IllegalArgumentException(hizmet.getAd() + " bu bölümde zaten tanımlı.");
        }
        hizmetler.add(hizmet);
    }

    /** Liste dışarıya salt-okunur verilir; dışarıdan add/remove yapılamaz (kapsülleme). */
    public List<Hizmet> getHizmetler() {
        return Collections.unmodifiableList(hizmetler);
    }

    public String getAd() {
        return ad;
    }

    @Override
    public String toString() {
        return ad;
    }
}
