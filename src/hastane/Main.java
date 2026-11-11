package hastane;

import hastane.istisna.HastaneException;
import hastane.model.Calisan;
import hastane.model.CalismaSaatleri;
import hastane.model.Hasta;
import hastane.model.Randevu;
import hastane.model.SaglikKurulusu;
import hastane.model.Yonetici;
import hastane.servis.HastaneSistemi;

import java.time.LocalTime;

/** Geliştirme denemesi (8. gün): Demo veriler, giriş ve yetki kontrolü. Menüler bir sonraki adımda. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");
        HastaneSistemi sistem = DemoVeri.olustur();

        System.out.println();
        for (SaglikKurulusu k : sistem.getDepo().getKuruluslar()) {
            System.out.println(k);
            for (Calisan c : sistem.getDepo().calisanlariGetir(k)) {
                System.out.println("   " + c + " -> " + c.getUygunluklar().size() + " uygunluk aralığı");
            }
        }
        System.out.println("Hasta sayısı: " + sistem.getDepo().kisileriGetir(Hasta.class).size());
        System.out.println("Randevular:");
        for (Randevu r : sistem.getDepo().getRandevular()) {
            System.out.println("   " + r);
        }

        // Giriş ve yetki kontrolü
        Yonetici selin = (Yonetici) sistem.getKimlikServisi().girisYap("selin.koc@papatya.test", "1234");
        SaglikKurulusu yildiz = sistem.getDepo().getKuruluslar().get(0);
        try {
            sistem.getYonetimServisi().subeEkle(selin, yildiz, "Yetkisiz Şube", "-",
                    CalismaSaatleri.haftaIci(LocalTime.of(9, 0), LocalTime.of(17, 0)));
        } catch (HastaneException e) {
            System.out.println("Beklenen yetki hatası: " + e.getMessage());
        }
        try {
            sistem.getKimlikServisi().girisYap("selin.koc@papatya.test", "yanlis");
        } catch (HastaneException e) {
            System.out.println("Beklenen giriş hatası: " + e.getMessage());
        }
    }
}
