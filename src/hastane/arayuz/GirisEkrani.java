package hastane.arayuz;

import hastane.DemoVeri;
import hastane.istisna.HastaneException;
import hastane.model.Calisan;
import hastane.model.Hasta;
import hastane.model.Kisi;
import hastane.model.Yonetici;
import hastane.servis.HastaneSistemi;

import java.time.LocalDate;

/** Uygulamanın açılış ekranı: giriş, hasta kaydı ve demo hesap listesi. */
public class GirisEkrani {

    private final HastaneSistemi sistem;
    private final Konsol konsol;

    public GirisEkrani(HastaneSistemi sistem, Konsol konsol) {
        this.sistem = sistem;
        this.konsol = konsol;
    }

    public void baslat() {
        while (true) {
            konsol.baslik("HASTANE / KLİNİK RANDEVU SİSTEMİ");
            konsol.yaz("   1) Giriş yap");
            konsol.yaz("   2) Yeni hasta kaydı");
            konsol.yaz("   3) Demo hesapları göster");
            konsol.yaz("   0) Programdan çık");
            int secim = konsol.sayiOku("Seçiminiz", 0, 3);
            if (secim == 0) {
                konsol.yaz("Güle güle!");
                return;
            }
            try {
                switch (secim) {
                    case 1 -> girisYap();
                    case 2 -> hastaKaydi();
                    default -> demoHesaplar();
                }
            } catch (HastaneException | IllegalArgumentException e) {
                konsol.hata(e.getMessage());
            }
        }
    }

    private void girisYap() {
        String eposta = konsol.metinOku("E-posta");
        String sifre = konsol.metinOku("Şifre");
        Kisi kisi = sistem.getKimlikServisi().girisYap(eposta, sifre);
        konsol.basari("Hoş geldiniz, " + kisi.getAdSoyad() + " (" + kisi.getRol().getGorunenAd() + ")");
        menuOlustur(kisi).calistir();
        konsol.basari("Oturum kapatıldı.");
    }

    /**
     * Giriş yapan kişinin türüne göre doğru menüyü üretir (FACTORY METHOD).
     * Dönüş tipi soyut Menu'dür; çağıran taraf hangi alt menü geldiğini bilmeden calistir() der.
     */
    private Menu menuOlustur(Kisi kisi) {
        if (kisi instanceof Hasta hasta) {
            return new HastaMenusu(sistem, konsol, hasta);
        }
        if (kisi instanceof Calisan calisan) {
            return new CalisanMenusu(sistem, konsol, calisan);
        }
        if (kisi instanceof Yonetici yonetici) {
            return new YoneticiMenusu(sistem, konsol, yonetici);
        }
        throw new IllegalStateException("Bilinmeyen kullanıcı türü: " + kisi.getClass().getSimpleName());
    }

    private void hastaKaydi() {
        konsol.yaz("-- Yeni hasta kaydı --");
        String ad = konsol.metinOku("Ad");
        String soyad = konsol.metinOku("Soyad");
        String tc = konsol.metinOku("TC kimlik no (11 hane)");
        LocalDate dogum = konsol.tarihOku("Doğum tarihi");
        String telefon = konsol.metinOku("Telefon");
        String eposta = konsol.metinOku("E-posta");
        String sifre = konsol.metinOku("Şifre (en az 4 karakter)");
        Hasta h = sistem.getKimlikServisi().hastaKaydet(ad, soyad, telefon, eposta, sifre, tc, dogum);
        konsol.basari("Kayıt tamamlandı. " + h.getEposta() + " ile giriş yapabilirsiniz.");
    }

    private void demoHesaplar() {
        konsol.yaz("-- Demo hesaplar (hepsinin şifresi: " + DemoVeri.DEMO_SIFRE + ") --");
        for (Kisi k : sistem.getDepo().getKisiler()) {
            konsol.yaz(String.format("  %-26s %-32s %s", k.getRol().getGorunenAd(), k.getEposta(), k.getAdSoyad()));
        }
    }
}
