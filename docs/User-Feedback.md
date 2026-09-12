# CVFlow — Kullanıcı Gözüyle Değerlendirme

Tarih: 2026-09-13
Kapsam: MVP (Phase 1–8 tamamlanmış hali), doğrulanmış akışlar üzerinden.

## Artılar

- **Vaat yerine getiriliyor:** Bir kez yaz → TR/EN versiyon → template → PDF akışı uçtan uca çalışıyor.
  Bulk translate + accept-all, "Türkçe'yi yazıp İngilizce'yi sıfırdan düzenleme" ağrısını çözüyor.
- **AI güvenlik modeli çok iyi:** Öneri önce PENDING geliyor, kullanıcı kabul etmeden içerik değişmiyor.
  Testte modelin uydurma metrik eklediği görüldü ("response times by 30%") — bu mimari sayesinde
  kullanıcıya zarar veremeden yakalanıyor.
- **Editör hijyeni:** Autosave (~800ms) + manuel Save + 409 conflict banner + "Reload latest".
- **Canlı önizleme** PDF'e yakın; template + TR/EN section başlıkları doğru çalışıyor.
- Swagger/OpenAPI, tutarlı `ApiResponse` hata formatı, compose ile tek komutla ayağa kalkma.

## Eksiler

- **Auth yok (en büyük eksik):** Tek mock kullanıcı; linki bilen herkes her CV'yi görür/düzenler.
  Gerçek SaaS olarak kullanılamaz, sadece local/demo.
- **Undo / versiyon geçmişi yok:** Yanlış kabul edilen öneriyi veya kötü düzenlemeyi geri alma yolu yok.
- **Editör kaba:** Sürükle-bırak yok (sadece ↑↓), tarih alanları serbest metin (date picker yok),
  education'da tarih tek metin kutusu (tutarsız), fotoğraf yok, özel section eklenemiyor, 8 section sabit.
- **Önizleme ≠ PDF garantisi:** Önizleme React ile, PDF backend HTML ile ayrı implementasyon —
  zamanla ıraksama riski var.
- **PDF tarafı katı:** 3 sabit template, renk/font/margin ayarı yok, dosya adı hep `cv-en.pdf`, DOCX yok.
- **Sadece TR/EN;** arayüzün kendisi de sadece İngilizce.
- **AI şeffaflığı yok:** Gemini'ye ne gönderildiği / kaç istek gittiği görünmüyor; kota/maliyet kontrolü yok.
- **Veri taşınabilirliği yok:** JSON import/export yok — yedek alınamıyor.

## Öncelikli Geliştirmeler

1. **P0 — Auth (JWT) + kullanıcı izolasyonu.** Bu olmadan diğer her şey vitrin.
2. **P0 — İçerik versiyon geçmişi + geri alma.** AI'lı üründe güvenlik ağı şart.
3. **P1 — Prompt sertleştirme.** Testte yakalanan metrik uydurma olayına karşı: daha katı negative
   constraint + öneride "değişen kısım" vurgusu.
4. **P1 — DOCX export + ATS-sade template.** Gerçek işe alım akışına uyar.
5. **P1 — JSON yedekleme (export/import).**
6. **P2 —** Sürükle-bırak sıralama, date picker, foto alanı, özel section'lar, TR arayüz, daha fazla dil.
7. **P3 —** Cover letter, ilan-bazlı uyarlama, ATS skoru, herkese açık paylaşım linki, billing/limitler.

## Sonuç

MVP vaadini tutan, dürüst bir temel — AI güvenlik modeli ve veri mimarisi doğru kurulmuş.
SaaS olması için auth + geçmiş + export çeşitliliği şart.
