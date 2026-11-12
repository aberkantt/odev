package hastane.arayuz;

import hastane.model.Bicim;
import hastane.model.Bolum;
import hastane.model.Calisan;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.GoruntulemeHizmeti;
import hastane.model.Hizmet;
import hastane.model.KurulusTuru;
import hastane.model.LaboratuvarHizmeti;
import hastane.model.MuayeneHizmeti;
import hastane.model.Randevu;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.SaglikPersoneli;
import hastane.model.Sube;
import hastane.model.Yonetici;
import hastane.servis.HastaneSistemi;
import hastane.servis.RandevuServisi;
import hastane.servis.YonetimServisi;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Hastane / klinik yöneticisinin menüsü: tanımlamalar, çalışan yönetimi, randevu onayları. */
public class YoneticiMenusu extends Menu {

    private final Yonetici yonetici;
    private final YonetimServisi ys;

    public YoneticiMenusu(HastaneSistemi sistem, Konsol konsol, Yonetici yonetici) {
        super(sistem, konsol);
        this.yonetici = yonetici;
        this.ys = sistem.getYonetimServisi();
        secenekEkle("Kuruluşlarımı görüntüle", this::kuruluslarim);
        secenekEkle("Yeni kuruluş tanımla (hastane / klinik)", this::kurulusTanimla);
        secenekEkle("Şube ekle", this::subeEkle);
        secenekEkle("Şube çalışma saatini düzenle", this::calismaSaatiDuzenle);
        secenekEkle("Bölüm ekle", this::bolumEkle);
        secenekEkle("Hizmet tanımla (süre ve ücret)", this::hizmetTanimla);
        secenekEkle("Doktor kaydet", this::doktorKaydet);
        secenekEkle("Sağlık personeli kaydet", this::personelKaydet);
        secenekEkle("Çalışana hizmet ata", this::hizmetAta);
        secenekEkle("Çalışana uygunluk zamanı ekle", this::uygunlukEkle);
        secenekEkle("Çalışanları listele", this::calisanlariListele);
        secenekEkle("Randevuları yönet (onay / ret / iptal / tamamla)", this::randevulariYonet);
    }

    @Override
    protected String getBaslik() {
        return "YÖNETİCİ MENÜSÜ - " + yonetici.getAdSoyad();
    }

    // ------------------------------------------------------------------
    //  Seçim yardımcıları
    // ------------------------------------------------------------------

    /**
     * Sistemdeki TÜM kuruluşları listeler, yetkili olunanları işaretler.
     * Yetkisiz bir kuruluş seçilirse servis katmanı YetkiException fırlatır (yetki kontrolü canlı görülebilir).
     */
    private SaglikKurulusu kurulusSec() {
        SaglikKurulusu k = konsol.listedenSec("Kuruluş seçin", sistem.getDepo().getKuruluslar(),
                kurulus -> kurulus + (yonetici.yetkiliMi(kurulus) ? "  [yetkili]" : ""));
        if (k != null) {
            ys.yetkiKontrol(yonetici, k);
        }
        return k;
    }

    private Sube subeSec(SaglikKurulusu kurulus) {
        if (kurulus.getSubeler().size() == 1) {
            return kurulus.getSubeler().get(0);
        }
        return konsol.listedenSec("Şube seçin", kurulus.getSubeler(), s -> s.getAd());
    }

    // ------------------------------------------------------------------
    //  Kuruluş tanımlamaları
    // ------------------------------------------------------------------

    private void kuruluslarim() {
        if (yonetici.getYonettigiKuruluslar().isEmpty()) {
            konsol.yaz("Henüz yönettiğiniz bir kuruluş yok.");
        }
        for (SaglikKurulusu k : yonetici.getYonettigiKuruluslar()) {
            kurulusDetayYaz(k);
        }
    }

    private void kurulusTanimla() {
        String ad = konsol.metinOku("Kuruluş adı");
        KurulusTuru tur = konsol.listedenSec("Tür seçin", Arrays.asList(KurulusTuru.values()), KurulusTuru::getGorunenAd);
        if (tur == null) {
            return;
        }
        SaglikKurulusu k = ys.kurulusOlustur(yonetici, ad, tur);
        konsol.basari(k.getAd() + " oluşturuldu. Şimdi ilk şubeyi ekleyelim (tek şubeli ise adını 'Merkez' verebilirsiniz).");
        subeEkleAkisi(k);
    }

    private void subeEkle() {
        SaglikKurulusu k = kurulusSec();
        if (k != null) {
            subeEkleAkisi(k);
        }
    }

    private void subeEkleAkisi(SaglikKurulusu kurulus) {
        String ad = konsol.metinOku("Şube adı");
        String adres = konsol.metinOku("Adres");
        konsol.yaz("Hafta içi (Pazartesi-Cuma) çalışma saatleri:");
        LocalTime acilis = konsol.saatOku("  Açılış");
        LocalTime kapanis = konsol.saatOku("  Kapanış");
        CalismaSaatleri cs = CalismaSaatleri.haftaIci(acilis, kapanis);
        for (DayOfWeek gun : new DayOfWeek[]{DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
            if (konsol.evetMi(Bicim.gun(gun) + " açık mı?")) {
                cs.gunAyarla(gun, konsol.saatOku("  Açılış"), konsol.saatOku("  Kapanış"));
            }
        }
        Sube sube = ys.subeEkle(yonetici, kurulus, ad, adres, cs);
        konsol.basari("Şube eklendi: " + sube + " [" + cs + "]");
    }

    private void calismaSaatiDuzenle() {
        SaglikKurulusu k = kurulusSec();
        Sube sube = k == null ? null : subeSec(k);
        if (sube == null) {
            return;
        }
        konsol.yaz("Mevcut: " + sube.getCalismaSaatleri());
        DayOfWeek gun = konsol.listedenSec("Gün seçin", Arrays.asList(DayOfWeek.values()), Bicim::gun);
        if (gun == null) {
            return;
        }
        SaatAraligi aralik = null;
        if (!konsol.evetMi("Bu gün kapalı mı olacak?")) {
            aralik = new SaatAraligi(konsol.saatOku("Açılış"), konsol.saatOku("Kapanış"));
        }
        ys.calismaSaatiAyarla(yonetici, sube, gun, aralik);
        konsol.basari("Güncellendi: " + sube.getCalismaSaatleri());
    }

    private void bolumEkle() {
        SaglikKurulusu k = kurulusSec();
        Sube sube = k == null ? null : subeSec(k);
        if (sube != null) {
            Bolum b = ys.bolumEkle(yonetici, sube, konsol.metinOku("Bölüm adı"));
            konsol.basari(b + " bölümü " + sube + " şubesine eklendi.");
        }
    }

    private void hizmetTanimla() {
        SaglikKurulusu k = kurulusSec();
        Sube sube = k == null ? null : subeSec(k);
        if (sube == null) {
            return;
        }
        Bolum bolum = konsol.listedenSec("Bölüm seçin", sube.getBolumler());
        if (bolum == null) {
            return;
        }
        int secim = konsol.sayiOku("1) Yeni hizmet oluştur  2) Kurumdaki mevcut bir hizmeti bu bölüme ekle", 1, 2);
        Hizmet hizmet = secim == 1 ? yeniHizmetOku()
                : konsol.listedenSec("Mevcut hizmetler", k.getTumHizmetler());
        if (hizmet != null) {
            ys.hizmetEkle(yonetici, sube, bolum, hizmet);
            konsol.basari("Hizmet eklendi: " + hizmet + " -> " + sube + " / " + bolum);
        }
    }

    /** Seçilen türe göre doğru alt sınıfı üretir (her biri Hizmet'tir). */
    private Hizmet yeniHizmetOku() {
        int tur = konsol.sayiOku("Hizmet türü: 1) Muayene  2) Görüntüleme  3) Laboratuvar", 1, 3);
        String ad = konsol.metinOku("Hizmet adı");
        int sure = konsol.sayiOku("Süre (dakika)", 1, 480);
        BigDecimal ucret = konsol.tutarOku("Ücret (TL)");
        switch (tur) {
            case 1:
                return new MuayeneHizmeti(ad, sure, ucret);
            case 2:
                String cihaz = konsol.metinOku("Cihaz (Röntgen, MR, Ultrason ...)");
                boolean kontrast = konsol.evetMi("Kontrast madde kullanılıyor mu?");
                return new GoruntulemeHizmeti(ad, sure, ucret, cihaz, kontrast);
            default:
                int aclik = konsol.sayiOku("Kaç saat açlık gerekli? (gerekmiyorsa 0)", 0, 24);
                return new LaboratuvarHizmeti(ad, sure, ucret, aclik);
        }
    }

    // ------------------------------------------------------------------
    //  Çalışan yönetimi
    // ------------------------------------------------------------------

    private void doktorKaydet() {
        SaglikKurulusu k = kurulusSec();
        if (k == null) {
            return;
        }
        String[] kisi = kisiBilgileriOku();
        String unvan = konsol.metinOku("Unvan (Dr., Uzm. Dr., Doç. Dr. ...)");
        String uzmanlik = konsol.metinOku("Uzmanlık alanı");
        Doktor d = ys.doktorKaydet(yonetici, k, kisi[0], kisi[1], kisi[2], kisi[3], kisi[4], unvan, uzmanlik);
        konsol.basari(d + " kaydedildi. Şimdi 'Çalışana hizmet ata' ve 'uygunluk zamanı ekle' adımlarını yapın.");
    }

    private void personelKaydet() {
        SaglikKurulusu k = kurulusSec();
        if (k == null) {
            return;
        }
        String[] kisi = kisiBilgileriOku();
        String gorev = konsol.metinOku("Görev (Hemşire, Laborant, Radyoloji Teknisyeni ...)");
        SaglikPersoneli p = ys.personelKaydet(yonetici, k, kisi[0], kisi[1], kisi[2], kisi[3], kisi[4], gorev);
        konsol.basari(p + " kaydedildi.");
    }

    /** ad, soyad, telefon, e-posta, şifre */
    private String[] kisiBilgileriOku() {
        return new String[]{
                konsol.metinOku("Ad"),
                konsol.metinOku("Soyad"),
                konsol.metinOku("Telefon"),
                konsol.metinOku("E-posta"),
                konsol.metinOku("Geçici şifre (en az 4 karakter)")
        };
    }

    private Calisan calisanSec(SaglikKurulusu kurulus) {
        return konsol.listedenSec("Çalışan seçin", sistem.getDepo().calisanlariGetir(kurulus));
    }

    private void hizmetAta() {
        SaglikKurulusu k = kurulusSec();
        Calisan c = k == null ? null : calisanSec(k);
        if (c == null) {
            return;
        }
        List<Hizmet> verilmeyenler = new ArrayList<>();
        for (Hizmet h : k.getTumHizmetler()) {
            if (!c.hizmetVerebilirMi(h)) {
                verilmeyenler.add(h);
            }
        }
        Hizmet h = konsol.listedenSec("Atanacak hizmet", verilmeyenler);
        if (h != null) {
            ys.calisanaHizmetAta(yonetici, c, h);
            konsol.basari(c + " artık '" + h.getAd() + "' hizmetini veriyor.");
        }
    }

    private void uygunlukEkle() {
        SaglikKurulusu k = kurulusSec();
        Calisan c = k == null ? null : calisanSec(k);
        if (c != null) {
            konsol.listele("Mevcut uygunluklar", c.getUygunluklar());
            uygunlukEkleAkisi(yonetici, c);
        }
    }

    private void calisanlariListele() {
        for (SaglikKurulusu k : yonetici.getYonettigiKuruluslar()) {
            konsol.yaz("");
            konsol.yaz("# " + k.getAd());
            for (Calisan c : sistem.getDepo().calisanlariGetir(k)) {
                konsol.yaz("  " + c + "  <" + c.getEposta() + ">");
                konsol.yaz("     Hizmetler  : " + hizmetAdlari(c.getVerdigiHizmetler()));
                konsol.yaz("     Uygunluklar: " + c.getUygunluklar());
            }
        }
    }

    private static String hizmetAdlari(List<Hizmet> hizmetler) {
        List<String> adlar = new ArrayList<>();
        for (Hizmet h : hizmetler) {
            adlar.add(h.getAd());
        }
        return adlar.isEmpty() ? "(yok)" : String.join(", ", adlar);
    }

    // ------------------------------------------------------------------
    //  Randevu yönetimi
    // ------------------------------------------------------------------

    private void randevulariYonet() {
        SaglikKurulusu k = kurulusSec();
        if (k == null) {
            return;
        }
        RandevuServisi rs = sistem.getRandevuServisi();
        Randevu r = konsol.listedenSec("Kurum randevuları", rs.kurulusRandevulari(k));
        if (r == null) {
            return;
        }
        konsol.yaz(r.detay());
        int islem = konsol.sayiOku("1) Onayla  2) Reddet  3) İptal et  4) Tamamlandı işaretle  0) Geri", 0, 4);
        switch (islem) {
            case 1 -> rs.onayla(yonetici, r);
            case 2 -> rs.reddet(yonetici, r, konsol.metinOku("Ret nedeni"));
            case 3 -> rs.iptalEt(yonetici, r, konsol.metinOku("İptal nedeni"));
            case 4 -> rs.tamamla(yonetici, r);
            default -> {
                return;
            }
        }
        konsol.basari("Randevu #" + r.getId() + " yeni durum: " + r.getDurum().getGorunenAd());
    }
}
