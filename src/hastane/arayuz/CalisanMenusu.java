package hastane.arayuz;

import hastane.model.Bicim;
import hastane.model.Calisan;
import hastane.model.Randevu;
import hastane.model.RandevuDurumu;
import hastane.servis.HastaneSistemi;
import hastane.servis.RandevuServisi;

import java.util.ArrayList;
import java.util.List;

/**
 * Doktor ve sağlık personelinin ortak menüsü.
 * Menü Calisan tipiyle çalışır; gelen nesnenin Doktor mu SaglikPersoneli mi olduğunu bilmesine gerek yoktur
 * (ÇOK BİÇİMLİLİK: başlıktaki rol adı getRol() ile nesnenin kendisinden gelir).
 */
public class CalisanMenusu extends Menu {

    private final Calisan calisan;

    public CalisanMenusu(HastaneSistemi sistem, Konsol konsol, Calisan calisan) {
        super(sistem, konsol);
        this.calisan = calisan;
        secenekEkle("Onay bekleyen randevular (onayla / reddet)", this::onayBekleyenler);
        secenekEkle("Randevu takvimim", this::takvim);
        secenekEkle("Randevuyu tamamlandı olarak işaretle", this::tamamla);
        secenekEkle("Randevu iptal et", this::iptalEt);
        secenekEkle("Uygunluk zamanlarım", this::uygunluklar);
        secenekEkle("Uygunluk zamanı ekle", () -> uygunlukEkleAkisi(calisan, calisan));
        secenekEkle("Verdiğim hizmetler", this::hizmetler);
        secenekEkle("Bildirimlerim", () -> bildirimleriGoster(calisan));
    }

    @Override
    protected String getBaslik() {
        return calisan.getRol().getGorunenAd().toUpperCase(Bicim.TR) + " MENÜSÜ - " + calisan;
    }

    @Override
    protected void ustBilgiYaz() {
        int bekleyen = sistem.getRandevuServisi().onayBekleyenler(calisan).size();
        if (bekleyen > 0) {
            konsol.yaz("  * Onayınızı bekleyen " + bekleyen + " randevu var.");
        }
        okunmamisBildirimUyarisi(calisan);
    }

    private void onayBekleyenler() {
        RandevuServisi rs = sistem.getRandevuServisi();
        while (true) {
            Randevu r = konsol.listedenSec("Onay bekleyen randevular", rs.onayBekleyenler(calisan));
            if (r == null) {
                return;
            }
            konsol.yaz(r.detay());
            int islem = konsol.sayiOku("1) Onayla  2) Reddet  0) Vazgeç", 0, 2);
            if (islem == 1) {
                rs.onayla(calisan, r);
                konsol.basari("Randevu #" + r.getId() + " onaylandı, hastaya bildirim gönderildi.");
            } else if (islem == 2) {
                rs.reddet(calisan, r, konsol.metinOku("Ret nedeni"));
                konsol.basari("Randevu #" + r.getId() + " reddedildi, hastaya bildirim gönderildi.");
            }
        }
    }

    private void takvim() {
        konsol.listele("Aktif randevularım (onay bekleyen + onaylı)", durumaGore(null));
    }

    private void tamamla() {
        Randevu r = konsol.listedenSec("Onaylı randevular", durumaGore(RandevuDurumu.ONAYLANDI));
        if (r != null) {
            sistem.getRandevuServisi().tamamla(calisan, r);
            konsol.basari("Randevu #" + r.getId() + " tamamlandı.");
        }
    }

    private void iptalEt() {
        Randevu r = konsol.listedenSec("İptal edilecek randevu", durumaGore(null));
        if (r != null) {
            sistem.getRandevuServisi().iptalEt(calisan, r, konsol.metinOku("İptal nedeni"));
            konsol.basari("Randevu #" + r.getId() + " iptal edildi, hastaya bildirim gönderildi.");
        }
    }

    /** durum == null ise tüm aktif randevular, değilse sadece o durumdakiler. */
    private List<Randevu> durumaGore(RandevuDurumu durum) {
        List<Randevu> sonuc = new ArrayList<>();
        for (Randevu r : sistem.getRandevuServisi().calisanRandevulari(calisan)) {
            if (durum == null ? r.aktifMi() : r.getDurum() == durum) {
                sonuc.add(r);
            }
        }
        return sonuc;
    }

    private void uygunluklar() {
        konsol.listele("Uygunluk zamanlarım", calisan.getUygunluklar());
    }

    private void hizmetler() {
        konsol.listele("Verdiğim hizmetler", calisan.getVerdigiHizmetler());
    }
}
