package ru.trainingapp.navigation

sealed class AppRoute(val route: String) {

    data object WorkoutList : AppRoute("workouts")

    data object WorkoutEditor : AppRoute("workout/{workoutId}") {

        fun createRoute(workoutId: Long): String {
            return "workout/${workoutId}"
        }

        const val ARG_WORKOUT_ID = "workoutId"
    }

    data object ExerciseCatalog : AppRoute("exercises")

    data object Progress : AppRoute("progress")

    data object ExerciseProgress : AppRoute("progress/exercise/{workoutExerciseId}") {

        fun createRoute(workoutExerciseId: Long): String {
            return "progress/exercise/$workoutExerciseId"
        }

        const val ARG_WORKOUT_EXERCISE_ID = "workoutExerciseId"
    }

    data object Settings : AppRoute("settings")
}