# TrainingApp

Android-приложение для тренировок. Архив закрывает этапы 1 и 2: создан базовый Android-проект и разложена структура пакетов под `core/*` и `feature/*` в одном `app`-модуле.

## Что уже настроено

- Kotlin Android
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room + KSP, пока без реальной схемы БД
- Version Catalog `gradle/libs.versions.toml`
- Feature-oriented структура пакетов с возможностью будущего выноса в Gradle-модули

## Структура

```text
app/src/main/java/ru/trainingapp
├── app
├── navigation
├── core
│   ├── database
│   ├── data
│   ├── domain
│   ├── model
│   └── ui
└── feature
    ├── workout_list
    ├── workout_editor
    ├── exercise_catalog
    ├── progress
    └── settings
```

## Как открыть

1. Распаковать архив.
2. Открыть папку `training-app-stage-1-2` в Android Studio.
3. Использовать JDK 17+.
4. Дождаться Gradle Sync.

В архив не добавлен `gradle-wrapper.jar`, потому что бинарник не генерировался в этой среде. Если нужен wrapper, локально выполнить:

```bash
gradle wrapper --gradle-version 9.4.1
```

После этого можно запускать:

```bash
./gradlew :app:assembleDebug
```

## Следующий шаг

Этап 3: Room-схема MVP.

Сначала добавляем entities и DAO:

- `ExerciseDefinition`
- `Workout`
- `WorkoutExercise`
- `WorkoutExerciseSet`
- `PendingWorkoutChange`
- `WorkoutExerciseProgressPoint`
- `WorkoutExerciseProgressSet`
- `Tag`
- `WorkoutTagCrossRef`

Потом подключаем репозитории и первые use case.
