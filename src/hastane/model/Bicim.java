package hastane.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Tarih, saat ve para birimini Türkçe biçimde yazdırmak için yardımcı sınıf. */
public final class Bicim {

    public static final Locale TR = Locale.forLanguageTag("tr-TR");
    public static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter TARIH_SAAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Bicim() {
    }

    public static String gun(DayOfWeek gun) {
        return gun.getDisplayName(TextStyle.FULL, TR);
    }

    public static String tarih(LocalDate tarih) {
        return tarih.format(TARIH) + " " + gun(tarih.getDayOfWeek());
    }

    public static String saat(LocalTime saat) {
        return saat.format(SAAT);
    }

    public static String tarihSaat(LocalDateTime zaman) {
        return zaman.format(TARIH_SAAT);
    }

    public static String para(BigDecimal tutar) {
        return String.format(TR, "%,.2f TL", tutar.setScale(2, RoundingMode.HALF_UP));
    }
}
