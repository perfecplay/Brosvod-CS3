# Brosvod-CS3 / InatBox Teknik Handoff

## Proje

- GitHub repo: `https://github.com/perfecplay/Brosvod-CS3`
- Kaynak dalı: `main`
- Derleme/yayın dalı: `builds`
- CloudStream repo adresi:
  - `https://raw.githubusercontent.com/perfecplay/Brosvod-CS3/refs/heads/builds/repo.json`
- Çalışan InatBox referans sürümü: **61**
- Ana kaynak dosyaları:
  - `InatBox/src/main/kotlin/com/keyiflerolsun/InatBox.kt`
  - `InatBox/src/main/kotlin/com/keyiflerolsun/InatBoxPlugin.kt`
  - `InatBox/src/main/kotlin/com/keyiflerolsun/FilmizleeeeeExtractor.kt`
  - `InatBox/build.gradle.kts`

## Önemli teslim tercihi

Küçük değişikliklerde bütün projeyi veya tam kaynak ZIP’ini verme. Yalnızca değişen dosyaları ver. Sürüm numarasını her yayınlanan düzeltmede artır.

---

## Mevcut çalışan durum

InatBox 61 sürümünde:

- Ana sayfa ve kategoriler geliyor.
- Kanal, film ve dizi listeleri sunucudan dinamik çekiliyor.
- Dinamik içerik adresi çözülüyor.
- Şifreli Filmizleeeee tabanlı oynatma linkleri çözülüyor.
- Gerçek `.m3u8` bağlantısı CloudStream oynatıcısına veriliyor.
- Gerekli `User-Agent`, `Referer`, cookie ve diğer header’lar aktarılıyor.
- `GlobalPluginChecker`, başka repo zorunluluğu, uzak hash kilidi veya korumalı hazır `.cs3` kullanılmıyor.
- Eklenti bizim açık kaynak kodumuzdan GitHub Actions ile derleniyor.

## Normal liste güncellemeleri

Yeni kanal, film, dizi veya kategori sunucu tarafına eklenirse eklenti normalde otomatik görür. Liste CS3 içine sabit gömülü değildir.

Kaynak kod güncellemesi yalnızca şu durumlarda gerekir:

- Yeni bir `chType` eklenirse
- AES veya cevap şifreleme biçimi değiştirilirse
- JSON alan isimleri değiştirilirse
- Yeni bir video hostu/extractor eklenirse
- Oynatma için gereken header/cookie biçimi değiştirilirse

---

## Daha önce bulunan sorunlar

### 1. Ana sayfanın boş kalması

İlk kaynak eski sabit adresi kullanıyordu:

```text
https://static.staticsave.com/fast/ct.js
```

Çalışan eklenti gerçekte önce şu adresi okuyordu:

```text
https://raw.githubusercontent.com/mtlshash/cert/main/hash
```

Bu cevap:

1. Sertifika başlıklarından temizleniyor.
2. `şifreliVeri:aesAnahtarı` olarak ayrılıyor.
3. AES ile birinci kez çözülüyor.
4. Çıkan cevap tekrar `şifreliVeri:aesAnahtarı` olarak ayrılıyor.
5. AES ile ikinci kez çözülüyor.
6. JSON içindeki `DC10` alanı güncel içerik adresi olarak kullanılıyor.
7. Hata olursa eski `ct.js` adresine fallback yapılıyor.

Bazı AES anahtarları Base64 biçimindeydi. Anahtar önce Base64 decode edilmeli; sonuç 16/24/32 bayt değilse ham UTF-8 anahtara düşülmeli.

### 2. Liste geldiği halde içeriklerin çoğunun açılmaması

Kod yalnızca şu türü yakalıyordu:

```text
tekli_regex_lb_sh_3
```

Gerçek içeriklerin çoğu şu türle geliyordu:

```text
tekli_regex_lb_sh_3_mode
```

Bu yüzden şifreli URL çözülmeden extractora gönderiliyordu.

Çözüm:

- `tekli_regex_lb_sh_3_mode` türünü de yakala.
- `chReg` içindeki `Regex1` AES anahtarını kullan.
- Cevabı iki aşamalı çöz.
- Gerçek `.m3u8` URL’sini çıkar.
- İstekten gelen gerekli header ve cookie’leri oynatıcı linkine ekle.

### 3. İlk derleme hatası

`TurkAnime` içinde `cryptoAESHandler()` artık `suspend` olduğu için onu çağıran fonksiyon da `suspend` yapılmıştı.

---

## Çalışan yapıyı nasıl analiz ettik

### MT Manager

Çalışan `InatBox.cs3` içindeki `classes.dex` açıldı ve şu sınıflar incelendi:

```text
com/kraptor/InatBox
com/kraptor/InatBox$Companion
com/kraptor/InatBox$Companion$getDomain$1
com/kraptor/Domain
```

Özellikle `getDomain` içindeki iki aşamalı AES ve `DC10` seçimi buradan çıkarıldı.

### HTTP Toolkit HAR

Oynatmayan içerikler sırasında HAR kaydı alındı. HAR sayesinde şunlar kesin görüldü:

- Gerçek `chType`
- `chReg` ve `Regex1`
- İstek header’ları
- Şifreli cevap yapısı
- Çözümden çıkan gerçek `.m3u8` adresi

Yeni sorunlarda tahmin yürütmek yerine yine HAR kullanılmalı.

---

## Yeni sorun çıkarsa izlenecek teşhis sırası

### Ana sayfa veya kategoriler boşsa

Kontrol et:

1. `DOMAIN_SOURCE_URL` erişiliyor mu?
2. İlk ve ikinci AES çözme başarılı mı?
3. JSON içinden `DC10` çıkıyor mu?
4. `DC10` URL’si erişilebilir mi?
5. Kategori cevabı JSON array olarak çözülebiliyor mu?
6. Kategori isteklerinde doğru POST body, `Referer`, `X-Requested-With`, `Host` ve `User-Agent` kullanılıyor mu?

Geçici loglar:

```kotlin
Log.d("InatBox", "contentUrl=$contentUrl")
Log.d("InatBox", "categoryUrl=$categoryUrl")
Log.d("InatBox", "categoryType=$categoryType")
Log.e("InatBox", "decode failed", throwable)
```

### Liste geliyor ama içerik açılmıyorsa

Her oynatma öğesinde şunları kaydet:

```kotlin
Log.d("InatBox", "chName=${item.optString("chName")}")
Log.d("InatBox", "chType=${item.optString("chType")}")
Log.d("InatBox", "chUrl=${item.optString("chUrl")}")
Log.d("InatBox", "chHeaders=${item.optString("chHeaders")}")
Log.d("InatBox", "chReg=${item.optString("chReg")}")
```

Sonra:

1. Yeni bir `chType` var mı?
2. URL doğrudan `.m3u8`/`.mpd` mi?
3. Şifreli ara endpoint mi?
4. `Regex1` veya başka anahtar alanı var mı?
5. Cookie/Referer/User-Agent zorunlu mu?
6. `loadExtractor()` doğru hostu tanıyor mu?
7. Extractor başarısızsa doğrudan link fallback’i uygun mu?

### Gerekli test kayıtları

Yeni hata bildirirken şunları topla:

- Çalışan ve çalışmayan içerik adı
- Ekran görüntüsü
- CloudStream logcat
- HTTP Toolkit HAR
- Gerekirse MT Manager’dan ilgili smali metodu
- GitHub Actions derleme hatasının ilk gerçek hata bloğu

---

## GitHub derleme/yayın yapısı

Workflow:

```text
.github/workflows/Derleyici.yml
```

İşleyiş:

1. `main` dalını checkout eder.
2. `builds` dalını checkout eder.
3. `./gradlew make makePluginsJson` çalıştırır.
4. Üretilen `.cs3` dosyalarını ve `plugins.json` dosyasını `builds` dalına yükler.
5. `repo.json` da `builds` dalında tutulur.

GitHub ayarı:

```text
Settings → Actions → General → Workflow permissions
```

Burada:

```text
Read and write permissions
```

seçili olmalı.

## Güncelleme komutları

```powershell
cd "F:\Desktop\Brosvod-CS3-ready"

git add InatBox/src/main/kotlin/com/keyiflerolsun/InatBox.kt InatBox/build.gradle.kts
git commit -m "Açıklayıcı commit mesajı"
git push
```

Push sonrası GitHub Actions otomatik çalışır.

---

## Koruma konusunda önemli uyarı

Çalışan üçüncü taraf `InatBox.cs3` içinde şunlar vardı:

- `GlobalPluginChecker`
- `cs-kraptor` repo kontrolü
- `FullHDFilmizlesene` zorunluluğu
- Uzak hash/sertifika kontrolü
- Lock dosyası

Bu nedenle üçüncü taraf hazır `.cs3` dosyasını kalıcı çözüm olarak repoya koyma. Yalnızca davranış ve ağ akışını analiz et; koruma sınıflarını alma.

---

## Başka yapay zekâya verilecek kısa görev metni

Aşağıdaki metni doğrudan kullan:

> `perfecplay/Brosvod-CS3` reposundaki InatBox CloudStream CS3 sağlayıcısında çalış. Çalışan referans sürüm 61’dir. Mevcut çalışan domain çözme, iki aşamalı AES, DC10, kategori çekme ve `tekli_regex_lb_sh_3_mode` oynatma mantığını bozma. Üçüncü taraf koruma sınıflarını, GlobalPluginChecker, başka repo zorunluluğu veya prebuilt CS3 ekleme. Sorunu HAR/logcat üzerinden teşhis et. Yeni `chType`, şifreleme, header, cookie veya extractor değişikliği varsa yalnızca gerekli kaynak dosyalarını güncelle. Küçük değişiklikte bütün projeyi değil yalnızca değişen dosyaları teslim et. Sürüm numarasını artır ve GitHub Actions ile derlenebilir durumda bırak.

