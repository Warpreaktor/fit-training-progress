package ru.trainingapp.core.data.tag

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.trainingapp.core.data.mapper.toDomain
import ru.trainingapp.core.database.dao.TagDao
import ru.trainingapp.core.database.entity.TagEntity
import ru.trainingapp.core.domain.repository.TagRepository
import ru.trainingapp.core.model.Tag
import javax.inject.Inject

class RoomTagRepository @Inject constructor(
    private val tagDao: TagDao,
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> {
        return tagDao
            .observeTags()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createTag(
        name: String,
        color: String?,
    ): Long {
        return getOrCreateTag(
            name = name,
            color = color,
        )
    }

    override suspend fun getOrCreateTag(
        name: String,
        color: String?,
    ): Long {
        val normalizedName = name
            .trim()
            .removePrefix("#")
            .trim()

        if (normalizedName.isBlank()) {
            return 0L
        }

        val existingTag = tagDao.getTagByName(normalizedName)

        if (existingTag != null) {
            return existingTag.id
        }

        val now = System.currentTimeMillis()

        val insertedId = tagDao.insertTag(
            TagEntity(
                name = normalizedName,
                color = color,
                createdAt = now,
                updatedAt = now,
            )
        )

        if (insertedId != -1L) {
            return insertedId
        }

        return tagDao.getTagByName(normalizedName)?.id ?: 0L
    }
}