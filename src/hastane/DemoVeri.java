package hastane;

import hastane.model.Bolum;
import hastane.model.CalismaSaatleri;
import hastane.model.Doktor;
import hastane.model.GoruntulemeHizmeti;
import hastane.model.Hasta;
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
import hastane.servis.KimlikServisi;
import hastane.servis.RandevuServisi;
import hastane.servis.YonetimServisi;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

/**
 * Uygulama açıldığında denenebilmesi için örnek veriler.
 * Kurum/kişi adları tamamen hayalidir; fiyatlar örnek değerlerdir.
 * Tüm kayıtlar servisler üzerinden eklenir, böylece doğrulama ve yetki kuralları burada da çalışır.
 */
public final class DemoVeri {

    public static final String DEMO_SIFRE = "1234";

    private DemoVeri() {
    }

    public static HastaneSistemi olustur() {
        HastaneSistemi sistem = new HastaneSistemi();
        YonetimServisi ys = sistem.getYonetimServisi();
        KimlikServisi ks = sistem.getKimlikServisi();
        RandevuServisi rs = sistem.getRandevuServisi();

        // ---------------- Yöneticiler ----------------
        Yonetici mehmet = new Yonetici("Mehmet", "Kaya", "0532 000 00 01", "mehmet.kaya@yildiz.test", DEMO_SIFRE);
        Yonetici selin = new Yonetici("Selin", "Koç", "0532 000 00 02", "selin.koc@papatya.test", DEMO_SIFRE);
        sistem.getDepo().kisiEkle(mehmet);
        sistem.getDepo().kisiEkle(selin);

        // ---------------- 1) Çok şubeli hastane ----------------
        SaglikKurulusu yildiz = ys.kurulusOlustur(mehmet, "Yıldız Hastanesi", KurulusTuru.HASTANE);

        CalismaSaatleri merkezSaatleri = CalismaSaatleri.haftaIci(saat(8, 0), saat(18, 0));
        merkezSaatleri.gunAyarla(SATURDAY, saat(9, 0), saat(13, 0));
        Sube merkez = ys.subeEkle(mehmet, yildiz, "Merkez Şube", "Adapazarı / Sakarya", merkezSaatleri);
        Sube serdivan = ys.subeEkle(mehmet, yildiz, "Serdivan Şubesi", "Serdivan / Sakarya",
                CalismaSaatleri.haftaIci(saat(9, 0), saat(17, 0)));

        Bolum mDahiliye = ys.bolumEkle(mehmet, merkez, "Dahiliye");
        Bolum mKardiyoloji = ys.bolumEkle(mehmet, merkez, "Kardiyoloji");
        Bolum mRadyoloji = ys.bolumEkle(mehmet, merkez, "Radyoloji");
        Bolum mLaboratuvar = ys.bolumEkle(mehmet, merkez, "Laboratuvar");
        Bolum sDahiliye = ys.bolumEkle(mehmet, serdivan, "Dahiliye");
        Bolum sLaboratuvar = ys.bolumEkle(mehmet, serdivan, "Laboratuvar");

        Hizmet genelMuayene = new MuayeneHizmeti("Genel Muayene", 20, tl("750"));
        Hizmet kardiyolojiMuayenesi = new MuayeneHizmeti("Kardiyoloji Muayenesi", 30, tl("1200"));
        Hizmet akcigerRontgeni = new GoruntulemeHizmeti("Akciğer Röntgeni", 15, tl("600"), "Röntgen", false);
        Hizmet beyinMr = new GoruntulemeHizmeti("Kontrastlı Beyin MR", 45, tl("3500"), "MR", true);
        Hizmet tamKanSayimi = new LaboratuvarHizmeti("Tam Kan Sayımı", 10, tl("250"), 0);
        Hizmet aclikKanSekeri = new LaboratuvarHizmeti("Açlık Kan Şekeri", 10, tl("200"), 8);

        ys.hizmetEkle(mehmet, merkez, mDahiliye, genelMuayene);
        ys.hizmetEkle(mehmet, merkez, mKardiyoloji, kardiyolojiMuayenesi);
        ys.hizmetEkle(mehmet, merkez, mRadyoloji, akcigerRontgeni);
        ys.hizmetEkle(mehmet, merkez, mRadyoloji, beyinMr);
        ys.hizmetEkle(mehmet, merkez, mLaboratuvar, tamKanSayimi);
        ys.hizmetEkle(mehmet, merkez, mLaboratuvar, aclikKanSekeri);
        // Aynı hizmetler Serdivan şubesinde de veriliyor (aynı nesne -> aynı süre ve fiyat)
        ys.hizmetEkle(mehmet, serdivan, sDahiliye, genelMuayene);
        ys.hizmetEkle(mehmet, serdivan, sLaboratuvar, tamKanSayimi);
        ys.hizmetEkle(mehmet, serdivan, sLaboratuvar, aclikKanSekeri);

        Doktor ayse = ys.doktorKaydet(mehmet, yildiz, "Ayşe", "Yılmaz", "0533 000 00 11",
                "ayse.yilmaz@yildiz.test", DEMO_SIFRE, "Uzm. Dr.", "İç Hastalıkları");
        ys.calisanaHizmetAta(mehmet, ayse, genelMuayene);
        ys.uygunlukEkle(mehmet, ayse, merkez, MONDAY, aralik(9, 0, 12, 0));
        ys.uygunlukEkle(mehmet, ayse, merkez, WEDNESDAY, aralik(9, 0, 12, 0));
        ys.uygunlukEkle(mehmet, ayse, serdivan, TUESDAY, aralik(13, 0, 17, 0));  // iki şubede çalışıyor
        ys.uygunlukEkle(mehmet, ayse, serdivan, THURSDAY, aralik(13, 0, 17, 0));

        Doktor can = ys.doktorKaydet(mehmet, yildiz, "Can", "Demir", "0533 000 00 12",
                "can.demir@yildiz.test", DEMO_SIFRE, "Doç. Dr.", "Kardiyoloji");
        ys.calisanaHizmetAta(mehmet, can, kardiyolojiMuayenesi);
        ys.uygunlukEkle(mehmet, can, merkez, TUESDAY, aralik(9, 0, 16, 0));
        ys.uygunlukEkle(mehmet, can, merkez, THURSDAY, aralik(9, 0, 16, 0));
        ys.uygunlukEkle(mehmet, can, merkez, FRIDAY, aralik(9, 0, 12, 0));

        SaglikPersoneli elif = ys.personelKaydet(mehmet, yildiz, "Elif", "Şahin", "0533 000 00 13",
                "elif.sahin@yildiz.test", DEMO_SIFRE, "Radyoloji Teknisyeni");
        ys.calisanaHizmetAta(mehmet, elif, akcigerRontgeni);
        ys.calisanaHizmetAta(mehmet, elif, beyinMr);
        for (DayOfWeek gun : new DayOfWeek[]{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY}) {
            ys.uygunlukEkle(mehmet, elif, merkez, gun, aralik(8, 0, 12, 0));
        }

        SaglikPersoneli burak = ys.personelKaydet(mehmet, yildiz, "Burak", "Öz", "0533 000 00 14",
                "burak.oz@yildiz.test", DEMO_SIFRE, "Laborant");
        ys.calisanaHizmetAta(mehmet, burak, tamKanSayimi);
        ys.calisanaHizmetAta(mehmet, burak, aclikKanSekeri);
        ys.uygunlukEkle(mehmet, burak, merkez, MONDAY, aralik(8, 0, 11, 0));
        ys.uygunlukEkle(mehmet, burak, merkez, WEDNESDAY, aralik(8, 0, 11, 0));
        ys.uygunlukEkle(mehmet, burak, merkez, FRIDAY, aralik(8, 0, 11, 0));
        ys.uygunlukEkle(mehmet, burak, merkez, SATURDAY, aralik(9, 0, 12, 0));
        ys.uygunlukEkle(mehmet, burak, serdivan, TUESDAY, aralik(9, 0, 12, 0));
        ys.uygunlukEkle(mehmet, burak, serdivan, THURSDAY, aralik(9, 0, 12, 0));

        // ---------------- 2) Tek şubeli klinik ----------------
        SaglikKurulusu papatya = ys.kurulusOlustur(selin, "Papatya Göz Kliniği", KurulusTuru.KLINIK);
        Sube papatyaMerkez = ys.subeEkle(selin, papatya, "Merkez", "Erenler / Sakarya",
                CalismaSaatleri.haftaIci(saat(9, 0), saat(18, 0)));
        Bolum goz = ys.bolumEkle(selin, papatyaMerkez, "Göz Hastalıkları");
        Hizmet gozMuayenesi = new MuayeneHizmeti("Göz Muayenesi", 20, tl("900"));
        Hizmet gozTansiyonu = new MuayeneHizmeti("Göz Tansiyonu Ölçümü", 10, tl("400"));
        ys.hizmetEkle(selin, papatyaMerkez, goz, gozMuayenesi);
        ys.hizmetEkle(selin, papatyaMerkez, goz, gozTansiyonu);

        Doktor zeynep = ys.doktorKaydet(selin, papatya, "Zeynep", "Arslan", "0533 000 00 21",
                "zeynep.arslan@papatya.test", DEMO_SIFRE, "Op. Dr.", "Göz Hastalıkları");
        ys.calisanaHizmetAta(selin, zeynep, gozMuayenesi);
        ys.calisanaHizmetAta(selin, zeynep, gozTansiyonu);
        for (DayOfWeek gun : new DayOfWeek[]{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY}) {
            ys.uygunlukEkle(selin, zeynep, papatyaMerkez, gun, aralik(10, 0, 12, 30));  // öğle arası var
            ys.uygunlukEkle(selin, zeynep, papatyaMerkez, gun, aralik(13, 30, 17, 0));
        }

        // ---------------- Hastalar ----------------
        Hasta ahmet = ks.hastaKaydet("Ahmet", "Çelik", "0535 000 00 31", "ahmet.celik@hasta.test",
                DEMO_SIFRE, "10000000146", LocalDate.of(1990, 5, 14));
        Hasta fatma = ks.hastaKaydet("Fatma", "Aydın", "0535 000 00 32", "fatma.aydin@hasta.test",
                DEMO_SIFRE, "20000000292", LocalDate.of(1985, 11, 2));

        // ---------------- Örnek randevular (her zaman ileri tarihli) ----------------
        LocalDate pazartesi = sonraki(MONDAY);
        Randevu r1 = rs.randevuOlustur(ahmet, ayse, genelMuayene, merkez, pazartesi.atTime(9, 0));
        rs.onayla(ayse, r1);
        rs.randevuOlustur(fatma, ayse, genelMuayene, merkez, pazartesi.atTime(9, 30));
        rs.randevuOlustur(ahmet, can, kardiyolojiMuayenesi, merkez, sonraki(TUESDAY).atTime(10, 0));
        rs.randevuOlustur(fatma, zeynep, gozMuayenesi, papatyaMerkez, sonraki(WEDNESDAY).atTime(10, 0));

        return sistem;
    }

    // ---------- kısa yardımcılar ----------

    private static LocalTime saat(int s, int d) {
        return LocalTime.of(s, d);
    }

    private static SaatAraligi aralik(int s1, int d1, int s2, int d2) {
        return new SaatAraligi(LocalTime.of(s1, d1), LocalTime.of(s2, d2));
    }

    private static BigDecimal tl(String tutar) {
        return new BigDecimal(tutar);
    }

    /** Bugünden sonraki ilk ilgili gün (bugün o günse bir sonraki hafta). */
    private static LocalDate sonraki(DayOfWeek gun) {
        return LocalDate.now().with(TemporalAdjusters.next(gun));
    }
}
