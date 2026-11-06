package hastane;

import hastane.istisna.HastaneException;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.Hasta;
import hastane.model.Hizmet;
import hastane.model.KurulusTuru;
import hastane.model.MuayeneHizmeti;
import hastane.model.Randevu;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/** Geliştirme denemesi (5. gün): Randevu nesnesi ve onay mekanizması. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");

        SaglikKurulusu hastane = new SaglikKurulusu("Yıldız Hastanesi", KurulusTuru.HASTANE);
        Sube merkez = hastane.subeEkle("Merkez Şube", "Adapazarı / Sakarya",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        Hizmet muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        merkez.bolumEkle("Dahiliye").hizmetEkle(muayene);
        Doktor ayse = new Doktor("Ayşe", "Yılmaz", "0533", "ayse.yilmaz@yildiz.test", "1234",
                hastane, "Uzm. Dr.", "İç Hastalıkları");
        Hasta ahmet = new Hasta("Ahmet", "Çelik", "0535", "ahmet.celik@hasta.test", "1234",
                "10000000146", LocalDate.of(1990, 5, 14));

        LocalDate pazartesi = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        Randevu r = new Randevu(ahmet, ayse, muayene, merkez, pazartesi.atTime(9, 0));
        System.out.println();
        System.out.println(r.detay());

        r.onayla();
        System.out.println();
        System.out.println("Onaylandıktan sonra: " + r.getDurum().getGorunenAd());

        try {
            r.onayla();   // ikinci kez onaylanamaz
        } catch (HastaneException e) {
            System.out.println("Beklenen hata: " + e.getMessage());
        }

        muayene.setUcret(new BigDecimal("900"));   // zam geldi
        System.out.println("Hizmetin yeni ücreti: " + muayene.getUcret() + ", randevudaki ücret: " + r.getUcret());
    }
}
