package user

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsSuperAdminUserParams(
  val emailId: String?
)

class IsSuperAdminUserParamSetter : ParamSetter<IsSuperAdminUserParams> {
  override fun map(ps: PreparedStatement, params: IsSuperAdminUserParams) {
    ps.setObject(1, params.emailId)
  }
}

data class IsSuperAdminUserResult(
  val exists: Boolean?
)

class IsSuperAdminUserRowMapper : RowMapper<IsSuperAdminUserResult> {
  override fun map(rs: ResultSet): IsSuperAdminUserResult = IsSuperAdminUserResult(
  exists = rs.getObject("exists") as kotlin.Boolean?)
}

class IsSuperAdminUserQuery : Query<IsSuperAdminUserParams, IsSuperAdminUserResult> {
  override val sql: String = """
      |SELECT EXISTS (
      |  SELECT 1 FROM users WHERE
      |      LOWER(email_id) = LOWER(?)
      |      AND is_super_admin = true
      |) ;
      """.trimMargin()

  override val mapper: RowMapper<IsSuperAdminUserResult> = IsSuperAdminUserRowMapper()

  override val paramSetter: ParamSetter<IsSuperAdminUserParams> = IsSuperAdminUserParamSetter()
}
