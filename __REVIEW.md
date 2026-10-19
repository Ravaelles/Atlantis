# SOLID review — `f/phase6-production-changes` vs `f/phase5-migration`

Zakres: 155 commitów, 188 plików, ~12 300 linii dodanych (w tym ~90 nowych testów).
Poniżej **konkretne rzeczy do poprawy**, pogrupowane po literze SOLID, z krótką
argumentacją i wskazaniem pliku/metody. Rzeczy są uporządkowane od najważniejszych.

---

## S — Single Responsibility

### S1. `BattleCommander` łączy 6 odpowiedzialności (157 → 768 linii)
**Plik:** `src/atlantis/battle/BattleCommander.java`

W jednej klasie żyją obecnie:
- bramka taktyki (`tactic()`, latch `fleeStartedAt`),
- czytanie pola bitwy (`breachForcesFight`, `clusterHoldsHomeGround`, `vanguardAssessor`),
- polityka kitingu (`shouldKite`, `desiredKiteDistance`, `kiteMargin*`),
- maszyna stanów pojedynczego aktu (`act()` ~210 linii),
- wybór punktów rally (`breachRallyPoint`, `regroupRallyPoint`, `gatherPointFor`),
- ~30 publicznych, mutowalnych stałych strojenia + 4 statyczne mapy.

**Argument:** każda z tych osi zmienia się z innego powodu (balans vs. dowodzenie vs.
percepcja vs. sterowanie jednostką). `act()` ma 6 wcześnie kończących się gałęzi —
każda nowa reguła dopisuje kolejny `return` przed kolejnym. Testy (`DragoonKiteTest`
746 linii) pinują zachowanie klasy, a nie modelu, więc refaktor jest już utrudniony.
To także SRP na poziomie „stanu gry”: `fleeStartedAt` i cztery mapy `last*Frame`
to stan per-jednostka/per-mecz trzymany w statyku klasy dowodzącej.

**Propozycja:** wydziel `KitePolicy` (czysta, na `AUnit`), `ClusterGate`
(taktyka + breach + home-ground), `UnitAct` (jedna decyzja = jedna klasa/metoda),
`RallyPoints`. `BattleCommander` zostaje fasatą iterującą po klastrze.

### S2. `ProduceBuilding` — bramka decyzyjna, polityka supply, rezerwa budżetu i logowanie w jednej klasie
**Plik:** `src/atlantis/production/requests/produce/ProduceBuilding.java`

Klasa odpowiada dziś na: „czy wydać decyzję”, „czy Pylon wolno”, „czy budynek
potrzebuje zasilania”, „czy bank jest zarezerwowany dla wcześniejszych”, „jak opisać
powód” i „jak wyciszyć logi”. Metoda `requestBuilding()` ma **10 kolejnych bramek**
(refusals), a każda nowa reguła właściciela dopisuje kolejną.

**Argument:** jest to kompozycja polityk w jednym miejscu — ladunki kohézyjne, ale
każda reguła ma inny cykl życia i inne dane wejściowe. Utrudnia to testowanie
(większość testów musi stubać cały świat) i wymusza powielanie reguł w V2
(`DynamicGoals`, `GameStateSnapshot`). `countUnstartedPylonsForOtherOrders()` iteruje
globalną kolekcję `ConstructionRequests.constructions` — brama zna magazyn.

**Propozycja:** lista `BuildGate` (`type, order -> Refusal`) zamiast sekwencji `ifów`,
plus osobny `SupplyPolicy` (patrz S3) odpowiedzialny za wszystkie progi Pylona.

### S3. Reguły supply Pylona są rozproszone po 5 miejscach i w dwóch warstwach
**Pliki:**
- `ProduceBuilding.tooManyPylonsInFlight()` — próg `< 40 → 1`, `>= 40 → 2`
- `ProduceBuilding.anotherUnstartedPylonExists()` — próg `> 60 → 2`, inaczej `1`
- `ProduceBuilding.requestBuilding()` — `pylon-overstock` (`< 40 && free > 8`)
- `ProtossNeedDynamicSupply.needsSupply()` — pasma `6/10/14`, `< 40 → 1`
- `DynamicGoals` — `supplyProvidersComing > (supplyTotal > 60 ? 1 : 0)`

**Argument:** ta sama decyzja („ile Pylonów może lecieć”) ma **pięć niezależnych
implementacji** z pięcioma progami. Każda zmiana balansu wymaga edycji w 5 plikach i
ich ponownego zestrojenia; dziś liczby już się różnią, bo porównują różne rzeczy
(`supplyUsed` vs `supplyTotal` vs `free`). To klasyczne OCP/SRP violation: wiedza
o polityce supply nie ma jednego właściciela.

**Propozycja:** jeden `SupplyPolicy` (czyste `needsSupply`, `maxInFlight`,
`overstockBand`), używany przez legacy i V2.

### S4. `WarpedBuildingRecorder` — rejestrator + analiza geometrii + analiza mapy
**Plik:** `src/atlantis/game/listeners/WarpedBuildingRecorder.java`

`noteAnchorWarp()` robi w jednej metodzie: filtrowanie typów, szukanie choke'a,
budowanie slotów ściany, rozwiązywanie zamówienia, konwersję tile → pozycja i `try/catch`
na wszystko.

**Argument:** listener pełni rolę „zapisz warp”, a teraz jeszcze „policz geometrię
wall-in”. Łamie SRP i DIP (bezpośrednio sięga po `Chokes.natural()`, `WallAnchors`).

**Propozycja:** `WarpGeometryProbe` za interfejsem `WallGeometrySource`; listener
tylko deleguje.

### S5. `ShouldNotTravelToConstructYet` — dwie różne decyzje pod jednym typem
**Plik:** `src/atlantis/production/constructions/builders/ShouldNotTravelToConstructYet.java`

Dwa publiczne `check(...)`: stary, statyczny margines (do 50 supply) i nowy,
symulowany forecast (do 50 supply), plus `check(building, distance)` używany jako
fallback także przy `supply > 50`.

**Argument:** reguła ma dwa różne modele tej samej decyzji, a wybór między nimi jest
rozproszony w warunkach na początku metod. Dodatkowo klasa robi ekonomię
(`EconomyModel`, `gasWorkers()`, `ResourceTimeline`), czego `Construction` nie powinien
wiedzieć. Zagnieżdżona pomocnicza logika (`earlierUnpaid`) duplikuje
`ProduceBuilding.earlierUnfinished*` w innej warstwie.

**Propozycja:** `TravelTimingPolicy` z dwiema implementacjami (`LegacyMargin`,
`Forecast`) i jedną fabryką; wspólne `EarlierDemandReserve`.

### S6. Logger zdarzeń i logika iteracji są w klasach domenowych
Dotyczy m.in. `ConstructionStatusChanger`, `PrepareConstructionSite`,
`GoalBoardFeeder`, `BuildOrderAudit`, `ProduceBuilding` (wszystkie wołają
`EventLog.key(...)....log(...)` w środku decyzji).

**Argument:** decyzja i format raportu zmieniają się z różnych powodów; testy pinują
literały logów, co blokuje refaktor. To również łamanie SRP („talk” + „decide”).

**Propozycja:** `*Twin` jako jedyne miejsce formatu (już tak są zrobione `KiteTwin`,
`HitTwin`, `V2DispatchTwin`) — ujednolicić pozostałe.

---

## O — Open/Closed

### O1. `ProduceBuilding.requestBuilding()` — otwarta na każdą nową regułę przez edycję
Każda nowa reguła właściciela w tym branchie (live-construction, pylon-cap,
pylon-overstock, unstarted-pylon, earlier-reserve, core-priority, power-not-funded,
funding) to nowy `if (...) return refuse(...);` w tej samej metodzie.

**Argument:** zamknięcie modyfikacji wymaga rejestru bramek, a nie kolejnego `if`.
Dziś każda zmiana ryzykuje regresję wcześniejszej bramki (kolejność ma znaczenie
semantyczne i nie jest nigdzie udokumentowana jako kontrakt).

**Propozycja:** `List<BuildGate>` z jawną kolejnością i nazwą refusala; test na każdą
bramkę w izolacji.

### O2. `GoalBoardFeeder` zna konkretne ID celu
```java
if ("supply-pylon".equals(goal.id()) && !NeedDynamicSupply.forCurrentRace()...)
```
**Argument:** feeder (ogólny mechanizm board → queue) zawiera wiedzę o jednym,
konkretnym handlerze. Dodanie `"gas-pylon"` czy `"core"` wymaga edycji klasy
generycznej. Powinno to być właściwością celu (np. `Goal.needed(state)`), nie
specjalnym przypadkiem w pętli.

### O3. `TrainQueuedDragoonImmediately` — zamknięta na inne jednostki
**Plik:** `src/atlantis/production/dynamic/protoss/units/TrainQueuedDragoonImmediately.java`
Dodanie tego samego „same-frame” zachowania dla Zealota/Immmortala wymaga nowej
klasy-listenera i kopiowania tej samej logiki.

**Argument:** klasa enkapsuluje konkretny typ zamiast reguły „produkcja dynamiczna
natychmiast dla jednostek, których nie ma w kolejce”. Powinien to być jeden listener
z polityką `ImmediateProductionPolicy` (który typ, które gateway, co gdy odmowa).

### O4. `DynamicGoals.addArmyGoal()` — reguła ekonomiczna zaszyta w metodzie
Nowy blok (`wantGoons`, `minerals < 275`, `gas >= 50`) to edge-case dopisany do
istniejącej metody zamiast oddzielnej reguły.

**Argument:** zmiana w modelu armii (np. nowy próg dla Immortali) zmusza do edycji
tej samej metody; trudno też przetestować oba warianty bez pełnego `GameStateSnapshot`.

### O5. `ConstructionOrderStatus` + `Construction` — rozszerzanie przez `switch`/warunki
Dodanie `STARTING` wymagało dopisania warunków w: `ConstructionRequests.isNotStarted`,
`ProduceBuilding.hasLiveConstruction`, `countUnstartedPylonsForOtherOrders`,
`BuilderManager`, `ConstructionStatusChanger`, `GameStateSnapshot`. Każdy nowy status
będzie generował ten sam zestaw edycji.

**Argument:** model statusu powinien odpowiadać na pytania semantyczne
(`isTravel`, `isWaiting`, `countsAsDemand`, `isTerminal`), a nie na porównania z
enumem w 6 klasach. Inaczej każdy nowy status dotyka wszystkich czytelników.

---

## L — Liskov Substitution

W tym diffie brak nowych hierarchii dziedziczenia, więc LSP jest w praktyce
nienaruszony. Dwie uwagi „na przyszłość”:

### L1. `NeedDynamicSupply.PermissiveNeedDynamicSupply` zwraca zawsze `true`
**Plik:** `src/atlantis/production/dynamic/supply/NeedDynamicSupply.java`
Kontrakt mówi „czy potrzebny jest dostawca”, a implementacja dla innych ras zwraca
„nie wiem, więc tak”. To jest złamanie kontraktu (semantyka odpowiedzi różna od
implementacji Protoss), które objawia się dopiero, gdy Zerg/Terran zacznie czytać
wynik. Jeśli to celowe, nazwij to `NoOpinion`/`Unknown` i nie zwracaj `boolean`.

### L2. `Select.ourWorkersFreeToBuildOrRepair` usunięte, zastąpione `FreeWorkers`
Zmiana jest w dobrym kierunku (jeden kontrakt „wolny robotnik”). Warto dopilnować, by
`FreeWorkers.get().first()` w `FindPosition.defineBuilder()` nie stało się cichym
null-em zmieniającym kontrakt sitingu — dziś `siting` zwraca `null` bez powodu.

---

## I — Interface Segregation

### I1. `BuildGate`-owe pytania są dziś w klasie, nie w interfejsie
`ProduceBuilding` eksponuje publicznie `tooManyPylonsInFlight`,
`anotherUnstartedPylonExists`, `countUnstartedPylonsForOtherOrders`, `isEchoWithoutUnit`
(package-visible), `hasLiveConstruction`, `earlierUnfinishedMinerals/Gas`,
`demandContext`. To publiczne API **tylko po to, żeby testy i V2 mogły to zawołać**.

**Argument:** szerokie API ujawnia szczegóły decyzji. Powinien istnieć wąski interfejs
(np. `PylonDemandQuery`, `ReserveQuery`) zaadresowany do konsumentów, a implementacja
w jednej klasie.

### I2. `PlacementPlanner.drainSupportRequests()` — defaultowa metoda w interfejsie
**Plik:** `src/atlantis/production/v2/PlacementPlanner.java`
Interfejs planujący miejsce dostaje metodę zwracającą żądania produkcji Pylona.
Dla większości implementacji zwraca `emptyList()`.

**Argument:** ISP — konsumenci planowania nie powinni znać pojęcia supportu
produkcyjnego; lepiej osobny interfejs `SupportDeclaring` implementowany tylko przez
planner, który to potrafi, i `instanceof`/osobna referencja w kompozycji.

---

## D — Dependency Inversion

### D1. Reguły domenowe czytają globalny stan gry bezpośrednio
`ProduceBuilding`, `NeedDynamicSupply`, `DynamicGoals`, `BattleCommander`,
`ShouldNotTravelToConstructYet`, `Construction.assignOptimalBuilder` — wszystkie
wołają `A.supplyUsed()`, `A.minerals()`, `A.isUms()`, `Count.*`, `Select.*`.

**Argument:** brak wstrzykiwania stanu uniemożliwia test bez stubowania świata i
powoduje duplikację progów (S3). `NeedDynamicSupply` dostał parametry `(used, total,
unfinished)` — to właściwy kierunek; trzeba go dokończyć także w bramkach
`ProduceBuilding` (np. `BuildGate(type, order, snapshot)`).

### D2. `ShouldNotTravelToConstructYet` zależy od warstwy ekonomii i `Queue`
**Plik:** `src/atlantis/production/constructions/builders/ShouldNotTravelToConstructYet.java`
Bezpośrednie `Queue.get().allOrders()`, `EconomyModel`, `ResourceTimeline`.

**Argument:** decyzja „czy iść teraz” powinna czytać `ReserveProjection` zamiast
dwóch warstw naraz; w obecnej formie `TravelToConstruct` (builder) nie da się
przetestować bez stuba kolejki.

### D3. `Env` jako globalny rejestr flag (tuning + launcher + ścieżki)
**Plik:** `src/atlantis/config/env/Env.java`
Dodane `rawEnv`, `envFileOverride`, `wineSuperFast` + tysiące statycznych setterów.
`ParamTweakerFactory` czyta `Env.rawEnv(...)` i zapisuje do statyków `BattleCommander`.

**Argument:** warstwa debug pisze do pola publicznego klasy domenowej (DIP do góry
nogami: debug zależy od domeny — OK; ale domena nie powinna mieć mutowalnego stanu
publicznego tylko dla tunera). Wprowadź `CombatTuning` (record z wartościami
wstrzykiwany do `BattleCommander`), tuner mutuje obiekt, nie statyki.

### D4. `WarpedBuildingRecorder` zależy od mapy/analizy bezpośrednio
Patrz S4 — dodatkowo `Chokes.natural()` i `WallAnchors` wołane z listenera.

---

## Zasady pomocnicze (czystość, testowalność, DRY)

### P1. Statyczny stan modułu — wycieki między grami JVM
**Pliki:** `ProduceBuilding.lastRefusedAt`, `HitTwin.lastTotalByUnit`,
`ShotTwin.lastHoldByUnit`, `WarpedBuildingRecorder.lastWarpByType`,
`AddToQueue.lastNexusQueueFrame`, `BuildOrderAudit.rows`, `Alpha`/`BattleCommander`
mapy `last*Frame`.

**Argument:** reset tylko w testach (`clearForTests`) lub tylko w `OnGameStarted`
dla części. `HitTwin`, `ShotTwin`, `WarpedBuildingRecorder`, `BuildOrderAudit` **nie są
w ogóle resetowane** w `OnGameStarted` ani w `AbstractTestWithUnits`, więc kolejna
gra/test dziedziczy stan (np. `lastWarpByType` da fałszywe `CLOSE_WARPS`; `BuildOrderAudit`
wypisze warpy z poprzedniej gry). To ten sam defekt, który `OnGameStarted` opisuje
dla `ScoutState`.

**Propozycja:** jedna fasada `PerGameState.resetAll()` wywoływana z `OnGameStarted`
i z setupu testów; usunąć ad-hoc `clearForTests`.

### P2. Zduplikowana logika rezerwy budżetu (trzy implementacje)
1. `ProduceBuilding.earlierUnfinishedMinerals/Gas` (po `Construction.id`),
2. `ShouldNotTravelToConstructYet.earlierUnpaid` (po liście `Queue.allOrders`),
3. `GoalBoardWallet` / `ReservedResources` (istniejące).

**Argument:** trzy różne modele „co już jest zarezerwowane”, różne sortowanie, różne
wykluczenia. Każdy wyścig (np. dwa Pylony) wynika właśnie z tego, że autorzy czytają
inne sumy. Powinien być jeden `EarlierDemand` z jawnym porządkiem i regułą „już
zapłacone = ma buildingUnit”.

### P3. `MoveAway` — martwa gałąź po `return`
```java
if (unit.distTo(newPosition) < moveDistance / 2) return null;
if (newPosition == null) { ... }   // newPosition jest już użyty w distTo
```
**Plik:** `src/atlantis/units/actions/move/MoveAway.destinationFor()`

**Argument:** sprawdzenie `null` jest po użyciu (NPE, gdyby mogło wystąpić) i
nieosiągalne w praktyce — martwy kod mylący czytelnika. Przenieść przed `distTo`.

### P4. `MoveAway.tryStep()` zwraca `true` także przy odmowie `unit.move(...)`
```java
if (unit.move(...)) return true;
else return true;
```
**Argument:** metoda deklaruje „czy udało się wykonać krok”, a zwraca sukces zawsze.
Komentarz w `bestDestination` mówi wprost „a refused order is not a step” — kontrakt
jest sprzeczny z implementacją. Zwróć `unit.move(...)` (albo przemianuj metodę).

### P5. `BattleCommander.shouldKite()` — duplikat warunku
Blok „low-HP yields ground early” powtarza `unit.hp() + unit.shields() < DRAGOON_LOW_HP`
już sprawdzony w linii wyżej (drugi warunek po `&&` jest zbędny).

### P6. Publiczne mutowalne stałe (`public static double KITE_*`, `DRAGOON_*`)
**Argument:** ~30 pól klasy domenowej jako publiczne pola — każdy może je zmienić w
dowolnym momencie, brak walidacji zakresu, brak niezmienniczości. Tuner ma już
`applyOrRandomize` z zakresami — ale nic nie broni innemu kodowi ustawić `NaN`/0.
Zastąp `CombatTuning` (P3 w D).

### P7. `GoalBoardFeeder` — podwójne logowanie tej samej informacji
`AConsole.println("BOARD_FED " ...)` i `EventLog.key(BOARD_FED)` obok siebie. To
dublowanie formatu w dwóch kanałach; konsola powinna renderować z `EventLog`.

### P8. `ConstructionRequests` — kolekcja publiczna i nazwy kłamliwe
`public static ConcurrentLinkedQueue<Construction> constructions` jest czytane i
modyfikowane z wielu klas (`ProduceBuilding`, `GameStateSnapshot`, `PrepareConstructionSite`,
`OverdueConstructions`). Do tego `getNotFinishedOfType()` faktycznie filtruje
`IN_PROGRESS` (nazwa nie zgadza się z implementacją).

**Argument:** publiczna mutable kolekcja = brak enkapsulacji i brak jednego miejsca
na inwarianty (np. „PREPARING nie liczy się do demand”). `getNotFinishedOfType`
zwraca `IN_PROGRESS` — każdy czytelnik, który założy inaczej, ma błąd.

### P9. `Main.java` — zakomentowane mapy i zmienione domyślne ścieżki w commicie produkcyjnym
**Plik:** `src/main/Main.java` (niezacommitowana zmiana w working tree + commit)

**Argument:** plik konfiguracyjny lokalnych testów trafił do gałęzi produkcyjnej z
innymi domyślnymi mapami. Powinien być parametrem (`--map=`), nie `if (true) return`.

### P10. `EventLog.countByKey*` — API dla tunera w ogólnym loggerze
`countByKey`, `countByKeyParam` istnieją tylko po to, żeby `ParamTweakerEvaluator`
czytał licznik. To ISP/DIP: logger staje się magazynem metryk. Rozwiązanie:
`MetricsSink` zbierający `hits/shots` przy `append`, niezależny od buffer flush.

---

## Podsumowanie priorytetów

| Priorytet | Pozycja | Wpływ |
|---|---|---|
| 1 | S3 — jedna polityka supply (5 miejsc) | Bezpośrednio wpływa na zachowanie gry, duplikacja progów |
| 1 | P1 — reset stanu per-gra | Fałszywe logi i złe decyzje w kolejnych grach/testach |
| 1 | P2 — jedna rezerwa budżetu | Źródło wyścigów Pylonów, niespójne sumy |
| 2 | S1 — rozbicie `BattleCommander` | Utrzymywalność kitingu, nazewnictwo, testy |
| 2 | S2/O1 — bramki `ProduceBuilding` jako rejestr | Każda nowa reguła dziś = edycja jednej 400-liniowej metody |
| 2 | O5 — pytania semantyczne na `ConstructionOrderStatus` | `STARTING` dotknął 6 klas |
| 3 | D1/D3 — snapshot zamiast globali, `CombatTuning` | Testowalność i tuner bez publicznych statyków |
| 3 | P3/P4/P5 — drobne defekty w `MoveAway`/`BattleCommander` | Martwy kod i sprzeczny kontrakt |
| 4 | O2/O3/O4, I1/I2, S4/S5/S6, P7–P10 | Porządki strukturalne |
