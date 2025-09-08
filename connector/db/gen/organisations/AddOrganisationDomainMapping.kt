package organisations

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddOrganisationDomainMappingParams(
  val organisationId: Long?,
  val allowedDomain: String?
)

class AddOrganisationDomainMappingParamSetter : ParamSetter<AddOrganisationDomainMappingParams> {
  override fun map(ps: PreparedStatement, params: AddOrganisationDomainMappingParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.allowedDomain)
  }
}

class AddOrganisationDomainMappingCommand : Command<AddOrganisationDomainMappingParams> {
  override val sql: String = """
      |INSERT INTO organisation_domain_mapping(
      |  organisation_id, allowed_domain
      |)
      |VALUES(
      |  ?,
      |  ?
      |) ;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddOrganisationDomainMappingParams> =
      AddOrganisationDomainMappingParamSetter()
}
