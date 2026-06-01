package ru.trainingapp.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import ru.trainingapp.navigation.TrainingNavHost

@Composable
fun TrainingAppRoot() {
    val navController = rememberNavController()
    TrainingNavHost(navController = navController)
}
