SELECT
  odm.sr_no, odm.allowed_domain
FROM
  organisation_domain_mapping odm
WHERE
  odm.organisation_id = :organisationId
ORDER BY odm.sr_no ;
