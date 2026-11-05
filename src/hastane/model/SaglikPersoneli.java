package hastane.model;

/** Doktor dışındaki sağlık çalışanı: hemşire, laborant, radyoloji teknisyeni ... */
public class SaglikPersoneli extends Calisan {

    private String gorev;

    public SaglikPersoneli(String ad, String soyad, String telefon, String eposta, String sifre,
                           SaglikKurulusu kurulus, String gorev) {
        super(ad, soyad, telefon, eposta, sifre, kurulus);
        setGorev(gorev);
    }

    @Override
    public Rol getRol() {
        return Rol.SAGLIK_PERSONELI;
    }

    @Override
    public String getUnvan() {
        return gorev;
    }

    public final void setGorev(String gorev) {
        this.gorev = Dogrulama.metinZorunlu(gorev, "Görev");
    }
}
