# Hastane / Klinik Randevu Sistemi

Nesneye Dayalı Programlama (NDP) dersi dönem projesi, **Seçenek A**.

Tek şubeli klinikleri ve çok şubeli hastaneleri destekleyen, rol tabanlı bir randevu sistemi.
Java 17 ile yazılmış bir konsol uygulamasıdır ve harici kütüphane kullanmaz.

> Proje geliştirme aşamasındadır. Ayrıntılı dokümantasyon tamamlandığında eklenecektir.

## Yol haritası

- [x] Proje iskeleti, `Kisi` soyut sınıfı ve `Hasta`
- [x] Hizmet hiyerarşisi (muayene, görüntüleme, laboratuvar)
- [ ] Kuruluş, şube, bölüm ve çalışma saatleri
- [ ] Çalışanlar (doktor, sağlık personeli), yönetici ve uygunluk aralıkları
- [ ] Randevu sınıfı ve onay mekanizması
- [ ] Çakışma kontrolü ve boş saat hesaplama
- [ ] Bildirim sistemi (uygulama içi, e-posta, SMS)
- [ ] Yönetim servisleri, yetki kontrolü ve demo veriler
- [ ] Rol bazlı konsol menüleri (hasta, çalışan, yönetici)
- [ ] Testler ve dokümantasyon (UML diyagramları)

## Çalıştırma

JDK 17 veya üstü gereklidir.

- Windows: `run.bat`
- Mac / Linux: `./run.sh`
