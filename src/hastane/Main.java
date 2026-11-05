package hastane;

import hastane.model.Calisan;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.Hizmet;
import hastane.model.Kisi;
import hastane.model.KurulusTuru;
import hastane.model.MuayeneHizmeti;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.SaglikPersoneli;
import hastane.model.Sube;
import hastane.model.UygunlukAraligi;
import hastane.model.Yonetici;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/** Geliştirme denemesi (4. gün): Çalışanlar, yönetici ve uygunluk aralıkları. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");

        SaglikKurulusu hastane = new SaglikKurulusu("Yıldız Hastanesi", KurulusTuru.HASTANE);
        Sube merkez = hastane.subeEkle("Merkez Şube", "Adapazarı / Sakarya",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        Hizmet muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        merkez.bolumEkle("Dahiliye").hizmetEkle(muayene);

        Yonetici yonetici = new Yonetici("Mehmet", "Kaya", "0532", "mehmet.kaya@yildiz.test", "1234");
        yonetici.kurulusYetkisiVer(hastane);

        Doktor ayse = new Doktor("Ayşe", "Yılmaz", "0533", "ayse.yilmaz@yildiz.test", "1234",
                hastane, "Uzm. Dr.", "İç Hastalıkları");
        ayse.hizmetEkle(muayene);
        ayse.uygunlukEkle(new UygunlukAraligi(DayOfWeek.MONDAY,
                new SaatAraligi(LocalTime.of(9, 0), LocalTime.of(12, 0)), merkez));

        SaglikPersoneli elif = new SaglikPersoneli("Elif", "Şahin", "0534", "elif.sahin@yildiz.test", "1234",
                hastane, "Radyoloji Teknisyeni");

        // Tek listede farklı türler: her biri kendi getRol() sonucunu verir
        List<Kisi> kisiler = List.of(yonetici, ayse, elif);
        for (Kisi k : kisiler) {
            System.out.println(k.getRol().getGorunenAd() + " -> " + k);
        }

        LocalDate pazartesi = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        Calisan c = ayse;
        System.out.println("Uygunluklar: " + c.getUygunluklar());
        System.out.println("Pzt 10:00-10:20 müsait mi? "
                + c.uygunMu(merkez, pazartesi.atTime(10, 0), pazartesi.atTime(10, 20)));
        LocalDateTime ogleden = pazartesi.atTime(14, 0);
        System.out.println("Pzt 14:00-14:20 müsait mi? " + c.uygunMu(merkez, ogleden, ogleden.plusMinutes(20)));

        try {   // şube 18:00'de kapanıyor
            ayse.uygunlukEkle(new UygunlukAraligi(DayOfWeek.TUESDAY,
                    new SaatAraligi(LocalTime.of(17, 0), LocalTime.of(19, 0)), merkez));
        } catch (IllegalArgumentException e) {
            System.out.println("Beklenen hata: " + e.getMessage());
        }
    }
}
