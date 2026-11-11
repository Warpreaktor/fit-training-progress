package ru.trainingapp.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt
import ru.trainingapp.app.AppAppearanceViewModel
import ru.trainingapp.core.model.AppAppearance
import ru.trainingapp.core.ui.component.ZoomCropContentScale

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: AppAppearanceViewModel = hiltViewModel(),
) {
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCropEditor by rememberSaveable { mutableStateOf(false) }
    var waitForImportedBackground by rememberSaveable { mutableStateOf(false) }
    var backgroundPathBeforeImport by rememberSaveable { mutableStateOf<String?>(null) }

    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            backgroundPathBeforeImport = appearance.backgroundImagePath
            waitForImportedBackground = true
            viewModel.importBackground(uri.toString())
        }
    }

    LaunchedEffect(
        appearance.backgroundImagePath,
        waitForImportedBackground,
        backgroundPathBeforeImport,
    ) {
        if (
            waitForImportedBackground &&
            appearance.backgroundImagePath != null &&
            appearance.backgroundImagePath != backgroundPathBeforeImport
        ) {
            waitForImportedBackground = false
            backgroundPathBeforeImport = null
            showCropEditor = true
        }
    }

    LaunchedEffect(message) {
        val currentMessage = message ?: return@LaunchedEffect
        waitForImportedBackground = false
        backgroundPathBeforeImport = null
        snackbarHostState.showSnackbar(currentMessage)
        viewModel.consumeMessage()
    }

    SettingsScreen(
        appearance = appearance,
        onBack = onBack,
        onChooseBackground = {
            backgroundPicker.launch(
                arrayOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                )
            )
        },
        onEditBackground = { showCropEditor = true },
        onRemoveBackground = viewModel::removeBackground,
        onDimAmountChange = viewModel::updateDimAmount,
        snackbarHostState = snackbarHostState,
    )

    if (showCropEditor && appearance.backgroundImagePath != null) {
        AppBackgroundCropEditor(
            appearance = appearance,
            onDismiss = { showCropEditor = false },
            onSave = { zoom, offsetX, offsetY ->
                viewModel.updateCrop(
                    zoom = zoom,
                    offsetX = offsetX,
                    offsetY = offsetY,
                )
                showCropEditor = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appearance: AppAppearance,
    onBack: () -> Unit,
    onChooseBackground: () -> Unit,
    onEditBackground: () -> Unit,
    onRemoveBackground: () -> Unit,
    onDimAmountChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var dimAmount by remember(appearance.backgroundDimAmount) {
        mutableFloatStateOf(appearance.backgroundDimAmount)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Оформление",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Фон приложения",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        if (appearance.backgroundImagePath != null) {
                            BackgroundPreview(appearance = appearance)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Используется стандартный фон",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(onClick = onChooseBackground) {
                                Text(
                                    if (appearance.backgroundImagePath == null) {
                                        "Выбрать изображение"
                                    } else {
                                        "Заменить"
                                    }
                                )
                            }

                            if (appearance.backgroundImagePath != null) {
                                TextButton(onClick = onEditBackground) {
                                    Text("Настроить")
                                }
                                TextButton(onClick = onRemoveBackground) {
                                    Text("Удалить")
                                }
                            }
                        }

                        if (appearance.backgroundImagePath != null) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Затемнение ${(dimAmount * 100f).roundToInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Slider(
                                    value = dimAmount,
                                    onValueChange = { value ->
                                        dimAmount = value.coerceIn(
                                            0f,
                                            AppAppearance.MAX_BACKGROUND_DIM_AMOUNT,
                                        )
                                    },
                                    onValueChangeFinished = {
                                        onDimAmountChange(dimAmount)
                                    },
                                    valueRange = 0f..AppAppearance.MAX_BACKGROUND_DIM_AMOUNT,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Фон применяется только к свободному пространству экранов. Карточки, меню и диалоги остаются обычными, чтобы текст не терялся на изображении.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun BackgroundPreview(
    appearance: AppAppearance,
) {
    val configuration = LocalConfiguration.current
    val aspectRatio = (
        configuration.screenWidthDp.toFloat() /
            configuration.screenHeightDp.toFloat().coerceAtLeast(1f)
        ).coerceIn(0.4f, 1.8f)
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(aspectRatio)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = File(requireNotNull(appearance.backgroundImagePath)),
                contentDescription = "Текущий фон приложения",
                modifier = Modifier.fillMaxSize(),
                alignment = BiasAbsoluteAlignment(
                    horizontalBias = appearance.backgroundCropOffsetX,
                    verticalBias = appearance.backgroundCropOffsetY,
                ),
                contentScale = ZoomCropContentScale(appearance.backgroundCropZoom),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = appearance.backgroundDimAmount,
                        )
                    ),
            )
        }
    }
}
