# Life Dashboard — карта экранов

## Основные разделы
- Home — обзор дня, задачи и проекты.
- Projects — проекты и карточки проектов.
- Tasks — список задач, фильтры и поиск.
- Project Details — прогресс проекта, этапы и связанные задачи.
- Notes — поиск, категории, закреплённые записи и мозаика карточек.
- Note Details — создание, просмотр, редактирование и удаление заметки.
- Menu — переход к дополнительным разделам.
- Analytics, Habits, Settings, Goals и Calendar — реализованы.

## Маршруты
- `home`
- `projects`
- `tasks?projectId={projectId}`
- `project_details/{projectId}`
- `notes`
- `note_details/{noteId}`
- `note_details/new`
- `menu`, `analytics`, `calendar`, `habits`, `habit_details/{habitId}`, `goals?goalId={goalId}`, `settings`
- `tasks?projectId={projectId}&taskId={taskId}` (параметры необязательны)

## Переходы Notes
- Menu → Notes.
- Notes → Note Details по нажатию карточки.
- Notes → New Note через пустое состояние.
- Quick Add → Note → New Note.
- Note Details → Save → предыдущий экран.
- Note Details → Delete → подтверждение → предыдущий экран.

## Общая навигация
Нижняя панель остаётся закреплённой в `Scaffold`. Notes и Note Details открываются поверх Menu/предыдущего экрана; разделы, не входящие в нижнюю навигацию, не добавляются в неё без отдельного решения.


## Stage 4 — Analytics и Habits

- `Analytics`: период Неделя/Месяц, выполнение задач, распределение задач по проектам, динамика.
- `Habits`: календарь активности, список привычек, streak и недельные отметки.
- `Habit Details`: история за последние семь дней и отметка выполнения сегодня.
- `habit_details/{habitId}`: маршрут деталей привычки.

## Переходы Analytics и Habits
- Menu → Analytics; переключение периода Неделя/Месяц и выбор столбца графика.
- Menu → Habits; создание привычки через кнопку `+`.
- Habits → Habit Details по нажатию карточки; отметка/отмена выполнения за сегодня синхронизируется с календарём и серией.
- На карточке привычки последний кружок также переключает отметку за сегодня.


## Stage 15 — Settings

- `Settings`: тема SYSTEM/LIGHT/DARK, глобальный переключатель напоминаний о привычках и отображение выполненных задач на Home.
- Настройки сохраняются через Preferences DataStore и восстанавливаются при следующем запуске.
- Из экрана Settings можно открыть Android-настройки разрешений уведомлений и точных будильников.

## Stage 16 — Backup и Export

- `Settings` содержит экспорт данных JSON, создание полной логической резервной копии Room + DataStore и восстановление из JSON.
- Импорт проходит через Android document picker и требует явного подтверждения замены текущих данных.


## Stage 17 — Goals

- `Goals`: обзор личного прогресса, фильтры по статусу, создание и редактирование целей.
- Карточка цели показывает текущее/целевое значение, прогресс, срок и связанный проект.
- Быстрые кнопки увеличивают/уменьшают прогресс; удаление требует подтверждения.
- Menu и Quick Add → Goal открывают раздел Goals.


## Stage 18 — Calendar

- `Menu` → `Calendar`; выбор месяца и даты, цветные маркеры и список событий выбранного дня.
- В календаре отображаются даты задач, проектов, целей, отметки привычек и активность из истории событий.
- Нажатие на событие открывает соответствующий раздел; задачи и цели дополнительно прокручиваются к нужной карточке и подсвечиваются.
- Календарь вычисляется из текущего состояния репозиториев и не хранит собственные записи.


## Stage 19 — Даты и фильтры

- Tasks, Project Editor и Goals Editor используют общий `LocalDatePickerField` на базе Material 3 Date Picker.
- В Tasks добавлен фильтр просроченных незавершённых задач.
- В Calendar доступны фильтры «Все», «7 дней», «Месяц» и «Просрочено».
- Доменные даты хранятся как `LocalDate`; Room сохраняет ISO `yyyy-MM-dd`.


## Stage 20 — Android Studio Preview

- `ui/preview/LifeDashboardScreenPreviews.kt` содержит 18 функций `@Preview`: Home Light/Dark/Compact/Tablet, Tasks Phone/Compact/Tablet, Projects Phone/Tablet, Project Details, Goals, Calendar, Habits, Habit Details, Notes, Note Editor, Analytics и Settings.
- Preview использует локальные тестовые модели и не обращается к Room/DataStore.
- Проект открывается как самостоятельный Android Gradle-проект после генерации Gradle Wrapper скриптом `tools/bootstrap-gradle-wrapper.*`.
- Инструкция по Compose Preview и Live Edit: `ANDROID_STUDIO_SETUP.md`.
