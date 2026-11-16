# Built-in exercise catalog

`exercises.json` is an ordered array. Its order **must match** the upstream
`harshvishu/free-exercise-db-with-videos` list. The current empty array is
intentional: exercises are added in reviewed batches.

Example entry (structure only, not an imported exercise):

```json
{
  "id": "upstream-stable-id",
  "name": "Русское название",
  "description": "Описание и техника выполнения на русском языке",
  "muscles": ["CHEST"],
  "secondaryMuscles": ["TRICEPS"],
  "images": ["images/upstream-stable-id/cover.webp"]
}
```

Muscle codes are `ru.trainingapp.core.model.MuscleGroup` enum codes.
Images are optional and resolve inside `app/src/main/assets/official_catalog/`.
The asset catalog is never written to at runtime; imports create independent
editable Room records. Video integration awaits completion of video classes
missing in the uploaded source archive.
