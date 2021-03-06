package in.reeltime.tool.deployment;

import in.reeltime.tool.access.Access;
import in.reeltime.tool.database.Database;
import in.reeltime.tool.external.ExternalConfiguration;
import in.reeltime.tool.network.Network;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.transcoder.Transcoder;

import java.io.File;

public class DeploymentConfiguration {

    private final boolean production;

    private final String environmentName;

    private final String applicationName;

    private final String applicationVersion;

    private final String hostedZoneDomainName;

    private final File war;

    private final Network network;

    private final Database database;

    private final Storage storage;

    private final Access access;

    private final Transcoder transcoder;

    private final ExternalConfiguration externalConfiguration;

    private DeploymentConfiguration(Builder builder) {
        this.production = builder.production;
        this.environmentName = builder.environmentName;
        this.applicationName = builder.applicationName;
        this.applicationVersion = builder.applicationVersion;
        this.hostedZoneDomainName = builder.hostedZoneDomainName;
        this.war = builder.war;
        this.network = builder.network;
        this.database = builder.database;
        this.storage = builder.storage;
        this.access = builder.access;
        this.transcoder = builder.transcoder;
        this.externalConfiguration = builder.externalConfiguration;
    }

    public boolean isProduction() {
        return production;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public String getHostedZoneDomainName() {
        return hostedZoneDomainName;
    }

    public File getWar() {
        return war;
    }

    public Network getNetwork() {
        return network;
    }

    public Database getDatabase() {
        return database;
    }

    public Storage getStorage() {
        return storage;
    }

    public Access getAccess() {
        return access;
    }

    public Transcoder getTranscoder() {
        return transcoder;
    }

    public ExternalConfiguration getExternalConfiguration() {
        return externalConfiguration;
    }

    public static class Builder {

        private boolean production;
        private String environmentName;
        private String applicationName;
        private String applicationVersion;
        private String hostedZoneDomainName;
        private File war;
        private Network network;
        private Database database;
        private Storage storage;
        private Access access;
        private Transcoder transcoder;
        private ExternalConfiguration externalConfiguration;

        Builder isProduction(boolean production) {
            this.production = production;
            return this;
        }

        Builder withEnvironmentName(String environmentName) {
            this.environmentName = environmentName;
            return this;
        }

        Builder withApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        Builder withApplicationVersion(String applicationVersion) {
            this.applicationVersion = applicationVersion;
            return this;
        }

        Builder withHostedZoneDomainName(String hostedZoneDomainName) {
            this.hostedZoneDomainName = hostedZoneDomainName;
            return this;
        }

        Builder withWar(File war) {
            this.war = war;
            return this;
        }

        Builder withNetwork(Network network) {
            this.network = network;
            return this;
        }

        Builder withDatabase(Database database) {
            this.database = database;
            return this;
        }

        Builder withStorage(Storage storage) {
            this.storage = storage;
            return this;
        }

        Builder withAccess(Access access) {
            this.access = access;
            return this;
        }

        Builder withTranscoder(Transcoder transcoder) {
            this.transcoder = transcoder;
            return this;
        }

        Builder withExternalConfiguration(ExternalConfiguration externalConfiguration) {
            this.externalConfiguration = externalConfiguration;
            return this;
        }

        DeploymentConfiguration build() {
            return new DeploymentConfiguration(this);
        }
    }
}
