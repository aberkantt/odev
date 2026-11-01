package hastane.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Sistemdeki tüm kişilerin (hasta, doktor, personel, yönetici) ortak atası.
 *
 * SOYUTLAMA  : abstract sınıftır, doğrudan "new Kisi(...)" yapılamaz.
 * KAPSÜLLEME : tüm alanlar private; değişiklikler doğrulama yapan setter'lar üzerinden olur.
 *              Şifre hiçbir zaman dışarı verilmez, sadece özeti (hash) saklanır.
 * ÇOK BİÇİMLİLİK: getRol() her alt sınıfta farklı sonuç döndürür.
 */
public abstract class Kisi {

    private static int sonId = 0;

    private final int id;
    private String ad;
    private String soyad;
    private String telefon;
    private String eposta;
    private String sifreOzeti;

    protected Kisi(String ad, String soyad, String telefon, String eposta, String sifre) {
        this.id = ++sonId;
        setAd(ad);
        setSoyad(soyad);
        setTelefon(telefon);
        setEposta(eposta);
        sifreDegistir(sifre);
    }

    /** Her alt sınıf kendi rolünü söyler. */
    public abstract Rol getRol();

    public final boolean sifreDogruMu(String denenenSifre) {
        return denenenSifre != null && sifreOzeti.equals(ozetle(denenenSifre));
    }

    public final void sifreDegistir(String yeniSifre) {
        if (yeniSifre == null || yeniSifre.length() < 4) {
            throw new IllegalArgumentException("Şifre en az 4 karakter olmalıdır.");
        }
        this.sifreOzeti = ozetle(yeniSifre);
    }

    /** Şifreyi SHA-256 ile özetler; düz metin şifre hiçbir yerde tutulmaz. */
    private static String ozetle(String metin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(metin.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 bulunamadı", e);
        }
    }

    // ---------- getter / setter ----------

    public int getId() {
        return id;
    }

    public String getAd() {
        return ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public String getAdSoyad() {
        return ad + " " + soyad;
    }

    public String getTelefon() {
        return telefon;
    }

    public String getEposta() {
        return eposta;
    }

    public final void setAd(String ad) {
        this.ad = Dogrulama.metinZorunlu(ad, "Ad");
    }

    public final void setSoyad(String soyad) {
        this.soyad = Dogrulama.metinZorunlu(soyad, "Soyad");
    }

    public final void setTelefon(String telefon) {
        this.telefon = Dogrulama.metinZorunlu(telefon, "Telefon");
    }

    public final void setEposta(String eposta) {
        this.eposta = Dogrulama.eposta(eposta);
    }

    @Override
    public String toString() {
        return getAdSoyad() + " (" + getRol().getGorunenAd() + ")";
    }
}
