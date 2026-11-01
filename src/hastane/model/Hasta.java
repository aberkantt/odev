package hastane.model;

import java.time.LocalDate;
import java.time.Period;

/** Randevu alan hasta. */
public class Hasta extends Kisi {

    private final String tcKimlikNo;
    private final LocalDate dogumTarihi;

    public Hasta(String ad, String soyad, String telefon, String eposta, String sifre,
                 String tcKimlikNo, LocalDate dogumTarihi) {
        super(ad, soyad, telefon, eposta, sifre);
        if (tcKimlikNo == null || !tcKimlikNo.matches("[1-9][0-9]{10}")) {
            throw new IllegalArgumentException("TC kimlik no 11 haneli olmalı ve 0 ile başlamamalıdır.");
        }
        if (dogumTarihi == null || dogumTarihi.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Doğum tarihi geçerli değil.");
        }
        this.tcKimlikNo = tcKimlikNo;
        this.dogumTarihi = dogumTarihi;
    }

    @Override
    public Rol getRol() {
        return Rol.HASTA;
    }

    public String getTcKimlikNo() {
        return tcKimlikNo;
    }

    /** Ekranda göstermek için TC'nin ortasını gizler: 123******01 */
    public String getMaskeliTcKimlikNo() {
        return tcKimlikNo.substring(0, 3) + "******" + tcKimlikNo.substring(9);
    }

    public LocalDate getDogumTarihi() {
        return dogumTarihi;
    }

    public int getYas() {
        return Period.between(dogumTarihi, LocalDate.now()).getYears();
    }
}
