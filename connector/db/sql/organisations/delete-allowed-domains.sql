DELETE FROM
organisation_domain_mapping odm
WHERE
odm.organisation_id = :organisationId
;