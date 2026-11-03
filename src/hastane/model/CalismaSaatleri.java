package hastane.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

/** Bir şubenin haftalık çalışma saatleri. Listede olmayan gün = kapalı. */
public class CalismaSaatleri {

    private final Map<DayOfWeek, SaatAraligi> saatler = new EnumMap<>(DayOfWeek.class);

    /** Pazartesi-Cuma aynı saatlerle açık bir çalışma takvimi oluşturur (fabrika metodu). */
    public static CalismaSaatleri haftaIci(LocalTime acilis, LocalTime kapanis) {
        CalismaSaatleri cs = new CalismaSaatleri();
        for (DayOfWeek gun : DayOfWeek.values()) {
            if (gun != DayOfWeek.SATURDAY && gun != DayOfWeek.SUNDAY) {
                cs.gunAyarla(gun, acilis, kapanis);
            }
        }
        return cs;
    }

    public void gunAyarla(DayOfWeek gun, LocalTime acilis, LocalTime kapanis) {
        saatler.put(gun, new SaatAraligi(acilis, kapanis));
    }

    public void kapaliYap(DayOfWeek gun) {
        saatler.remove(gun);
    }

    public boolean acikMi(DayOfWeek gun) {
        return saatler.containsKey(gun);
    }

    public SaatAraligi getSaatler(DayOfWeek gun) {
        return saatler.get(gun);
    }

    /** Verilen zaman dilimi tamamen çalışma saatleri içinde mi? (Aynı gün içinde olmalı.) */
    public boolean kapsiyorMu(LocalDateTime bas, LocalDateTime bit) {
        if (!bas.toLocalDate().equals(bit.toLocalDate())) {
            return false;
        }
        SaatAraligi aralik = saatler.get(bas.getDayOfWeek());
        return aralik != null && aralik.kapsiyorMu(bas.toLocalTime(), bit.toLocalTime());
    }

    /** Bir saat aralığı o günün çalışma saatleri içinde mi? (uygunluk tanımlarken kullanılır) */
    public boolean kapsiyorMu(DayOfWeek gun, SaatAraligi aralik) {
        SaatAraligi acik = saatler.get(gun);
        return acik != null && acik.kapsiyorMu(aralik.getBaslangic(), aralik.getBitis());
    }

    @Override
    public String toString() {
        if (saatler.isEmpty()) {
            return "Tanımlanmamış";
        }
        StringJoiner sj = new StringJoiner(", ");
        saatler.forEach((gun, aralik) -> sj.add(Bicim.gun(gun) + " " + aralik));
        return sj.toString();
    }
}
