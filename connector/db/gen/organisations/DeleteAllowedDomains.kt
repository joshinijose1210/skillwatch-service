package organisations

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeleteAllowedDomainsParams(
  val organisationId: Long?
)

class DeleteAllowedDomainsParamSetter : ParamSetter<DeleteAllowedDomainsParams> {
  override fun map(ps: PreparedStatement, params: DeleteAllowedDomainsParams) {
    ps.setObject(1, params.organisationId)
  }
}

class DeleteAllowedDomainsCommand : Command<DeleteAllowedDomainsParams> {
  override val sql: String = """
      |DELETE FROM
      |organisation_domain_mapping odm
      |WHERE
      |odm.organisation_id = ?
      |;
      """.trimMargin()

  override val paramSetter: ParamSetter<DeleteAllowedDomainsParams> =
      DeleteAllowedDomainsParamSetter()
}
