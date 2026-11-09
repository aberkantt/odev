package hastane.bildirim;

import hastane.model.Kisi;

/** SMS bildirimi (simülasyon: gerçekten SMS atmaz, ekrana yazar). Mesaj 160 karakterle sınırlanır. */
public class SmsBildirimi implements BildirimKanali {

    private static final int SMS_UZUNLUGU = 160;

    @Override
    public String getAd() {
        return "SMS";
    }

    @Override
    public void gonder(Kisi alici, String mesaj) {
        String kisa = mesaj.length() > SMS_UZUNLUGU ? mesaj.substring(0, SMS_UZUNLUGU - 3) + "..." : mesaj;
        System.out.println("   [SMS] " + alici.getTelefon() + " numarasına " + kisa.length() + " karakterlik mesaj gönderildi.");
    }
}
