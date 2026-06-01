package ru.trainingapp.core.domain

fun interface UseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}
