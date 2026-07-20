Brosvod görünür ad güncellemesi - yalnız değişen dosyalar

Uygulananlar:
- 37 aktif CloudStream sağlayıcısının görünen adı `Brosvod • ...` yapıldı.
- 37 modülün sürümü 1 artırıldı; mevcut kurulumlar güncelleme alabilir.
- GitHub Actions tarafından üretilen plugins.json içindeki `name` alanı otomatik olarak `Brosvod • ...` yapılır.
- `internalName` değiştirilmez; eklentiler ikinci bir kopya olarak kurulmaz.
- InatBox oynatma/kategori/AES koduna dokunulmadı. Yalnız adı InatBox -> Brosvod • InatBox, sürümü 61 -> 62 oldu.

UYGULAMA
1. Bu ZIP'i mevcut Brosvod-CS3 kaynak klasörünün üzerine çıkarın.
2. Dosyaların değiştirilmesine izin verin.
3. GitHub'a gönderin. Ana dala push sonrası Derleyici workflow'u .cs3 ve plugins.json dosyalarını üretir.

PowerShell örneği:
git add -A
git commit -m "Prefix provider names with Brosvod"
git push

DERLEME SONRASI
CloudStream içinde repo/eklenti listesini yenileyin. Görünen isimler örneğin:
- Brosvod • DiziPal
- Brosvod • DiziYou
- Brosvod • InatBox

Not: Bu teslimde .cs3 ikilileri yerel olarak derlenmedi; GitHub Actions derlemesi hedeflenmiştir.
