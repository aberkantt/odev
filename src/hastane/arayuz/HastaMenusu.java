package hastane.arayuz;

import hastane.model.Bicim;
import hastane.model.Calisan;
import hastane.model.Hasta;
import hastane.model.Hizmet;
import hastane.model.Randevu;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;
import hastane.servis.HastaneSistemi;
import hastane.servis.RandevuServisi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Hasta rolündeki kullanıcının menüsü. */
public class HastaMenusu extends Menu {

    private static final int KAC_GUN_ILERI = 14;

    private final Hasta hasta;

    public HastaMenusu(HastaneSistemi sistem, Konsol konsol, Hasta hasta) {
        super(sistem, konsol);
        this.hasta = hasta;
        secenekEkle("Randevu al", this::randevuAl);
        secenekEkle("Randevularım", this::randevularim);
        secenekEkle("Randevu iptal et", this::randevuIptal);
        secenekEkle("Kurumlar, hizmetler ve ücretler", this::kurumlariGoster);
        secenekEkle("Bildirimlerim", () -> bildirimleriGoster(hasta));
    }

    @Override
    protected String getBaslik() {
        return "HASTA MENÜSÜ - " + hasta.getAdSoyad();
    }

    @Override
    protected void ustBilgiYaz() {
        okunmamisBildirimUyarisi(hasta);
    }

    private void randevuAl() {
        RandevuServisi rs = sistem.getRandevuServisi();

        // 1) Kurum
        SaglikKurulusu kurulus = konsol.listedenSec("Kurum seçin", sistem.getDepo().getKuruluslar());
        if (kurulus == null) {
            return;
        }
        // 2) Şube (tek şube varsa otomatik seçilir)
        Sube sube = kurulus.getSubeler().size() == 1 ? kurulus.getSubeler().get(0)
                : konsol.listedenSec("Şube seçin", kurulus.getSubeler(), s -> s.getAd() + " - " + s.getAdres());
        if (sube == null) {
            return;
        }
        // 3) Hizmet
        Hizmet hizmet = konsol.listedenSec("Hizmet seçin", sube.getSunulanHizmetler(),
                h -> sube.hizmetinBolumu(h) + " / " + h);
        if (hizmet == null) {
            return;
        }
        // 4) Çalışan: bu hizmeti veren ve bu şubede çalışanlar
        List<Calisan> adaylar = new ArrayList<>();
        for (Calisan c : sistem.getDepo().calisanlariGetir(kurulus)) {
            if (c.hizmetVerebilirMi(hizmet) && c.subedeCalisiyorMu(sube)) {
                adaylar.add(c);
            }
        }
        Calisan calisan = konsol.listedenSec("Çalışan seçin", adaylar);
        if (calisan == null) {
            return;
        }
        // 5) Gün ve saat: sadece uygunluk aralıklarındaki ve çakışmayan saatler listelenir
        Map<LocalDate, List<LocalDateTime>> gunler = rs.uygunGunler(hasta, calisan, hizmet, sube, KAC_GUN_ILERI);
        if (gunler.isEmpty()) {
            konsol.hata("Önümüzdeki " + KAC_GUN_ILERI + " gün içinde boş saat bulunamadı.");
            return;
        }
        LocalDate gun = konsol.listedenSec("Gün seçin", new ArrayList<>(gunler.keySet()),
                g -> Bicim.tarih(g) + "  (" + gunler.get(g).size() + " boş saat)");
        if (gun == null) {
            return;
        }
        LocalDateTime baslangic = konsol.listedenSec("Saat seçin", gunler.get(gun),
                z -> Bicim.saat(z.toLocalTime()) + " - " + Bicim.saat(z.toLocalTime().plusMinutes(hizmet.getSureDakika())));
        if (baslangic == null) {
            return;
        }
        // 6) Özet ve onay
        konsol.yaz("");
        konsol.yaz("İşlem   : " + hizmet.getAd() + " [" + hizmet.getTur() + "]");
        konsol.yaz("Çalışan : " + calisan);
        konsol.yaz("Yer     : " + sube);
        konsol.yaz("Zaman   : " + Bicim.tarih(gun) + " " + Bicim.saat(baslangic.toLocalTime()));
        konsol.yaz("Süre    : " + hizmet.getSureDakika() + " dk   Ücret: " + Bicim.para(hizmet.getUcret()));
        konsol.yaz("Hazırlık: " + hizmet.getHazirlikBilgisi());
        if (konsol.evetMi("Randevu talebi oluşturulsun mu?")) {
            Randevu r = rs.randevuOlustur(hasta, calisan, hizmet, sube, baslangic);
            konsol.basari("Randevu talebiniz alındı (#" + r.getId() + "). Çalışan onayladığında kesinleşecek.");
        }
    }

    private void randevularim() {
        List<Randevu> liste = sistem.getRandevuServisi().hastaRandevulari(hasta);
        Randevu secilen = konsol.listedenSec("Randevularım (detay için seçin)", liste);
        if (secilen != null) {
            konsol.yaz(secilen.detay());
        }
    }

    private void randevuIptal() {
        List<Randevu> aktifler = new ArrayList<>();
        for (Randevu r : sistem.getRandevuServisi().hastaRandevulari(hasta)) {
            if (r.aktifMi()) {
                aktifler.add(r);
            }
        }
        Randevu r = konsol.listedenSec("İptal edilecek randevu", aktifler);
        if (r == null) {
            return;
        }
        String neden = konsol.metinOku("İptal nedeni");
        sistem.getRandevuServisi().iptalEt(hasta, r, neden);
        konsol.basari("Randevu #" + r.getId() + " iptal edildi.");
    }

    private void kurumlariGoster() {
        for (SaglikKurulusu k : sistem.getDepo().getKuruluslar()) {
            kurulusDetayYaz(k);
        }
    }
}
