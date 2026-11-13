package hastane.test;

import hastane.istisna.GecersizIslemException;
import hastane.istisna.RandevuCakismaException;
import hastane.istisna.YetkiException;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.GoruntulemeHizmeti;
import hastane.model.Hasta;
import hastane.model.Hizmet;
import hastane.model.KurulusTuru;
import hastane.model.LaboratuvarHizmeti;
import hastane.model.MuayeneHizmeti;
import hastane.model.Randevu;
import hastane.model.RandevuDurumu;
import hastane.model.SaatAraligi;
import hastane.model.SaglikKurulusu;
import hastane.model.Sube;
import hastane.model.Yonetici;
import hastane.servis.HastaneSistemi;
import hastane.servis.RandevuServisi;
import hastane.servis.YonetimServisi;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Harici kütüphane gerektirmeyen basit test sınıfı.
 * Çalıştırma: test.bat (Windows) veya ./test.sh (Mac/Linux)
 *
 * Her testte sıfırdan küçük bir senaryo kurulur:
 *   - "Test Hastanesi" > "Merkez" şube (hafta içi 08:00-18:00) > "Dahiliye" bölümü
 *   - Dr. Test: Pazartesi 09:00-12:00 müsait, 20 dk'lık "Genel Muayene" veriyor
 *   - İki hasta
 */
public class SistemTestleri {

    private static int basarili = 0;
    private static int basarisiz = 0;

    // Her testte yeniden kurulan senaryo nesneleri
    private HastaneSistemi sistem;
    private RandevuServisi rs;
    private YonetimServisi ys;
    private Yonetici yonetici;
    private SaglikKurulusu kurulus;
    private Sube merkez;
    private Hizmet muayene;
    private Doktor doktor;
    private Hasta hasta1;
    private Hasta hasta2;
    private LocalDate pazartesi;

    public static void main(String[] args) {
        SistemTestleri t = new SistemTestleri();

        t.calistir("Geçerli randevu oluşur ve 'onay bekliyor' durumunda başlar", t::gecerliRandevu);
        t.calistir("Randevu ücret ve süreyi o anki hizmetten kopyalar", t::ucretKopyalanir);
        t.calistir("Aynı doktora aynı saatte ikinci randevu -> çakışma", t::ayniSaatCakisir);
        t.calistir("Kısmen üst üste binen randevu -> çakışma", t::kismiCakisma);
        t.calistir("Bitişik randevular çakışmaz (09:00-09:20 ve 09:20-09:40)", t::bitisikCakismaz);
        t.calistir("Hasta aynı saatte iki farklı doktordan randevu alamaz", t::hastaCiftRandevu);
        t.calistir("Doktorun uygunluk saati dışına randevu alınamaz", t::uygunlukDisi);
        t.calistir("Şubenin kapalı olduğu güne randevu alınamaz", t::subeKapali);
        t.calistir("Doktorun vermediği hizmete randevu alınamaz", t::verilmeyenHizmet);
        t.calistir("Geçmiş zamana randevu alınamaz", t::gecmisZaman);
        t.calistir("Reddedilen randevunun saati tekrar alınabilir", t::reddedilenSaatBosalir);
        t.calistir("Onay mekanizması: onayla -> ONAYLANDI, tekrar onaylanamaz", t::onayMekanizmasi);
        t.calistir("Başka bir doktor randevuyu onaylayamaz", t::baskaDoktorOnaylayamaz);
        t.calistir("Kurum yöneticisi randevuyu onaylayabilir", t::yoneticiOnaylayabilir);
        t.calistir("Başka kuruluşun yöneticisi şube ekleyemez (yetki)", t::yetkisizYonetici);
        t.calistir("Boş saat listesi dolu saatleri içermez", t::bosSaatler);
        t.calistir("Çalışanın aynı gün çakışan iki uygunluğu olamaz", t::uygunlukCakismasi);
        t.calistir("Negatif ücretli hizmet oluşturulamaz", t::negatifUcret);
        t.calistir("Çok biçimlilik: hizmet türüne göre farklı hazırlık bilgisi", t::cokBicimlilik);
        t.calistir("Şifre doğru/yanlış kontrolü", t::sifreKontrolu);

        System.out.println();
        System.out.println("SONUÇ: " + basarili + " başarılı, " + basarisiz + " başarısız");
        if (basarisiz > 0) {
            System.exit(1);
        }
    }

    // =====================================================================
    //  Senaryo kurulumu
    // =====================================================================

    private void kur() {
        sistem = new HastaneSistemi();
        rs = sistem.getRandevuServisi();
        ys = sistem.getYonetimServisi();

        yonetici = new Yonetici("Test", "Yönetici", "0500", "yonetici@test.test", "1234");
        sistem.getDepo().kisiEkle(yonetici);
        kurulus = ys.kurulusOlustur(yonetici, "Test Hastanesi", KurulusTuru.HASTANE);
        merkez = ys.subeEkle(yonetici, kurulus, "Merkez", "Adres",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        var dahiliye = ys.bolumEkle(yonetici, merkez, "Dahiliye");
        muayene = new MuayeneHizmeti("Genel Muayene", 20, new BigDecimal("750"));
        ys.hizmetEkle(yonetici, merkez, dahiliye, muayene);

        doktor = ys.doktorKaydet(yonetici, kurulus, "Dr", "Test", "0501", "dr@test.test", "1234", "Dr.", "Dahiliye");
        ys.calisanaHizmetAta(yonetici, doktor, muayene);
        ys.uygunlukEkle(yonetici, doktor, merkez, DayOfWeek.MONDAY, aralik(9, 0, 12, 0));

        hasta1 = sistem.getKimlikServisi().hastaKaydet("Hasta", "Bir", "0502", "h1@test.test", "1234",
                "11111111111", LocalDate.of(1990, 1, 1));
        hasta2 = sistem.getKimlikServisi().hastaKaydet("Hasta", "Iki", "0503", "h2@test.test", "1234",
                "22222222222", LocalDate.of(1991, 1, 1));

        pazartesi = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private LocalDateTime pzt(int saat, int dakika) {
        return pazartesi.atTime(saat, dakika);
    }

    private static SaatAraligi aralik(int s1, int d1, int s2, int d2) {
        return new SaatAraligi(LocalTime.of(s1, d1), LocalTime.of(s2, d2));
    }

    // =====================================================================
    //  Testler
    // =====================================================================

    private void gecerliRandevu() {
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        esitMi(RandevuDurumu.BEKLEMEDE, r.getDurum());
        esitMi(pzt(9, 20), r.getBitis());
    }

    private void ucretKopyalanir() {
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        muayene.setUcret(new BigDecimal("999"));
        esitMi(new BigDecimal("750"), r.getUcret());
    }

    private void ayniSaatCakisir() {
        rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        hataBekle(RandevuCakismaException.class, () -> rs.randevuOlustur(hasta2, doktor, muayene, merkez, pzt(9, 0)));
    }

    private void kismiCakisma() {
        rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        hataBekle(RandevuCakismaException.class, () -> rs.randevuOlustur(hasta2, doktor, muayene, merkez, pzt(9, 10)));
    }

    private void bitisikCakismaz() {
        rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        Randevu r2 = rs.randevuOlustur(hasta2, doktor, muayene, merkez, pzt(9, 20));
        esitMi(RandevuDurumu.BEKLEMEDE, r2.getDurum());
    }

    private void hastaCiftRandevu() {
        Doktor ikinci = ys.doktorKaydet(yonetici, kurulus, "Dr", "Iki", "0504", "dr2@test.test", "1234", "Dr.", "Dahiliye");
        ys.calisanaHizmetAta(yonetici, ikinci, muayene);
        ys.uygunlukEkle(yonetici, ikinci, merkez, DayOfWeek.MONDAY, aralik(9, 0, 12, 0));
        rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(10, 0));
        hataBekle(RandevuCakismaException.class, () -> rs.randevuOlustur(hasta1, ikinci, muayene, merkez, pzt(10, 10)));
    }

    private void uygunlukDisi() {
        hataBekle(GecersizIslemException.class, () -> rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(14, 0)));
        // 11:50 + 20 dk = 12:10 -> uygunluk 12:00'de bitiyor
        hataBekle(GecersizIslemException.class, () -> rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(11, 50)));
    }

    private void subeKapali() {
        LocalDateTime pazar = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).atTime(10, 0);
        hataBekle(GecersizIslemException.class, () -> rs.randevuOlustur(hasta1, doktor, muayene, merkez, pazar));
    }

    private void verilmeyenHizmet() {
        var lab = ys.bolumEkle(yonetici, merkez, "Laboratuvar");
        Hizmet tahlil = new LaboratuvarHizmeti("Tam Kan Sayımı", 10, new BigDecimal("250"), 0);
        ys.hizmetEkle(yonetici, merkez, lab, tahlil);
        hataBekle(GecersizIslemException.class, () -> rs.randevuOlustur(hasta1, doktor, tahlil, merkez, pzt(9, 0)));
    }

    private void gecmisZaman() {
        LocalDateTime gecenPazartesi = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).atTime(9, 0);
        hataBekle(GecersizIslemException.class, () -> rs.randevuOlustur(hasta1, doktor, muayene, merkez, gecenPazartesi));
    }

    private void reddedilenSaatBosalir() {
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        rs.reddet(doktor, r, "Kongre");
        Randevu yeni = rs.randevuOlustur(hasta2, doktor, muayene, merkez, pzt(9, 0));
        esitMi(RandevuDurumu.BEKLEMEDE, yeni.getDurum());
    }

    private void onayMekanizmasi() {
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        rs.onayla(doktor, r);
        esitMi(RandevuDurumu.ONAYLANDI, r.getDurum());
        hataBekle(GecersizIslemException.class, () -> rs.onayla(doktor, r));
        hataBekle(GecersizIslemException.class, () -> rs.reddet(doktor, r, "geç oldu"));
    }

    private void baskaDoktorOnaylayamaz() {
        Doktor baska = ys.doktorKaydet(yonetici, kurulus, "Baska", "Doktor", "0505", "dr3@test.test", "1234", "Dr.", "Dahiliye");
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        hataBekle(YetkiException.class, () -> rs.onayla(baska, r));
        hataBekle(YetkiException.class, () -> rs.onayla(hasta1, r));
    }

    private void yoneticiOnaylayabilir() {
        Randevu r = rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));
        rs.onayla(yonetici, r);
        esitMi(RandevuDurumu.ONAYLANDI, r.getDurum());
    }

    private void yetkisizYonetici() {
        Yonetici baskaYonetici = new Yonetici("Diger", "Yonetici", "0506", "diger@test.test", "1234");
        hataBekle(YetkiException.class, () -> ys.subeEkle(baskaYonetici, kurulus, "Yeni Şube", "Adres",
                CalismaSaatleri.haftaIci(LocalTime.of(9, 0), LocalTime.of(17, 0))));
        hataBekle(YetkiException.class, () -> ys.doktorKaydet(baskaYonetici, kurulus, "X", "Y", "0", "x@test.test",
                "1234", "Dr.", "Z"));
    }

    private void bosSaatler() {
        // 09:00-12:00, 15 dk adımla, 20 dk'lık hizmet: 09:00 ... 11:30 arası 11 aday saat
        List<LocalDateTime> once = rs.bosSaatler(hasta2, doktor, muayene, merkez, pazartesi);
        esitMi(11, once.size());
        rs.randevuOlustur(hasta1, doktor, muayene, merkez, pzt(9, 0));   // 09:00-09:20 dolu
        List<LocalDateTime> sonra = rs.bosSaatler(hasta2, doktor, muayene, merkez, pazartesi);
        dogruMu(!sonra.contains(pzt(9, 0)), "09:00 listede olmamalı");
        dogruMu(!sonra.contains(pzt(9, 15)), "09:15 listede olmamalı (09:20'ye kadar dolu)");
        dogruMu(sonra.contains(pzt(9, 30)), "09:30 listede olmalı");
    }

    private void uygunlukCakismasi() {
        Sube ikinciSube = ys.subeEkle(yonetici, kurulus, "Şube 2", "Adres 2",
                CalismaSaatleri.haftaIci(LocalTime.of(8, 0), LocalTime.of(18, 0)));
        // Doktor Pazartesi 09-12 Merkez'de; aynı saatte başka şubede olamaz
        hataBekle(IllegalArgumentException.class,
                () -> ys.uygunlukEkle(yonetici, doktor, ikinciSube, DayOfWeek.MONDAY, aralik(11, 0, 13, 0)));
        // Ama öğleden sonra olabilir
        ys.uygunlukEkle(yonetici, doktor, ikinciSube, DayOfWeek.MONDAY, aralik(13, 0, 16, 0));
        esitMi(2, doktor.getUygunluklar().size());
    }

    private void negatifUcret() {
        hataBekle(IllegalArgumentException.class, () -> new MuayeneHizmeti("X", 20, new BigDecimal("-1")));
        hataBekle(IllegalArgumentException.class, () -> new MuayeneHizmeti("X", 0, new BigDecimal("10")));
    }

    private void cokBicimlilik() {
        List<Hizmet> hizmetler = List.of(
                new MuayeneHizmeti("Muayene", 20, BigDecimal.TEN),
                new GoruntulemeHizmeti("MR", 45, BigDecimal.TEN, "MR", true),
                new LaboratuvarHizmeti("Şeker", 10, BigDecimal.TEN, 8));
        // Aynı metot çağrısı (getHazirlikBilgisi), nesnenin gerçek türüne göre farklı sonuç verir
        dogruMu(hizmetler.get(0).getHazirlikBilgisi().contains("ilaç"), "muayene hazırlığı");
        dogruMu(hizmetler.get(1).getHazirlikBilgisi().contains("Kontrast"), "MR hazırlığı");
        dogruMu(hizmetler.get(2).getHazirlikBilgisi().contains("8 saat"), "laboratuvar hazırlığı");
    }

    private void sifreKontrolu() {
        dogruMu(hasta1.sifreDogruMu("1234"), "doğru şifre kabul edilmeli");
        dogruMu(!hasta1.sifreDogruMu("12345"), "yanlış şifre reddedilmeli");
        hataBekle(GecersizIslemException.class, () -> sistem.getKimlikServisi().girisYap("h1@test.test", "yanlis"));
    }

    // =====================================================================
    //  Mini test altyapısı
    // =====================================================================

    private void calistir(String ad, Runnable test) {
        try {
            kur();
            test.run();
            basarili++;
            System.out.println("[ OK ] " + ad);
        } catch (Throwable e) {
            basarisiz++;
            System.out.println("[HATA] " + ad + "  ->  " + e);
        }
    }

    private static void esitMi(Object beklenen, Object gercek) {
        boolean esit = beklenen instanceof BigDecimal b && gercek instanceof BigDecimal g
                ? b.compareTo(g) == 0
                : beklenen.equals(gercek);
        if (!esit) {
            throw new AssertionError("Beklenen: " + beklenen + ", gelen: " + gercek);
        }
    }

    private static void dogruMu(boolean kosul, String aciklama) {
        if (!kosul) {
            throw new AssertionError(aciklama);
        }
    }

    private static void hataBekle(Class<? extends Throwable> beklenenHata, Runnable kod) {
        try {
            kod.run();
        } catch (Throwable e) {
            if (beklenenHata.isInstance(e)) {
                return;
            }
            throw new AssertionError(beklenenHata.getSimpleName() + " bekleniyordu ama " + e + " geldi");
        }
        throw new AssertionError(beklenenHata.getSimpleName() + " bekleniyordu ama hata oluşmadı");
    }
}
