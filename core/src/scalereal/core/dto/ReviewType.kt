package scalereal.core.dto

enum class ReviewType(
    val id: Int,
) {
    SELF(1),
    MANAGER(2),
    CHECK_IN(3),
}
