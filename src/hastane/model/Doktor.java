package hastane.model;

/** Doktor: uzmanlık alanı ve akademik unvanı olan çalışan. */
public class Doktor extends Calisan {

    private String unvan;
    private String uzmanlikAlani;

    public Doktor(String ad, String soyad, String telefon, String eposta, String sifre,
                  SaglikKurulusu kurulus, String unvan, String uzmanlikAlani) {
        super(ad, soyad, telefon, eposta, sifre, kurulus);
        setUnvan(unvan);
        setUzmanlikAlani(uzmanlikAlani);
    }

    @Override
    public Rol getRol() {
        return Rol.DOKTOR;
    }

    @Override
    public String getUnvan() {
        return unvan;
    }

    public String getUzmanlikAlani() {
        return uzmanlikAlani;
    }

    public final void setUnvan(String unvan) {
        this.unvan = Dogrulama.metinZorunlu(unvan, "Unvan");
    }

    public final void setUzmanlikAlani(String uzmanlikAlani) {
        this.uzmanlikAlani = Dogrulama.metinZorunlu(uzmanlikAlani, "Uzmanlık alanı");
    }

    @Override
    public String toString() {
        return super.toString() + " - " + uzmanlikAlani;
    }
}
