---
type: output
status: processed
created: 2026-05-16
updated: 2026-05-16
tags:
  - runbook
  - workflow
  - dev
  - tksc-001
---

# Workflow разработчика — TKSC-001

Прагматичный пошаговый flow от полученного handoff'а до готового MR. Каждый шаг — **Вход / Промпт / Выход**. Контекст накапливается от шага к шагу.

Этот RUNBOOK запускается в **репозитории `packagesearch`** (другая сессия opencode, отличная от той, где работал аналитик). Единственный мостик между двумя сессиями — файл handoff'а.

---

## Стартовый контекст

Что у разработчика на руках, когда он садится за работу:

### 1. Handoff от аналитика

Файл `FS/TKSC-001-handoff.md` (либо скопирован локально в репозиторий, либо открыт через shared vault аналитика). Содержит: refined AC (8 пунктов), edge cases (11), C4 архитектуру, business sequence flow, что dev решает сам, follow-ups.

### 2. Репозиторий и инструменты

- **Репозиторий:** `packagesearch` (Spring Boot 3, Java 17, Maven, parent `ru.it_alnc.ita_forms.core:ita-lib-bom`).
- **opencode** запущен в корне `packagesearch/`.
- **MCP подключены:**
  - `jetbrains` — навигация по коду (find usages, go to definition), запуск тестов через IDE terminal, refactoring helpers.
  - `postgres` — **read-only** доступ к схеме PRC и данным (для понимания конфигурации grace).
  - `sequence-diagrams` (или аналог) — генерация Mermaid sequence из кода или вручную.

### 3. Известная отправная точка из handoff'а

- Endpoint, который дорабатывается: `POST /FL/gracePeriod`.
- Изменение: additive (добавление поля в response). Request не меняется.
- Backwards compatibility критична.
- 4 dev-decision'а на dev'е: имя поля, технический тип, способ сериализации отсутствия значения, место логики вычисления.

---

## Лестница артефактов

После прохождения шагов в репозитории появится **7 артефактов** (4 документа + 3 изменения в коде):

| # | Файл | Что в нём |
|---|---|---|
| 1 | `docs/TKSC-001/impact.md` | Карта изменений: какие файлы трогаем, какие таблицы БД задействованы |
| 2 | `docs/TKSC-001/as-is-sequence.md` | Sequence diagram **текущего** поведения `/FL/gracePeriod` |
| 3 | `docs/TKSC-001/plan.md` | План реализации с dev-decisions |
| 4 | `src/main/java/.../GracePeriodResponseDto.java` | + новое поле |
| 5 | `src/main/java/.../GracePeriodService.java` | + метод вычисления + вызов |
| 6 | `src/test/java/.../GracePeriodServiceTest.java` | Новый тестовый класс, 11 unit-тестов |
| 7 | `docs/TKSC-001/mr-description.md` | Описание MR для ревьюеров |

---

## Шаг 1 — Impact analysis (jetbrains + postgres)

### Вход

- Handoff `docs/FS/TKSC-001-handoff.md` (открыт через `@`-mention или скопирован).
- Доступ к коду через MCP `jetbrains`.
- Доступ к схеме БД через MCP `postgres`.

### Промпт

```text
Прочитай @docs/FS/TKSC-001-handoff.md (или вставь handoff сюда).

Задача — добавить в response endpoint'а POST /FL/gracePeriod значение
«осталось дней» (additive change, backwards-compatible).

Через MCP jetbrains:
1. Найди файл с endpoint POST /FL/gracePeriod (поиск по
   @PostMapping или @RequestMapping("/FL/gracePeriod")).
2. Открой связанный Controller, Service, Request DTO, Response DTO.
3. Найди Repository / Entity для чтения grace-периода из БД.
4. Find usages для каждого затронутого файла — кто вызывает
   Controller (тесты, другие endpoint'ы)?

Через MCP postgres (read-only):
1. Прочитай схему таблицы для grace-периода (вероятно
   GRACEPERIODGUIDELINE в схеме PRC).
2. Опиши поля: тип, nullable, что значит каждое (особенно поля
   `gracePeriod`, `begDate`, `endDate`, `defaultPaymentDuration`).
3. Покажи 2-3 примера строк (обезличенных) для понимания формата.

Собери impact-карту в docs/TKSC-001/impact.md:

## 1. Файлы, которые меняем
| Файл | Тип изменения | Что добавляем |

## 2. Файлы, которые НЕ трогаем (удержание scope)
- Request DTO — не меняем (контракт запроса остаётся)
- Controller — не меняем (endpoint и его сигнатура не меняются)
- Repository / Entity — не меняем (БД не меняется)

## 3. Таблицы БД (read-only)
| Таблица | Используется как | Какие поля релевантны |

## 4. Зависимости
- Что вызывает Controller (тесты, integration tests)
- Что Controller вызывает дальше (Service → QueryBuilder → Repository)

## 5. Backwards compatibility — обоснование
Краткое: почему additive change не ломает существующих потребителей.
```

### Выход

`docs/TKSC-001/impact.md` со списком файлов, таблиц, зависимостей. Должно быть **точно ясно**, что меняется и что нет.

**Проверка:** ни один файл из «не трогаем» не должен оказаться в плане Шага 3.

---

## Шаг 2 — AS-IS sequence diagram

### Вход

- `docs/TKSC-001/impact.md` (список файлов из Шага 1).
- Доступ к коду через MCP `jetbrains`.
- MCP `sequence-diagrams` (опционально — для автогенерации).

### Промпт

```text
На основе docs/TKSC-001/impact.md создай AS-IS sequence diagram
текущего поведения POST /FL/gracePeriod в
docs/TKSC-001/as-is-sequence.md.

Через MCP jetbrains прочитай существующий код Controller → Service
→ QueryBuilder → Repository → БД. Построй sequence:
- Какие классы вызывают какие методы
- Какие данные передаются
- Где сейчас вычисляется graceEndDate, paymentDate
- Где **отсутствует** вычисление «осталось дней» (это место для
  изменения)

Формат — Mermaid sequenceDiagram. В отличие от business-sequence
из handoff'а, на этом уровне МОЖНО упоминать имена классов и
методов — это AS-IS реального кода, не business-flow.

После диаграммы — короткий комментарий «Где встроиться» (1-2 абзаца):
конкретно после какого вызова в Service нужно добавить новый метод
вычисления.
```

### Выход

`docs/TKSC-001/as-is-sequence.md` с Mermaid sequence + комментарий «где встроиться». Должно быть очевидно для ревьюера, **в какую точку кода** ляжет новое изменение.

---

## Шаг 3 — План реализации с dev-decisions

### Вход

- `docs/TKSC-001/impact.md`
- `docs/TKSC-001/as-is-sequence.md`
- `FS/TKSC-001-handoff.md` (раздел «Что dev решает сам»)

### Промпт

```text
На основе предыдущих артефактов составь план реализации в
@docs/TKSC-001/plan.md.
Артифакты:
@docs/FS/TKSC-001-handoff.md
@docs/TKSC-001/as-is-sequence.md
@docs/TKSC-001/impact.md


## 1. Dev-decisions (зафиксировать явно)

Из раздела «Что разработчику решать самому» в handoff'е — выбери
конкретные значения с обоснованием.

Через MCP jetbrains посмотри соглашения проекта (имена полей в
существующих DTO, использование @JsonInclude, Optional, nullable
Integer и т.д.) и предложи решения, согласованные со стилем
кодовой базы:

1. Имя поля в response (camelCase, лаконичное)
2. Технический тип (Integer / Long / Optional / nullable Integer)
3. Способ сериализации отсутствия значения (@JsonInclude.NON_NULL?
   отдельная аннотация? Optional?)
4. Место логики вычисления (новый private метод в Service /
   отдельный helper-класс / inline)

## 2. Изменения в коде

| Файл | Что добавить | Где именно (метод / строка) |

## 3. Тест-план (под все 11 edge cases handoff'а)

| # | Edge case | Тест-метод |
|---|---|---|
| 1 | Grace заканчивается сегодня в 23:00 MSK → `1` | testGraceEndsToday_returnsOne |
| ... | ... | ... |

## 4. Risk note — типичный bug

ChronoUnit.DAYS.between(ZonedDateTime, ZonedDateTime) считает
24-часовые интервалы между instants, НЕ календарные дни.
Для календарной семантики MSK (Q1=A, Q2=B):
- Привести обе даты к LocalDate через
  withZoneSameInstant(ZoneId.of("Europe/Moscow")).toLocalDate()
- Считать ChronoUnit.DAYS.between(LocalDate, LocalDate)
- Это покрывает AC #3 (календарный день), AC #4 (MSK),
  AC #8 (полуночная граница декремента)
- Без этого Edge cases #5, #6, #8 упадут на полуночных границах.

## 5. Backwards compat checklist

- [ ] Request DTO не меняется
- [ ] Controller signature не меняется
- [ ] Существующие тесты остаются зелёными
- [ ] Новое поле — nullable / @JsonInclude.NON_NULL, чтобы старые
      потребители его игнорировали без ошибок
```

### Выход

`docs/TKSC-001/plan.md` с конкретными dev-decisions, тест-планом на 11 кейсов, явным risk note про `ChronoUnit` truncation, backwards-compat checklist.

**Это ключевой артефакт перед кодом.** Не начинай реализацию, пока plan не утверждён глазами.

---

## Шаг 4 — Реализация (DTO + Service)

### Вход

- `docs/TKSC-001/plan.md`
- Доступ к коду через MCP `jetbrains`.

### Промпт

```text
Реализуй изменения по docs/TKSC-001/plan.md.

Через MCP jetbrains:

1. Открой Response DTO (GracePeriodResponseDto.java).
   - Добавь поле с именем, типом и аннотациями из plan §1.
   - Используй @JsonInclude(JsonInclude.Include.NON_NULL) если plan
     это требует.
   - Добавь getter / setter (или используй существующий Lombok-стиль,
     если он применяется в проекте).

2. Открой GracePeriodService.java.
   - Добавь приватный метод вычисления по plan §1.4. Реализация
     (псевдокод — адаптируй под стиль проекта):

     ZoneId msk = ZoneId.of("Europe/Moscow");
     LocalDate today = calculationDate.withZoneSameInstant(msk).toLocalDate();
     LocalDate end   = graceEndDate.withZoneSameInstant(msk).toLocalDate();
     if (end.isBefore(today)) return null;
     return (int) ChronoUnit.DAYS.between(today, end);

   - В существующем методе gracePeriodSearch добавь вызов нового
     метода СРАЗУ ПОСЛЕ result.setGraceEndDate(graceEndDate), установи
     результат в response DTO.

3. Проверь diff через jetbrains:
   - Не должны измениться: Request DTO, Controller, Repository,
     Entity, QueryBuilder.
   - Должны измениться только: Response DTO + Service.

НЕ запускай тесты на этом шаге — это в Шаге 6.
```

### Выход

Изменения в 2 файлах:
- `src/main/java/.../dto/graceperiod/GracePeriodResponseDto.java` — новое поле
- `src/main/java/.../service/GracePeriodService.java` — новый private метод + вызов в gracePeriodSearch

Diff должен быть **компактный** (≤ 50 строк добавленного кода в обоих файлах суммарно).

---

## Шаг 5 — Unit-тесты на 11 edge cases

### Вход

- Реализация из Шага 4.
- Тест-план из `docs/TKSC-001/plan.md` §3.

### Промпт

```text
Создай unit-тесты в
src/test/java/ru/it_alnc/packagesearch/service/GracePeriodServiceTest.java
(если файла нет — создай новый класс).

11 тестовых методов на все edge cases из handoff'а:

1. grace заканчивается сегодня в 23:00 MSK → expected: 1
2. grace заканчивается завтра в 10:00 MSK → expected: 1
3. grace заканчивается через 2 дня в 15:00 MSK → expected: 2
4. grace уже истёк (graceEndDate в прошлом) → expected: null
5. запрос за 59 минут до полуночи MSK, остаётся 3 дня → expected: 3
6. (тот же grace) запрос через минуту после полуночи MSK → expected: 2
7. grace заканчивается в 02:00 MSK след. дня, запрос за 30 мин до
   полуночи MSK → expected: 1
8. grace заканчивается завтра в 23:59 MSK, запрос сразу после
   полуночи MSK → expected: 1
9. конфигурация grace отсутствует → expected: null
10. graceEndDate пустая при наличии grace-записи → expected: null
11. grace истёк давно (месяц назад) → expected: null

Требования:
- JUnit 5, AssertJ или стандартные assertions — как принято в
  проекте (проверь существующие тесты через jetbrains).
- Mock зависимостей если нужно (GracePeriodQueryBuilder).
- Для проверки полуночных переходов используй ZonedDateTime с явной
  таймзоной Europe/Moscow:
  ZonedDateTime.of(2026, 5, 15, 23, 1, 0, 0, ZoneId.of("Europe/Moscow"))

Через MCP jetbrains:
- Посмотри стиль существующих тестов в проекте (если есть)
- Используй те же naming conventions, structure (Given-When-Then?)

Имя тестового метода — описательное:
testDaysRemaining_GraceEndsToday_ReturnsOne
testDaysRemaining_GraceExpired_ReturnsNull
и т.д.

НЕ запускай mvn test на этом шаге — это в Шаге 6.
```

### Выход

`GracePeriodServiceTest.java` с 11 тестами, имена осмысленные, edge cases покрыты 1:1 со списком из handoff'а.

---

## Шаг 6 — Local run + self-review

### Вход

- Реализация (Шаги 4-5)
- Diff с момента старта работы

### Промпт

```text
1. Через MCP jetbrains запусти тесты:
   mvn -Dtest=GracePeriodServiceTest test

   Проверь:
   - Все 11 новых тестов зелёные.
   - Существующие тесты (если есть) тоже зелёные — backwards
     compatibility не нарушена.

2. Если хотя бы один тест красный — НЕ переходи дальше, найди
   причину. Типичные:
   - ChronoUnit.DAYS.between на ZonedDateTime напрямую (truncation
     bug) → исправь на LocalDate
   - неверная обработка null в graceEndDate → добавь проверку
   - timezone не явно указан → добавь ZoneId.of("Europe/Moscow")

3. Когда тесты зелёные — self-review через jetbrains.
   Проверь diff на следующие риски:

   ## Self-review checklist

   - [ ] ChronoUnit.DAYS.between используется на LocalDate, не на
         ZonedDateTime
   - [ ] Timezone MSK явно указан везде где есть преобразование
         ZonedDateTime → LocalDate
   - [ ] null-handling корректный во всех ветках (edge cases 4, 9, 10, 11)
   - [ ] Request DTO не модифицирован (backwards compat)
   - [ ] Controller signature не модифицирована
   - [ ] @JsonInclude или эквивалент применён корректно (старые
         потребители не получат поле null в JSON, если plan этого требует)
   - [ ] Нет лишних import'ов
   - [ ] Нет лишних изменений в файлах вне scope
   - [ ] Все 11 edge cases имеют свой тест
   - [ ] Тесты используют осмысленные имена

   По каждому пункту — pass / fail / N/A с комментарием. Если есть
   fail — исправь и перезапусти тесты.

Опционально — повторный прогон mvn test после правок.
```

### Выход

- `mvn -Dtest=GracePeriodServiceTest test` → все зелёные.
- Self-review checklist пройден, findings (если были) исправлены.
- Diff финальный и стабильный.

**Это критический gate перед MR.** Если что-то красное — назад на Шаг 4.

---

## Шаг 7 — MR-описание

### Вход

- Все артефакты Шагов 1-6.
- `FS/TKSC-001-handoff.md` (для ссылки).

### Промпт

```text
Собери описание Merge Request в docs/TKSC-001/mr-description.md.

## TKSC-001 — Унификация показателя «осталось дней» grace-периода

### Что меняется
1-2 фразы про additive change в response endpoint'а
POST /FL/gracePeriod.

### Контракт
- Endpoint: POST /FL/gracePeriod (не меняется)
- Request: не меняется
- Response: добавлено поле `<имя_поля>` (тип `<тип>`, nullable)
  - Возвращается, если grace активен — целое число календарных дней
    до конца grace (MSK, день включительно)
  - Не возвращается / null, если grace истёк, конфигурация отсутствует,
    или graceEndDate пустая

### Изменённые файлы
- `src/main/java/.../GracePeriodResponseDto.java` — +N строк
- `src/main/java/.../GracePeriodService.java` — +N строк
- `src/test/java/.../GracePeriodServiceTest.java` — новый файл, N тестов

### Тестовое покрытие
- 11 unit-тестов на все edge cases из handoff'а
- Все существующие тесты остались зелёными
- Local run: `mvn -Dtest=GracePeriodServiceTest test` → OK

### Backwards compatibility
- Request DTO не изменён
- Controller signature не изменён
- Новое поле в response — nullable + @JsonInclude.NON_NULL
  (старые потребители получают JSON без этого ключа)

### Ссылки
- Handoff аналитика: <ссылка или путь>
- Impact map: docs/TKSC-001/impact.md
- AS-IS sequence: docs/TKSC-001/as-is-sequence.md
- План реализации: docs/TKSC-001/plan.md

### Self-review notes
Если в Шаге 6 были findings — перечислить findings + правки.

### Out of scope (по handoff'у)
- Push / immediate invalidation — отдельный тикет следующего квартала
- Изменение UI-формулировок на стороне потребителей
- Локализация / форматирование числа на стороне потребителей

### Что ревьюеру обратить внимание
- LocalDate vs ZonedDateTime в методе вычисления (типичная ловушка
  ChronoUnit truncation)
- Тесты на полуночные границы MSK (#5, #6, #8) — критичны
- @JsonInclude корректно настроен для null-handling
```

### Выход

`docs/TKSC-001/mr-description.md` — копи-паст в GitLab/Bitbucket MR при создании.

---

## Финальное состояние

После прохождения 7 шагов в репозитории `packagesearch`:

```
docs/TKSC-001/
├── impact.md
├── as-is-sequence.md
├── plan.md
└── mr-description.md

src/main/java/ru/it_alnc/packagesearch/
├── dto/graceperiod/GracePeriodResponseDto.java   (модифицирован: +1 поле)
└── service/GracePeriodService.java               (модифицирован: +1 метод)

src/test/java/ru/it_alnc/packagesearch/service/
└── GracePeriodServiceTest.java                   (новый файл, 11 тестов)
```

Готово к созданию MR.

---

## Опциональные расширения

- **Шаг 8 — Integration test** через testcontainers (Postgres + Kafka) — если в проекте есть integration suite. Не обязательно для additive change в одном поле.
- **Шаг 9 — Performance check** — если есть подозрение на overhead вычисления. Для одного `ChronoUnit.DAYS.between` это noise, можно пропустить.
- **Шаг 10 — ADR** — если в процессе появились архитектурные решения (например, вынос логики в helper-класс с переиспользованием). Не обязательно, handoff явно говорит «ADR не требуется».
