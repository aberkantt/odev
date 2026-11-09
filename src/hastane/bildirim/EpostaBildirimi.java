package hastane.bildirim;

import hastane.model.Kisi;

/** E-posta bildirimi (simülasyon: gerçekten e-posta atmaz, ekrana yazar). */
public class EpostaBildirimi implements BildirimKanali {

    @Override
    public String getAd() {
        return "E-posta";
    }

    @Override
    public void gonder(Kisi alici, String mesaj) {
        System.out.println("   [E-posta] " + alici.getEposta() + " adresine bildirim gönderildi.");
    }
}
