package ru.trainingapp.core.domain.tag

import ru.trainingapp.core.domain.repository.TagRepository
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository,
) {

    suspend operator fun invoke(name: String): Long {
        val normalizedName = name.trim()

        if (normalizedName.isBlank()) {
            return 0L
        }

        return repository.createTag(name = normalizedName)
    }
}