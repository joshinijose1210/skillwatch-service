package scalereal.core.models.domain

data class EmployeesType(
    val fullTime: EmployeesTypeData,
    val consultant: EmployeesTypeData,
)

data class EmployeesTypeData(
    val count: Int,
    val percentage: Double,
)
