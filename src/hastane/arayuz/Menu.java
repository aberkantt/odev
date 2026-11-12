package hastane.arayuz;

import hastane.istisna.HastaneException;
import hastane.model.Bicim;
import hastane.model.Bolum;
import hastane.model.Calisan;
import hastane.model.Hizmet;
import hastane.model.Kisi;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;
import hastane.servis.HastaneSistemi;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tüm rol menülerinin soyut atası.
 *
 * TEMPLATE METHOD deseni: calistir() metodundaki menü döngüsü (başlık yaz, seçenekleri listele,
 * seçimi oku, işlemi çalıştır, hatayı yakala) tüm menülerde aynıdır ve burada bir kez yazılır.
 * Alt sınıflar sadece başlığı ve seçenekleri belirler.
 */
public abstract class Menu {

    protected final HastaneSistemi sistem;
    protected final Konsol konsol;
    private final List<Secenek> secenekler = new ArrayList<>();

    protected Menu(HastaneSistemi sistem, Konsol konsol) {
        this.sistem = sistem;
        this.konsol = konsol;
    }

    /** Menünün başlığı (her alt sınıf kendi başlığını verir). */
    protected abstract String getBaslik();

    /** Başlığın altında gösterilecek ek bilgi. İsteğe bağlı olarak override edilir (hook metot). */
    protected void ustBilgiYaz() {
    }

    protected final void secenekEkle(String etiket, Runnable islem) {
        secenekler.add(new Secenek(etiket, islem));
    }

    /** Menü döngüsü. "0) Çıkış yap" seçilene kadar döner. */
    public final void calistir() {
        while (true) {
            konsol.baslik(getBaslik());
            ustBilgiYaz();
            for (int i = 0; i < secenekler.size(); i++) {
                konsol.yaz(String.format("  %2d) %s", i + 1, secenekler.get(i).etiket));
            }
            konsol.yaz("   0) Çıkış yap");
            int secim = konsol.sayiOku("Seçiminiz", 0, secenekler.size());
            if (secim == 0) {
                return;
            }
            try {
                secenekler.get(secim - 1).islem.run();
            } catch (HastaneException | IllegalArgumentException e) {
                konsol.hata(e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    //  Birden fazla menünün kullandığı ortak işlemler
    // ------------------------------------------------------------------

    protected void bildirimleriGoster(Kisi kisi) {
        List<String> bildirimler = sistem.getGelenKutusu().oku(kisi);
        konsol.listele("Bildirimler", bildirimler);
    }

    protected void okunmamisBildirimUyarisi(Kisi kisi) {
        int sayi = sistem.getGelenKutusu().okunmamisSayisi(kisi);
        if (sayi > 0) {
            konsol.yaz("  * " + sayi + " okunmamış bildiriminiz var.");
        }
    }

    protected void kurulusDetayYaz(SaglikKurulusu kurulus) {
        konsol.yaz("");
        konsol.yaz("# " + kurulus);
        for (Sube sube : kurulus.getSubeler()) {
            konsol.yaz("  Şube: " + sube.getAd() + " (" + sube.getAdres() + ")");
            konsol.yaz("    Çalışma saatleri: " + sube.getCalismaSaatleri());
            for (Bolum bolum : sube.getBolumler()) {
                konsol.yaz("    Bölüm: " + bolum.getAd());
                for (Hizmet h : bolum.getHizmetler()) {
                    konsol.yaz("      - " + h);
                }
            }
        }
    }

    protected void uygunlukEkleAkisi(Kisi islemYapan, Calisan calisan) {
        Sube sube = konsol.listedenSec("Şube seçin", calisan.getKurulus().getSubeler(),
                s -> s.getAd() + "  [" + s.getCalismaSaatleri() + "]");
        if (sube == null) {
            return;
        }
        List<DayOfWeek> acikGunler = new ArrayList<>();
        for (DayOfWeek gun : DayOfWeek.values()) {
            if (sube.getCalismaSaatleri().acikMi(gun)) {
                acikGunler.add(gun);
            }
        }
        DayOfWeek gun = konsol.listedenSec("Gün seçin", acikGunler,
                g -> Bicim.gun(g) + " (şube " + sube.getCalismaSaatleri().getSaatler(g) + " açık)");
        if (gun == null) {
            return;
        }
        LocalTime bas = konsol.saatOku("Başlangıç saati");
        LocalTime bit = konsol.saatOku("Bitiş saati");
        sistem.getYonetimServisi().uygunlukEkle(islemYapan, calisan, sube, gun, new SaatAraligi(bas, bit));
        konsol.basari("Uygunluk eklendi: " + Bicim.gun(gun) + " " + Bicim.saat(bas) + "-" + Bicim.saat(bit)
                + " @ " + sube.getAd());
    }

    /** Menü seçeneği: ekranda görünen yazı + seçilince çalışacak kod. */
    private static final class Secenek {
        private final String etiket;
        private final Runnable islem;

        private Secenek(String etiket, Runnable islem) {
            this.etiket = etiket;
            this.islem = islem;
        }
    }
}
