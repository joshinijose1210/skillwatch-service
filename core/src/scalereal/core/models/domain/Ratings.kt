package scalereal.core.models.domain

data class Ratings(
    val unsatisfactory: Int,
    val needsImprovement: Int,
    val meetsExpectations: Int,
    val exceedsExpectations: Int,
    val outstanding: Int,
)
