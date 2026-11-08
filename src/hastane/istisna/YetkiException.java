package hastane.istisna;

/** Kullanıcının rolü/yetkisi bu işlemi yapmaya yetmiyor. */
public class YetkiException extends HastaneException {

    public YetkiException(String mesaj) {
        super(mesaj);
    }
}
