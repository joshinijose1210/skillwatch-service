SELECT id
    FROM modules
    WHERE UPPER(name) = UPPER(:moduleName) ;
