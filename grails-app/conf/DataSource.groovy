// We use MongoDB for both test, dev and production environment.

environments {
    production {
        grails {
            mongo {
                databaseName = "sa"
                host = "localhost" // mongos
            }
        }
    }
    development {
        grails {
            mongo {
                databaseName = "sa"
                host = "localhost" // mongod
            }
        }
    }
    test {
        grails {
            mongo {
                databaseName = "test"
            }
        }
    }
}