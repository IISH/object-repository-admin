import com.mongodb.ReadPreference

// We use MongoDB for both test, dev and production environment.
def _readPreference = (System.getProperty("layout", "not") == 'disseminate') ? ReadPreference.SECONDARY : ReadPreference.PRIMARY
println("_readPreference=" + _readPreference)

environments {
    production {
        dataSource {
            databaseName = "sa"
            host = "localhost" // mongos
            options {
                            readPreference = _readPreference
                        }
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