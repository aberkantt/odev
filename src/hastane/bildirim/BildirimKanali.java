package hastane.bildirim;

import hastane.model.Kisi;

/**
 * Bildirim gönderme yolu (ARAYÜZ / interface).
 * Uygulama içi, e-posta, SMS ... hepsi bu sözleşmeyi uygular.
 * Yeni bir kanal (ör. WhatsApp) eklemek için sadece bu arayüzü uygulayan yeni bir sınıf yazmak yeterli;
 * mevcut kod değişmez (Strategy deseni + Açık/Kapalı prensibi).
 */
public interface BildirimKanali {

    String getAd();

    void gonder(Kisi alici, String mesaj);
}
