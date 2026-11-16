package ru.trainingapp.core.data.exercise

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.trainingapp.core.domain.repository.ExerciseDefinitionRepository

/** Copies catalog data to editable Room entries. No reference to bundled data remains. */
class OfficialExerciseCopy @Inject constructor(
    private val catalog: OfficialExerciseCatalog,
    private val repository: ExerciseDefinitionRepository,
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences("official_exercise_copies", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    suspend fun copyToMyExercises(officialId: Long): Long = mutex.withLock {
        val source = requireNotNull(catalog.get(officialId)) { "Official exercise not found" }
        val storedId = preferences.getLong(source.sourceId, 0L)
        val active = repository.observeActiveExerciseDefinitions().first()
        if (storedId > 0 && active.any { it.id == storedId }) return@withLock storedId

        val newId = repository.createExerciseDefinition(source.model.name, source.model.description)
        require(newId > 0L) { "Failed to create exercise" }
        repository.replaceExerciseDefinitionMuscles(
            newId, source.model.muscles.toSet(), source.model.secondaryMuscles.toSet(),
        )
        if (source.images.isNotEmpty()) {
            val imageUris = withContext(Dispatchers.IO) { catalog.imageUris(source) }
            repository.addExerciseImages(newId, imageUris)
        }
        preferences.edit().putLong(source.sourceId, newId).apply()
        newId
    }
}
