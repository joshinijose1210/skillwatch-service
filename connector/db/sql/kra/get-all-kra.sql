SELECT k1.id, k1.sr_no, k1.name, k1.weightage, k1.version_number, k1.organisation_id
FROM kra k1
JOIN (
    -- Subquery to get the latest version number for each KRA within the organisation
    SELECT sr_no, MAX(version_number) AS latest_version
    FROM kra
    WHERE organisation_id = :organisationId
    GROUP BY sr_no
) k2
ON k1.sr_no = k2.sr_no
AND k1.version_number = k2.latest_version
WHERE k1.organisation_id = :organisationId
ORDER BY k1.sr_no;
