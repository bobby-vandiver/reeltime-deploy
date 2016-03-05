package in.reeltime.deploy.database;

import com.amazonaws.services.rds.model.DBInstance;

public class Database {

    private final DatabaseConfiguration configuration;
    private final DBInstance dbInstance;

    public Database(DatabaseConfiguration configuration, DBInstance dbInstance) {
        this.configuration = configuration;
        this.dbInstance = dbInstance;
    }

    public String getDatabaseName() {
        return configuration.getDbName();
    }

    public DBInstance getDbInstance() {
        return dbInstance;
    }
}
