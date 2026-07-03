package ru.trainingapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.trainingapp.core.model.Tag

interface TagRepository {

    fun observeTags(): Flow<List<Tag>>

    suspend fun createTag(
        name: String,
        color: String? = null,
    ): Long

    suspend fun getOrCreateTag(
        name: String,
        color: String? = null,
    ): Long
}