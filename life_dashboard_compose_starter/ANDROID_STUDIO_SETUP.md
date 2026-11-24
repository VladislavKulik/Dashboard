# Stage 20 — запуск проекта в Android Studio и Compose Preview

На этом этапе исходники объединены в самостоятельный Android Gradle-проект. Проект ориентирован на Jetpack Compose, Material 3, Room + KSP и Preferences DataStore.

## Версии инструментов

| Компонент | Версия | Причина выбора |
|---|---:|---|
| Android Studio | Rabbit 2 / 2026.2.2 или новее Stable | Совместима с AGP 9.3.1 |
| Android Gradle Plugin | 9.3.1 | Совместим с Kotlin Gradle Plugin 2.4.21 |
| Gradle | 9.5.0 | Минимальная версия для AGP 9.3.x |
| Kotlin Gradle Plugin | 2.4.21 | Явно закреплён на buildscript classpath, чтобы совпадать с Compose Compiler plugin |
| Compose Compiler plugin | 2.4.21 | Версия синхронизирована с Kotlin |
| KSP | 2.3.12 | Поддерживает Kotlin 2.4.x, AGP 8.13+ и Gradle 9.x |
| Compose BOM | 2026.09.00 | Последний стабильный BOM, рекомендованный в документации на момент сборки |
| Room | 2.8.5 | Room runtime, KTX и compiler через KSP |
| Navigation Compose | 2.10.2 | Стабильная версия |
| DataStore Preferences | 1.3.0 | Стабильная версия |

AGP 9 включает встроенную поддержку Kotlin, поэтому в модуле намеренно **не** применяется `org.jetbrains.kotlin.android`. При этом AGP 9 может принести встроенную версию KGP, поэтому корневой `build.gradle.kts` явно закрепляет `org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21` на buildscript classpath. Это выравнивает Kotlin toolchain с Compose Compiler plugin 2.4.21. Compose Compiler plugin подключается отдельно, а KSP выполняет генерацию Room-кода.

## 1. Подготовить Gradle Wrapper

В текущем рабочем окружении нет Gradle distribution и Android SDK, а сетевой доступ контейнера ограничен. Поэтому официальный `gradle-wrapper.jar` не вкладывался в архив как непроверенный бинарный файл.

В проект добавлены скрипты, которые скачивают официальный Gradle 9.5.0, сверяют SHA-256 с контрольной суммой Gradle, создают стандартные Wrapper-файлы во временном пустом Gradle-проекте (без конфигурации Android SDK) и переносят их в корень приложения. SHA-256 записывается и в `gradle-wrapper.properties`, чтобы Gradle Wrapper повторно проверял дистрибутив при загрузке.

**Windows (PowerShell):** из корневой папки проекта:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\tools\bootstrap-gradle-wrapper.ps1
```

**macOS / Linux:**

```bash
bash tools/bootstrap-gradle-wrapper.sh
```

После выполнения появятся `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` и `gradle/wrapper/gradle-wrapper.properties`. Это стандартный Gradle Wrapper, создаваемый командой Gradle `wrapper`.

## 2. Открыть проект

1. Установи Android Studio Stable. Подходит Rabbit 2 (2026.2.2) или более новая стабильная версия.
2. В SDK Manager установи Android SDK Platform 37 и Android SDK Build-Tools, предлагаемые выбранной версией AGP.
3. Убедись, что Gradle JDK — 17 или новее. Рекомендуется встроенный JBR Android Studio.
4. Выполни bootstrap Wrapper из предыдущего раздела.
5. В Android Studio выбери **File → Open** и укажи папку `life_dashboard_compose_stage20` (корень проекта, где находятся `settings.gradle.kts` и `build.gradle.kts`).
6. Дождись **Sync Project with Gradle Files**.

`local.properties` создаётся Android Studio автоматически и не включён в архив, потому что путь к SDK зависит от компьютера.

## 3. Собрать и запустить

Из терминала в корне проекта:

```bash
./gradlew :app:assembleDebug
```

В Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

Затем выбери эмулятор или подключённое устройство и нажми **Run**. `minSdk = 26`, `targetSdk = 36`, `compileSdk = 37`.

## 4. Смотреть интерфейс через Compose Preview

Открой файл:

`app/src/main/java/com/example/lifedashboard/ui/preview/LifeDashboardScreenPreviews.kt`

В нём находятся 18 Preview-вариантов основных экранов и экранов деталей: Home (Light/Dark/Compact/Tablet), Tasks (Phone/Compact/Tablet), Projects (Phone/Tablet), Project Details, Goals, Calendar, Habits, Habit Details, Notes, Note Editor, Analytics и Settings.

1. Открой файл Kotlin в редакторе.
2. Переключи редактор на **Split** или **Design**.
3. Нажми **Build & Refresh** в панели Preview.
4. Выбери нужный экран в списке Preview.
5. Измени параметры компонента, цвета, `Modifier`, отступы или типографику и обнови Preview.

Тестовые данные находятся в объекте `PreviewData` в том же файле. Они не записываются в Room и используются только в дизайн-предпросмотре.

Чтобы добавить Preview для нового composable, создай функцию без параметров:

```kotlin
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun MyComponentPreview() {
    LifeDashboardTheme {
        MyComponent(/* preview parameters */)
    }
}
```

## 5. Live Edit на эмуляторе/устройстве

Compose Preview подходит для изолированного просмотра экрана. Для просмотра работающего приложения:

1. Запусти `app` на эмуляторе или телефоне.
2. В Android Studio открой **Settings → Editor → Live Edit**.
3. Включи Live Edit и выбери Automatic либо Manual on Save.
4. Изменяй код внутри composable-функций и смотри применяемые изменения на работающем экране.

Live Edit ускоряет итерации по UI, но не заменяет обычную сборку. Изменения API, сигнатур, структуры классов, зависимостей и поведения данных могут требовать полного Build/Run.

## Примечания

- В `Type.kt` пока используется `FontFamily.SansSerif` как fallback. Для совпадения с HTML-макетом Manrope нужно добавить TTF/OTF в `app/src/main/res/font` и подключить `FontFamily`.
- Room schema export включён; после первой Gradle-сборки KSP создаст версионированную схему в `app/schemas`.
- Данные приложения не сбрасываются автоматически. При установке поверх предыдущей версии сохраняется прежний `applicationId`.
- `applicationId` общий для debug/release вариантов: это сохраняет одну базу данных при установке debug-сборки поверх предыдущей версии этого же приложения. Если когда-нибудь понадобится параллельно держать два независимых экземпляра, суффикс можно добавить отдельно.
- Полная Gradle-сборка в среде подготовки архива не запускалась: здесь отсутствуют Gradle и Android SDK, а исходящие сетевые запросы к репозиториям блокируются. Поэтому первая Sync/Build в Android Studio остаётся обязательной проверкой.
