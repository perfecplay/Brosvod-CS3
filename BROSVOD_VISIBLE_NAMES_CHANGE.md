# Brosvod Görünür Sağlayıcı Adları

Bu değişiklik, CloudStream içinde aynı sağlayıcının farklı repolardaki kopyalarını ayırt etmek için hazırlanmıştır.

## Uygulanan kural

- Görünen eklenti/sağlayıcı adı: `Brosvod • KaynakAdı`
- `internalName` değiştirilmez.
- Mevcut kurulumun üzerine güncelleme gelebilmesi için her aktif modülün sürümü 1 artırılmıştır.
- InatBox çalışma mantığına dokunulmamıştır; yalnızca görünen adı ve paket sürümü değiştirilmiştir.

## InatBox

- Kaynak mantığı: çalışan InatBox 61 tabanı
- Yeni paket sürümü: 62
- Görünen ad: `Brosvod • InatBox`
- `internalName`: `InatBox` olarak kalır.

## Derleme

GitHub Actions ana dala gönderimden sonra `.cs3` dosyalarını ve `plugins.json` dosyasını yeniden üretir.
Workflow, üretilen `plugins.json` içindeki yalnızca `name` alanını prefixler; `internalName` alanına dokunmaz.
