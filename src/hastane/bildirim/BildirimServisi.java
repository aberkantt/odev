package hastane.bildirim;

import hastane.model.Kisi;

import java.util.ArrayList;
import java.util.List;

/**
 * Bir mesajı kayıtlı tüm kanallara iletir.
 * Hangi kanalın nasıl çalıştığını bilmez; sadece BildirimKanali arayüzünü tanır (ÇOK BİÇİMLİLİK).
 */
public class BildirimServisi {

    private final List<BildirimKanali> kanallar = new ArrayList<>();

    public void kanalEkle(BildirimKanali kanal) {
        kanallar.add(kanal);
    }

    public void bildir(Kisi alici, String mesaj) {
        for (BildirimKanali kanal : kanallar) {
            kanal.gonder(alici, mesaj);
        }
    }
}
