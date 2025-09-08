SELECT kra.id, kra.weightage FROM kra
    WHERE id = :kraId AND
    organisation_id = :organisationId ;
