package ru.trainingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.trainingapp.feature.exercise_catalog.ExerciseCatalogRoute
import ru.trainingapp.feature.progress.ProgressRoute
import ru.trainingapp.feature.settings.SettingsRoute
import ru.trainingapp.feature.workout_editor.WorkoutEditorRoute
import ru.trainingapp.feature.workout_list.WorkoutListRoute

@Composable
fun TrainingNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.WorkoutList.route,
    ) {
        composable(AppRoute.WorkoutList.route) {
            WorkoutListRoute(
                onOpenWorkout = { workoutId ->
                    navController.navigate(AppRoute.WorkoutEditor.createRoute(workoutId))
                },
                onOpenExerciseCatalog = {
                    navController.navigate(AppRoute.ExerciseCatalog.route)
                },
                onOpenProgress = {
                    navController.navigate(AppRoute.Progress.route)
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.Settings.route)
                },
            )
        }

        composable(
            route = AppRoute.WorkoutEditor.route,
            arguments = listOf(
                navArgument(AppRoute.WorkoutEditor.ARG_WORKOUT_ID) {
                    type = NavType.LongType
                }
            ),
        ) { backStackEntry ->
            val workoutId = requireNotNull(backStackEntry.arguments) {
                "Missing navigation arguments"
            }.getLong(AppRoute.WorkoutEditor.ARG_WORKOUT_ID)

            WorkoutEditorRoute(
                workoutId = workoutId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.ExerciseCatalog.route) {
            ExerciseCatalogRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.Progress.route) {
            ProgressRoute(onBack = { navController.popBackStack() })
        }

        composable(AppRoute.Settings.route) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}