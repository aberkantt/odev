package hastane;

import hastane.bildirim.BildirimServisi;
import hastane.bildirim.EpostaBildirimi;
import hastane.bildirim.SmsBildirimi;
import hastane.bildirim.UygulamaIciBildirim;
import hastane.depo.VeriDeposu;
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
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/** Geliştirme denemesi (7. gün): Bildirim kanalları. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");

        UygulamaIciBildirim gelenKutusu = new UygulamaIciBildirim();
        BildirimServisi bildirim = new BildirimServisi();
        bildirim.kanalEkle(gelenKutusu);
        bildirim.kanalEkle(new EpostaBildirimi());
        bildirim.kanalEkle(new SmsBildirimi());

        VeriDeposu depo = new VeriDeposu();
        RandevuServisi rs = new RandevuServisi(depo, bildirim);

        SaglikKurulusu hastane = new SaglikKurulusu("Yıldız Hastanesi", KurulusTuru.HASTANE);
        Sube merkez = hastane.subeEkle("Merkez Şube", "Adapazarı / Sakarya",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        Hizmet muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        merkez.bolumEkle("Dahiliye").hizmetEkle(muayene);
        Doktor ayse = new Doktor("Ayşe", "Yılmaz", "0533 000 00 11", "ayse.yilmaz@yildiz.test", "1234",
                hastane, "Uzm. Dr.", "İç Hastalıkları");
        ayse.hizmetEkle(muayene);
        ayse.uygunlukEkle(new UygunlukAraligi(DayOfWeek.MONDAY,
                new SaatAraligi(LocalTime.of(9, 0), LocalTime.of(12, 0)), merkez));
        Hasta ahmet = new Hasta("Ahmet", "Çelik", "0535 000 00 31", "ahmet.celik@hasta.test", "1234",
                "10000000146", LocalDate.of(1990, 5, 14));

        LocalDate pazartesi = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        System.out.println("\n-- Randevu talebi --");
        Randevu r = rs.randevuOlustur(ahmet, ayse, muayene, merkez, pazartesi.atTime(9, 0));
        System.out.println("\n-- Doktor onaylıyor --");
        rs.onayla(ayse, r);

        System.out.println("\nAhmet'in gelen kutusu (" + gelenKutusu.okunmamisSayisi(ahmet) + " okunmamış):");
        for (String mesaj : gelenKutusu.oku(ahmet)) {
            System.out.println("  " + mesaj);
        }
    }
}
