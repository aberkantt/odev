package hastane.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hastane / klinik yöneticisi. Sadece kendi yönettiği kuruluşlar üzerinde
 * tanımlama yapabilir (yetki kontrolü servis katmanında bu sınıf üzerinden yapılır).
 */
public class Yonetici extends Kisi {

    private final List<SaglikKurulusu> yonettigiKuruluslar = new ArrayList<>();

    public Yonetici(String ad, String soyad, String telefon, String eposta, String sifre) {
        super(ad, soyad, telefon, eposta, sifre);
    }

    @Override
    public Rol getRol() {
        return Rol.YONETICI;
    }

    public void kurulusYetkisiVer(SaglikKurulusu kurulus) {
        if (!yonettigiKuruluslar.contains(kurulus)) {
            yonettigiKuruluslar.add(kurulus);
        }
    }

    public boolean yetkiliMi(SaglikKurulusu kurulus) {
        return yonettigiKuruluslar.contains(kurulus);
    }

    public List<SaglikKurulusu> getYonettigiKuruluslar() {
        return Collections.unmodifiableList(yonettigiKuruluslar);
    }
}
