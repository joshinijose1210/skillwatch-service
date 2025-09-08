package scalereal.core.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateReviewRequest(
    @field:Schema(description = "review to employee ID", example = "11")
    val reviewToId: Long,
    @field:Schema(description = "review from employee ID", example = "98")
    val reviewFromId: Long,
    @field:Schema(description = "organisation ID", example = "134")
    val organisationId: Long,
    @field:Schema(description = "review cycle ID", example = "212")
    val reviewCycleId: Long,
    @field:Schema(description = "review type ID", example = "2")
    val reviewTypeId: Int,
    @field:Schema(description = "review ID", example = "143")
    val reviewId: Long,
    @field:Schema(description = "review data", example = "Review data string")
    val review: String?,
    @field:Schema(description = "rating", example = "4")
    val rating: Int?,
)
