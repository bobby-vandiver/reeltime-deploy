package in.reeltime.deploy.database;

import com.amazonaws.services.rds.model.DBInstance;

public class Database {

    private final String databaseName;
    private final DBInstance dbInstance;

    public Database(String databaseName, DBInstance dbInstance) {
        this.databaseName = databaseName;
        this.dbInstance = dbInstance;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public DBInstance getDbInstance() {
        return dbInstance;
    }
}
