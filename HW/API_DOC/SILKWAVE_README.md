# ICEROAD SILKWAVE

## Variable options
Used to configure this instance of Silkwave.
Once in the container, the template files can be found in /tmp/sw-templates.
Variables can be viewed at /opt/docker/sw-dockerenv.sh.

| Name | Default | Description |
|------|---------|-------------|
| SW_HUB_DOMAIN | test.appgeo.hub1 | hub domain name |
| SW_CALLBACK_HOST | ${HOSTNAME} | Hostname advertised to clients |
| CLASS_SW_BASE_CLASS | U | base.classification |
| CLASS_SW_BASE_OWNER | USA | base.owner |
| CLASS_SW_BASE_AUTHID | NA | base.authID |
| CLASS_SW_BASE_SCI | | base.sci |
| CLASS_SW_BASE_DISSEM | | base.dissem |
| CLASS_SW_BASE_REL | | base.rel |
| CLASS_SW_HUB_CLASS | NotSet | default.classification |
| CLASS_SW_HUB_OWNER | NotSet | default.owner |
| CLASS_SW_HUB_AUTHID | NA | default.authID |
| CLASS_SW_HUB_SCI | | default.sci |
| CLASS_SW_HUB_DISSEM | | default.dissem |
| CLASS_SW_HUB_REL | | default.rel |
| CLASS_SW_HUB_REASON | | default.classRsn |
| TCP_ENABLED | true | set to true or false |
| SSL_ENABLED | false | set to true or false |
| KS_PATH | /opt/server_keystore/silkwave.p12 | Server Keystore |
| KS_TYPE | PKCS12 | Keystore Type |
| KS_PASSWORD | silkwave | Keystore Password |
| KS_CERT_ALIAS | silkwave | Keystore Alias |
| TS_PATH | /opt/server_keystore/icKS.jks | Server Truststore |
| TS_TYPE | JKS | Truststore Type |
| TS_PASSWORD | silkwave | Truststore Password |
| SW_HTTPS_REQUIRE_AUTH | false | HTTPS Require Authentication |
| SW_HTTPS_TRUST_ALL | true | HTTPS Trust All |
| SW_STREAMS_PORT | 9552 | UDP stream port |
| SW_HTTP_FILES_PORT | 8443 | TCP file transfer port |
| SW_HTTPS_FILES_PORT | 8444 | SSL file transfer port |
| WEBSERVER_LOCAL_ONLY | false | set to true or false |
| WEBSERVER_HTTP_PORT | 8501 | set to valid TCP/IP port |
| WEBSERVER_HTTPS_PORT | 8601 | set to valid SSL/IP port |
| NAMESERVER | cs,ww,oio.appgeo,isrfabric | nameserver domains |
| SW_HUB_USER | client | Connection Manager user |
| SW_HUB_PASS | manager | Connection Manager password |
| SW_TCP_PORT | 61616 | SW Port when running TCP |
| SW_SSL_PORT | 61617 | SW Port when running SSL |
| STOMP_SSL_ENABLED | false | set to true or false |
| SW_STOMP_SSL_PORT | 61622 | SW Port when running STOMP SSL |
| STOMP_TCP_ENABLED | false | set to true or false |
| SW_STOMP_TCP_PORT | 61623 | SW Port when running STOMP TCP |
| MAX_NS_INACTIVITY | 120000 | Maximum inactivity time in MS before nameserver purges item |
| LOG_SW_LOGGER_LEV | INFO | Silkwave Log Level |
| LOG_XML_LOGGER_LEV | ALL | Silkwave xml log level |
| LOG_MAX_ROLL_NUM | 10 | Maximum number of rollover logs |
| SW_INIT_HEAP_SIZE_GB | 1 | Default JVM Memory in GB |
| SW_MAX_HEAP_SIZE_GB | 2 | Maximum JVM Memroy in GB |
| FILE_TEMP_DIR | /tmp | service.files.tmpdir for file service |
| FILE_EXPIRE_SECONDS | 300 | service.files.expire for file service |


## Config Information
- The template files being configured (activemq.xml, silkwave.properties, log4j2.xml) are located at /opt/silkwave/config

## Health Check
- Health check script can be found at /usr/local/bin/sw_health_check.sh
- The health check simply checks that the silkwave java process is running, it is running as a service, and listening on enabled/configured ports for TCP, SSL and STOMP.
- If all checks pass - it's running as healthy.
