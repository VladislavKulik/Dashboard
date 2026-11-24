# Patch instructions — Stage 20

Этот патч предназначен для исходников **Stage 19**.

1. Распакуй `LifeDashboard_Compose_Stage19.zip`.
2. Распакуй содержимое `LifeDashboard_Compose_Stage20_Patch.zip` поверх папки Stage 19, сохранив структуру каталогов и согласившись на замену файлов.
3. Запусти `tools/bootstrap-gradle-wrapper.ps1` в Windows или `bash tools/bootstrap-gradle-wrapper.sh` в macOS/Linux.
4. Открой корневую папку проекта в Android Studio и выполни Gradle Sync.
5. Подробная инструкция — `ANDROID_STUDIO_SETUP.md`.

Патч добавляет standalone Gradle-конфигурацию и Compose Preview-каталог, а также обновляет Manifest, Room schema export и документацию. Он не включает `gradlew`, `gradlew.bat` и `gradle-wrapper.jar`: скрипт загружает официальный Gradle 9.5.0, сверяет SHA-256 и генерирует эти стандартные файлы локально.

Не накладывай патч Stage 20 поверх более раннего этапа напрямую — сначала обнови исходники до Stage 19.
