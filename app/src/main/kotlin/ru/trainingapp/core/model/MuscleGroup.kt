package ru.trainingapp.core.model

enum class MuscleGroup(
    val code: String,
    val displayName: String,
    val aliases: Set<String>,
) {
    NECK(
        code = "NECK",
        displayName = "Шея",
        aliases = setOf("мышцы шеи", "neck"),
    ),
    CHEST(
        code = "CHEST",
        displayName = "Грудь",
        aliases = setOf("грудные", "пекторальные", "chest", "pecs"),
    ),
    LATS(
        code = "LATS",
        displayName = "Широчайшие",
        aliases = setOf("широчайшие мышцы спины", "крылья", "lats", "latissimus"),
    ),
    UPPER_BACK(
        code = "UPPER_BACK",
        displayName = "Верх спины",
        aliases = setOf("верхняя часть спины", "ромбовидные", "upper back"),
    ),
    LOWER_BACK(
        code = "LOWER_BACK",
        displayName = "Поясница",
        aliases = setOf("низ спины", "разгибатели спины", "lower back"),
    ),
    TRAPEZIUS(
        code = "TRAPEZIUS",
        displayName = "Трапеции",
        aliases = setOf("трапеция", "трапециевидные", "traps", "trapezius"),
    ),
    FRONT_DELTS(
        code = "FRONT_DELTS",
        displayName = "Передняя дельта",
        aliases = setOf("передние дельты", "front delts", "anterior deltoid"),
    ),
    SIDE_DELTS(
        code = "SIDE_DELTS",
        displayName = "Средняя дельта",
        aliases = setOf("боковая дельта", "средние дельты", "side delts", "lateral deltoid"),
    ),
    REAR_DELTS(
        code = "REAR_DELTS",
        displayName = "Задняя дельта",
        aliases = setOf("задние дельты", "rear delts", "posterior deltoid"),
    ),
    BICEPS(
        code = "BICEPS",
        displayName = "Бицепс",
        aliases = setOf("двуглавая мышца плеча", "biceps"),
    ),
    TRICEPS(
        code = "TRICEPS",
        displayName = "Трицепс",
        aliases = setOf("трёхглавая мышца плеча", "трехглавая мышца плеча", "triceps"),
    ),
    FOREARMS(
        code = "FOREARMS",
        displayName = "Предплечья",
        aliases = setOf("предплечье", "forearms"),
    ),
    ABS(
        code = "ABS",
        displayName = "Пресс",
        aliases = setOf("прямая мышца живота", "мышцы живота", "abs", "abdominals"),
    ),
    OBLIQUES(
        code = "OBLIQUES",
        displayName = "Косые мышцы живота",
        aliases = setOf("косые", "боковой пресс", "obliques"),
    ),
    GLUTES(
        code = "GLUTES",
        displayName = "Ягодицы",
        aliases = setOf("ягодичные", "ягодицы", "glutes", "gluteus"),
    ),
    QUADRICEPS(
        code = "QUADRICEPS",
        displayName = "Квадрицепс",
        aliases = setOf("передняя поверхность бедра", "четырёхглавая мышца бедра", "quadriceps", "quads"),
    ),
    HAMSTRINGS(
        code = "HAMSTRINGS",
        displayName = "Бицепс бедра",
        aliases = setOf("задняя поверхность бедра", "хамстринги", "hamstrings"),
    ),
    ADDUCTORS(
        code = "ADDUCTORS",
        displayName = "Приводящие мышцы бедра",
        aliases = setOf("приводящие", "внутренняя поверхность бедра", "adductors"),
    ),
    ABDUCTORS(
        code = "ABDUCTORS",
        displayName = "Отводящие мышцы бедра",
        aliases = setOf("отводящие", "внешняя поверхность бедра", "abductors"),
    ),
    CALVES(
        code = "CALVES",
        displayName = "Икры",
        aliases = setOf("икроножные", "икроножная мышца", "calves"),
    ),
    HIP_FLEXORS(
        code = "HIP_FLEXORS",
        displayName = "Сгибатели бедра",
        aliases = setOf("подвздошно-поясничная", "сгибатели тазобедренного сустава", "hip flexors"),
    );

    companion object {
        private val byCode = entries.associateBy { muscleGroup -> muscleGroup.code }

        fun fromCode(code: String): MuscleGroup? = byCode[code]
    }
}
