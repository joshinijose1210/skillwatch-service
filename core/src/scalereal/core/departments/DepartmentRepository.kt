package scalereal.core.departments

import scalereal.core.models.domain.Department
import scalereal.core.models.domain.DepartmentStatus

interface DepartmentRepository {
    fun create(
        organisationId: Long,
        id: Long,
        departmentName: String,
        departmentStatus: Boolean,
    ): Long

    fun isDepartmentExists(
        organisationId: Long,
        departmentName: String,
    ): DepartmentStatus

    fun getMaxDepartmentId(organisationId: Long): Long

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
    ): List<Department>

    fun departmentCount(
        organisationId: Long,
        searchText: String,
    ): Int

    fun update(
        organisationId: Long,
        departmentId: Long,
        departmentName: String,
        departmentStatus: Boolean,
    )

    fun getDepartmentDataById(
        departmentId: Long,
        organisationId: Long,
    ): Department

    fun getDepartmentId(
        organisationId: Long,
        departmentName: String,
    ): Long
}
