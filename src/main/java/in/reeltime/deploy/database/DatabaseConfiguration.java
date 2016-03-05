package in.reeltime.deploy.database;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.google.common.collect.Lists;

import java.util.List;

public class DatabaseConfiguration {

    private final String dbName;

    private final String dbInstanceClass;
    private final String dbInstanceIdentifier;

    private final DBSubnetGroup dbSubnetGroup;

    private final String engine;
    private final String engineVersion;

    private final String masterUsername;
    private final String masterPassword;

    private final SecurityGroup securityGroup;

    private DatabaseConfiguration(Builder builder) {
        this.dbName = builder.dbName;
        this.dbInstanceClass = builder.dbInstanceClass;
        this.dbInstanceIdentifier = builder.dbInstanceIdentifier;
        this.dbSubnetGroup = builder.dbSubnetGroup;
        this.engine = builder.engine;
        this.engineVersion = builder.engineVersion;
        this.masterUsername = builder.masterUsername;
        this.masterPassword = builder.masterPassword;
        this.securityGroup = builder.securityGroup;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public DBSubnetGroup getDbSubnetGroup() {
        return dbSubnetGroup;
    }

    public String getEngine() {
        return engine;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public String getMasterUsername() {
        return masterUsername;
    }

    public String getMasterPassword() {
        return masterPassword;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    public static class Builder {
        private String dbName;

        private String dbInstanceClass;
        private String dbInstanceIdentifier;

        private DBSubnetGroup dbSubnetGroup;

        private String engine;
        private String engineVersion;

        private String masterUsername;
        private String masterPassword;

        private SecurityGroup securityGroup;

        public Builder withDBName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public Builder withDBInstanceClass(String dbInstanceClass) {
            this.dbInstanceClass = dbInstanceClass;
            return this;
        }

        public Builder withDBInstanceIdentifier(String dbInstanceIdentifier) {
            this.dbInstanceIdentifier = dbInstanceIdentifier;
            return this;
        }

        public Builder withDBSubnetGroup(DBSubnetGroup subnetGroup) {
            this.dbSubnetGroup = subnetGroup;
            return this;
        }

        public Builder withEngine(String engine, String engineVersion) {
            this.engine = engine;
            this.engineVersion = engineVersion;
            return this;
        }

        public Builder withCredentials(String masterUsername, String masterPassword) {
            this.masterUsername = masterUsername;
            this.masterPassword = masterPassword;
            return this;
        }

        public Builder withSecurityGroup(SecurityGroup securityGroup) {
            this.securityGroup = securityGroup;
            return this;
        }

        public DatabaseConfiguration build() {
            return new DatabaseConfiguration(this);
        }
    }
}
