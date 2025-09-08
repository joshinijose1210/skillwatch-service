package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class FetchSuperAdminEmailIdsParams

class FetchSuperAdminEmailIdsParamSetter : ParamSetter<FetchSuperAdminEmailIdsParams> {
  override fun map(ps: PreparedStatement, params: FetchSuperAdminEmailIdsParams) {
  }
}

data class FetchSuperAdminEmailIdsResult(
  val emailId: String
)

class FetchSuperAdminEmailIdsRowMapper : RowMapper<FetchSuperAdminEmailIdsResult> {
  override fun map(rs: ResultSet): FetchSuperAdminEmailIdsResult = FetchSuperAdminEmailIdsResult(
  emailId = rs.getObject("email_id") as kotlin.String)
}

class FetchSuperAdminEmailIdsQuery : Query<FetchSuperAdminEmailIdsParams,
    FetchSuperAdminEmailIdsResult> {
  override val sql: String = """
      |SELECT email_id
      |FROM users
      |WHERE is_super_admin = true ;
      |""".trimMargin()

  override val mapper: RowMapper<FetchSuperAdminEmailIdsResult> = FetchSuperAdminEmailIdsRowMapper()

  override val paramSetter: ParamSetter<FetchSuperAdminEmailIdsParams> =
      FetchSuperAdminEmailIdsParamSetter()
}
