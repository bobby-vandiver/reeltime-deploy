package in.reeltime.tool.database;

import com.amazonaws.services.rds.model.DBInstance;

public class Database {

    private final DatabaseConfiguration configuration;
    private final DBInstance dbInstance;

    public Database(DatabaseConfiguration configuration, DBInstance dbInstance) {
        this.configuration = configuration;
        this.dbInstance = dbInstance;
    }

    public DatabaseConfiguration getConfiguration() {
        return configuration;
    }

    public DBInstance getDbInstance() {
        return dbInstance;
    }
}
