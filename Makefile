create-db:
	psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'sample'" | grep -q 1 || psql -U postgres -c "CREATE DATABASE sample"

drop-db:
	psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'sample'" | grep -q 1 && psql -U postgres -c "DROP DATABASE sample"

reset-db: drop-db create-db

install-norm-codegen:
	mkdir -p ".gradle"
	curl -L https://github.com/medly/norm/releases/download/v0.0.5/norm-codegen.zip --output ./.gradle/norm-codegen.zip
	unzip -o ./.gradle/norm-codegen.zip -d ./.gradle && rm ./.gradle/norm-codegen.zip
	chmod +x .gradle/norm-codegen/bin/norm-codegen

norm-codegen:
	@git diff --cached --name-only -- "connector/db/sql/**.sql" | xargs .gradle/norm-codegen/bin/norm-codegen -b connector/db/sql -o connector/db/gen -j "jdbc:postgresql://localhost/sample" -u "postgres" -p "password"
