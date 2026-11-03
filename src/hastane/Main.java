package hastane;

import hastane.model.Bolum;
import hastane.model.CalismaSaatleri;
import hastane.model.Hizmet;
import hastane.model.KurulusTuru;
import hastane.model.LaboratuvarHizmeti;
import hastane.model.MuayeneHizmeti;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Geliştirme denemesi (3. gün): Kuruluş, şube, bölüm ve çalışma saatleri. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");

        SaglikKurulusu hastane = new SaglikKurulusu("Yıldız Hastanesi", KurulusTuru.HASTANE);
        CalismaSaatleri merkezSaat = CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0));
        merkezSaat.gunAyarla(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(13, 0));
        Sube merkez = hastane.subeEkle("Merkez Şube", "Adapazarı / Sakarya", merkezSaat);
        Sube serdivan = hastane.subeEkle("Serdivan Şubesi", "Serdivan / Sakarya",
                CalismaSaatleri.haftaIci(LocalTime.of(9, 0), LocalTime.of(17, 0)));

        Hizmet muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        Hizmet kanSayimi = new LaboratuvarHizmeti("Tam Kan Sayımı", 10, new BigDecimal("250"), 0);
        merkez.bolumEkle("Dahiliye").hizmetEkle(muayene);
        merkez.bolumEkle("Laboratuvar").hizmetEkle(kanSayimi);
        serdivan.bolumEkle("Dahiliye").hizmetEkle(muayene);   // aynı hizmet iki şubede

        System.out.println();
        System.out.println(hastane + "  çok şubeli mi? " + hastane.cokSubeliMi());
        for (Sube s : hastane.getSubeler()) {
            System.out.println("  " + s.getAd() + " - " + s.getCalismaSaatleri());
            for (Bolum b : s.getBolumler()) {
                System.out.println("    " + b + ": " + b.getHizmetler());
            }
        }
        System.out.println("Serdivan'da kan sayımı var mı? " + serdivan.hizmetSunuyorMu(kanSayimi));
        System.out.println("Merkez pazar günü açık mı? " + merkezSaat.acikMi(DayOfWeek.SUNDAY));
    }
}
