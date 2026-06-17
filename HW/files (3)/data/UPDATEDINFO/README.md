This generates a repository folder and deployment Maven project.

## Terminology
1. Local repository: A location where Maven stores artifacts downloaded or transferred from a central repository.
2. Server central repository: A server that provides artifacts for Maven to download.
3. Directory central repository: A local directory on the machine that will perform the same function as a server
   central repository. This is not a local repository, and a local repository cannot be used as a directory central
   repository due to lacking certain metadata files.
4. Distribution repository: This describes the repository distributed with this project. It contains all the Maven
   artifacts, but it does not include the metadata that makes it either a local repository or a central repository as
   above. This is addressed by deploying the artifacts to a central repository and fetching them to a local repository
   in the course of a build.

## To Deploy to a Server Central Repository
The recipient can deploy these artifacts to their company's Maven server, e.g. JFrog Artifactory or Sonatype Nexus. This
allows all the company's users to have ready access to the artifacts needed to build Common Services applications.

The distribution contains a `deploy-pom.xml` file that is used to effect the deployment.

```console
mvn -f deploy-pom.xml -Durl=(repositoryUrl) -Drepository=(repository id) deploy
```

where `repository id` matches the id of the server defined in settings.xml with the correct deployment credentials.

## To Deploy to a Directory Central Repository
The recipient can also deploy to a directory on their own computer. This is useful to allow for local builds without
needing network connectivity.

If the user has access to the public Maven repositories to download the artifacts needed to deploy the server, they can
use the following command:

```console
mvn -f deploy-pom.xml -Durl=(repositoryUrl) deploy
```

repository url should be in form:

 > `file://(absolute path on your machine)`
e.g. `file:///tmp/mvnlocal/central_repository`

If the user is working on an isolated system without access to the public Maven repository, they can use the Docker
build image included in this distribution to bootstrap to a working repository. This can be accomplished via the following command:

```console
./install_csdk_artifact.sh local-maven-repository ~/.m2/central_repository
```

If the Docker images contained in the distribution are loaded directly from the `csdk-docker-{version}.tgz` file,
this will work without further specification to create a directory central repository in your home directory's .m2
subfolder. If the Docker images have been loaded to a company repository, a third argument is passed with the path to
the company's repository to locate the required image.

After the artifacts are deployed through either method, the file `~/.m2/settings.xml` should be generated similar to the following:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <!-- This is where artifacts are stored for builds, not where they are provided from. -->
  <localRepository>/tmp/maven/repository</localRepository>

  <servers>
    <server>
      <id>mvnlocal</id>
    </server>
  </servers>
  <mirrors>
    <mirror>
      <id>mvnlocal</id>
      <name>Local</name>
      <!-- This is where the artifacts are stored so that they can be provided for Maven builds,
           a directory central repository. -->
      <url>file:///path/to/directory/central/repository</url>
      <mirrorOf>external:*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

You can now build projects such as CS Java Example Code that use the cs-java-api.

## Installing the Java Example Code
To get a copy of the CS Java Example Code, perform the following command:

```console
./install_csdk_artifact.sh java-example-code
```

This will unzip the zip artifact into your current directory. 

If you do not have Maven or want to install from the provided `repository` directory in this directory, you may pass a Maven
repository or conformant directory structure:

```console
./install_csdk_artifact.sh --repo /path/to/repository java-example-code
```

### Notes on building the Java Example Code:

The `build` script provided in the root of the project will compile the Java Example Code using the provided build
container (see Docker image zip distribution), but will need some customization to be able to pull from your configured
Maven repository.

#### To build from a server central repository:
1. Change to the installed directory, e.g. `cd cs-example-3.1.4`.
2. Create a subdirectory .m2, `mkdir .m2`.
3. Copy in the settings file for your Maven repository. This settings file should include a `mirrors` tag similar to the above
   where all artifacts from this distribution have been deployed. If there is a local
   repository you wish to use on the command-line, it must be referred to with a `localRepository` tag *identical* to
   the following and mounted via the `-r` argument in the next section:
   
  ```xml
  <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <!-- This must be this exact value due to the -r flag mounting the host directory to this location. -->
    <localRepository>/opt/repository</localRepository>

    <servers>
      <server>
        <id>my-server</id>
        <!-- Deployment credentials for the server -->
        <username>username</username>
        <password>password</password>
      </server>
    </servers>
    <mirrors>
      <mirror>
        <id>my-server</id>
        <name>My-Server</name>
        <url>https://my-server-url/to/maven/repository</url>
        <mirrorOf>external:*</mirrorOf>
      </mirror>
    </mirrors>
  </settings>
  ```

Additional levels of snapshot and release servers may also be configured, see the Maven documentation.

4. The build will then be launched with the command `./build`. If you wish to use a local repository directory, you can use
   the form `./build -r /path/to/local/repository` and the above tag must be included in the settings.xml file created
   in the last step. This is because the -r flag will cause the provided path to be mounted to `/opt/repository` in the
   build container.

#### To build from a directory central repository:

1. Perform the same steps above, but the settings.xml file contents must be changed in the following way:
  ```xml
  <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <!-- This can be omitted to use the default of the .m2 directory, or it can be /tmp directory as shown -->
    <localRepository>/tmp/maven/repository</localRepository>

    <servers>
      <server>
        <id>mvnlocal</id>
      </server>
    </servers>
    <mirrors>
      <mirror>
        <id>mvnlocal</id>
        <!-- This must be the exact value given because the -r argument will be mounted to this location -->
        <url>file:///opt/repository</url>
        <mirrorOf>external:*</mirrorOf>
      </mirror>
    </mirrors>
  </settings>
  ```
2. Run the command `./build -r /path/to/directory/central/repository`. This will mount the given path to the
   `/opt/repository` directory in the container and the above settings will use it as a central repository for building
   the Java Example Code.
