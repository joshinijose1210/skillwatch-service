package scalereal.core.models.domain

data class ReviewCount(
    val completed: Long,
    val inProgress: Long,
    val pending: Long,
)

data class ReviewStatus(
    val self: ReviewCount,
    val manager1: ReviewCount,
    val manager2: ReviewCount,
    val checkIn: ReviewCount,
)
