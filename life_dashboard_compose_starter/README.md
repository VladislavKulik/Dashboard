# Life Dashboard — Android Studio / Jetpack Compose

Перенос предоставленных HTML-макетов Life Dashboard в Kotlin / Jetpack Compose. Начиная со Stage 20 архив содержит самостоятельный Android Gradle-проект, который можно открыть в Android Studio после однократного создания Gradle Wrapper скриптом из `tools/`.

**Быстрый старт:** открой `ANDROID_STUDIO_SETUP.md`, выполни `tools/bootstrap-gradle-wrapper.ps1` (Windows) или `bash tools/bootstrap-gradle-wrapper.sh` (macOS/Linux), затем открой корневую папку проекта в Android Studio. Полноценный Gradle Sync/Build в среде подготовки архива не выполнялся и должен быть проверен на твоём компьютере.

## Stage 20 — Android Studio, Compose Preview и Live Edit

- Добавлены `settings.gradle.kts`, корневой и app `build.gradle.kts`, Version Catalog, `gradle.properties` и конфигурация Android Manifest/resources.
- Подключены Jetpack Compose, Material 3, Navigation Compose, Lifecycle, Room + KSP, Preferences DataStore и Coroutines.
- Добавлены 18 Compose Preview-вариантов для Home (Light/Dark/Compact/Tablet), Tasks (Phone/Compact/Tablet), Projects (Phone/Tablet), Project Details, Goals, Calendar, Habits, Habit Details, Notes, Note Editor, Analytics и Settings.
- Добавлены PowerShell/Bash bootstrap-скрипты для загрузки официального Gradle 9.5.0 с проверкой SHA-256 и генерации стандартного Wrapper.
- Настройка Android Studio, Compose Preview и Live Edit описана в `ANDROID_STUDIO_SETUP.md`.
- Версии и причины выбора: AGP 9.3.1, Kotlin/Compose Compiler 2.4.21, KSP 2.3.12, Compose BOM 2026.09.00, Room 2.8.5, Navigation Compose 2.10.2 и DataStore 1.3.0.
- Полная Gradle-сборка и реальный рендер Preview в этой среде не подтверждены; см. `VALIDATION_STAGE20.md`.

## Что реализовано

### Этап 1 — основа интерфейса
- Палитра, формы и типографическая шкала на основе загруженных макетов.
- Светлая тема Material 3.
- Общие компоненты: `PastelCard`, `CircleIconButton`, `FilterPill`, `ProgressTrack`, `TaskRow`, `ScreenTopBar`.
- Нижняя навигация с центральной жёлтой кнопкой добавления.
- Bottom Sheet быстрого добавления.
- Навигационный каркас.
- Экраны Home, Projects и Menu.

### Этап 2 — задачи и подробности проекта
- Полноценный экран Tasks с фильтрами «Все», «Сегодня», «Предстоящие», «Выполнено».
- Поиск задач по названию.
- Добавление задачи с выбором приоритета.
- Переключение состояния выполнения.
- Фильтрация задач внутри конкретного проекта.
- Экран Project Details: описание, срок, прогресс, этапы, связанные задачи и последняя активность.
- Переходы Projects → Project Details → Tasks проекта.
- Единое in-memory хранилище `DemoDashboardStore`: состояние выполнения синхронизировано между Home, Tasks, Projects и Project Details.
- Исправлено незавершённое состояние `TaskRow` в Home: главная теперь использует тот же список задач.

### Этап 3 — заметки
- Экран Notes повторяет исходную компоновку: поиск, горизонтальные фильтры и двухколоночная staggered-мозаика пастельных карточек.
- Поиск по названию и содержимому заметки.
- Категории и динамические счётчики.
- Закрепление заметок и сортировка закреплённых выше остальных.
- Создание, просмотр, редактирование и удаление с подтверждением.
- Выбор категории и цвета карточки, поддержка длинного текста.
- Quick Add → Note сразу открывает редактор новой заметки.
- Счётчик заметок в Menu связан с демонстрационным хранилищем.

### Этап 4 — Analytics и Habits
- Analytics: переключатель «Неделя / Месяц», график выполненных задач с выбором столбца, распределение задач по проектам с легендой и график динамики.
- Распределение по проектам рассчитывается по текущим связям задач и проектов, полученным из Room Flow.
- Исторические недельные/месячные значения и линия динамики пока демонстрационные: текущая модель задач не хранит дату завершения.
- Habits: календарь текущего месяца с легендой, карточки привычек, отметки за последние 7 дней и серии.
- Создание привычки с выбором пастельного цвета, экран Habit Details и переключение отметки за сегодня.
- История привычек хранится как локальные ключи дат `yyyy-MM-dd`; `HabitMetrics` рассчитывает серию и процент активных дней месяца.

## Этап 19 — типизированные даты и календарные фильтры

- Сроки задач, дедлайны проектов и сроки целей в доменных моделях теперь представлены `java.time.LocalDate`, а Room хранит их как ISO `TEXT` через `DateConverters`.
- В таблицы `tasks` и `projects` добавлены nullable-поля `dueDate` и `deadlineDate`; для таблицы целей физический формат `TEXT` не менялся.
- Room обновлён до версии 6. Миграция 5→6 добавляет новые поля и пытается преобразовать прежние подписи («Сегодня», «Завтра», `27 ноя`, `05.11`, `Сегодня · 16:00`) в настоящие даты. Исходные подписи остаются в legacy-полях для обратной совместимости и отображения.
- Нераспознанные или не содержащие срока подписи остаются без даты и не получают выдуманное значение.
- Для задач, проектов и целей используется общий Material 3 Date Picker. Дата может быть очищена; прежнее время в подписи задачи сохраняется при изменении только даты.
- Tasks получил фильтр «Просрочено». Calendar получил фильтры «Все», «7 дней», «Месяц» и «Просрочено».
- JSON backup обновлён до версии 3 и включает поля `dueDate` и `deadlineDate`. Импорт форматов v1 и v2 остаётся поддержан; при импорте старых файлов дата выводится из прежней подписи.
- Для `LocalDate` на Android ниже API 26 нужен Core Library Desugaring. В standalone Gradle-конфигурации Stage 20 установлен `minSdk 26`, поэтому отдельный desugaring dependency не требуется.
- Проверки: `VALIDATION_STAGE19.md`.

## Этап 18 — Calendar

- Добавлен экран `Calendar`, доступный из Menu; отдельная нижняя вкладка не добавлялась.
- Календарь собирает события из текущего `DashboardUiState`: сроки задач, проектов и целей, даты выполнения привычек и события активности.
- Календарные события являются вычисляемой read-only проекцией. Новая таблица Room не создаётся, исходные записи остаются единственным источником истины.
- Добавлены переключение месяцев, выбор дня, индикаторы типов событий и список событий выбранной даты.
- Поддерживаются ISO-даты, «Сегодня», «Завтра», «Послезавтра», «Вчера», «через N дней», числовые даты и русские подписи вида `27 ноя` без года. Даты без года интерпретируются ближайшим годом в пределах полугодового окна; неизвестные подписи не преобразуются в выдуманные даты.
- Нажатие на задачу открывает список задач, прокручивает к нужной записи и подсвечивает её; цель открывается в Goals с прокруткой к карточке; проект и привычка открываются на своих экранах; активность ведёт в Analytics.
- На этом этапе календарь использовал строковые подписи дат; в Stage 19 они получили типизированные поля `LocalDate`.
- Проверки: `VALIDATION_STAGE18.md`.

## Этап 5 — Room и история активности

- Добавлена Room-база `life_dashboard.db`.
- Сохраняются задачи, проекты, этапы проектов, заметки и привычки с историей дат.
- При первом запуске демонстрационные данные записываются в базу; при следующих запусках состояние восстанавливается из Room.
- На этом этапе операции добавления/изменения заметок, привычек и данных проектов сохраняются снимками в транзакции.
- Для последовательной записи снимков используется `Mutex`, чтобы быстрые изменения не перезаписывали друг друга в обратном порядке.
- Изменения статуса задачи записываются в `activity_events`; Analytics строит недельные и месячные столбцы по реальным событиям выполнения.
- История начинается с момента подключения Room. Старые задачи, уже отмеченные выполненными в демонстрационных данных, не имеют достоверной даты завершения и не добавляются в прошлые периоды.

В Stage 11 `DemoDashboardStore` полностью удалён. Room-backed репозитории стали источниками данных, а `DashboardViewModel` объединяет их потоки в единый неизменяемый `DashboardUiState`.

В Stage 12 добавлены полноценные CRUD-операции задач и проектов: редактирование и удаление задач, создание/редактирование/удаление проектов, управление этапами с сохранением их порядка. При удалении проекта его задачи остаются в приложении и отвязываются от проекта.

## Структура

```text
com.example.lifedashboard
├── data
│   ├── DateKeys.kt
│   ├── HabitMetrics.kt
│   ├── DashboardSeedData.kt
│   ├── local
│   │   ├── DashboardDao.kt
│   │   ├── SettingsDataStore.kt
│   │   ├── DashboardEntities.kt
│   │   ├── LifeDashboardDatabase.kt
│   │   └── RoomDashboardPersistence.kt
│   ├── repository
│   │   ├── DashboardRepository.kt
│   │   ├── TaskRepository.kt
│   │   ├── NotesRepository.kt
│   │   ├── HabitsRepository.kt
│   │   ├── ProjectsRepository.kt
│   │   ├── ActivityRepository.kt
│   │   └── SettingsRepository.kt
│   └── model
│       ├── DashboardModels.kt
│       └── DashboardSettings.kt
├── presentation
│   ├── DashboardUiState.kt
│   └── DashboardViewModel.kt
├── feature
│   ├── common
│   ├── calendar
│   │   ├── CalendarScreen.kt
│   │   └── CalendarEvents.kt
│   ├── home
│   ├── menu
│   ├── settings
│   ├── analytics
│   │   └── AnalyticsScreen.kt
│   ├── habits
│   │   ├── HabitsScreen.kt
│   │   ├── HabitDetailsScreen.kt
│   │   └── HabitEditorDialog.kt
│   ├── notes
│   │   ├── NotesScreen.kt
│   │   └── NoteDetailsScreen.kt
│   ├── projects
│   │   ├── ProjectsScreen.kt
│   │   ├── ProjectDetailsScreen.kt
│   │   └── ProjectEditorDialog.kt
│   └── tasks
│       └── TasksScreen.kt
├── navigation
├── ui
│   ├── components
│   └── theme
└── MainActivity.kt
```

## Интеграция исходников в другой Android-проект (опционально)

Stage 20 уже содержит самостоятельную Gradle-конфигурацию. Этот раздел нужен только в том случае, если ты переносишь исходники Life Dashboard в другой, уже существующий Android-проект.

1. Убедись, что целевой проект настроен для Jetpack Compose и Material 3.
2. Добавь Navigation Compose и Material Icons Extended, если их ещё нет:

```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:<your-compose-bom-version>"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:<your-navigation-compose-version>")
    implementation("androidx.room:room-runtime:<room-version>")
    implementation("androidx.datastore:datastore-preferences:<datastore-version>")
    implementation("androidx.room:room-ktx:<room-version>")
    ksp("androidx.room:room-compiler:<room-version>")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

Не добавляй повторно библиотеки, которые уже есть в проекте.

3. В корневом `build.gradle.kts` подключи KSP-плагин, совместимый с используемой версией Kotlin, а в `app/build.gradle.kts` добавь `id("com.google.devtools.ksp")`. Если проект использует KAPT, вместо `ksp(...)` используй `kapt("androidx.room:room-compiler:<room-version>")` и включи KAPT plugin. Не смешивай KSP и KAPT для одного Room compiler.
4. Скопируй содержимое `app/src/main/java/com/example/lifedashboard/` в `app/src/main/java/<твой-пакет>/`. При отличающемся package замените package и imports во всех файлах.
5. В `MainActivity` вызови `LifeDashboardApp()` внутри `setContent`: приложение само загружает настройки DataStore и выбирает активную тему. Не заменяй существующий `MainActivity` целиком, если в нём уже есть настройки edge-to-edge или другие нужные действия.
6. Проверь сборку и запусти приложение на эмуляторе или устройстве.

## Шрифт Manrope

В исходном HTML используется Manrope. Для удобства переноса `Type.kt` пока использует `FontFamily.SansSerif` как fallback. Добавь файлы Manrope TTF/OTF в `app/src/main/res/font/` и подключи их в `DashboardFont`. WOFF2 из HTML напрямую в `res/font` использовать не следует.

## Адаптивность

- Длинные экраны используют `LazyColumn`.
- Нижняя навигация закреплена через `Scaffold`.
- Список Notes использует `LazyVerticalStaggeredGrid`; нужна версия Compose Foundation с поддержкой staggered grid.
- Фильтры прокручиваются горизонтально.
- Главная адаптирует соседние карточки к ширине.
- Высота карточек определяется содержимым, а не фиксированной высотой макета.

## Что ещё не реализовано

- Goals реализован в Stage 17: CRUD целей, измеримый прогресс, срок (`LocalDate`) и связь с проектом.
- Calendar реализован в Stage 18 и дополнен в Stage 19: сроки, история привычек и активность агрегируются из Room Flow без отдельной таблицы; доступны фильтры по периоду.
- Системные Android-уведомления и локальные напоминания о привычках реализованы в Stage 14; глобальное отключение уведомлений и системные настройки доступа подключены в Stage 15.
- Историческая аналитика до даты установки Room: прошлые события не восстановить задним числом.
- Повторяющиеся задачи.

## Историческое примечание о ранних этапах

До Stage 20 исходные пакеты не включали самостоятельный Gradle-проект, поэтому ранние проверки выполнялись с тестовыми заглушками. Stage 20 добавил отдельную конфигурацию сборки. Полноценная Gradle-проверка этого архива ожидается после первого запуска проекта в Android Studio; актуальные зависимости уже перечислены в `gradle/libs.versions.toml` и `app/build.gradle.kts`.

## Этап 6 — Repository и ViewModel

- Добавлен контракт `DashboardRepository`; `RoomDashboardPersistence` теперь является его Room-реализацией.
- `DashboardViewModel` создаётся через `ViewModelProvider.Factory` и живёт в lifecycle Activity.
- Добавлена неизменяемая модель `DashboardUiState`, публикуемая через `StateFlow` после изменений и восстановления данных.
- Экраны пока продолжают использовать прежние Compose state lists через `viewModel.store`. Это промежуточный этап: он отделяет конкретную Room-реализацию и вводит lifecycle/ViewModel без массового переписывания UI.
- Для `viewModel()` добавь `androidx.lifecycle:lifecycle-viewmodel-compose` совместимой версии, если зависимости ещё нет.
- Подробности и ограничения: `STAGE6_ARCHITECTURE.md`.


## Этап 7 — TaskRepository и StateFlow

- Добавлен отдельный контракт `TaskRepository` с наблюдением задач через `Flow<List<DashboardTask>>`.
- Запись задачи, обновление счётчиков связанного проекта и добавление события активности выполняются одной Room-транзакцией.
- `DashboardViewModel` выполняет операции добавления/переключения задач; параллельные изменения сериализуются через `Mutex`.
- Home, Tasks, Projects, Project Details и Analytics получают список задач из `DashboardUiState`, собираемого через `collectAsState()`.
- После первого запуска задачи записываются до начала Flow-наблюдения. Последующие широкие snapshot-записи больше не перезаписывают таблицу задач устаревшим состоянием.
- Задачи остаются синхронизированными между основными экранами. Внешний вид экранов не менялся.
- Заметки, привычки и прочие данные проектов пока остаются на прежнем `DashboardRepository` и `DemoDashboardStore`.
- Подробности: `STAGE7_TASKS_REPOSITORY.md`.


## Этап 8 — NotesRepository и Room Flow

- Добавлен `NotesRepository`: `observeNotes()`, `saveNote()`, `deleteNote()` и `togglePinned()`.
- `RoomDashboardPersistence` реализует новый контракт; заметки читаются через `Flow<List<DashboardNote>>`.
- `DashboardViewModel` ожидает завершения начального восстановления/заполнения базы, затем подписывается на поток заметок.
- Список заметок в `DashboardUiState` обновляется после изменений Room и используется экраном Notes, редактором и счётчиком в Menu.
- Все команды редактора и закрепления теперь проходят через ViewModel. Сохранение и удаление закрывают экран только после успешной записи; ошибка показывается на экране.
- Закрепление меняется внутри Room-транзакции на основе актуальной записи из базы, а не устаревшего объекта Compose.
- Снимки привычек/проектов больше не очищают и не перезаписывают таблицу заметок. Демо-заметки добавляются только при первом заполнении пустой базы.
- Структура таблицы `notes` не менялась, поэтому этот этап не добавляет новую миграцию Room.
- Визуальный дизайн, поиск, категории, мозаика карточек, выбор цвета и подтверждение удаления сохранены.
- Подробности: `STAGE8_NOTES_REPOSITORY.md`; проверки: `VALIDATION_STAGE8.md`.


## Этап 9 — HabitsRepository

- Добавлен `HabitsRepository` для наблюдения и изменения привычек.
- Room `Flow` автоматически обновляет `DashboardUiState`.
- Добавление привычки и переключение отметки за дату выполняются через `DashboardViewModel`.
- Переключение даты выполняется атомарно внутри Room-транзакции.
- История хранится как отсортированные локальные ключи `yyyy-MM-dd`; повторное нажатие снимает отметку.
- Снимки других функций больше не очищают и не перезаписывают таблицу привычек.
- Экран Habits получает данные из `DashboardUiState`, а ошибки сохранения отображаются пользователю.
- Управление привычками выполняется через `HabitsRepository` и ViewModel.

Подробности: `STAGE9_HABITS_REPOSITORY.md` и `VALIDATION_STAGE9.md`.

## Stage 10 — ProjectsRepository

Project persistence is now available through `ProjectsRepository`. Room emits projects and stages together as a Flow; `DashboardViewModel` exposes project save and stage-toggle commands. Existing project screens retain their design and remain read-only in this stage. See `STAGE10_PROJECTS_REPOSITORY.md` and `VALIDATION_STAGE10.md`.


## Stage 11 — удаление DemoDashboardStore и независимость слоя данных

- `DemoDashboardStore.kt` удалён. Временные mutable lists и ручные операции синхронизации больше не используются.
- `DashboardViewModel` собирает Tasks, Projects, Notes, Habits и Activity через пять repository Flow и формирует единый `DashboardUiState`.
- Обработка добавления/переключения задач полностью перенесена в `TaskRepository`; обновление задачи, счётчика проекта и события активности выполняется одной Room-транзакцией.
- Добавлен `ActivityRepository` для чтения истории событий. Запись completion-событий остаётся частью транзакции изменения задачи.
- Демоданные вынесены в `DashboardSeedData`. `DashboardRepository.initializeIfNeeded()` выполняет заполнение только при первичном создании базы.
- Добавлены `tasks.sortOrder`, таблица `app_metadata`, маркер первоначального заполнения и миграция Room `1 → 2`. Существующие строки и порядок задач сохраняются; база с данными помечается без повторного заполнения.
- Модель `DashboardProject` теперь хранит `colorArgb: Int`, а преобразование в Compose `Color` выполняется только в UI. Слой `data` больше не импортирует Compose и не использует Compose state.
- Порядок стартовых задач и существующей базы сохраняется через `sortOrder`; новые задачи добавляются в начало, изменение статуса не меняет позицию.

Подробности: `STAGE11_ARCHITECTURE.md`. Проверки и ограничения: `VALIDATION_STAGE11.md`.


## Этап 12 — CRUD для задач и проектов

- `TaskRepository` расширен операциями `updateTask()` и `deleteTask()`.
- Редактирование задачи позволяет менять название, срок, приоритет, категорию и связанный проект.
- Перенос задачи между проектами атомарно корректирует счётчики обоих проектов; удаление задачи корректирует счётчики её текущего проекта.
- Удаление задачи требует подтверждения. Исторические события аналитики не удаляются, чтобы журнал не терял факт выполненного ранее действия.
- `ProjectsRepository` расширен `deleteProject()`; при удалении проекта связанные задачи сохраняются и становятся задачами без проекта.
- На экране Projects добавлено создание проекта. На Project Details доступны редактирование/удаление проекта и переключение этапов.
- Редактор проекта поддерживает название, описание, срок, цвет карточки и список этапов (один этап на строку).
- Порядок этапов теперь хранится явно в `project_stages.sortOrder`. Миграция Room 2 → 3 нормализует default `tasks.sortOrder`, добавляет сортировку этапов и сохраняет существующие строки; миграция 1 → 2 сохранена в цепочке.
- Новые операции проходят через ViewModel, репозитории и Room; ошибки показываются на соответствующих экранах.
- Дизайн-система и основная компоновка экранов сохранены; добавлены только необходимые элементы управления.

Подробности: `STAGE12_CRUD.md`; проверки: `VALIDATION_STAGE12.md`.


## Этап 13 — CRUD привычек и настройки напоминаний

- `HabitsRepository` получил отдельные операции создания, редактирования и удаления привычек.
- На экране деталей добавлен редактор названия, пастельного цвета, времени и дней напоминания. На экране списка форма позволяет создать привычку с теми же настройками.
- Удаление привычки доступно на экране деталей и требует подтверждения; удаляется также история отметок, что явно указано в диалоге.
- При редактировании Room сохраняет актуальный `completionDates` из базы и обновляет только настройки — открытая форма не может затереть новые отметки.
- На этапе 13 модель напоминания сохраняла enabled, время `HH:mm` и выбранные дни недели; само расписание OS-уведомлений добавлено на этапе 14.
- Room обновлён до версии 4; миграция 3 → 4 добавляет три reminder-столбца с defaults. Для существующих привычек напоминания остаются выключенными, история дат сохраняется.

Подробности: `STAGE13_HABITS_CRUD.md`; проверки: `VALIDATION_STAGE13.md`.


## Этап 14 — Android Notifications

- Подключён `AlarmManager` для локальных напоминаний привычек по выбранным дням недели и времени.
- Хранится ближайший будильник на привычку; после срабатывания receiver планирует следующую дату.
- Сохранение изменяет расписание, выключение и удаление привычки отменяют будильник.
- При запуске приложения и после перезагрузки устройства расписание восстанавливается из Room. Пересчёт также выполняется после изменения системного времени/часового пояса и выдачи доступа к точным будильникам.
- На Android 13+ запрашивается `POST_NOTIFICATIONS`. На Android 12+ приложение предлагает выдать специальный доступ `SCHEDULE_EXACT_ALARM`; без него включается inexact fallback, который может срабатывать с задержкой.
- Миграция Room не требуется: расписание использует поля привычек, добавленные в Stage 13. Подробности: `STAGE14_ANDROID_NOTIFICATIONS.md`, проверки: `VALIDATION_STAGE14.md`.


## Stage 15 — Settings и Preferences DataStore

- Добавлен `SettingsRepository` и `DataStoreSettingsRepository`; настройки хранятся отдельно от сущностей Room.
- В DataStore сохраняются режим темы (`SYSTEM`, `LIGHT`, `DARK`), глобальное включение напоминаний и отображение выполненных задач на главном экране.
- Экран Settings заменил заглушку: настройки меняются без отдельной кнопки сохранения.
- Светлая тема сохраняет исходную HTML-палитру. Для тёмной темы добавлена адаптивная семантическая палитра через CompositionLocal; системный режим следует настройке Android.
- Выключение всех уведомлений отменяет запланированные будильники. Включение заново планирует напоминания для привычек с включённым индивидуальным расписанием. BroadcastReceiver проверяет глобальную настройку перед показом уведомления и при восстановлении расписания.
- Разрешения Android 13+ и доступ к точным будильникам доступны из Settings; отказ в `POST_NOTIFICATIONS` не включает глобальные уведомления. При отсутствии exact-alarm access приложение продолжает использовать best-effort будильник.
- Room-схема не менялась; миграция не требуется.
- Для подключения добавь `androidx.datastore:datastore-preferences:<datastore-version>` совместимой с проектом версии.

Подробности: `STAGE15_SETTINGS_DATASTORE.md`; проверки: `VALIDATION_STAGE15.md`.


## Этап 16 — Backup и Export

- Добавлены два JSON-формата: переносимый экспорт всех таблиц Room и полная логическая резервная копия Room + Preferences DataStore.
- Системный Android Storage Access Framework используется для сохранения и выбора файла; разрешение на общий накопитель не требуется.
- Перед восстановлением показывается предупреждение, поскольку импорт заменяет текущие записи. Файл полностью валидируется до одной Room-транзакции.
- Проверяются идентификаторы и связи сущностей, enum-значения, расписание привычек и версия формата. Неизвестные версии отклоняются без изменения базы.
- После восстановления старые AlarmManager-события отменяются, а расписание привычек пересоздаётся с учётом восстановленных настроек.
- Полная копия является логическим snapshot всех строк Room, снятым в транзакции, и настроек DataStore; живой SQLite/WAL файл не копируется.
- Импорт ограничен 20 МБ. Вложения файлов и Android-разрешения не входят в текущий формат.

Подробности: `STAGE16_BACKUP_AND_EXPORT.md`; проверки: `VALIDATION_STAGE16.md`.


## Этап 17 — Goals

- Добавлена сущность `GoalEntity`, модель `DashboardGoal` и enum `GoalColor`. Цель хранит название, описание, необязательный срок `yyyy-MM-dd`, текущее/целевое значение, единицу измерения, необязательную связь с проектом, цвет и порядок.
- Room обновлён до версии 5; миграция 4 → 5 создаёт таблицу `goals`, не перестраивая и не изменяя существующие таблицы.
- `GoalsRepository` реализован в `RoomDashboardPersistence`; список целей передаётся в `DashboardUiState` через Room Flow. ViewModel обрабатывает сохранение, удаление и атомарное изменение прогресса.
- Экран Goals поддерживает фильтры «Все», «В процессе», «Достигнуты», создание/редактирование/удаление, даты, связь с проектом, пастельные цвета и быстрые +/- изменения прогресса.
- При удалении проекта связанная цель сохраняется и отвязывается от него. Изменения прогресса ограничены диапазоном 0..targetValue.
- Menu и Quick Add открывают Goals, счётчик достигнутых целей вычисляется из Room-состояния.
- JSON-резервная копия обновлена до формата v2 и включает цели. Импорт формата v1 остаётся поддержанным; так как v1 целей не содержал, импорт v1 заменяет список целей пустым списком, как и остальные данные при полном восстановлении.

Подробности: `STAGE17_GOALS.md`; проверки: `VALIDATION_STAGE17.md`.


## Открытие проекта в Android Studio

Подробная инструкция по подготовке Gradle Wrapper, SDK, открытию проекта, работе с Compose Preview и Live Edit находится в [`ANDROID_STUDIO_SETUP.md`](ANDROID_STUDIO_SETUP.md). Результаты проверок и неподтверждённые пункты перечислены в [`VALIDATION_STAGE20.md`](VALIDATION_STAGE20.md).

## Stage 21 — Recurring tasks

Tasks now support daily, weekly (selected weekdays), and monthly recurrence. The next occurrence is created transactionally when the current occurrence is completed; skipping an open recurring occurrence advances the series. Recurrence data is stored in Room v7 and included in backup JSON v4. See `STAGE21_RECURRING_TASKS.md` and `VALIDATION_STAGE21.md`.

## Stage 22 — Task reminders

Tasks can now have an optional reminder time (`HH:mm`). Android AlarmManager schedules a one-shot notification for the selected task occurrence; reminders follow task date/time edits, are cancelled on completion/deletion, and are rebuilt after reboot or time-zone changes. Recurring occurrences inherit reminder settings. Room is now v8 and backup JSON is v5; older backups restore with task reminders disabled. See `STAGE22_TASK_REMINDERS.md`.


## Stage 23 — Calendar improvements

The calendar now supports filtering by event type, displays enabled task reminder times in the selected-day agenda, sorts events consistently, and preserves the selected day when navigating between months (clamping to the target month's length when needed). It remains a read-only projection of existing repository data; no Room migration or backup format change was introduced. See `STAGE23_CALENDAR_IMPROVEMENTS.md` and `VALIDATION_STAGE23.md`.

## Stage 25 — Home quick access

The Home screen now includes shortcuts to Tasks, Notes, and Habits. These reuse the existing navigation destinations and screens; creation behavior remains in the established add controls and Quick Add sheet. See `STAGE25_QUICK_ACCESS.md` and run `bash validation/run_stage25_checks.sh` for focused checks.
