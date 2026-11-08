package hastane.depo;

import hastane.model.Calisan;
import hastane.model.Hasta;
import hastane.model.Kisi;
import hastane.model.Randevu;
import hastane.model.SaglikKurulusu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uygulamanın tüm verisini bellekte tutan basit depo.
 * (Gerçek bir sistemde bu sınıfın yerini bir veritabanı katmanı alır; servisler değişmeden kalır.)
 */
public class VeriDeposu {

    private final List<SaglikKurulusu> kuruluslar = new ArrayList<>();
    private final List<Kisi> kisiler = new ArrayList<>();
    private final List<Randevu> randevular = new ArrayList<>();

    // ---------- Kuruluşlar ----------

    public void kurulusEkle(SaglikKurulusu kurulus) {
        kuruluslar.add(kurulus);
    }

    public List<SaglikKurulusu> getKuruluslar() {
        return Collections.unmodifiableList(kuruluslar);
    }

    // ---------- Kişiler ----------

    public void kisiEkle(Kisi kisi) {
        if (kisiBul(kisi.getEposta()) != null) {
            throw new IllegalArgumentException(kisi.getEposta() + " e-posta adresi zaten kayıtlı.");
        }
        if (kisi instanceof Hasta yeni) {
            for (Hasta h : kisileriGetir(Hasta.class)) {
                if (h.getTcKimlikNo().equals(yeni.getTcKimlikNo())) {
                    throw new IllegalArgumentException("Bu TC kimlik numarasıyla kayıtlı bir hasta zaten var.");
                }
            }
        }
        kisiler.add(kisi);
    }

    public Kisi kisiBul(String eposta) {
        if (eposta == null) {
            return null;
        }
        for (Kisi k : kisiler) {
            if (k.getEposta().equalsIgnoreCase(eposta.trim())) {
                return k;
            }
        }
        return null;
    }

    /**
     * İstenen türdeki kişileri döndürür. Örn: kisileriGetir(Doktor.class)
     * GENERIC metot + ÇOK BİÇİMLİLİK: tek liste içinde farklı alt sınıflar tutulur,
     * isInstance ile istenen tür süzülür.
     */
    public <T extends Kisi> List<T> kisileriGetir(Class<T> tur) {
        List<T> sonuc = new ArrayList<>();
        for (Kisi k : kisiler) {
            if (tur.isInstance(k)) {
                sonuc.add(tur.cast(k));
            }
        }
        return sonuc;
    }

    public List<Calisan> calisanlariGetir(SaglikKurulusu kurulus) {
        List<Calisan> sonuc = new ArrayList<>();
        for (Calisan c : kisileriGetir(Calisan.class)) {
            if (c.getKurulus() == kurulus) {
                sonuc.add(c);
            }
        }
        return sonuc;
    }

    public List<Kisi> getKisiler() {
        return Collections.unmodifiableList(kisiler);
    }

    // ---------- Randevular ----------

    public void randevuEkle(Randevu randevu) {
        randevular.add(randevu);
    }

    public List<Randevu> getRandevular() {
        return Collections.unmodifiableList(randevular);
    }
}
