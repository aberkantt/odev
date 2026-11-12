package hastane;

import hastane.arayuz.GirisEkrani;
import hastane.arayuz.Konsol;
import hastane.servis.HastaneSistemi;

/** Programın başlangıç noktası. */
public class Main {

    public static void main(String[] args) {
        HastaneSistemi sistem = DemoVeri.olustur();
        sistem.disKanallariEkle();   // e-posta ve SMS bildirimleri (simülasyon) açılır
        try {
            new GirisEkrani(sistem, new Konsol()).baslat();
        } catch (Konsol.GirdiBittiException e) {
            System.out.println();
            System.out.println("Girdi sona erdi, program kapatılıyor.");
        }
    }
}
