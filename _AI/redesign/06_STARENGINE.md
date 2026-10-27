# StarEngine: własny silnik testowy (priorytet maksymalny)

Status: **plan, nie started.** Decyzja ownera 2026-10-27: kasacja legacy
(WO-144–147) czeka, bo nie da się jej zrobić bez potężnego silnika do
testów. Ten plik to wymagania + fazy. Język: polski (plik roboczy ownera),
identyfikatory techniczne po angielsku.

## 0. Twarde ograniczenia (nie do ruszenia)

- **Żadnego verbatim-restore starego StarEngine** (revert `b60a7029`
  stoi, `_AI/OPENBW-HANDOFF.md`: „No verbatim StarEngine restore").
  Budujemy nowy silnik, nie odkupujemy starego.
- **OpenBW zamrożone** (vehicle: tylko Wine; #216 hands-off). StarEngine
  nie konkuruje z OpenBW — zastępuje je tam, gdzie OpenBW miało być
  platformą testową, a jest nią Wine (wolno) albo nic.
- **Wine = ground truth.** Jedyna prawdziwa gra, jaką mamy. StarEngine
  jest kalibrowany względem logów Wine (`BUILDINGS.json`: klatki warpów,
  banki, supply), nigdy odwrotnie i nigdy „z pamięci".
- Gra leci w tle w tej chwili — ten plan to same odczyty + jeden plik,
  zero suite'ów, zero gier.

## 1. Po co (cel mierzalny)

1. **Bramka otwarcia in-suite**: „Zealot into Goon" grany od 1. klatki do
   50 supply w simie, asercje: kolejność budynków = BO, bank ≤550 przed
   50 supply (dziś to robi `scripts/assert-wine-opening.sh` na logach
   prawdziwej gry, ~15 minut; sim ma to robić w sekundach).
2. **Odblokowanie kasacji legacy**: każda pozycja WO-144–147 dostaje
   gate simowy zanim plik wyląduje w `src-old/` (dziś kasacja jedzie na
   grach Wine, 1 gra = 1 wieczór).
3. **Szybkość > wierność** (decyzja ownera): silnik ma być szybki, nie
   idealny. Dwa tryby (patrz §5).

## 2. Co już mamy (inwentarz, stan na dziś)

| Element | Gdzie | Stan |
|---|---|---|
| Kopia statyczna TauCross | `src/tests/resources/taucross-snapshot.json` | JEST: 128×128 kafle (`x`/`.`/`w`), 21 regionów z sąsiadami, 21 choke'ów (środek+szerokość+końce+regiony), 93 minerały + 9 gejzerów **z amounts**, 3 starty, 1 preplaced |
| Loader mapy | `src/tests/unit/sim/SimMap.java` | JEST: ładuje snapshot, podpina `Regions` + `MapTiles` (walkable/buildable/explored), helpery `minerals()`/`geysers()`/`preplaced()` |
| Dumper (ground truth) | `src/atlantis/map/snapshot/MapSnapshot*.java` | JEST: `MAP_SNAPSHOT=1` zrzuca pełną statykę z żywej gry; format czyta i pisze ta sama klasa (brak dryfu schematu) |
| Kroczek walki | `src/tests/unit/sim/SimWorld.java` + `SimArena.java` | JEST, ale **tylko walka**: ruch z akceleracją, cykle ataku z danych silnika, obrażenia. Zero ekonomii |
| Fałszywe jednostki | `src/tests/fakes/FakeUnit.java` + `FakeOrderSink.java` | JEST: pozycje, HP, rozkazy idą do sinka (można asercjonować `build`), brak konsumpcji rozkazów |
| Harness świata | `tests/acceptance/AbstractTestWithWorld.world()` | JEST: klatki, `currentMinerals` ustawiane ręką, `options` na supply — **dochód nie istnieje** |
| Seamy bota | `ProductionEngine.updateFrame()`, `ConstructionRequests`, `Construction` (+statusy `PREPARING/NOT_STARTED/STARTING/IN_PROGRESS`) | JEST: V2 liczy plan z samych liczb; `Construction` ma maszynę stanów, ale **nic jej nie tyka w stubie** |
| Batch runner | `scripts/run-sim-batch.sh` | JEST: kontrakt jak Wine-batch, ale tylko pojedynki (`1Drag_v_1Zeal`) |

### Czego snapshot NIE ma (luki pod §1)

- **Obiektów bazowych**: starty i minerały są, ale brak „baza = start +
  jej linia minerałów + natural". (Pochodne: policzyć z odległości, patrz P0.)
- **Ground-distance**: `SimMap.install()` odpowiada `hasPathBetween=true`
  zawsze; `distTo` w stubie to Euclid. Brak BFS po kaflach.
- **Dynamiki**: snapshot to zdjęcie statyki. Dochód, kolejki, postęp
  warpów, ukończenia — nic z tego nie istnieje.

## 3. Wymagania funkcjonalne (co sim MUSI umieć)

- **F1 Mapa 1:1 TauCross**: starty, minerały/gejzery z amounts, regiony
  z łącznością, choke'i z geometrią — wszystko ze snapshotu, plus
  pochodne liczone raz: baza per start (najbliższa linia minerałów),
  BFS ground-dist po kaflach 128×128 (16k kafelków — tanio).
- **F2 Zegar**: `A.now()` tyka 1:1 z klatkami sima; `world(N)` gra N
  klatek bez ręcznego sterowania bankiem.
- **F3 Ekonomia**: model miningu (sondy na patchach → minerały/klatkę,
  kalibracja z Wine, patrz §6), supply used/total, wydawanie na rozkazy
  (bank nie może zejść poniżej zera — asercja invariatywna).
- **F4 Produkcja jednostek**: kolejki Gateway/Nexus, czasy z danych
  silnika (`AUnitType.totalTrainTime`), prereq z `requiredUnits()`,
  ukończenie = nowa jednostka w świecie.
- **F5 Budynki**: rezerwacja kafla → podróż buildera (ruch!) → warp
  z postępem w klatkach → ukończenie = jednostka-budynek; anulowanie
  (kasuje rezerwację, zwraca minerały — dzisiejsza burza
  proximity-cancel musi być odtwarzalna).
- **F6 Placement**: buildable/power/odkryte; w FAST dopuszcza się
  „zawsze się da" TYLKO za flagą testu (patrz §5), default sprawdza.
- **F7 Rozkazy i ruch**: konsumpcja `FakeOrderSink` — `move`/`gather`/
  `build`/`train` zmieniają świat, nie lądują w `/dev/null`.
  Ruch: istniejący mover SimWorld (FAITHFUL) vs odcinek prosty
  (FAST).
- **F8 Wróg**: faza 1 — brak wroga (testy budowlane); faza 3 —
  skryptowany (scout/runby o zadanych klatkach, nie AI).
- **F9 Determinizm i budżet**: ten sam seed = te same klatki warpów;
  BO do 50 supply w **<30 s** w tierze suite (inaczej bramka umiera).

## 4. Co sim celowo NIE robi (poza zakresem na zawsze)

Pełne AI wroga, fog-of-war poza „odkryte=wszystko", mikro walki
lepsze niż dzisiejszy SimWorld, wiele map naraz (na start TYLKO
TauCross), replaye, rendering. Każde „a może jeszcze X" wraca tu
jako wpis — inaczej silnik urośnie w drugiego OpenBW i umrze tak samo.

## 5. Dwa tryby (decyzja ownera)

| | FAST (aproksymacyjny) | FAITHFUL (1:1) |
|---|---|---|
| Ruch buildera | odcinek prosty, stała prędkość | mover SimWorld (accel/braking/turn) |
| Placement | flaga: accept-all LUB check | pełny check (kafel+power) |
| Mining | stały dochód/sondę/klatkę | tripy: podróż do patcha + 8 minerałów (kalibracja §6) |
| Walka | brak (testy budowlane) | SimWorld jak dziś |
| Użycie | bramka otwarcia, kasacje, suite | spory placement/travel, debug „dlaczego warp f3063 a nie f2307" |

Jeden `SimConfig { mode, seed, scenario }`, jeden kod, rozjazd TYLKO
w trzech punktach (ruch/mining/placement-check). Dwa osobne silniki =
dwa trupy do utrzymania.

## 6. Kalibracja: skąd liczby (tabela źródeł, nie z głowy)

| Liczba | Źródło |
|---|---|
| Ceny, czasy budowy/treningu, supply, prereq | `bwapi.UnitType` przez `AUnitType` (już w bocie, CONVENTIONS §9) |
| Dochód/sondę, krzywa bank-vs-supply | `BUILDINGS.json` z gier Wine (bank przy każdym warpie) — 3 gry już mamy |
| Klatki warpów referencyjnych | jw. + `assert-wine-opening.sh` PASS jako złoty wynik |
| Prędkości jednostek, range, dmg | binding, jak SimWorld dziś (`UnitStatsTable`) |
| Windup/accel/braking | parametry modelu jak dziś (WINDUP w SimWorld), pinowane testami |

Parzystość: scenariusz „Zealot into Goon do 50 supply" grany w simie
ma odtwarzać klatki warpów z Wine w tolerancji (propozycja: ±10% lub
±150 klatek, do decyzji w P1 po pierwszych pomiarach). Rozjazd ponad
tolerancję = bug sima ALBO bota — obie odpowiedzi są cenne.

## 7. Architektura (jeden obraz)

```
taucross-snapshot.json
  → SimMapLib (statyka + pochodne: bazy, BFS-dist)   [NOWE, P0]
  → SimClock (A.now)                                  [NOWE, P1]
  → SimBank (minerały/gaz/supply, spend/gain)         [NOWE, P1]
  → SimOrders (konsumpcja FakeOrderSink)              [NOWE, P1]
  → SimConstructions (NOT_STARTED→STARTING→           [NOWE, P1-P2]
       IN_PROGRESS→budynek; kolejki treningu; cancele)
  → SimMovement (mover dziś + tryb FAST)              [ISTNIEJE/ROZSZERZYĆ, P2]
  → feedery: A/Count/Select/ConstructionRequests      [NOWE, P1]
  → scenariusz: SimScenario.playBO("Zealot into Goon", do 50 supply)
```

Bot wchodzi TYLKO przez istniejące seamy (`ProductionEngine`,
`ConstructionRequests`, `Select/Count/A`) — zero `if (isSim)` w kodzie
produkcyjnym. Sim jest kolejnym „światem" harnesu, nie forkiem bota.

## 8. Fazy z bramkami (każda ląduje zielona albo nie ląduje)

- **P0 Mapa zamrożona (1–2 dni)**: pochodne (bazy, BFS-dist), złoty
  test `SimMapSnapshotTest` rozszerzony o: 3 starty, minerały per baza
  (liczby!), łączność regionów start↔start przez choke'i, BFS dist
  main↔natural vs rząd wielkości z gry. Gate: golden bez zmian przy
  refactorach.
- **P1 FAST ekonomia (tydzień)**: SimClock+Bank+Orders+Constructions
  w trybie FAST, wróg=brak, placement=check. Scenariusz BO-do-50:
  asercje kolejności + bank≤550 (in-suite kopia `assert-wine-opening`).
  Gate: zielono w `run-full-tests.sh` <30 s; parzystość z Wine w
  tolerancji z §6.
- **P2 FAITHFUL ruch+placement (tydzień)**: mover, podróż buildera,
  cancele, placement-check default. Gate: scenariusz odtwarza burzę
  proximity-cancel (kształt, nie liczbę) + warp Gateway w tolerancji
  parzystości.
- **P3 Wróg skryptowany**: scout o klatce N, runby 4 lingów o klatce M
  (kształt śmierci 115321!). Gate: armia odpowiada, baza nie ginie
  w scenariuszu „ling-flood f4000".
- **P4 Podpięcie kasacji**: każdy plik z WO-144–147 dostaje gate simowy
  PRZED `git mv src-old/`. Gate: kasacja bez zielonego sima nie ląduje.
- **P5 Upkeep**: złote klatki warpów w repo; rozjazd >tolerancji
  blokuje commit (jak arch-testy).

## 9. Mapowanie: co sim odblokowuje

- WO-144 (supply): gate „zero blokad 17/25 + banki" w simie, nie we Wine.
- WO-145 (placement): A/B catalogue-vs-legacy w simie w sekundach
  (placement-check w obu trybach).
- WO-146 (`APositionFinder`): gate „zero `Can't find place`" w simie.
- WO-147 (`Queue/**`): sim gra BO tylko na V2 — dowód, że kolejka jest
  martwa.
- NEXT: Forge/Cannon-authors (port `ForgeGoal`+`CannonGoal`), sierota-
  prerequisite w schedulerze, stance-from-focus — każdy z pinem simowym.

## 10. Ryzyka (nazwane, żeby nie wróciły)

- **Drugi OpenBW**: silnik większy niż potrzeba. Lek: §4 + review
  każdego „jeszcze tylko X" pod kątem faz.
- **Pułapka wierności**: gonienie 1:1 w nieskończoność. Lek: tolerancje
  w §6 zamiast równości; FAST jest defaultem suite.
- **Dryf od Wine**: sim bez parzystości kłamie. Lek: P5, złote klatki.
- **Testy, które nic nie testują**: scenariusz przechodzący,
  bo placement=accept-all i wróg=brak. Lek: FAST accept-all TYLKO za
  flagą, default check; P3 obowiązkowe przed P4.
- **Statyki w produkcji**: `Regions.useSource` i spółka to globale —
  sim MUSI sprzątać (`uninstall`), inaczej zatruje sąsiednie testy.

## 11. Pytania do ownera (decyzje przed P1)

1. Tolerancja parzystości klatek warpów: ±10% / ±150 klatek / inaczej?
2. Wróg w P1: absolutnie brak, czy skryptowany scout (wpływa na BO)?
3. Budżet suite dla bramki otwarcia: <30 s twardo, czy wolno więcej?
4. Czy sim ma KIEDYKOLWIEK zastąpić Wine w WO-145 A/B, czy tylko
   poprzedzać (sim szybko, Wine na końcu)?
5. Język tego pliku/plany dalej: polski jak tu, czy angielski jak reszta `_AI/`?
