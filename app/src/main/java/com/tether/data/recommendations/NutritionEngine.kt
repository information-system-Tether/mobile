package com.tether.data.recommendations

import com.tether.data.model.FoodItem
import com.tether.data.model.Recommendation
import com.tether.data.model.RecommendationCategory
import com.tether.data.model.UserProfile
import kotlin.math.roundToInt

object NutritionEngine {

    /**
     * Preloaded scientific knowledge base of fitness & nutrition recommendations
     */
    val recommendationsDatabase: List<Recommendation> = listOf(
        Recommendation(
            category = RecommendationCategory.NUTRITION,
            title = "Оптимум белка для восстановления",
            shortSummary = "1.6 - 2.2 г белка на кг веса тела максимализирует синтез мышечного протеина.",
            detailedExplanation = "Научные исследования (ISSN) подтверждают, что при силовых нагрузках и дефиците калорий организму требуется от 1.6 до 2.2 грамма белка на килограмм массы тела. Это защищает мышечную массу от катаболизма и обеспечивает насыщение благодаря высокому термическому эффекту пищи (TEF ~20-30%).",
            tag = "Белок"
        ),
        Recommendation(
            category = RecommendationCategory.NUTRITION,
            title = "Качественные жиры для гормональной системы",
            shortSummary = "Не опускайте жиры ниже 0.8–1.0 г на кг массы тела.",
            detailedExplanation = "Жиры необходимы для синтеза стероидных гормонов (тестостерона и эстрогенов), усвоения жирорастворимых витаминов (A, D, E, K) и здоровья клеточных мембран. Рекомендуется сочетать омега-3 жирные кислоты (жирная рыба, льняное масло) с мононенасыщенными жирами (оливковое масло, авокадо, орехи).",
            tag = "Гормоны & Жиры"
        ),
        Recommendation(
            category = RecommendationCategory.NUTRITION,
            title = "Углеводы как главное топливо для ЦНС",
            shortSummary = "Углеводы перед тренировкой повышают выносливость и силовые показатели.",
            detailedExplanation = "Сложные углеводы (овсянка, гречка, бурый рис) обеспечивают плавный уровень гликемии и наполняют гликогеновые депо в мышцах и печени. Прием порции углеводов за 1.5–2 часа до тренировки помогает тренироваться с максимальной отдачей.",
            tag = "Энергия"
        ),
        Recommendation(
            category = RecommendationCategory.HYDRATION,
            title = "Правило гидратации во время тренировок",
            shortSummary = "Пейте 150–250 мл воды каждые 15–20 минут физической активности.",
            detailedExplanation = "Потеря всего 2% жидкости от массы тела снижает физическую работоспособность и выносливость на 15–20%. Для контроля гидратации ориентируйтесь на светлый цвет мочи и добавляйте электролиты при интенсивном потоотделении на длительных маршрутах.",
            tag = "Вода"
        ),
        Recommendation(
            category = RecommendationCategory.WORKOUT,
            title = "Принцип прогрессивной перегрузки",
            shortSummary = "Рост мышц и силы требует регулярного увеличения стимула нагрузки.",
            detailedExplanation = "Чтобы прогрессировать, старайтесь на каждой тренировке добавить хотя бы 1 повторение, немного увеличить рабочий вес или улучшить чистоту техники. Записывайте рабочие веса в дневник тренировок Tether!",
            tag = "Сила & Гипертрофия"
        ),
        Recommendation(
            category = RecommendationCategory.RECOVERY,
            title = "Сон — главный анаболический фактор",
            shortSummary = "7–9 часов качественного сна обеспечивают пик выработки соматотропина.",
            detailedExplanation = "До 80% гормона роста (GH) вырабатывается во время фаз глубокого сна. Недостаток сна повышает уровень кортизола и гормона голода грелина, провоцируя срывы в диете и замедляя восстановление связок и суставов.",
            tag = "Восстановление"
        ),
        Recommendation(
            category = RecommendationCategory.WORKOUT,
            title = "10 000 шагов для здоровья сердца (NEAT)",
            shortSummary = "Внетренировочная активность сжигает больше калорий, чем часовая тренировка в зале.",
            detailedExplanation = "NEAT (Non-Exercise Activity Thermogenesis) — это калории, которые вы тратите при ходьбе, бытовых делах и подъеме по лестнице. Ежедневные 8 000–10 000 шагов снижают риск сердечно-сосудистых заболеваний на 50%.",
            tag = "Шаги & Кардио"
        )
    )

    /**
     * Dynamically generates personalized recommendations based on actual logged food and water vs goals
     */
    fun analyzeDailyIntake(
        profile: UserProfile,
        foods: List<FoodItem>,
        consumedWaterMl: Int,
        steps: Long
    ): List<Recommendation> {
        val totalCalories = foods.sumOf { it.totalCalories }
        val totalProtein = foods.sumOf { it.totalProtein.toDouble() }.toFloat()
        val totalFat = foods.sumOf { it.totalFat.toDouble() }.toFloat()
        val totalCarbs = foods.sumOf { it.totalCarbs.toDouble() }.toFloat()

        val results = mutableListOf<Recommendation>()

        // 1. Calorie Balance
        val calDiff = totalCalories - profile.targetCalories
        if (totalCalories == 0) {
            results.add(
                Recommendation(
                    category = RecommendationCategory.NUTRITION,
                    title = "Дневник ждет первой записи!",
                    shortSummary = "Ваша целевая суточная норма: ${profile.targetCalories} ккал.",
                    detailedExplanation = "Регулярный учет продуктов помогает осознанно подходить к питанию. Добавьте первый прием пищи, нажав '+' внизу экрана.",
                    tag = "Старт"
                )
            )
        } else if (calDiff > 250) {
            results.add(
                Recommendation(
                    category = RecommendationCategory.NUTRITION,
                    title = "Превышение суточного калоража на $calDiff ккал",
                    shortSummary = "Текущий прием: $totalCalories ккал из нормы ${profile.targetCalories} ккал.",
                    detailedExplanation = "Вы превысили дневную цель. Чтобы сбалансировать день, сделайте вечернюю прогулку (добавит +3000-4000 шагов и сожжет около 150-200 ккал) или выберите легкий белковый ужин из клетчатки и нежирного творога.",
                    tag = "Баланс калорий"
                )
            )
        } else if (calDiff < -500 && foods.isNotEmpty()) {
            results.add(
                Recommendation(
                    category = RecommendationCategory.NUTRITION,
                    title = "Слишком большой дефицит калорий",
                    shortSummary = "Осталось добрать ${-calDiff} ккал до нормы.",
                    detailedExplanation = "Слишком сильный дефицит замедляет метаболизм и приводит к потере мышечной массы. Добавьте полезный перекус (горсть орехов, банан, йогурт) для комфортного достижения цели.",
                    tag = "Дефицит"
                )
            )
        }

        // 2. Protein check
        val targetProtein = profile.targetProteinGrams
        if (totalProtein < targetProtein * 0.6f && foods.isNotEmpty()) {
            results.add(
                Recommendation(
                    category = RecommendationCategory.NUTRITION,
                    title = "Недостаток белка в рационе (${totalProtein.roundToInt()} г из $targetProtein г)",
                    shortSummary = "Белок необходим для мышц и сытости.",
                    detailedExplanation = "Вы набрали меньше 60% суточной нормы белка. Рекомендуем добавить куриное филе, яйца, тофу, тунец или протеиновый коктейль.",
                    tag = "Белок"
                )
            )
        }

        // 3. Hydration check
        val targetWater = profile.targetWaterMl
        if (consumedWaterMl < targetWater * 0.5f) {
            val remainingWater = targetWater - consumedWaterMl
            results.add(
                Recommendation(
                    category = RecommendationCategory.HYDRATION,
                    title = "Низкий уровень гидратации",
                    shortSummary = "Выпито $consumedWaterMl мл из $targetWater мл (осталось $remainingWater мл).",
                    detailedExplanation = "Организм нуждается в воде для поддержания метаболизма и нормальной работы почек. Держите бутылку воды рядом с рабочим местом и сделайте пару глотков прямо сейчас.",
                    tag = "Вода"
                )
            )
        }

        // 4. Step counter check
        if (steps >= 10000) {
            results.add(
                Recommendation(
                    category = RecommendationCategory.WORKOUT,
                    title = "Отличная активность! Норма в 10 000 шагов выполнена",
                    shortSummary = "Вы прошли $steps шагов за сегодня!",
                    detailedExplanation = "Высокий уровень внетренировочной активности отлично поддерживает сердечно-сосудистую систему и сжигает до 350-450 дополнительных килокалорий.",
                    tag = "Шаги"
                )
            )
        }

        // Add default science base if list is short
        results.addAll(recommendationsDatabase)
        return results
    }
}
