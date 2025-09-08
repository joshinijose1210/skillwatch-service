package scalereal.core.models.domain

data class GendersData(
    val gendersCount: GendersCount,
    val gendersPercentage: GendersPercentage,
)

data class GendersPercentage(
    val malesPercentage: Double,
    val femalesPercentage: Double,
    val othersPercentage: Double,
)

data class GendersCount(
    val malesCount: Int,
    val femalesCount: Int,
    val othersCount: Int,
)
