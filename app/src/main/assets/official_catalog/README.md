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
  "images": ["images/upstream-stable-id/cover.webp"],
  "videos": ["videos/upstream-stable-id/demo.mp4"]
}
```

Muscle codes are `ru.trainingapp.core.model.MuscleGroup` enum codes.
Images and videos are optional and resolve inside `app/src/main/assets/official_catalog/`.
Recommended MP4 profile for catalog videos: H.264, 540p, no audio, accelerated ~2.5x.
The asset catalog is never written to at runtime; imports create independent
editable Room records.
