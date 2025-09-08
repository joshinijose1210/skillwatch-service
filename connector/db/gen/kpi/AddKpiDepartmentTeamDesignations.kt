package kpi

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddKpiDepartmentTeamDesignationsParams(
  val kpiId: Long?,
  val departmentId: Long?,
  val teamId: Long?,
  val designationId: Long?
)

class AddKpiDepartmentTeamDesignationsParamSetter :
    ParamSetter<AddKpiDepartmentTeamDesignationsParams> {
  override fun map(ps: PreparedStatement, params: AddKpiDepartmentTeamDesignationsParams) {
    ps.setObject(1, params.kpiId)
    ps.setObject(2, params.departmentId)
    ps.setObject(3, params.teamId)
    ps.setObject(4, params.designationId)
  }
}

class AddKpiDepartmentTeamDesignationsCommand : Command<AddKpiDepartmentTeamDesignationsParams> {
  override val sql: String = """
      |INSERT INTO kpi_department_team_designation_mapping(kpi_id, department_id, team_id, designation_id)
      |VALUES( ?, ?, ?, ?);
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddKpiDepartmentTeamDesignationsParams> =
      AddKpiDepartmentTeamDesignationsParamSetter()
}
