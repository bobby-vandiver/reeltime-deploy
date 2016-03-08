package in.reeltime.tool.deployment;

import in.reeltime.tool.access.Access;
import in.reeltime.tool.access.AccessService;
import in.reeltime.tool.access.role.RolePolicyParameters;
import in.reeltime.tool.beanstalk.BeanstalkService;
import in.reeltime.tool.database.Database;
import in.reeltime.tool.database.DatabaseService;
import in.reeltime.tool.log.Logger;
import in.reeltime.tool.network.Network;
import in.reeltime.tool.network.NetworkService;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.storage.StorageService;
import in.reeltime.tool.transcoder.Transcoder;
import in.reeltime.tool.transcoder.TranscoderService;

import java.io.File;
import java.io.FileNotFoundException;

public class DeploymentService {

    private final NetworkService networkService;

    private final DatabaseService databaseService;

    private final StorageService storageService;

    private final AccessService accessService;

    private final TranscoderService transcoderService;

    private final BeanstalkService beanstalkService;

    public DeploymentService(NetworkService networkService, DatabaseService databaseService,
                             StorageService storageService, AccessService accessService,
                             TranscoderService transcoderService, BeanstalkService beanstalkService) {
        this.networkService = networkService;
        this.databaseService = databaseService;
        this.storageService = storageService;
        this.accessService = accessService;
        this.transcoderService = transcoderService;
        this.beanstalkService = beanstalkService;
    }

    public void deploy(String accountId, String environmentName, String applicationName, String applicationVersion,
                       File war, boolean production, boolean removeResources) throws FileNotFoundException {
        if (!production) {
            Logger.info("Tearing down transcoder and database so they are recreated");
            transcoderService.tearDownTranscoder();
            databaseService.tearDownDatabase();
        }

        Network network = networkService.setupNetwork();
        Database database = databaseService.setupDatabase(network);

        Storage storage = storageService.setupStorage();
        String transcoderTopicName = transcoderService.getTranscoderTopicName();

        RolePolicyParameters rolePolicyParameters = new RolePolicyParameters(
                accountId,
                storage.getMasterVideosBucket(),
                storage.getThumbnailsBucket(),
                storage.getPlaylistsAndSegmentsBucket(),
                transcoderTopicName);

        Access access = accessService.setupAccess(rolePolicyParameters);
        Transcoder transcoder = transcoderService.setupTranscoder(storage, access);

        DeploymentConfiguration configuration = new DeploymentConfiguration.Builder()
                .isProduction(production)
                .withAccess(access)
                .withApplicationName(applicationName)
                .withApplicationVersion(applicationVersion)
                .withDatabase(database)
                .withEnvironmentName(environmentName)
                .withNetwork(network)
                .withStorage(storage)
                .withTranscoder(transcoder)
                .withWar(war)
                .build();

        beanstalkService.deploy(configuration);
    }
}
