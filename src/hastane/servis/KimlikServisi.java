package hastane.servis;

import hastane.depo.VeriDeposu;
import hastane.istisna.GecersizIslemException;
import hastane.model.Hasta;
import hastane.model.Kisi;

import java.time.LocalDate;

/** Giriş yapma ve hasta kaydı. */
public class KimlikServisi {

    private final VeriDeposu depo;

    public KimlikServisi(VeriDeposu depo) {
        this.depo = depo;
    }

    public Kisi girisYap(String eposta, String sifre) {
        Kisi kisi = depo.kisiBul(eposta);
        if (kisi == null || !kisi.sifreDogruMu(sifre)) {
            throw new GecersizIslemException("E-posta veya şifre hatalı.");
        }
        return kisi;
    }

    public Hasta hastaKaydet(String ad, String soyad, String telefon, String eposta, String sifre,
                             String tcKimlikNo, LocalDate dogumTarihi) {
        Hasta hasta = new Hasta(ad, soyad, telefon, eposta, sifre, tcKimlikNo, dogumTarihi);
        depo.kisiEkle(hasta);
        return hasta;
    }
}
