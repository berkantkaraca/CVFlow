# CVFlow — Backend Kuralları

Bu dosya, kod incelemelerinde ve yeni kod yazımında uyulması zorunlu kuralları tutar.
Her kuralın kodu `BK-###` formatındadır. Yeni kural eklenirken numara artırılır,
kurallar silinmez (geçersizse "kaldırıldı" olarak işaretlenir).

## BK-001 — Döngü içinde repo yazma işlemi yasak

Bir koleksiyonu kaydederken/silerken döngü içinde `save()`, `saveAndFlush()` veya
`delete()` çağrılamaz. Tek sorguda toplu işlem yapılır:

- Kaydetme: `saveAll(...)`
- Silme: `deleteAll(...)`, `deleteAllByIdInBatch(...)` veya `deleteBy...In(...)` türevi
- Gerekçe: N sorgu yerine 1 sorgu; batch insert/delete, tek transaction.

Örnek (yanlış):

```java
for (CvLanguageVersion copy : copies) {
    languageVersionRepository.save(copy);
}
```

Örnek (doğru):

```java
languageVersionRepository.saveAll(copies);
```

## BK-002 — Toplu okumada N+1 yasak

Bir liste için döngü içinde tekil `find...ById(...)` / `find...ByX(...)` çağrılamaz.
`...In(Collection)` türeviyle tek sorguda çekilir, eşleştirme bellekte yapılır:

```java
Map<UUID, MasterCv> found = masterCvRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(MasterCv::getId, Function.identity()));
```

## BK-003 — Sayım için satır çekmek yasak

"Kaç tane var?" sorusu için `findAll...(...).size()` kullanılamaz.
`countBy...(...)` türevi kullanılır:

```java
long count = masterCvRepository.countByUserId(userId);
```

## BK-004 — Toplu API yanıtı özet döner

Toplu işlem endpoint'leri (`bulkDelete` gibi) kısmi başarıyı destekler ve
`{deleted: [...], failed: [{id, code}]}` formatında özet döner. Tekil hata
tüm grubu düşürmez; istemci özetten devam eder.
