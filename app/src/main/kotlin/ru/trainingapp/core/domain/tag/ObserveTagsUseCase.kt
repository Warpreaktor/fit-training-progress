package ru.trainingapp.core.domain.tag

import ru.trainingapp.core.domain.repository.TagRepository
import javax.inject.Inject

class ObserveTagsUseCase @Inject constructor(
    private val repository: TagRepository,
) {

    operator fun invoke() = repository.observeTags()
}