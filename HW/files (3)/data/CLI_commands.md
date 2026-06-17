./cs-cli-linux-amd64
Usage: cs-cli [OPTIONS] CATEGORY COMMAND [ARGS]
   Or: cs-cli [OPTIONS] COMMAND [ARGS]

Global options:
    --json
        Print some machine-readable output as JSON objects.
        Not all commands are affected.
    --verbose
        Print more output.

Categories:
    companion
        Commands for managing cs-cli's companion Docker container.
    csmf
        Common Services Message/Micro Format tools.
    csta
        CS Test Automation toolkit
    formats
        Miscellaneous file format tools.
    maven
        Maven repository commands.
    orca
        Commands for managing ORCA (Object Repository for Collaborative Applications),
        a distributed git repository for sharing CS documents and definitions.
    versions
        Commands for managing different versions of the cs-cli tool.

Commands:
    clean
        Removes all cached files created by cs-cli.
    upgrade
        Upgrade to the latest version of cs-cli.
    version
        Display version information
    xray
        Launch the Xray web server.



./cs-cli-linux-amd64 companion
Usage: cs-cli companion COMMAND [ARGS]

Commands:
    run
        Run a shell command in a temporary container.



./cs-cli-linux-amd64 csmf
Usage: cs-cli csmf COMMAND [ARGS]

Commands:
    analyze-artifacts
        Analyzes CSMF artifacts and prints a report in JSON format.
    codegen
        Generate code in target language(s) for a given set of CSMF artifacts.
    render-diagram
        Renders an SVG diagram of the given CSMF definition.
    validate
        Validate message XML documents against a given set of CSMF artifacts.


./cs-cli-linux-amd64 csmf analyze-artifacts
Usage: cs-cli csmf analyze-artifacts --artifacts [PATH]

Parameters:
    --artifacts
        List of CSMF artifacts to analyze.
        Each path must be a local directory.
        This command does not currently support .zip or Maven artifacts.


./cs-cli-linux-amd64 csmf codegen
Usage: cs-cli csmf codegen --artifacts [PATH]
   Or: cs-cli csmf codegen --orca-branch NAME
   Or: cs-cli csmf codegen --orca-commit HASH

Parameters:
    --artifacts
        List of CSMF artifacts for which to generate code.
        Each path can be a zip file, a directory, or a Maven artifact.
            See `cs-cli maven download` for Maven artifact syntax.
        Artifacts will be recursively searched for CSMF definition files
        (.mfd, .cm, and so on).
    --orca-branch
        An ORCA branch for which to generate code.
    --orca-commit
        An ORCA commit hash for which to generate code.
    --targets
        List of languages to generate code for.
        Supported languages: cpp, java, xsd



./cs-cli-linux-amd64 csmf render-diagram
Usage: cs-cli csmf render-diagram --orca-branch NAME --definitions [KEY]

Parameters:
    --orca-branch
        Name of ORCA branch from which to retrieve CSMF definitions.
    --definitions
        List of CSMF definitions for which to generate diagrams.
        Definition keys are in the form 'NAME.VERSION', e.g.
            urn:csmf:rfsensor:status.2.0
    --no-cache
        Output diagram files are normally cached based on the
        last-modified timestamps of the input definition files.
        This flag forces the output diagrams to always be rebuilt.


./cs-cli-linux-amd64 csmf validate
Usage: cs-cli csmf validate --artifacts [PATH] --messages [FILE]
   Or: cs-cli csmf validate --orca-branch NAME --messages [FILE]
   Or: cs-cli csmf validate --orca-commit HASH --messages [FILE]

Parameters:
    --artifacts
        List of CSMF artifacts against which to validate messages.
        Each path can be a zip file, a directory, or a Maven artifact.
            See `cs-cli maven download` for Maven artifact syntax.
        Artifacts will be recursively searched for CSMF definition files
        (.mfd, .cm, and so on).
    --messages
        List of message XML files to validate.
    --orca-branch
        An ORCA branch against which to validate message XML.
    --orca-commit
        An ORCA commit hash against which to validate message XML.



./cs-cli-linux-amd64 csta

EXEC: docker run --rm --interactive --platform linux/amd64 --network host --env 'CSTA_LAUNCHER_NAME=cs-cli csta' --workdir /csta_workspace/csdk-dist-2025-09 --user 894414806:894400513 --group-add 986 --volume /home/jhmcdo3/szcsdk:/csta_workspace --volume /home/jhmcdo3/.common-services:/cs_home --volume /var/run/docker.sock:/var/run/docker.sock --volume /tmp/cs-test-automation:/tmp/cs-test-automation --volume /home/jhmcdo3/.ssh:/home/jhmcdo3/.ssh:ro /cs/csta:latest

docker: invalid reference format.
See 'docker run --help'.
exit status 125


./cs-cli-linux-amd64 formats
Usage: cs-cli formats COMMAND [ARGS]

Commands:
    blue-print
        Display the header and extended header contents of a Midas BLUE file.


./cs-cli-linux-amd64 maven
Usage: cs-cli maven COMMAND [ARGS]

Commands:
    download
        Downloads files from configured Maven repositories.



./cs-cli-linux-amd64 orca
Usage: cs-cli orca COMMAND [ARGS]

Commands:
    add-artifact
        Adds a CSMF artifact to an ORCA branch.
    analyze
        Analyzes an ORCA branch and prints the result as JSON.
    clean
        Discards all changes to a branch of your local ORCA repository,
        including unstaged, staged, and untracked files.
    clone
        Clones the repository from a remote URL.
    copy-artifact
        Copies a CSMF artifact from one ORCA branch to another.
    create-branch
        Creates a new ORCA repository branch.
    delete-branch
        Deletes an ORCA repository branch.
    exec
        Executes a shell command in the repository directory.
    export-artifact
        Create a zip file of a CSMF artifact.
    export-branch
        Exports a branch of the local repository to an archive file.
    import-branch
        Imports a branch of the local repository from an archive file.



./cs-cli-linux-amd64 versions
Usage: cs-cli versions COMMAND [ARGS]

Commands:
    install
        Downloads and installs a specified version cs-cli.
    list-installed
        Displays the list of installed versions of cs-cli.
    list-remote
        Retrieves the list of available versions of cs-cli
        that can be downloaded and installed.
    select
        Updates the cs-cli symlink to activate a particular version of cs-cli.



./cs-cli-linux-amd64 versions install
Usage: cs-cli versions install VERSION [--platform NAME]

Parameters:
    --platform NAME
        Optional parameter to download a binary for a specific
        OS and CPU type, e.g. linux/amd64.