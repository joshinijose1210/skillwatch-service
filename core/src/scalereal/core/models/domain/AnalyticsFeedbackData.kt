package scalereal.core.models.domain

data class AnalyticsFeedbackCount(
    val positive: Long,
    val improvement: Long,
    val appreciation: Long,
)

data class AnalyticsFeedbackPercentage(
    val positive: Double,
    val improvement: Double,
    val appreciation: Double,
)

data class AnalyticsFeedbackResponse(
    val analyticsFeedbackCount: AnalyticsFeedbackCount,
    val analyticsFeedbackPercentage: AnalyticsFeedbackPercentage,
)
