# Plan: Reminder trigger time, past-event cleanup, push delivery diagnosis
**Created:** 2026-10-21
**Status:** in-progress
**Started:** 2026-10-21 12:20
**Current Phase:** 3
**Workspace:** master working copy, NO commits (user decision 2026-10-21 — the user commits after review). Phases run serially; reviews check the working-tree diff.
**Complexity:** simple
**Review cadence:** 3
**Check-ins:** checkpoints
---
## Context

**Problem:**
1. The reminder panel on the detail screen says "3 h before start" / "За 193 хв до початку". The user wants the
   moment the reminder will fire instead (event at 16:00, 3 h → "Нагадаю о 13:00").
2. New-event push does not reach the phone (Redmi, MIUI, Android 15). Server side looks configured: the FCM key is
   mounted, `FCM_CREDENTIALS_PATH=/run/secrets/fcm.json`, events 31 and 32 (future dates) were approved and no
   exception was logged. But the api logger has no INFO handler, so a successful send is invisible too. On the
   phone (2026-10-21): `POST_NOTIFICATIONS` granted, the in-app notifications toggle on, **MIUI autostart
   (appop 10008) = ignore**, the app is not on the battery whitelist, no FCM errors in the app process.
3. Past events pile up in the local cache. They must be removed automatically.

**Success criteria:**
1. The panel subtitle shows the trigger moment, never the offset in minutes; a trigger that crosses midnight
   shows the correct (previous) date.
2. The cause of the missing push is established with evidence from the phone and the server; a client-side cause
   is fixed and a Firebase Console test message to topic `conferences` is shown on the phone; a non-client cause
   is written down as exact steps.
3. After a successful sync and on app start, events whose date + one full day has passed (Kyiv) are deleted from
   the cache, and their reminders are dropped. JVM tests cover the threshold.
4. `./gradlew assembleDebug` and the unit tests pass; the app launches on the phone without a crash.

## Constraints
- The backend is never modified from this repository (`CLAUDE.md`). Reading its code/logs and running a
  **read-only `dry_run`** inside the `api` container are allowed.
- The FCM service-account key is a secret: never print, copy or commit its contents.
- UI strings only in `res/values/strings.xml` (uk) and `res/values-en/strings.xml` (en). No Russian anywhere.
- Times are shown in the event zone `AppConfig.eventZone` (`Europe/Kyiv`), as everywhere else.
- `domain/` stays pure Kotlin (no Android imports) and is tested on the JVM.
- No WorkManager, no polling (user decision 2026-10-19 stands).
- Events without a date, or with a date that does not parse, are **never** deleted.
- Follow `docs/code-standards.md`.

---
## Implementation Phases

### Phase 1: Reminder trigger moment in the panel
**Skills:** none -- small presentation change over existing, already-tested time math; no skill's triggers (design, debugging, refactoring, defensive hardening) match
**Model:** sonnet
**Gate:** Standard
**Depends on:** none
**File scope:** app/src/main/java/com/KSU/EventsParser/ui/components/ReminderPanel.kt, app/src/main/java/com/KSU/EventsParser/ui/util/ReminderDisplay.kt, app/src/main/java/com/KSU/EventsParser/domain/reminder/ReminderSchedule.kt, app/src/main/res/values/strings.xml, app/src/main/res/values-en/strings.xml, app/src/test/java/com/KSU/EventsParser/domain/reminder/**

**Goal:** When a reminder is on, the panel subtitle shows when it will fire instead of how long before the start.

**Scope:**
- IN: Subtitle of `ReminderPanel` when `offsetMinutes != null` and availability is `Available`:
  - trigger on the same calendar day as the event start (event zone) → time only:
    uk «Нагадаю о 13:00», en "Reminder at 13:00";
  - trigger on another day → day + month + time:
    uk «Нагадаю 20 жовтня о 16:00» (genitive month, `AppDateFormat.dayMonth`), en "Reminder on 20 October at 16:00".
  - Trigger moment = `ReminderSchedule.triggerAt(available.start, offsetMinutes)`; time via `AppDateFormat.time`.
  - Contract — new pure function in `domain/reminder/ReminderSchedule.kt`:
    `fun isSameDay(start: ZonedDateTime, trigger: ZonedDateTime): Boolean`
    (compares `toLocalDate()` in the zone of `start`), so the midnight case is JVM-testable.
  - Locale for `AppDateFormat.dayMonth`: taken inside the composable from
    `LocalConfiguration.current.locales[0]` — `ReminderPanel`'s parameter list does not change.
  - Reminder on but `availability` is not `Available` (`Started`/`NoDate`, e.g. a moderator moved the event;
    the row is removed by the next `reconcile()`): subtitle shows `R.string.settings_state_on` («Увімкнено»),
    the existing hint below explains why. Today it shows the offset — that path is replaced.
  - Remove the now-unused `reminder_before` string from both locales if nothing else uses it.
  - Update the `@Preview`s so they show the new subtitle.
- OUT: Chip labels (presets stay "15 min", "3 h" …), `ReminderCustomDialog`, the notification body
  (`AppNotifications.showReminder`, "Через 3 год · …"), any change to scheduling.

**Edge cases:**
- Event at 00:30, offset 60 → trigger 23:30 of the previous day → date must be shown.
- Offset of whole days (1440) → trigger on an earlier day → date shown.
- Event without a parseable time uses the 09:00 fallback start: trigger is computed from 09:00 and the existing
  fallback hint stays.
- Reminder on but availability is `Started`/`NoDate` → no crash, subtitle «Увімкнено», never a trigger
  computed without a start.

**Done when:**
- [ ] DW-1.1: For an event at 16:00 with a 3 h reminder the panel subtitle reads «Нагадаю о 13:00» (uk) and "Reminder at 13:00" (en); no offset text in the subtitle.
- [ ] DW-1.2: For an event at 00:30 with a 60 min reminder the subtitle includes the previous day's date and 23:30.
- [ ] DW-1.3: JVM tests for the same-day helper cover same day, previous day across midnight, and a whole-day offset.
- [ ] DW-1.4: Both locales contain the new strings; no hardcoded text, dp or colour added to the composable; `reminder_before` removed if unused.
- [ ] DW-1.5: With a reminder on and availability `Started` or `NoDate`, the subtitle reads «Увімкнено» / "On" (a `@Preview` shows it).
- [ ] DW-1.6: `./gradlew :app:compileDebugKotlin testDebugUnitTest` and `./gradlew assembleDebug` pass; the app launches on the phone without a crash (`adb logcat -d -b crash` empty).

### Phase 2: Automatic cleanup of past events
**Skills:** code-foundations:cc-defensive-programming
**Model:** sonnet
**Gate:** Standard
**Depends on:** none
**File scope:** app/src/main/java/com/KSU/EventsParser/domain/model/**, app/src/main/java/com/KSU/EventsParser/data/local/EventDao.kt, app/src/main/java/com/KSU/EventsParser/data/repository/EventsRepository.kt, app/src/main/java/com/KSU/EventsParser/ui/EventsViewModel.kt, app/src/main/java/com/KSU/EventsParser/EventsApp.kt, app/src/test/java/com/KSU/EventsParser/domain/model/**, CLAUDE.md

**Goal:** Events whose date and one more full day have passed (Kyiv) are deleted from the Room cache automatically, and their reminders are dropped.

**Scope:**
- IN:
  - Contracts — pure Kotlin in new file `domain/model/EventExpiry.kt` (no Android):
    - `fun Event.isExpired(today: LocalDate): Boolean` = `date != null && date.plusDays(1) < today`
      (event dated 19 Oct is kept through 20 Oct and deleted from 21 Oct);
    - `fun expiredIds(events: List<Event>, today: LocalDate): List<Int>`.
    All selection logic lives here and is what the JVM tests cover.
  - `EventDao`: `@Query("SELECT * FROM events") suspend fun getAll(): List<EventEntity>`; reuse `deleteByIds`.
  - `EventsRepository` gains a constructor parameter `clock: Clock = Clock.systemUTC()` and
    `suspend fun purgeExpired(): Int` — a thin wrapper: `today = LocalDate.now(clock.withZone(AppConfig.eventZone))`,
    `dao.getAll().map(EventMapper::toDomain)` (a malformed `date` becomes `null` and is kept), `expiredIds(...)`,
    `if (ids.isNotEmpty()) dao.deleteByIds(ids)`, log the count with `TAG`, return it. `AppContainer` keeps the default clock.
  - Call order in `EventsViewModel`, same coroutine each time:
    - `init`: `viewModelScope.launch { repository.purgeExpired(); reminderRepository.reconcile() }` replaces the
      current `launch { reminderRepository.reconcile() }`; `refresh()` stays as is.
    - `runSync()`, `Success` branch only: `repository.purgeExpired()` then the existing `reminderRepository.reconcile()`.
    - Never on the `Failure` branch.
  - `CLAUDE.md`: rewrite invariant 5 and the Architecture/Work-status notes — the client now deletes events
    after date + 1 day (Kyiv); `upcoming` is still not used and the feed is still not re-filtered for display.
- OUT: Hiding events in the UI without deleting, a Settings switch or button, changing the server query,
  deleting anything from the server, `BootReceiver` (it only reconciles alarms).

**Edge cases:**
- `date == null` → never deleted.
- `dateRaw` that does not parse (mapper yields `date == null`) → never deleted.
- Boundary: today = date + 1 → kept; today = date + 2 → deleted.
- Cache empty or nothing expired → no DELETE issued, returns 0.
- Everything expired → cache becomes empty; `reconcile()` then skips (its empty-cache guard), leftover reminder
  rows are cleaned later by `consume()` — acceptable, the events are already past.
- A past event edited on the server comes back through incremental sync and is purged again on the same run.
- After the 422 branch (cache cleared and full resync) old events reappear and are purged in the same run.

**Rollback:** point of no return for the deleted rows — mitigation: they are a cache only; clearing the sync
cursor (or reinstalling) refetches the whole feed from the server.

**Done when:**
- [ ] DW-2.1: JVM tests for the expiry rule: date + 1 = today → kept; date + 2 = today → expired; `null` date → kept; future date → kept.
- [ ] DW-2.2: JVM tests for `expiredIds`: returns only expired ids from a mixed list; an event built with `date = null` (as the mapper produces for an unparseable `dateRaw`) is not returned; an all-future list returns an empty list.
- [ ] DW-2.3: `EventsViewModel` runs `purgeExpired()` then `reconcile()` in one coroutine in `init`, and in the same order inside the `Success` branch of `runSync()`; nothing on the `Failure` branch; `purgeExpired()` skips `deleteByIds` when the list is empty (code inspection).
- [ ] DW-2.4: On the phone: event 30 (dated 2026-09-30, present in the live feed) disappears from the list and the September calendar after launch; logcat `EventsRepository` shows a purge count ≥ 1. Reminders of purged events: removed by the following `reconcile()` (non-empty cache) — the empty-cache case is left to `consume()` by design.
- [ ] DW-2.5: `CLAUDE.md` invariant 5 and the related notes describe the new behaviour; no Russian.
- [ ] DW-2.6: `./gradlew :app:compileDebugKotlin testDebugUnitTest` and `./gradlew assembleDebug` pass; the app launches on the phone without a crash.

### Phase 3: Push delivery diagnosis
**Skills:** code-foundations:cc-debugging
**Model:** sonnet
**Gate:** Standard
**Depends on:** Phase 2 (both edit `CLAUDE.md`; run them one after another)
**File scope:** app/src/main/java/com/KSU/EventsParser/push/**, app/src/main/java/com/KSU/EventsParser/data/repository/DeviceRepository.kt, app/src/main/AndroidManifest.xml, CLAUDE.md

**Goal:** Find where the new-event push is lost (server → FCM → phone → app) and fix it if the cause is in the client, or document exact steps if it is not.

**Scope:**
- IN:
  - Server, read-only: inside the `api` container build the same `messaging.Message` as `notify_event` for an
    approved future event and call `messaging.send(message, dry_run=True)` — proves the key and project are
    valid without delivering anything. Report the result, never the key.
  - Delivery path: the user sends a Firebase Console test message to topic `conferences` with data
    `event_id=<existing id>`; check three states — app in foreground, in background, swiped away — first with
    MIUI autostart off, then with autostart on and battery "No restrictions". Capture `adb logcat` for each.
  - Client checks: the app's topic subscription actually completed (logcat / FCM prefs), the notifications toggle
    path calls `subscribeToTopic`, channel `events` exists and is not blocked.
  - If a client defect is found: fix it in `push/` / `DeviceRepository` / manifest.
  - Write the finding into `CLAUDE.md` → "Why push does not arrive" (replace the outdated "FCM_CREDENTIALS_PATH
    is empty" text) with the verified state and, if it is MIUI, the exact phone settings.
- OUT: Any backend code or config change (including enabling INFO logging — report it as a suggestion only),
  WorkManager/polling, changing the server payload, adding an in-app MIUI settings screen.

**Edge cases:**
- `dry_run` fails (bad key / wrong project) → stop: the cause is server-side; document it, no client change.
- Console message arrives in foreground but not when swiped away → MIUI autostart/battery restriction; document.
- Console message arrives in all states but a real approve does not → server sends nothing or sends to another
  topic; compare topic names and the `date >= today` rule (`app/services/moderation.py`).
- Nothing arrives even in foreground → subscription missing; check the toggle and `subscribeToTopic` result.

**Security-sensitive:** yes -- the diagnosis runs code that loads the FCM service-account key; the key must not be printed, copied or committed.

**Done when:**
- [ ] DW-3.1: The `dry_run` result from the `api` container is recorded (success or the exact error), without exposing the key.
- [ ] DW-3.2: Console test-message results are recorded for foreground / background / swiped-away, with autostart off and on, each backed by a logcat excerpt.
- [ ] DW-3.3: The root cause is stated with that evidence.
- [ ] DW-3.4: If the cause is in the client, it is fixed and a Console test message is shown on the phone with the app swiped away; otherwise no client code is changed.
- [ ] DW-3.5: `CLAUDE.md` "Why push does not arrive" reflects the verified state and the exact steps; no Russian.
- [ ] DW-3.6: If client code changed: `./gradlew assembleDebug` passes and the app launches on the phone without a crash.

**Blocked on the user:** DW-3.2 onward need the phone on USB, Console test messages from the user's Firebase
login and toggling MIUI autostart. Build stops after DW-3.1 and asks.

---
## Test Coverage
**Level:** 100%

## Test Plan
- [ ] Same-day helper: event 16:00, offset 180 → same day (DW-1.1, DW-1.3)
- [ ] Same-day helper: event 00:30, offset 60 → previous day — dirty test, midnight boundary (DW-1.2, DW-1.3)
- [ ] Same-day helper: offset 1440 → earlier day (DW-1.3)
- [ ] Manual: panel subtitle in uk and en on the phone for a same-day and a cross-midnight reminder (DW-1.1, DW-1.2, DW-1.4)
- [ ] Preview / manual: reminder on + `Started` → «Увімкнено» — dirty case, unavailable event (DW-1.5)
- [ ] Expiry rule: date + 1 = today → kept — dirty test, day boundary (DW-2.1)
- [ ] Expiry rule: date + 2 = today → expired; future → kept; null → kept (DW-2.1)
- [ ] `expiredIds`: mixed list → only expired ids; `date = null` (unparseable `dateRaw`) not returned — dirty test, malformed input (DW-2.2)
- [ ] `expiredIds`: all future → empty list (DW-2.2)
- [ ] Code inspection: call order and empty-list guard (DW-2.3)
- [ ] Manual: event 30 disappears after launch; logcat shows the purge count (DW-2.4)
- [ ] Manual: `dry_run` in the `api` container (DW-3.1)
- [ ] Manual: Console test message × 3 app states × autostart off/on, with logcat (DW-3.2, DW-3.3)
- [ ] Manual, if a client fix was made: Console message shown with the app swiped away (DW-3.4) — dirty case: app killed
- [ ] `./gradlew :app:compileDebugKotlin testDebugUnitTest` and `assembleDebug` pass; no crash on launch (all phases)

---
## Notes
- Threshold "19 Oct disappears on 21 Oct" was proposed and not objected to; if the user wants 20 Oct, change the
  rule to `date < today`.
- Phase 3 needs the user: phone on USB, sending Console test messages (their Firebase login), toggling MIUI
  autostart when asked.
- `CLAUDE.md` says the server pushes for events "with no date" too; `moderation.approve_event` actually requires
  `date is not None and date >= today`. Phase 3 corrects the note.
- The debug base URL is currently `http://10.177.36.201:8000` (network `EVAM_Public`); it changes with the Wi-Fi.
- Device clock far in the future would purge events early; acceptable — the cache refills on a full resync.
---
## Decided During Build
_Filled by /code-foundations:build — one row per decision an agent made or question it raised:_
| # | Phase | Decision or question | Source | Status |
|---|---|---|---|---|
| 1 | 1 | Subtitle text built in `ui/util/ReminderDisplay.kt` as `reminderTriggerText(res, locale, start, offsetMinutes)`, like the sibling `reminderOffsetLabel`; locale from the existing `rememberAppLocale()` | build agent (codebase pattern) | assumed |
| 2 | 1 | `isSameDay` converts `trigger` to `start`'s zone with `withZoneSameInstant` before comparing dates | build agent (plan) | assumed |
| 3 | 2 | `purgeExpired()` catches non-cancellation exceptions (rethrows `CancellationException`), logs with `Log.e`, returns 0 — a DB error in cleanup must not crash the app or skip `reconcile()` | build agent (cc-defensive-programming) | assumed |
| 4 | 2 | `EventsApp.kt` unchanged — `AppContainer` keeps the default `Clock` | build agent (plan) | assumed |
| 5 | 2 | The repository wrapper has no JVM test (Android `Log` + Room); covered by inspection and the on-device purge log | build agent (code-standards) | assumed |
---
## Execution Log
### Phase 1: Reminder trigger moment in the panel (Gate: Standard)
- [x] BUILD: Discovery + design + implementation (stub → implement → validate) complete — discovery file not written (permission rule on `.code-foundations/build/`), content in the agent report
- [x] REVIEW: DEFERRED — batch pending (tests green: 35 JVM tests; assembleDebug ok; launched on phone, crash buffer empty)
- [ ] Committed — not committed by design (user decision: no commits)
Summary: The reminder panel subtitle now shows the trigger moment («Нагадаю о 13:00», or with a date when the trigger falls on another day; «Увімкнено» when the event has no usable start) via pure `ReminderSchedule.isSameDay` and `reminderTriggerText`; `reminder_before` removed.

### Phase 2: Automatic cleanup of past events (Gate: Standard)
- [x] BUILD: Discovery + design + implementation (stub → implement → validate) complete — discovery file not written (permission rule), content in the agent report
- [x] REVIEW: DEFERRED — batch pending (tests green: 12 new JVM tests in `EventExpiryTest`; assembleDebug ok; on the phone first launch logged `purge … =10`, the post-sync purge 0, crash buffer empty)
- [ ] Committed — not committed by design (user decision: no commits)
Summary: Events whose date + 1 full day has passed (Kyiv) are deleted from the Room cache by `EventsRepository.purgeExpired()` (pure rule in `domain/model/EventExpiry.kt`, injected `Clock`), run before `reconcile()` in `EventsViewModel.init` and after each successful sync; CLAUDE.md invariant 5 rewritten.
