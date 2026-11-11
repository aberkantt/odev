package hastane.servis;

import hastane.bildirim.BildirimServisi;
import hastane.bildirim.EpostaBildirimi;
import hastane.bildirim.SmsBildirimi;
import hastane.bildirim.UygulamaIciBildirim;
import hastane.depo.VeriDeposu;

/**
 * Uygulamanın tüm parçalarını bir araya getiren giriş noktası (FACADE deseni).
 * Arayüz katmanı depo ve servislerin nasıl kurulduğunu bilmez; sadece bu sınıfı kullanır.
 */
public class HastaneSistemi {

    private final VeriDeposu depo = new VeriDeposu();
    private final UygulamaIciBildirim gelenKutusu = new UygulamaIciBildirim();
    private final BildirimServisi bildirimServisi = new BildirimServisi();
    private final RandevuServisi randevuServisi;
    private final YonetimServisi yonetimServisi;
    private final KimlikServisi kimlikServisi;

    public HastaneSistemi() {
        bildirimServisi.kanalEkle(gelenKutusu);
        randevuServisi = new RandevuServisi(depo, bildirimServisi);
        yonetimServisi = new YonetimServisi(depo);
        kimlikServisi = new KimlikServisi(depo);
    }

    /** E-posta ve SMS kanallarını (simülasyon) devreye alır. */
    public void disKanallariEkle() {
        bildirimServisi.kanalEkle(new EpostaBildirimi());
        bildirimServisi.kanalEkle(new SmsBildirimi());
    }

    public VeriDeposu getDepo() {
        return depo;
    }

    public UygulamaIciBildirim getGelenKutusu() {
        return gelenKutusu;
    }

    public RandevuServisi getRandevuServisi() {
        return randevuServisi;
    }

    public YonetimServisi getYonetimServisi() {
        return yonetimServisi;
    }

    public KimlikServisi getKimlikServisi() {
        return kimlikServisi;
    }
}
