package hastane;

import hastane.model.Hasta;
import hastane.model.Kisi;

import java.time.LocalDate;

/** Geliştirme denemesi (1. gün): Kisi soyut sınıfı ve Hasta alt sınıfı. Menüler ilerleyen günlerde eklenecek. */
public class Main {

    public static void main(String[] args) {
        System.out.println("Hastane / Klinik Randevu Sistemi - geliştirme sürümü");
        System.out.println();

        // Değişkenin tipi soyut Kisi, nesnenin kendisi Hasta
        Kisi kisi = new Hasta("Ahmet", "Çelik", "0535 000 00 31", "ahmet.celik@hasta.test", "1234",
                "10000000146", LocalDate.of(1990, 5, 14));
        System.out.println("Oluşturulan kişi : " + kisi);
        System.out.println("Rolü             : " + kisi.getRol().getGorunenAd());
        System.out.println("Şifre 1234 doğru mu? " + kisi.sifreDogruMu("1234"));
        System.out.println("Şifre 0000 doğru mu? " + kisi.sifreDogruMu("0000"));

        // Doğrulama: boş ad ile nesne oluşturulamaz
        try {
            new Hasta(" ", "Deneme", "0500", "deneme@hasta.test", "1234", "10000000146", LocalDate.of(1990, 1, 1));
        } catch (IllegalArgumentException e) {
            System.out.println("Beklenen hata     : " + e.getMessage());
        }
    }
}
