package hastane;

import hastane.model.GoruntulemeHizmeti;
import hastane.model.Hizmet;
import hastane.model.LaboratuvarHizmeti;
import hastane.model.MuayeneHizmeti;

import java.math.BigDecimal;
import java.util.List;

/** Geliştirme denemesi (2. gün): Hizmet hiyerarşisi ve çok biçimlilik. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");
        System.out.println();

        List<Hizmet> hizmetler = List.of(
                new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750")),
                new GoruntulemeHizmeti("Kontrastlı Beyin MR", 45, new BigDecimal("3500"), "MR", true),
                new LaboratuvarHizmeti("Açlık Kan Şekeri", 10, new BigDecimal("200"), 8));

        // Aynı metot çağrısı, nesnenin gerçek türüne göre farklı sonuç verir
        for (Hizmet h : hizmetler) {
            System.out.println(h);
            System.out.println("   Hazırlık: " + h.getHazirlikBilgisi());
        }

        try {
            new MuayeneHizmeti("Hatalı", 20, new BigDecimal("-5"));
        } catch (IllegalArgumentException e) {
            System.out.println("Beklenen hata: " + e.getMessage());
        }
    }
}
