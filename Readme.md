# Overview

There are 5 executable components of the projects

1. Initializer                  // creates shared keys for Authentication Server and other entities
2. AuthenticationServer
3. Client
4. MasterServer (not implemented yet)
5. DepartmentServer (not implemented yet)

# Configurations

For seamless integration of all components, proper configurations need to be provided.

TODO Write a complete list of configurations. For now please look into `data/staging` folder try to make sense

# Usages

First need to make sure that proper configuration file exists. I have added a set of configuration files
under `data/staging` folder. Next generate shared keys, then run authentication server and client.

## Generate keys

Using bash script

    $ bash run.sh init keys data/staging/auth_server/as.properties

Using maven

    $ mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.Initializer" -Dexec.args="keys data/staging/auth_server/as.properties"


## Run Authentication Server

Using bash script

    $ bash run.sh as data/staging/auth_server/

Using maven

    $ mvn exec:java '-Dexec.mainClass="edu.utdallas.netsec.sfts.as.AuthenticationServer"' '-Dexec.args="data/staging/auth_server/"'

## Run a Client

Using bash script

    $ bash run.sh client data/staging/client_bumblebee/

Using maven

    $ mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.client.Client" -Dexec.args="data/staging/client_bumblebee/"


## Run MasterServer

Using maven

     $ mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.master.MasterFileServer" -Dexec.args="data/staging/master_server/"
