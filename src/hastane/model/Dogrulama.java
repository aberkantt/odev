package hastane.model;

import java.math.BigDecimal;

/**
 * Model sınıflarında tekrar eden doğrulama kontrollerini tek yerde toplayan yardımcı sınıf.
 * Nesnesi oluşturulamaz (private constructor), sadece static metotları vardır.
 */
public final class Dogrulama {

    private Dogrulama() {
    }

    /** Metin boş ya da null ise hata fırlatır, değilse kırpılmış (trim) halini döndürür. */
    public static String metinZorunlu(String deger, String alanAdi) {
        if (deger == null || deger.trim().isEmpty()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz.");
        }
        return deger.trim();
    }

    /** Sayı 0'dan büyük değilse hata fırlatır. */
    public static int pozitif(int deger, String alanAdi) {
        if (deger <= 0) {
            throw new IllegalArgumentException(alanAdi + " 0'dan büyük olmalıdır.");
        }
        return deger;
    }

    /** Tutar negatifse hata fırlatır. */
    public static BigDecimal negatifOlamaz(BigDecimal deger, String alanAdi) {
        if (deger == null || deger.signum() < 0) {
            throw new IllegalArgumentException(alanAdi + " negatif olamaz.");
        }
        return deger;
    }

    /** Basit e-posta kontrolü: içinde @ ve nokta olmalı. */
    public static String eposta(String deger) {
        String e = metinZorunlu(deger, "E-posta").toLowerCase();
        int at = e.indexOf('@');
        if (at < 1 || e.lastIndexOf('.') < at) {
            throw new IllegalArgumentException("Geçersiz e-posta adresi: " + deger);
        }
        return e;
    }
}
