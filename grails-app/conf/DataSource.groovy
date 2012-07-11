// We use MongoDB for both test, dev and production environment.

environments {
    production {
        dataSource {
            databaseName = "sa"
            host = "localhost" // mongos
        }
    }
    development {
        dataSource {
            databaseName = "sa"
            host = "localhost" // mongod
        }
    }
    test {
        dataSource {
            databaseName = "test"
        }
    }
}