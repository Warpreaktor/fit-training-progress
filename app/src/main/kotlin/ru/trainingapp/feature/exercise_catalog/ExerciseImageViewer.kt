package ru.trainingapp.feature.exercise_catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import ru.trainingapp.core.model.ExerciseDefinition

@Composable
fun ExerciseImageViewer(
    exercise: ExerciseDefinition,
    onDismiss: () -> Unit,
    onAddImagesClick: () -> Unit,
    onSetCoverClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
) {
    val images = exercise.images

    if (images.isEmpty()) {
        return
    }

    val initialPage = images
        .indexOfFirst { image -> image.isCover }
        .takeIf { index -> index >= 0 }
        ?: 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size },
    )

    LaunchedEffect(images.size) {
        if (pagerState.currentPage > images.lastIndex) {
            pagerState.scrollToPage(images.lastIndex)
        }
    }

    val currentPage = pagerState.currentPage.coerceIn(
        minimumValue = 0,
        maximumValue = images.lastIndex,
    )

    val currentImage = images.getOrNull(currentPage)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            text = "${currentPage + 1} из ${images.size}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Закрыть")
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    key = { page ->
                        images.getOrNull(page)?.id ?: page.toLong()
                    },
                ) { page ->

                    val image = images.getOrNull(page)
                        ?: return@HorizontalPager

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = "Изображение упражнения ${exercise.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(
                        onClick = {
                            currentImage?.let { image ->
                                onSetCoverClick(image.id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentImage != null && !currentImage.isCover,
                    ) {
                        Text(
                            text = if (currentImage?.isCover == true) {
                                "Это обложка"
                            } else {
                                "Сделать обложкой"
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(
                            onClick = onAddImagesClick,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Добавить")
                        }

                        TextButton(
                            onClick = {
                                currentImage?.let { image ->
                                    onDeleteClick(image.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = currentImage != null,
                        ) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}