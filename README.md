ReelTime: Deployment Tool
=========================

Deployment tool for [ReelTime](https://github.com/bobby-vandiver/reeltime).

This is a simple CLI tool that is intended to be building block in a continuous deployment pipeline.

This project is provided as-is.

Usage
-----

```shell
usage: reeltime-deploy
    --account-id <arg>                The AWS Account ID.
    --application-name <arg>          The name of the application.
    --application-version <arg>       The version of the application.
    --aws-access-key <arg>            The AWS access key.
    --aws-secret-key <arg>            The AWS secret key.
    --certificate-domain-name <arg>   The domain name of the certificate
                                      to use.
    --environment-name <arg>          The name of the environment.
    --hosted-zone-domain-name <arg>   The domain name of the hosted zone
                                      to use.
    --mailgun-api-key <arg>           The Mailgun API key.
    --production                      Flag to enable additional
                                      configuration for production
                                      environment.
    --remove-resources                Flag to force removal of existing
                                      resources.
    --war <arg>                       The file path to the war to deploy.

```