WITH subquery AS (
    SELECT d.status
    FROM designations AS d
    JOIN team_designation_mapping tdm ON d.id = tdm.designation_id
    WHERE
    d.organisation_id = :organisationId
    AND LOWER(d.designation_name) = LOWER(:designationName)
    AND tdm.team_id = :teamId
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);