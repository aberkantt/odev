package hastane.bildirim;

import hastane.model.Bicim;
import hastane.model.Kisi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Bildirimleri kullanıcının uygulama içi "gelen kutusunda" saklar. */
public class UygulamaIciBildirim implements BildirimKanali {

    private final Map<Integer, List<String>> kutular = new HashMap<>();
    private final Map<Integer, Integer> okunanSayisi = new HashMap<>();

    @Override
    public String getAd() {
        return "Uygulama içi";
    }

    @Override
    public void gonder(Kisi alici, String mesaj) {
        String satir = "[" + Bicim.tarihSaat(LocalDateTime.now()) + "] " + mesaj;
        kutular.computeIfAbsent(alici.getId(), id -> new ArrayList<>()).add(satir);
    }

    public int okunmamisSayisi(Kisi kisi) {
        return tumBildirimler(kisi).size() - okunanSayisi.getOrDefault(kisi.getId(), 0);
    }

    /** Tüm bildirimleri döndürür ve hepsini okundu sayar. */
    public List<String> oku(Kisi kisi) {
        List<String> liste = tumBildirimler(kisi);
        okunanSayisi.put(kisi.getId(), liste.size());
        return liste;
    }

    private List<String> tumBildirimler(Kisi kisi) {
        return new ArrayList<>(kutular.getOrDefault(kisi.getId(), List.of()));
    }
}
