package hastane.arayuz;

import java.io.Console;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

import hastane.model.Bicim;

/** Klavyeden güvenli okuma ve ekrana düzenli yazma için yardımcı sınıf. */
public class Konsol {

    // Klavyeden gelen Türkçe karakterlerin (ç, ğ, ş ...) bozulmaması için konsolun karakter seti kullanılır.
    private final Scanner scanner = new Scanner(System.in, konsolKarakterSeti());

    private static Charset konsolKarakterSeti() {
        Console c = System.console();
        if (c != null) {
            return c.charset();
        }
        String stdout = System.getProperty("stdout.encoding");
        try {
            return stdout != null ? Charset.forName(stdout) : Charset.defaultCharset();
        } catch (RuntimeException e) {
            return Charset.defaultCharset();
        }
    }

    public void baslik(String metin) {
        System.out.println();
        System.out.println("=".repeat(64));
        System.out.println("  " + metin);
        System.out.println("=".repeat(64));
    }

    public void yaz(String metin) {
        System.out.println(metin);
    }

    public void basari(String metin) {
        System.out.println(">> " + metin);
    }

    public void hata(String metin) {
        System.out.println("!! HATA: " + metin);
    }

    public String metinOku(String soru) {
        while (true) {
            System.out.print(soru + ": ");
            if (!scanner.hasNextLine()) {
                throw new GirdiBittiException();
            }
            String satir = scanner.nextLine().trim();
            if (!satir.isEmpty()) {
                return satir;
            }
            hata("Boş bırakılamaz.");
        }
    }

    public int sayiOku(String soru, int min, int max) {
        while (true) {
            String girdi = metinOku(soru);
            try {
                int sayi = Integer.parseInt(girdi);
                if (sayi >= min && sayi <= max) {
                    return sayi;
                }
            } catch (NumberFormatException ignored) {
                // aşağıda uyarı verilir
            }
            hata(min + " ile " + max + " arasında bir sayı giriniz.");
        }
    }

    public BigDecimal tutarOku(String soru) {
        while (true) {
            try {
                BigDecimal tutar = new BigDecimal(metinOku(soru).replace(',', '.'));
                if (tutar.signum() >= 0) {
                    return tutar;
                }
            } catch (NumberFormatException ignored) {
                // aşağıda uyarı verilir
            }
            hata("Geçerli bir tutar giriniz (örn: 750 veya 750.50).");
        }
    }

    public LocalDate tarihOku(String soru) {
        while (true) {
            try {
                return LocalDate.parse(metinOku(soru + " (gg.aa.yyyy)"), Bicim.TARIH);
            } catch (DateTimeParseException e) {
                hata("Tarih gg.aa.yyyy biçiminde olmalı (örn: 05.03.1990).");
            }
        }
    }

    public LocalTime saatOku(String soru) {
        while (true) {
            try {
                return LocalTime.parse(metinOku(soru + " (ss:dd)"), Bicim.SAAT);
            } catch (DateTimeParseException e) {
                hata("Saat ss:dd biçiminde olmalı (örn: 09:30).");
            }
        }
    }

    public boolean evetMi(String soru) {
        String cevap = metinOku(soru + " (e/h)").toLowerCase(Bicim.TR);
        return cevap.startsWith("e") || cevap.startsWith("y");
    }

    /**
     * Listeyi numaralandırarak gösterir, kullanıcının seçtiği elemanı döndürür.
     * 0 seçilirse null döner (geri dön). GENERIC metot: her türden liste ile çalışır.
     */
    public <T> T listedenSec(String baslik, List<T> liste, Function<T, String> gosterim) {
        if (liste.isEmpty()) {
            yaz("(" + baslik + ": listelenecek kayıt yok)");
            return null;
        }
        yaz("-- " + baslik + " --");
        for (int i = 0; i < liste.size(); i++) {
            yaz(String.format("  %2d) %s", i + 1, gosterim.apply(liste.get(i))));
        }
        yaz("   0) Geri");
        int secim = sayiOku("Seçiminiz", 0, liste.size());
        return secim == 0 ? null : liste.get(secim - 1);
    }

    public <T> T listedenSec(String baslik, List<T> liste) {
        return listedenSec(baslik, liste, String::valueOf);
    }

    public void listele(String baslik, List<?> liste) {
        yaz("-- " + baslik + " --");
        if (liste.isEmpty()) {
            yaz("  (kayıt yok)");
        }
        for (Object o : liste) {
            yaz("  - " + o);
        }
    }

    /** Giriş akışı (ör. dosyadan yönlendirilmiş girdi) bittiğinde programı düzgünce kapatmak için. */
    public static class GirdiBittiException extends RuntimeException {
        public GirdiBittiException() {
            super("Girdi sona erdi.");
        }
    }
}
