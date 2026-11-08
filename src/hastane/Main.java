package hastane;

import hastane.depo.VeriDeposu;
import hastane.istisna.HastaneException;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.Hasta;
import hastane.model.Hizmet;
import hastane.model.KurulusTuru;
import hastane.model.MuayeneHizmeti;
import hastane.model.Randevu;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;
import hastane.model.UygunlukAraligi;
import hastane.servis.RandevuServisi;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/** Geliştirme denemesi (6. gün): RandevuServisi ile çakışma kontrolü ve boş saatler. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");

        VeriDeposu depo = new VeriDeposu();
        RandevuServisi rs = new RandevuServisi(depo);

        SaglikKurulusu hastane = new SaglikKurulusu("Yıldız Hastanesi", KurulusTuru.HASTANE);
        depo.kurulusEkle(hastane);
        Sube merkez = hastane.subeEkle("Merkez Şube", "Adapazarı / Sakarya",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        Hizmet muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        merkez.bolumEkle("Dahiliye").hizmetEkle(muayene);
        Doktor ayse = new Doktor("Ayşe", "Yılmaz", "0533", "ayse.yilmaz@yildiz.test", "1234",
                hastane, "Uzm. Dr.", "İç Hastalıkları");
        ayse.hizmetEkle(muayene);
        ayse.uygunlukEkle(new UygunlukAraligi(DayOfWeek.MONDAY,
                new SaatAraligi(LocalTime.of(9, 0), LocalTime.of(10, 0)), merkez));
        Hasta ahmet = new Hasta("Ahmet", "Çelik", "0535", "ahmet.celik@hasta.test", "1234",
                "10000000146", LocalDate.of(1990, 5, 14));
        Hasta fatma = new Hasta("Fatma", "Aydın", "0536", "fatma.aydin@hasta.test", "1234",
                "20000000292", LocalDate.of(1985, 11, 2));
        depo.kisiEkle(ayse);
        depo.kisiEkle(ahmet);
        depo.kisiEkle(fatma);

        LocalDate pazartesi = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        System.out.println("Boş saatler (önce) : " + rs.bosSaatler(fatma, ayse, muayene, merkez, pazartesi));

        Randevu r1 = rs.randevuOlustur(ahmet, ayse, muayene, merkez, pazartesi.atTime(9, 0));
        System.out.println("Oluşturuldu: " + r1);
        System.out.println("Boş saatler (sonra): " + rs.bosSaatler(fatma, ayse, muayene, merkez, pazartesi));

        LocalDateTime[] denemeler = {pazartesi.atTime(9, 10), pazartesi.atTime(9, 20), pazartesi.atTime(9, 50)};
        for (LocalDateTime zaman : denemeler) {
            try {
                Randevu r = rs.randevuOlustur(fatma, ayse, muayene, merkez, zaman);
                System.out.println("Oluşturuldu: " + r);
            } catch (HastaneException e) {
                System.out.println(zaman.toLocalTime() + " reddedildi -> " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
            }
        }
    }
}
