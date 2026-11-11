package ru.trainingapp.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import ru.trainingapp.core.ui.component.AppBackground
import ru.trainingapp.navigation.TrainingNavHost

@Composable
fun TrainingAppRoot(
    viewModel: AppAppearanceViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()

    AppBackground(appearance = appearance) {
        TrainingNavHost(navController = navController)
    }
}
