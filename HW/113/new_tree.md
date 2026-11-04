.
├── build.gradle.kts
├── buildSrc
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src
│       ├── main
│       │   └── kotlin
│       │       ├── gov
│       │       │   └── ic
│       │       │       └── silkwave
│       │       │           └── gradle
│       │       │               ├── CleanBuildFormatting.kt
│       │       │               └── FileChecksumTasks.kt
│       │       └── silkwave-conventions.gradle.kts
│       └── test
│           └── java
│               └── gov
│                   └── ic
│                       └── silkwave
│                           └── gradle
│                               ├── CleanBuildFormattingTest.java
│                               └── FileChecksumTasksTest.java
├── CHANGELOG.md
├── core
│   ├── activemq.plugin
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── gov
│   │                   └── ic
│   │                       └── silkwave
│   │                           └── activemq
│   │                               ├── destination
│   │                               │   ├── Destination.java
│   │                               │   ├── DestManagement.java
│   │                               │   ├── DestManagementMBean.java
│   │                               │   └── InputDestinationWrapper.java
│   │                               └── interceptor
│   │                                   ├── SilkwaveBrokerFilter.java
│   │                                   └── SilkwavePlugin.java
│   ├── core
│   │   ├── build.gradle.kts
│   │   ├── ClientA.info
│   │   ├── ClientB.info
│   │   ├── gradle.properties
│   │   ├── instances.properties
│   │   ├── resources
│   │   │   ├── activemq.xml
│   │   │   ├── dns.properties
│   │   │   ├── groups.properties
│   │   │   ├── init.d
│   │   │   │   └── silkwave
│   │   │   ├── jmx.yaml
│   │   │   ├── log4j2.xml
│   │   │   ├── login.config
│   │   │   ├── permissions.xml
│   │   │   ├── restrictions.xml
│   │   │   ├── samples
│   │   │   │   ├── activemq.xml.template
│   │   │   │   ├── client.crt
│   │   │   │   ├── client.csr
│   │   │   │   ├── client.key
│   │   │   │   ├── client.keystore
│   │   │   │   ├── client.p12
│   │   │   │   ├── client.pem
│   │   │   │   ├── client.truststore
│   │   │   │   ├── legacy
│   │   │   │   │   ├── client.crt
│   │   │   │   │   ├── client.p12
│   │   │   │   │   ├── client.pem
│   │   │   │   │   ├── rootCA.pem
│   │   │   │   │   ├── server.crt
│   │   │   │   │   └── server.p12
│   │   │   │   ├── log4j2.xml.template
│   │   │   │   ├── permissions.xml
│   │   │   │   ├── renew_ca.conf
│   │   │   │   ├── renew_ca.csr
│   │   │   │   ├── rootCA.key
│   │   │   │   ├── rootCA.pem
│   │   │   │   ├── rootCA.srl
│   │   │   │   ├── server.crt
│   │   │   │   ├── server.csr
│   │   │   │   ├── server.key
│   │   │   │   ├── server.keystore
│   │   │   │   ├── server.p12
│   │   │   │   ├── server.truststore
│   │   │   │   └── silkwave.properties.template
│   │   │   ├── systemd
│   │   │   │   └── silkwave.service
│   │   │   └── users.properties
│   │   ├── rpmbuild
│   │   │   └── SPECS
│   │   │       └── core.spec
│   │   ├── silkwave.sh
│   │   ├── src
│   │   │   ├── main
│   │   │   │   └── java
│   │   │   │       └── gov
│   │   │   │           └── ic
│   │   │   │               └── silkwave
│   │   │   │                   ├── AppVersion.java
│   │   │   │                   ├── async
│   │   │   │                   │   ├── AsyncProcessingContext.java
│   │   │   │                   │   ├── AsyncProcessingListener.java
│   │   │   │                   │   ├── AsyncProcessingTimeoutManager.java
│   │   │   │                   │   ├── AsyncRequestCallback.java
│   │   │   │                   │   └── AsyncRequestManager.java
│   │   │   │                   ├── authentication
│   │   │   │                   │   ├── AuthenticationCore.java
│   │   │   │                   │   ├── AuthenticationRegistry.java
│   │   │   │                   │   ├── AuthenticationResult.java
│   │   │   │                   │   ├── AuthenticationService.java
│   │   │   │                   │   ├── AuthenticationWarnings.java
│   │   │   │                   │   ├── Authenticator.java
│   │   │   │                   │   └── ExtendedAuthenticationService.java
│   │   │   │                   ├── authorization
│   │   │   │                   │   ├── AuthorizationCore.java
│   │   │   │                   │   └── AuthorizationService.java
│   │   │   │                   ├── BaseMonitorServlet.java
│   │   │   │                   ├── data
│   │   │   │                   │   ├── AbstractDataDistributionService.java
│   │   │   │                   │   ├── CoreRoutingURI.java
│   │   │   │                   │   ├── files
│   │   │   │                   │   │   ├── ChunkContentProvider.java
│   │   │   │                   │   │   ├── FileAdminServlet.java
│   │   │   │                   │   │   ├── FileData.java
│   │   │   │                   │   │   ├── FileDestination.java
│   │   │   │                   │   │   ├── FileProgress.java
│   │   │   │                   │   │   ├── FileServiceContext.java
│   │   │   │                   │   │   ├── FileService.java
│   │   │   │                   │   │   ├── FileSource.java
│   │   │   │                   │   │   ├── FileStateEnum.java
│   │   │   │                   │   │   ├── FileState.java
│   │   │   │                   │   │   ├── HasFileProgress.java
│   │   │   │                   │   │   ├── InvalidFileState.java
│   │   │   │                   │   │   ├── SendToNextHopRunnable.java
│   │   │   │                   │   │   ├── TrackedOutputStream.java
│   │   │   │                   │   │   └── TransferMetadata.java
│   │   │   │                   │   ├── handlers
│   │   │   │                   │   │   ├── AbstractFileHandler.java
│   │   │   │                   │   │   ├── DestinationFileTransferResponseHandler.java
│   │   │   │                   │   │   ├── FailureNotifHandler.java
│   │   │   │                   │   │   ├── FileDetailInfoResponseHandler.java
│   │   │   │                   │   │   ├── FileInfoResponseHandler.java
│   │   │   │                   │   │   ├── FileReceivedHandler.java
│   │   │   │                   │   │   ├── FileTransferCancelRequestHandler.java
│   │   │   │                   │   │   ├── FileTransferRequestHandler.java
│   │   │   │                   │   │   ├── HandlerOperations.java
│   │   │   │                   │   │   ├── InternalFileTransferRequestHandler.java
│   │   │   │                   │   │   ├── InternalFileTransferResponseHandler.java
│   │   │   │                   │   │   ├── MessageContext.java
│   │   │   │                   │   │   ├── MessageHandler.java
│   │   │   │                   │   │   ├── RequestCancelDestHandler.java
│   │   │   │                   │   │   ├── RequestCancelSourceHandler.java
│   │   │   │                   │   │   ├── RequestDestinationHandler.java
│   │   │   │                   │   │   ├── RequestSourceHandler.java
│   │   │   │                   │   │   └── SourceFileTransferResponseHandler.java
│   │   │   │                   │   ├── SimpleFileServer.java
│   │   │   │                   │   ├── SourceRoutingURI.java
│   │   │   │                   │   ├── streams
│   │   │   │                   │   │   ├── ConcurrentSoftMultiValueRegistry.java
│   │   │   │                   │   │   ├── ContextPacket.java
│   │   │   │                   │   │   ├── DestinationPair.java
│   │   │   │                   │   │   ├── DestinationStateEnum.java
│   │   │   │                   │   │   ├── DestinationState.java
│   │   │   │                   │   │   ├── HeartBeatTracker.java
│   │   │   │                   │   │   ├── InvalidIdException.java
│   │   │   │                   │   │   ├── KeyCallbackPredicate.java
│   │   │   │                   │   │   ├── KeyedWeakReference.java
│   │   │   │                   │   │   ├── Key.java
│   │   │   │                   │   │   ├── KeyStreamHandlerPredicate.java
│   │   │   │                   │   │   ├── LocalDestination.java
│   │   │   │                   │   │   ├── MultiValueRegistry.java
│   │   │   │                   │   │   ├── NotFoundStream.java
│   │   │   │                   │   │   ├── ProxyHeartbeatTracker.java
│   │   │   │                   │   │   ├── Request.java
│   │   │   │                   │   │   ├── StoredHeaders.java
│   │   │   │                   │   │   ├── StreamAdminServlet.java
│   │   │   │                   │   │   ├── StreamCallback.java
│   │   │   │                   │   │   ├── StreamHandler.java
│   │   │   │                   │   │   ├── StreamHandlerState.java
│   │   │   │                   │   │   ├── StreamService.java
│   │   │   │                   │   │   ├── StreamStateEnum.java
│   │   │   │                   │   │   ├── StreamState.java
│   │   │   │                   │   │   ├── UniqueId.java
│   │   │   │                   │   │   └── UniqueIdKey.java
│   │   │   │                   │   ├── URIs
│   │   │   │                   │   │   └── DestinationRoutingURI.java
│   │   │   │                   │   ├── URIServiceBuilder.java
│   │   │   │                   │   └── URIValidationException.java
│   │   │   │                   ├── DeadLetterManager.java
│   │   │   │                   ├── DeadLetterTypeEnum.java
│   │   │   │                   ├── discovery
│   │   │   │                   │   ├── DiscoveryEntry.java
│   │   │   │                   │   ├── DiscoveryListener.java
│   │   │   │                   │   ├── DiscoveryServiceAdminServlet.java
│   │   │   │                   │   ├── DiscoveryService.java
│   │   │   │                   │   ├── DiscoveryTask.java
│   │   │   │                   │   ├── record
│   │   │   │                   │   │   ├── DiscoveryServiceAdminServletResponseRecord.java
│   │   │   │                   │   │   └── DiscoveryServiceAdminServletRowRecord.java
│   │   │   │                   │   └── RemoteDiscoveryService.java
│   │   │   │                   ├── fileserver
│   │   │   │                   │   └── FileServer.java
│   │   │   │                   ├── JsonMapper.java
│   │   │   │                   ├── naming
│   │   │   │                   │   ├── NameResolution.java
│   │   │   │                   │   ├── NameResolver.java
│   │   │   │                   │   ├── NamingAdminServlet.java
│   │   │   │                   │   ├── NamingRegistry.java
│   │   │   │                   │   └── record
│   │   │   │                   │       └── NamingAdminServletNameCachesResponseRecord.java
│   │   │   │                   ├── networkinfo
│   │   │   │                   │   ├── handlers
│   │   │   │                   │   │   ├── DiagnosticsRequestHandler.java
│   │   │   │                   │   │   └── util
│   │   │   │                   │   │       ├── Jar.java
│   │   │   │                   │   │       └── OS.java
│   │   │   │                   │   └── NetworkInfoService.java
│   │   │   │                   ├── NetworkManager.java
│   │   │   │                   ├── networkstatus
│   │   │   │                   │   ├── NetworkStatusAdminServlet.java
│   │   │   │                   │   ├── NetworkStatusService.java
│   │   │   │                   │   ├── ScheduledOwner.java
│   │   │   │                   │   └── ScheduledStatus.java
│   │   │   │                   ├── policy
│   │   │   │                   │   ├── AuthenticationCheck.java
│   │   │   │                   │   ├── AuthenticationServiceEnum.java
│   │   │   │                   │   ├── AuthenticationServiceFactory.java
│   │   │   │                   │   ├── AuthenticationService.java
│   │   │   │                   │   ├── MockAuthenticationService.java
│   │   │   │                   │   └── RecipientAuthorizationCheck.java
│   │   │   │                   ├── record
│   │   │   │                   │   ├── GenericAdminServletResponseRecord.java
│   │   │   │                   │   └── GenericAdminServletRowRecord.java
│   │   │   │                   ├── registration
│   │   │   │                   │   ├── LocalRegistryCallback.java
│   │   │   │                   │   ├── LocalRegistry.java
│   │   │   │                   │   ├── LocalRoute.java
│   │   │   │                   │   ├── QueuesAdminServlet.java
│   │   │   │                   │   ├── record
│   │   │   │                   │   │   ├── RegistrationAdminServletConnectionInfoResponseRecord.java
│   │   │   │                   │   │   ├── RegistrationAdminServletConnectionInfoRowRecord.java
│   │   │   │                   │   │   ├── RegistrationAdminServletDestinationInfoResponseRecord.java
│   │   │   │                   │   │   ├── RegistrationAdminServletRegistrationInfoResponseRecord.java
│   │   │   │                   │   │   └── RegistrationAdminServletRegistrationInfoRowRecord.java
│   │   │   │                   │   ├── Registrar.java
│   │   │   │                   │   └── RegistrationAdminServlet.java
│   │   │   │                   ├── routing
│   │   │   │                   │   ├── EndpointCost.java
│   │   │   │                   │   ├── Letter.java
│   │   │   │                   │   ├── Neighbor.java
│   │   │   │                   │   ├── Route.java
│   │   │   │                   │   ├── RouteOption.java
│   │   │   │                   │   ├── RouteTables.java
│   │   │   │                   │   ├── RoutingAdminServlet.java
│   │   │   │                   │   ├── RoutingMessage.java
│   │   │   │                   │   ├── RoutingService.java
│   │   │   │                   │   └── TableUpdate.java
│   │   │   │                   ├── security
│   │   │   │                   │   ├── Authorizer.java
│   │   │   │                   │   ├── cache
│   │   │   │                   │   │   ├── AuthenticationCache.java
│   │   │   │                   │   │   ├── AuthorizationCache.java
│   │   │   │                   │   │   ├── AuthorizationServices.java
│   │   │   │                   │   │   ├── AuthorizedCacheObject.java
│   │   │   │                   │   │   ├── ExpiredAuthentication.java
│   │   │   │                   │   │   └── Restrictions.java
│   │   │   │                   │   ├── handler
│   │   │   │                   │   │   ├── AuthenticateResponseHandler.java
│   │   │   │                   │   │   ├── AuthorizeResponseHandler.java
│   │   │   │                   │   │   ├── AuthorizerInfoResponseResponseHandler.java
│   │   │   │                   │   │   ├── BaseSecurityHandler.java
│   │   │   │                   │   │   ├── FailureNotificationHandler.java
│   │   │   │                   │   │   ├── MsgHandle.java
│   │   │   │                   │   │   ├── NetworkStatusHandler.java
│   │   │   │                   │   │   ├── PurgeAuthenticationRequestHandler.java
│   │   │   │                   │   │   ├── PurgeAuthorizationCacheRequestHandler.java
│   │   │   │                   │   │   ├── ResourceRegistrationResponseHandler.java
│   │   │   │                   │   │   ├── SecurityAuthorizationRequestHandler.java
│   │   │   │                   │   │   ├── SecurityAuthorizationResponseHandler.java
│   │   │   │                   │   │   └── UpdateSecurityCredentialsHandler.java
│   │   │   │                   │   ├── requests
│   │   │   │                   │   │   ├── AuthenticatedRequest.java
│   │   │   │                   │   │   ├── AuthorizedRequest.java
│   │   │   │                   │   │   ├── RegisterRequest.java
│   │   │   │                   │   │   ├── RemoteAuthorizationRequest.java
│   │   │   │                   │   │   └── Request.java
│   │   │   │                   │   ├── responses
│   │   │   │                   │   │   ├── AuthenticatedResponse.java
│   │   │   │                   │   │   └── AuthorizedResponse.java
│   │   │   │                   │   ├── RestrictionManager.java
│   │   │   │                   │   ├── SecurityAdminServlet.java
│   │   │   │                   │   ├── SecurityOnlyNameServer.java
│   │   │   │                   │   ├── SecurityQueueHandler.java
│   │   │   │                   │   └── SecurityService.java
│   │   │   │                   ├── Service.java
│   │   │   │                   ├── ServiceLocator.java
│   │   │   │                   ├── transport
│   │   │   │                   │   └── destination
│   │   │   │                   │       ├── ConnectionInfo.java
│   │   │   │                   │       ├── DestinationInfo.java
│   │   │   │                   │       ├── DestinationManager.java
│   │   │   │                   │       ├── JMSDestinationManager.java
│   │   │   │                   │       └── record
│   │   │   │                   │           ├── DestinationInfoRecord.java
│   │   │   │                   │           └── DomainRecord.java
│   │   │   │                   └── web
│   │   │   │                       ├── CasportV3SimServlet.java
│   │   │   │                       ├── HealthServlet.java
│   │   │   │                       ├── NamingServiceAdminServlet.java
│   │   │   │                       ├── PropertiesServlet.java
│   │   │   │                       ├── record
│   │   │   │                       │   ├── CasportV3SimServletRecord.java
│   │   │   │                       │   ├── DeadLetterRecord.java
│   │   │   │                       │   ├── NameServersRecord.java
│   │   │   │                       │   ├── QueueRecord.java
│   │   │   │                       │   ├── SilkwaveAdminServletDomainResponseRecord.java
│   │   │   │                       │   └── SilkwaveAdminServletLoadResponseRecord.java
│   │   │   │                       ├── RestrictionsLoginService.java
│   │   │   │                       ├── SilkwaveAdminServlet.java
│   │   │   │                       └── WebServer.java
│   │   │   └── test
│   │   │       ├── java
│   │   │       │   └── gov
│   │   │       │       └── ic
│   │   │       │           └── silkwave
│   │   │       │               ├── async
│   │   │       │               │   ├── AsyncProcessingContextTest.java
│   │   │       │               │   ├── AsyncProcessingListenerTest.java
│   │   │       │               │   ├── AsyncProcessingTimeoutManagerTest.java
│   │   │       │               │   ├── AsyncRequestCallbackTest.java
│   │   │       │               │   └── AsyncRequestManagerTest.java
│   │   │       │               ├── authentication
│   │   │       │               │   ├── AuthenticationCoreTest.java
│   │   │       │               │   ├── AuthenticationRegistryTest.java
│   │   │       │               │   ├── AuthenticationResultTest.java
│   │   │       │               │   ├── AuthenticationServiceTest.java
│   │   │       │               │   ├── AuthenticationWarningsTest.java
│   │   │       │               │   ├── AuthenticatorTest.java
│   │   │       │               │   └── ExtendedAuthenticationServiceTest.java
│   │   │       │               ├── authorization
│   │   │       │               │   ├── AuthorizationCoreTest.java
│   │   │       │               │   └── AuthorizationServiceTest.java
│   │   │       │               ├── data
│   │   │       │               │   ├── AbstractDataDistributionServiceTest.java
│   │   │       │               │   ├── CoreRoutingURITest.java
│   │   │       │               │   ├── files
│   │   │       │               │   │   ├── ChunkContentProviderTest.java
│   │   │       │               │   │   ├── FileAdminServletTest.java
│   │   │       │               │   │   ├── FileDestinationTest.java
│   │   │       │               │   │   ├── FileServiceTest.java
│   │   │       │               │   │   ├── FileSourceTest.java
│   │   │       │               │   │   ├── FileStateTest.java
│   │   │       │               │   │   ├── SendToNextHopRunnableTest.java
│   │   │       │               │   │   └── TrackedOutputStreamTest.java
│   │   │       │               │   ├── handlers
│   │   │       │               │   │   ├── DestinationFileTransferResponseHandlerTest.java
│   │   │       │               │   │   ├── FileReceivedHandlerTest.java
│   │   │       │               │   │   ├── MessagingHandlersTest.java
│   │   │       │               │   │   ├── RequestDestinationHandlerTest.java
│   │   │       │               │   │   └── RequestSourceHandlerTest.java
│   │   │       │               │   ├── SimpleFileServerTest.java
│   │   │       │               │   ├── SourceURITest.java
│   │   │       │               │   ├── streams
│   │   │       │               │   │   ├── ContextPacketTest.java
│   │   │       │               │   │   ├── DestinationPairTest.java
│   │   │       │               │   │   ├── InvalidIdExceptionTest.java
│   │   │       │               │   │   ├── KeyCallbackPredicateTest.java
│   │   │       │               │   │   ├── KeyStreamHandlerPredicateTest.java
│   │   │       │               │   │   ├── KeyTest.java
│   │   │       │               │   │   ├── LocalDestinationBufferTest.java
│   │   │       │               │   │   ├── LocalDestinationConcurrencyTest.java
│   │   │       │               │   │   ├── LocalDestinationContractsTest.java
│   │   │       │               │   │   ├── NotFoundStreamTest.java
│   │   │       │               │   │   ├── StreamCleanupTaskTest.java
│   │   │       │               │   │   ├── StreamHandlerConcurrencyTest.java
│   │   │       │               │   │   ├── StreamHandlerContractsTest.java
│   │   │       │               │   │   ├── StreamHandlerDefensiveCopyTest.java
│   │   │       │               │   │   ├── StreamHandlerDestinationsTest.java
│   │   │       │               │   │   ├── StreamHandlerLifecycleTest.java
│   │   │       │               │   │   ├── StreamHandlerTest.java
│   │   │       │               │   │   ├── StreamHeartbeatTaskTest.java
│   │   │       │               │   │   ├── StreamServiceCancelRequestTest.java
│   │   │       │               │   │   ├── StreamServiceClockTest.java
│   │   │       │               │   │   ├── StreamServiceCollectionNullContractsTest.java
│   │   │       │               │   │   ├── StreamServiceDestinationResponseTest.java
│   │   │       │               │   │   ├── StreamServiceDestinationStopResponseTest.java
│   │   │       │               │   │   ├── StreamServiceFailureNotificationTest.java
│   │   │       │               │   │   ├── StreamServiceGetStreamHandlerTest.java
│   │   │       │               │   │   ├── StreamServiceHandlerIndexesTest.java
│   │   │       │               │   │   ├── StreamServiceHeartbeatRequestTest.java
│   │   │       │               │   │   ├── StreamServiceHeartbeatResponseTest.java
│   │   │       │               │   │   ├── StreamServiceInternalRequestTest.java
│   │   │       │               │   │   ├── StreamServiceInternalResponseTest.java
│   │   │       │               │   │   ├── StreamServiceSourceSetupResponseTest.java
│   │   │       │               │   │   ├── StreamServiceSourceStopResponseTest.java
│   │   │       │               │   │   ├── StreamServiceStreamRequestTest.java
│   │   │       │               │   │   ├── StreamServiceTest.java
│   │   │       │               │   │   ├── UniqueIdKeyTest.java
│   │   │       │               │   │   └── UniqueIdTest.java
│   │   │       │               │   └── URIValidationExceptionTest.java
│   │   │       │               ├── DeadLetterManagerTest.java
│   │   │       │               ├── discovery
│   │   │       │               │   ├── DiscoveryEntryTest.java
│   │   │       │               │   ├── DiscoveryListenerTest.java
│   │   │       │               │   ├── DiscoveryServiceAdminServletTest.java
│   │   │       │               │   ├── DiscoveryServiceTest.java
│   │   │       │               │   ├── DiscoveryTaskTest.java
│   │   │       │               │   └── RemoteDiscoveryServiceTest.java
│   │   │       │               ├── fileserver
│   │   │       │               │   └── FileServerTest.java
│   │   │       │               ├── nameserver
│   │   │       │               │   └── NameServerTest.java
│   │   │       │               ├── naming
│   │   │       │               │   ├── NameResolutionTest.java
│   │   │       │               │   ├── NameResolverTest.java
│   │   │       │               │   ├── NamingAdminServletTest.java
│   │   │       │               │   └── NamingRegistryTest.java
│   │   │       │               ├── networkinfo
│   │   │       │               │   ├── handlers
│   │   │       │               │   │   └── DiagnosticsRequestHandlerTest.java
│   │   │       │               │   └── NetworkInfoServiceTest.java
│   │   │       │               ├── NetworkManagerTest.java
│   │   │       │               ├── networkstatus
│   │   │       │               │   ├── NetworkStatusAdminServletTest.java
│   │   │       │               │   ├── NetworkStatusServiceTest.java
│   │   │       │               │   ├── ScheduledOwnerTest.java
│   │   │       │               │   └── ScheduledStatusTest.java
│   │   │       │               ├── policy
│   │   │       │               │   ├── AuthenticationCheckTest.java
│   │   │       │               │   ├── AuthenticationServiceFactoryTest.java
│   │   │       │               │   ├── AuthenticationServiceTest.java
│   │   │       │               │   ├── MockAuthenticationServiceTest.java
│   │   │       │               │   ├── PolicyManagerTest.java
│   │   │       │               │   └── RecipientAuthorizationCheckTest.java
│   │   │       │               ├── records
│   │   │       │               │   └── GenericAdminServletRowRecordTest.java
│   │   │       │               ├── registration
│   │   │       │               │   ├── LocalRegistryCallbackTest.java
│   │   │       │               │   ├── LocalRegistryTest.java
│   │   │       │               │   ├── LocalRouteTest.java
│   │   │       │               │   ├── RegistrarTest.java
│   │   │       │               │   ├── RegistrationAdminServletDestinationInfoTest.java
│   │   │       │               │   └── RegistrationAdminServletTest.java
│   │   │       │               ├── routing
│   │   │       │               │   ├── EndpointCostTest.java
│   │   │       │               │   ├── LetterTest.java
│   │   │       │               │   ├── NeighborTest.java
│   │   │       │               │   ├── RouteOptionTest.java
│   │   │       │               │   ├── RouteTablesTest2.java
│   │   │       │               │   ├── RouteTablesTest.java
│   │   │       │               │   ├── RouteTest.java
│   │   │       │               │   ├── RoutingAdminServletTest.java
│   │   │       │               │   ├── RoutingMessageTest.java
│   │   │       │               │   ├── RoutingServiceTest.java
│   │   │       │               │   └── TableUpdateTest.java
│   │   │       │               ├── security
│   │   │       │               │   ├── AuthorizerTest.java
│   │   │       │               │   ├── cache
│   │   │       │               │   │   ├── AuthenticationCacheTest.java
│   │   │       │               │   │   ├── AuthorizationCacheTest.java
│   │   │       │               │   │   ├── AuthorizationServicesTest.java
│   │   │       │               │   │   ├── AuthorizedCacheObjectTest.java
│   │   │       │               │   │   ├── ExpiredAuthenticationTest.java
│   │   │       │               │   │   └── RestrictionsTest.java
│   │   │       │               │   ├── handler
│   │   │       │               │   │   ├── AuthenticateResponseHandlerTest.java
│   │   │       │               │   │   ├── AuthorizeResponseHandlerTest.java
│   │   │       │               │   │   ├── AuthorizerInfoResponseResponseHandlerTest.java
│   │   │       │               │   │   ├── FailureNotificationHandlerTest.java
│   │   │       │               │   │   ├── NetworkStatusHandlerTest.java
│   │   │       │               │   │   ├── PurgeAuthenticationRequestHandlerTest.java
│   │   │       │               │   │   ├── PurgeAuthorizationCacheRequestHandlerTest.java
│   │   │       │               │   │   ├── ResourceRegistrationResponseHandlerTest.java
│   │   │       │               │   │   ├── SecurityAuthorizationRequestHandlerTest.java
│   │   │       │               │   │   ├── SecurityAuthorizationResponseHandlerTest.java
│   │   │       │               │   │   └── UpdateSecurityCredentialsHandlerTest.java
│   │   │       │               │   ├── requests
│   │   │       │               │   │   ├── AuthenticatedRequestTest.java
│   │   │       │               │   │   ├── AuthorizedRequestTest.java
│   │   │       │               │   │   ├── RegisterRequestTest.java
│   │   │       │               │   │   ├── RemoteAuthorizationRequestTest.java
│   │   │       │               │   │   └── RequestTest.java
│   │   │       │               │   ├── responses
│   │   │       │               │   │   ├── AuthenticatedResponseTest.java
│   │   │       │               │   │   └── AuthorizedResponseTest.java
│   │   │       │               │   ├── RestrictionManagerTest.java
│   │   │       │               │   ├── SecurityAdminServletTest.java
│   │   │       │               │   ├── SecurityOnlyNameServerTest.java
│   │   │       │               │   ├── SecurityQueueHandlerTest.java
│   │   │       │               │   └── SecurityServiceTest.java
│   │   │       │               ├── ServiceLocatorTest.java
│   │   │       │               ├── TestUtils.java
│   │   │       │               ├── transport
│   │   │       │               │   └── destination
│   │   │       │               │       ├── ConnectionInfoTest.java
│   │   │       │               │       ├── DestinationInfoTest.java
│   │   │       │               │       └── JMSDestinationManagerTest.java
│   │   │       │               └── web
│   │   │       │                   ├── CasportV3SimServletTest.java
│   │   │       │                   ├── HealthServletTest.java
│   │   │       │                   ├── NamingServiceAdminServletTest.java
│   │   │       │                   ├── RestrictionsLoginServiceTest.java
│   │   │       │                   ├── SilkwaveAdminServletTest.java
│   │   │       │                   └── WebServerTest.java
│   │   │       ├── js
│   │   │       │   ├── csvEscape.test.js
│   │   │       │   └── routing.test.js
│   │   │       └── resources
│   │   │           ├── activemq.xml
│   │   │           ├── dns.properties
│   │   │           ├── groups.properties
│   │   │           ├── log4j2.xml
│   │   │           ├── silkwave.properties
│   │   │           └── users.properties
│   │   ├── test
│   │   │   └── gov
│   │   │       └── ic
│   │   │           └── silkwave
│   │   │               ├── activemq1.xml
│   │   │               ├── activemq2.xml
│   │   │               ├── activemq3.xml
│   │   │               ├── AllTests.java
│   │   │               ├── AMQJmxExampleTest.java
│   │   │               ├── A.properties
│   │   │               ├── AsyncTest.java
│   │   │               ├── AuthenticationServiceTests.java
│   │   │               ├── AuthorizationServiceTests.java
│   │   │               ├── AuthorizationTest.java
│   │   │               ├── B.properties
│   │   │               ├── connector-log4j2.xml
│   │   │               ├── C.properties
│   │   │               ├── data
│   │   │               │   └── streams
│   │   │               │       ├── MulticastTestStreams.java
│   │   │               │       ├── StreamChanger.java
│   │   │               │       └── StreamListener.java
│   │   │               ├── filexfer
│   │   │               │   ├── ChunkManagerTestCase.java
│   │   │               │   ├── ChunkManagerTestFiles.java
│   │   │               │   ├── FileTransferTestClient.java
│   │   │               │   └── FileTransferTestGUI.java
│   │   │               ├── KeystoreTest.java
│   │   │               ├── log4j2.xml
│   │   │               ├── MessageProtocol.java
│   │   │               ├── MessageTest.java
│   │   │               ├── messaging
│   │   │               │   ├── blindDestinationTest1.xml
│   │   │               │   ├── blindDestinationTest2.xml
│   │   │               │   ├── blindDestinationTest3.xml
│   │   │               │   ├── EnvelopeTest.java
│   │   │               │   ├── internalTest1.xml
│   │   │               │   ├── internalTest2.xml
│   │   │               │   ├── internalTest3.xml
│   │   │               │   ├── invalidTest1.xml
│   │   │               │   ├── invalidTest2.xml
│   │   │               │   ├── invalidTest3.xml
│   │   │               │   ├── invalidTest4.xml
│   │   │               │   ├── largeTest1.xml
│   │   │               │   ├── noSessionIdTest1.xml
│   │   │               │   ├── noSessionIdTest2.xml
│   │   │               │   ├── noSessionIdTest3.xml
│   │   │               │   ├── noSessionIdTest4.xml
│   │   │               │   ├── noSessionIdTest5.xml
│   │   │               │   ├── noSessionIdTest6.xml
│   │   │               │   ├── sessionIdTest1.xml
│   │   │               │   ├── sessionIdTest2.xml
│   │   │               │   ├── sessionIdTest3.xml
│   │   │               │   ├── sessionIdTest4.xml
│   │   │               │   ├── sessionIdTest5.xml
│   │   │               │   └── sessionIdTest6.xml
│   │   │               ├── MultiHubTest.java
│   │   │               ├── MultipleConnectionsTest.java
│   │   │               ├── networkinfo
│   │   │               │   └── NetworkInfoTests.java
│   │   │               ├── routing
│   │   │               │   └── test
│   │   │               │       ├── RouteTablesTest.java
│   │   │               │       ├── RouteUpdateTest.java
│   │   │               │       └── RoutingMessageTest.java
│   │   │               ├── security
│   │   │               │   ├── cache
│   │   │               │   │   ├── AuthenticationCacheTest.java
│   │   │               │   │   ├── AuthorizationCacheTest.java
│   │   │               │   │   └── AuthorizationServicesTest.java
│   │   │               │   ├── handler
│   │   │               │   │   ├── AuthenticateResponseHandlerTest.java
│   │   │               │   │   ├── AuthorizeResponseHandlerTest.java
│   │   │               │   │   ├── AuthorizerInfoResponseResponseHandlerTest.java
│   │   │               │   │   ├── FailureNotificationHandlerTest.java
│   │   │               │   │   ├── PurgeAuthenticationRequestHandlerTest.java
│   │   │               │   │   ├── PurgeAuthorizationCacheRequestHandlerTest.java
│   │   │               │   │   ├── ResourceRegistrationResponseHandlerTest.java
│   │   │               │   │   ├── SecurityAuthorizationRequestHandlerTest.java
│   │   │               │   │   ├── SecurityAuthorizationResponseHandlerTest.java
│   │   │               │   │   ├── TestAll.java
│   │   │               │   │   └── UpdateSecurityCredentialsHandlerTest.java
│   │   │               │   ├── requests
│   │   │               │   │   ├── AuthenticatedRequestTest.java
│   │   │               │   │   ├── AuthorizedRequestTest.java
│   │   │               │   │   ├── RegisterRequestTest.java
│   │   │               │   │   ├── RemoteAuthorizationRequestTest.java
│   │   │               │   │   ├── RequestTest.java
│   │   │               │   │   └── TestAll.java
│   │   │               │   ├── responses
│   │   │               │   │   ├── AuthenticatedResponseTest.java
│   │   │               │   │   ├── AuthorizedResponseTest.java
│   │   │               │   │   └── TestAll.java
│   │   │               │   ├── RestrictionManagerTest.java
│   │   │               │   ├── SecurityOnlyNameServerTest.java
│   │   │               │   ├── SecurityQueueHandlerTest.java
│   │   │               │   ├── SecurityServiceTest.java
│   │   │               │   ├── SecurityTest.java
│   │   │               │   └── TestAll.java
│   │   │               ├── silkwave1.properties
│   │   │               ├── silkwave2.properties
│   │   │               ├── silkwave3.properties
│   │   │               ├── SpeedTest.java
│   │   │               └── TestMessageConsumer.java
│   │   ├── traceroute
│   │   ├── utils
│   │   │   ├── generate_instances.sh
│   │   │   ├── ReadMe.txt
│   │   │   ├── SSL
│   │   │   │   └── gen.sh
│   │   │   ├── stomp.py-3.1.3.tar
│   │   │   └── test
│   │   │       ├── cpp
│   │   │       │   ├── doc
│   │   │       │   │   └── ReadMe.txt
│   │   │       │   ├── LogAll.sh
│   │   │       │   ├── makefile
│   │   │       │   ├── src
│   │   │       │   │   ├── EnterpriseSecurity_1_4.cxx
│   │   │       │   │   ├── EnterpriseSecurity_1_4.hxx
│   │   │       │   │   ├── envelope.cxx
│   │   │       │   │   ├── envelope.hxx
│   │   │       │   │   ├── main.cpp
│   │   │       │   │   ├── markings.cxx
│   │   │       │   │   ├── markings.hxx
│   │   │       │   │   ├── PropertyFile.cpp
│   │   │       │   │   ├── PropertyFile.h
│   │   │       │   │   ├── trAll.cpp
│   │   │       │   │   └── trAll.h
│   │   │       │   └── trAll.properties
│   │   │       └── python
│   │   │           ├── LogAll.sh
│   │   │           ├── ReadMe.txt
│   │   │           └── trAll.py
│   │   └── WebContent
│   │       ├── connections.html
│   │       ├── css
│   │       │   ├── dark-mode.css
│   │       │   ├── login.css
│   │       │   ├── login-error.css
│   │       │   └── silkwave.css
│   │       ├── discovery.html
│   │       ├── favicon.ico
│   │       ├── files.html
│   │       ├── images
│   │       │   └── ajax-loader.gif
│   │       ├── index.html
│   │       ├── jqGrid
│   │       │   ├── css
│   │       │   │   └── ui.jqgrid.css
│   │       │   └── js
│   │       │       ├── grid.locale-en.js
│   │       │       └── jquery.jqGrid.min.js
│   │       ├── jquery
│   │       │   └── jquery-2.1.0.min.js
│   │       ├── jquery-ui
│   │       │   ├── css
│   │       │   │   └── redmond
│   │       │   │       ├── images
│   │       │   │       │   ├── animated-overlay.gif
│   │       │   │       │   ├── ui-bg_flat_0_aaaaaa_40x100.png
│   │       │   │       │   ├── ui-bg_flat_55_fbec88_40x100.png
│   │       │   │       │   ├── ui-bg_glass_75_d0e5f5_1x400.png
│   │       │   │       │   ├── ui-bg_glass_85_dfeffc_1x400.png
│   │       │   │       │   ├── ui-bg_glass_95_fef1ec_1x400.png
│   │       │   │       │   ├── ui-bg_gloss-wave_55_5c9ccc_500x100.png
│   │       │   │       │   ├── ui-bg_inset-hard_100_f5f8f9_1x100.png
│   │       │   │       │   ├── ui-bg_inset-hard_100_fcfdfd_1x100.png
│   │       │   │       │   ├── ui-icons_217bc0_256x240.png
│   │       │   │       │   ├── ui-icons_2e83ff_256x240.png
│   │       │   │       │   ├── ui-icons_469bdd_256x240.png
│   │       │   │       │   ├── ui-icons_6da8d5_256x240.png
│   │       │   │       │   ├── ui-icons_cd0a0a_256x240.png
│   │       │   │       │   ├── ui-icons_d8e7f3_256x240.png
│   │       │   │       │   └── ui-icons_f9bd01_256x240.png
│   │       │   │       ├── jquery-ui-1.10.4.custom.css
│   │       │   │       └── jquery-ui-1.10.4.custom.min.css
│   │       │   └── js
│   │       │       ├── jquery-ui-1.10.4.custom.js
│   │       │       └── jquery-ui-1.10.4.custom.min.js
│   │       ├── js
│   │       │   ├── connections.js
│   │       │   ├── discovery.js
│   │       │   ├── files.js
│   │       │   ├── login-error.js
│   │       │   ├── login.js
│   │       │   ├── namingcache.js
│   │       │   ├── namingservice.js
│   │       │   ├── networkstatus.js
│   │       │   ├── properties.js
│   │       │   ├── queues.js
│   │       │   ├── routing.js
│   │       │   ├── security.js
│   │       │   ├── silkwave.js
│   │       │   ├── streamhandlers.js
│   │       │   ├── streams.js
│   │       │   ├── summary.js
│   │       │   └── utils.js
│   │       ├── loginerror.html
│   │       ├── login.html
│   │       ├── namingcache.html
│   │       ├── namingservice.html
│   │       ├── networkstatus.html
│   │       ├── policy.html
│   │       ├── properties.html
│   │       ├── queues.html
│   │       ├── routing.html
│   │       ├── security.html
│   │       ├── streamhandlers.html
│   │       ├── streams.html
│   │       ├── summary.html
│   │       └── utils.html
│   ├── core.common
│   │   ├── build.gradle.kts
│   │   ├── gradle.properties
│   │   ├── resources
│   │   │   ├── speedtestconsumer
│   │   │   └── speedtestproducer
│   │   ├── silkwave.properties.sha256
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── gov
│   │   │   │   │       └── ic
│   │   │   │   │           └── silkwave
│   │   │   │   │               └── common
│   │   │   │   │                   ├── certs
│   │   │   │   │                   │   ├── CertificateAuthority.java
│   │   │   │   │                   │   ├── CertUtils.java
│   │   │   │   │                   │   └── CRLManager.java
│   │   │   │   │                   ├── client
│   │   │   │   │                   │   ├── ConnectionInterface.java
│   │   │   │   │                   │   ├── ConnectionManager.java
│   │   │   │   │                   │   ├── FileCallback.java
│   │   │   │   │                   │   ├── FileManager.java
│   │   │   │   │                   │   ├── queue
│   │   │   │   │                   │   │   ├── AbstractMessageQueueHandler.java
│   │   │   │   │                   │   │   ├── LocalAbortPolicy.java
│   │   │   │   │                   │   │   ├── MessageHandlerFactory.java
│   │   │   │   │                   │   │   ├── MessageQueueListener.java
│   │   │   │   │                   │   │   └── MessageQueueMonitorThread.java
│   │   │   │   │                   │   ├── security
│   │   │   │   │                   │   │   ├── JKSFileSpec.java
│   │   │   │   │                   │   │   ├── KeySpec.java
│   │   │   │   │                   │   │   ├── KeystoreSpec.java
│   │   │   │   │                   │   │   ├── P12FileSpec.java
│   │   │   │   │                   │   │   └── TrustSpec.java
│   │   │   │   │                   │   └── StreamManager.java
│   │   │   │   │                   ├── error
│   │   │   │   │                   │   ├── ErrorCode.java
│   │   │   │   │                   │   ├── GeneralErrorCode.java
│   │   │   │   │                   │   └── SystemException.java
│   │   │   │   │                   ├── files
│   │   │   │   │                   │   ├── AsyncFilesServlet.java
│   │   │   │   │                   │   ├── CancelableOutputStream.java
│   │   │   │   │                   │   ├── FileDestinationInfo.java
│   │   │   │   │                   │   ├── FileEndpoint.java
│   │   │   │   │                   │   ├── FileFormatDetector.java
│   │   │   │   │                   │   ├── FileInfo.java
│   │   │   │   │                   │   ├── FileInputContainer.java
│   │   │   │   │                   │   ├── FileManagementService.java
│   │   │   │   │                   │   ├── FileOutputContainer.java
│   │   │   │   │                   │   ├── FilePushHandler.java
│   │   │   │   │                   │   ├── FileReceptionCallback.java
│   │   │   │   │                   │   ├── FileReceptionHandler.java
│   │   │   │   │                   │   ├── FileRequestCallback.java
│   │   │   │   │                   │   ├── FileRequestHandler.java
│   │   │   │   │                   │   ├── FileServerAsyncCallback.java
│   │   │   │   │                   │   ├── FileServer.java
│   │   │   │   │                   │   ├── FileSourceInfo.java
│   │   │   │   │                   │   ├── file-types.json
│   │   │   │   │                   │   ├── ForbidTraceHandler.java
│   │   │   │   │                   │   ├── HttpFileEndpoint.java
│   │   │   │   │                   │   ├── HttpFileServer.java
│   │   │   │   │                   │   └── processors
│   │   │   │   │                   │       ├── FileProcessor.java
│   │   │   │   │                   │       ├── FileProcessorResult.java
│   │   │   │   │                   │       └── MultiFileProcessor.java
│   │   │   │   │                   ├── logging
│   │   │   │   │                   │   ├── AuditLogger.java
│   │   │   │   │                   │   ├── LoggerHelper.java
│   │   │   │   │                   │   └── TrackingLogger.java
│   │   │   │   │                   ├── messages
│   │   │   │   │                   │   ├── builders
│   │   │   │   │                   │   │   ├── NetworkMessageManipulator.java
│   │   │   │   │                   │   │   ├── RestrictionManipulator.java
│   │   │   │   │                   │   │   ├── RtemsgManipulator.java
│   │   │   │   │                   │   │   ├── SecurityManipulator.java
│   │   │   │   │                   │   │   ├── SILKWAVEMessageManipulator.java
│   │   │   │   │                   │   │   ├── SourceFileTransferResponseManipulator.java
│   │   │   │   │                   │   │   └── StreamSetupResponseManipulator.java
│   │   │   │   │                   │   ├── processors
│   │   │   │   │                   │   │   ├── Bridge420to421Processor.java
│   │   │   │   │                   │   │   ├── Bridge421to420Processor.java
│   │   │   │   │                   │   │   ├── ConfigParams.java
│   │   │   │   │                   │   │   ├── Convert421To42Processor.java
│   │   │   │   │                   │   │   ├── Convert42To421Processor.java
│   │   │   │   │                   │   │   ├── ConvertAppGEO421To42Processor.java
│   │   │   │   │                   │   │   ├── ConvertAppGEO42To421Processor.java
│   │   │   │   │                   │   │   ├── EnvelopeAltLabelProcessor.java
│   │   │   │   │                   │   │   ├── EnvelopeFieldProcessor.java
│   │   │   │   │                   │   │   ├── EnvelopeFieldProcessorParams.java
│   │   │   │   │                   │   │   ├── EnvelopeProcessor.java
│   │   │   │   │                   │   │   ├── IMsgProcessor.java
│   │   │   │   │                   │   │   ├── InvertSuccessMsgProcessor.java
│   │   │   │   │                   │   │   ├── LogMsgProcessor.java
│   │   │   │   │                   │   │   ├── msgdata
│   │   │   │   │                   │   │   │   ├── DOMMsgData.java
│   │   │   │   │                   │   │   │   ├── EnvelopeMsgData.java
│   │   │   │   │                   │   │   │   ├── MsgDataException.java
│   │   │   │   │                   │   │   │   ├── MsgData.java
│   │   │   │   │                   │   │   │   ├── ProcessorResult.java
│   │   │   │   │                   │   │   │   └── XMLStringMsgData.java
│   │   │   │   │                   │   │   ├── MultiMsgProcessor.java
│   │   │   │   │                   │   │   ├── MultiProcessorUtil.java
│   │   │   │   │                   │   │   ├── PluginException.java
│   │   │   │   │                   │   │   ├── Processor.java
│   │   │   │   │                   │   │   ├── ProcessorUtil.java
│   │   │   │   │                   │   │   ├── SecurityFilterProcessor.java
│   │   │   │   │                   │   │   ├── SimpleNamespaceContext.java
│   │   │   │   │                   │   │   ├── StatusMsgProcessor.java
│   │   │   │   │                   │   │   ├── XMLValidateMsgProcessor.java
│   │   │   │   │                   │   │   ├── XPathFilterMsgProcessor.java
│   │   │   │   │                   │   │   └── XSLTMsgProcessor.java
│   │   │   │   │                   │   └── queue
│   │   │   │   │                   │       └── LocalAbortPolicy.java
│   │   │   │   │                   ├── messaging
│   │   │   │   │                   │   ├── Envelope.java
│   │   │   │   │                   │   ├── EnvelopeParser.java
│   │   │   │   │                   │   ├── MessageCallback.java
│   │   │   │   │                   │   ├── MessageValidationException.java
│   │   │   │   │                   │   ├── request
│   │   │   │   │                   │   │   ├── ActiveStreamRequest.java
│   │   │   │   │                   │   │   ├── Request.java
│   │   │   │   │                   │   │   └── ResourceRequest.java
│   │   │   │   │                   │   ├── ScratchPad.java
│   │   │   │   │                   │   ├── TagEvent.java
│   │   │   │   │                   │   └── TraceRouteCallback.java
│   │   │   │   │                   ├── permissions
│   │   │   │   │                   │   ├── Group.java
│   │   │   │   │                   │   └── PermissionManager.java
│   │   │   │   │                   ├── RoutingScheme.java
│   │   │   │   │                   ├── RoutingURI.java
│   │   │   │   │                   ├── RoutingURISpec.java
│   │   │   │   │                   ├── ServiceException.java
│   │   │   │   │                   ├── streams
│   │   │   │   │                   │   ├── processors
│   │   │   │   │                   │   │   ├── MultiStreamProcessor.java
│   │   │   │   │                   │   │   └── StreamProcessor.java
│   │   │   │   │                   │   ├── SourceStreamHandler.java
│   │   │   │   │                   │   ├── StreamEndpoint.java
│   │   │   │   │                   │   ├── StreamMath.java
│   │   │   │   │                   │   ├── StreamReceptionHandler.java
│   │   │   │   │                   │   ├── StreamServer.java
│   │   │   │   │                   │   ├── udpstream
│   │   │   │   │                   │   │   ├── DatagramChannelHolder.java
│   │   │   │   │                   │   │   ├── DatagramPacket.java
│   │   │   │   │                   │   │   ├── RegistrationItem.java
│   │   │   │   │                   │   │   ├── UdpClientKey.java
│   │   │   │   │                   │   │   ├── UdpServerKey.java
│   │   │   │   │                   │   │   ├── UdpStreamEndpoint.java
│   │   │   │   │                   │   │   ├── UdpStreamServer.java
│   │   │   │   │                   │   │   └── UdpStreamType.java
│   │   │   │   │                   │   └── virtualstream
│   │   │   │   │                   │       ├── VirtualStreamEndpoint.java
│   │   │   │   │                   │       └── VirtualStreamServer.java
│   │   │   │   │                   ├── thread
│   │   │   │   │                   │   └── PausableThreadPoolExecutor.java
│   │   │   │   │                   ├── transport
│   │   │   │   │                   │   ├── jms
│   │   │   │   │                   │   │   ├── ByteCounterJMSMessageListener.java
│   │   │   │   │                   │   │   ├── ByteCounterMessageSender.java
│   │   │   │   │                   │   │   ├── IRealTimeMessageHandler.java
│   │   │   │   │                   │   │   ├── IRealTimeNetworkStatusHandler.java
│   │   │   │   │                   │   │   ├── ISilkwaveTransportListener.java
│   │   │   │   │                   │   │   ├── JMSConnection.java
│   │   │   │   │                   │   │   ├── JmsDestination.java
│   │   │   │   │                   │   │   ├── JMSMessageListener.java
│   │   │   │   │                   │   │   ├── MessageListenerFactory.java
│   │   │   │   │                   │   │   ├── MessageSelector.java
│   │   │   │   │                   │   │   ├── MessageSenderFactory.java
│   │   │   │   │                   │   │   ├── MessageSender.java
│   │   │   │   │                   │   │   ├── MetricsEnabledMessageHandler.java
│   │   │   │   │                   │   │   └── PayloadDestination.java
│   │   │   │   │                   │   └── TransportException.java
│   │   │   │   │                   ├── utils
│   │   │   │   │                   │   ├── buffer
│   │   │   │   │                   │   │   ├── ChunkedInputStream.java
│   │   │   │   │                   │   │   ├── ChunkedOutputStream.java
│   │   │   │   │                   │   │   ├── Chunk.java
│   │   │   │   │                   │   │   ├── ChunkManager.java
│   │   │   │   │                   │   │   ├── ChunkReference.java
│   │   │   │   │                   │   │   ├── ChunkRefType.java
│   │   │   │   │                   │   │   └── InvalidChunkHandlerException.java
│   │   │   │   │                   │   ├── CheckedSupplier.java
│   │   │   │   │                   │   ├── Constants.java
│   │   │   │   │                   │   ├── ExcludedProperties.java
│   │   │   │   │                   │   ├── FactoryUtil.java
│   │   │   │   │                   │   ├── FileUtil.java
│   │   │   │   │                   │   ├── GsonFactory.java
│   │   │   │   │                   │   ├── JsonMapper.java
│   │   │   │   │                   │   ├── NetUtils.java
│   │   │   │   │                   │   ├── NetworkUtil.java
│   │   │   │   │                   │   ├── NumberConvertor.java
│   │   │   │   │                   │   ├── SilkwaveProperties.java
│   │   │   │   │                   │   ├── SpeedTestConsumer.java
│   │   │   │   │                   │   ├── SpeedTestProducer.java
│   │   │   │   │                   │   ├── SpeedTestUtil.java
│   │   │   │   │                   │   ├── SslUtils.java
│   │   │   │   │                   │   ├── TopicMessageUtil.java
│   │   │   │   │                   │   └── TraceRoute.java
│   │   │   │   │                   ├── xml
│   │   │   │   │                   │   ├── DateFromXMLAdapter.java
│   │   │   │   │                   │   ├── Input.java
│   │   │   │   │                   │   ├── jaxb
│   │   │   │   │                   │   │   ├── Converter.java
│   │   │   │   │                   │   │   └── Silkwave.java
│   │   │   │   │                   │   ├── XMLTransformer.java
│   │   │   │   │                   │   └── XMLUtil.java
│   │   │   │   │                   └── xsd
│   │   │   │   │                       └── internal.xsd
│   │   │   │   └── resources
│   │   │   │       ├── silkwave-all.properties
│   │   │   │       ├── silkwave.properties
│   │   │   │       ├── web
│   │   │   │       │   ├── bootstrap-3.1.0
│   │   │   │       │   │   ├── css
│   │   │   │       │   │   │   ├── bootstrap.css
│   │   │   │       │   │   │   ├── bootstrap.css.map
│   │   │   │       │   │   │   ├── bootstrap.min.css
│   │   │   │       │   │   │   ├── bootstrap-theme.css
│   │   │   │       │   │   │   ├── bootstrap-theme.css.map
│   │   │   │       │   │   │   └── bootstrap-theme.min.css
│   │   │   │       │   │   ├── fonts
│   │   │   │       │   │   │   ├── glyphicons-halflings-regular.eot
│   │   │   │       │   │   │   ├── glyphicons-halflings-regular.svg
│   │   │   │       │   │   │   ├── glyphicons-halflings-regular.ttf
│   │   │   │       │   │   │   └── glyphicons-halflings-regular.woff
│   │   │   │       │   │   └── js
│   │   │   │       │   │       ├── bootstrap.js
│   │   │   │       │   │       └── bootstrap.min.js
│   │   │   │       │   ├── index.html
│   │   │   │       │   ├── jquery-2.1.0.min.js
│   │   │   │       │   └── knockout-3.1.0.js
│   │   │   │       ├── xml
│   │   │   │       │   └── output.xsl
│   │   │   │       └── xsd
│   │   │   │           └── internal.xsd
│   │   │   └── test
│   │   │       ├── java
│   │   │       │   └── gov
│   │   │       │       └── ic
│   │   │       │           └── silkwave
│   │   │       │               └── common
│   │   │       │                   ├── messages
│   │   │       │                   │   └── builders
│   │   │       │                   │       └── SecurityManipulatorTest.java
│   │   │       │                   ├── messaging
│   │   │       │                   │   └── EnvelopeTest.java
│   │   │       │                   ├── permissions
│   │   │       │                   │   └── GroupTest.java
│   │   │       │                   ├── RoutingURITest.java
│   │   │       │                   ├── transport
│   │   │       │                   │   └── jms
│   │   │       │                   │       └── JMSMessageListenerTest.java
│   │   │       │                   ├── utils
│   │   │       │                   │   ├── ConstantsTest.java
│   │   │       │                   │   ├── FactoryUtilTest.java
│   │   │       │                   │   └── SslUtilsTest.java
│   │   │       │                   └── xml
│   │   │       │                       ├── jaxb
│   │   │       │                       │   ├── ConverterTest.java
│   │   │       │                       │   └── SilkwaveTest.java
│   │   │       │                       └── XMLUtilTest.java
│   │   │       └── resources
│   │   │           ├── ConnectRequest.xml
│   │   │           ├── envelopeTest.properties
│   │   │           ├── rtemsgchild.xml
│   │   │           ├── rtemsg.xml
│   │   │           ├── silkwave-all.properties
│   │   │           └── silkwave.properties
│   │   └── test
│   │       └── gov
│   │           └── ic
│   │               └── silkwave
│   │                   └── common
│   │                       ├── client
│   │                       │   └── ConnectionManagerTest.java
│   │                       ├── messages
│   │                       │   ├── builders
│   │                       │   │   ├── RestrictionManipulatorTest.java
│   │                       │   │   └── SecurityManipulatorTest.java
│   │                       │   └── processors
│   │                       │       ├── ConvertAppGeo421to42ProcessorTest.java
│   │                       │       ├── ConvertAppGeo42to421ProcessorTest.java
│   │                       │       ├── EnvelopeAltLabelProcessorTest.java
│   │                       │       ├── EnvelopeProcessorTest.java
│   │                       │       ├── SecurityFilterProcessorTest.java
│   │                       │       └── StatusMsgProcessorTest.java
│   │                       └── support
│   │                           ├── InterceptInputStream.java
│   │                           └── TestUtils.java
│   ├── silkwave.api
│   │   ├── build.gradle.kts
│   │   ├── gradle.properties
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── gov
│   │                   └── ic
│   │                       └── silkwave
│   │                           └── common
│   │                               ├── client
│   │                               │   └── MockConnection.java
│   │                               ├── files
│   │                               │   └── HttpRequestMonitor.java
│   │                               └── messages
│   │                                   └── processors
│   │                                       └── AppGeoEnvelopeProcessor.java
│   ├── silkwave.connector
│   │   ├── build.gradle.kts
│   │   ├── connections
│   │   ├── connector.sh
│   │   ├── resources
│   │   │   ├── connectorLocalA.prop
│   │   │   ├── connectorLocalB.prop
│   │   │   ├── connector-log4j2.xml
│   │   │   ├── connectorRemoteA.prop
│   │   │   ├── connectorRemoteB.prop
│   │   │   ├── init.d.connector
│   │   │   ├── samples
│   │   │   │   └── filter
│   │   │   │       ├── 421_to_42_config.xml
│   │   │   │       ├── 42_to_421_config.xml
│   │   │   │       ├── chat_filter_config.xml
│   │   │   │       ├── connectorLocalA.prop
│   │   │   │       └── status_filter_config.xml
│   │   │   └── systemd
│   │   │       ├── connector@.service
│   │   │       ├── connectors.target
│   │   │       └── connector-systemd.sh
│   │   ├── src
│   │   │   └── main
│   │   │       └── java
│   │   │           └── gov
│   │   │               └── ic
│   │   │                   └── silkwave
│   │   │                       └── connector
│   │   │                           ├── A.properties
│   │   │                           ├── B2.properties
│   │   │                           ├── B.properties
│   │   │                           ├── Connector.java
│   │   │                           ├── C.properties
│   │   │                           ├── example_421_to_42_config.xml
│   │   │                           └── example_42_to_421_config.xml
│   │   └── test
│   │       └── gov
│   │           └── ic
│   │               └── silkwave
│   │                   └── connector
│   │                       ├── A.properties
│   │                       ├── B.properties
│   │                       ├── ClientTest.java
│   │                       └── C.properties
│   ├── silkwave.intf
│   │   ├── build.gradle.kts
│   │   ├── gradle.properties
│   │   └── src
│   │       └── main
│   │           └── resources
│   │               ├── chat.xsd
│   │               ├── connector_instrumentation_extension.xsd
│   │               ├── diagnostics.xsd
│   │               ├── EnterpriseSecurity_1_4.xsd
│   │               ├── envelope.xsd
│   │               ├── instrumentation.xsd
│   │               ├── internal.xsd
│   │               ├── markings.xsd
│   │               ├── master.xsd
│   │               ├── network.xsd
│   │               ├── NSInternal.xsd
│   │               ├── security.xsd
│   │               ├── topic.xsd
│   │               └── x509pki.xsd
│   ├── silkwave.nameserver
│   │   ├── build.gradle.kts
│   │   ├── gradle.properties
│   │   └── src
│   │       └── main
│   │           ├── java
│   │           │   └── gov
│   │           │       └── ic
│   │           │           └── silkwave
│   │           │               └── nameserver
│   │           │                   ├── NameServer.java
│   │           │                   ├── NameServerLocator.java
│   │           │                   ├── NetId.java
│   │           │                   ├── NetResources.java
│   │           │                   └── ResourceRoute.java
│   │           └── resources
│   │               └── xsd
│   │                   └── NSInternal.xsd
│   └── silkwave.topic
│       ├── build.gradle.kts
│       ├── resources
│       │   ├── silkwavetopic-log4j2.xml
│       │   ├── silkwavetopic-systemd.sh
│       │   ├── systemd
│       │   │   ├── silkwavetopic@.service
│       │   │   └── silkwavetopic.target
│       │   ├── TopicConfig.xml
│       │   └── topics
│       ├── silkwavetopic
│       └── src
│           ├── main
│           │   └── java
│           │       └── silkwave
│           │           └── topic
│           │               └── service
│           │                   ├── config
│           │                   │   ├── ParseConfig.java
│           │                   │   └── TopicConfig.java
│           │                   ├── helper
│           │                   │   └── XMLUtil.java
│           │                   ├── SubscriptionInfo.java
│           │                   ├── SubscriptionsHolder.java
│           │                   └── Topic.java
│           └── test
│               └── java
│                   └── silkwave
│                       └── topic
│                           └── service
│                               ├── helper
│                               │   └── XMLUtilDiagnosticsTest.java
│                               └── TopicHandlerDiagnosticsTest.java
├── DELIVERY_REPORT.md
├── dependency-platform
│   ├── build.gradle.kts
│   └── gradle.properties
├── deploy
│   ├── makefile
│   └── rpm
│       └── core
│           └── SPECS
│               └── core.spec
├── devtools
│   ├── pull-and-install.sh
│   └── README.md
├── docker
│   ├── docker
│   │   ├── container_exec.sh
│   │   ├── figlet-2.2.5-23.20151018gita565ae1.el9.x86_64.rpm
│   │   ├── SILKWAVE_README.md
│   │   ├── sw_banner.sh
│   │   ├── sw_health_check.sh
│   │   └── sw-templates
│   │       ├── activemq.xml
│   │       ├── log4j2.xml
│   │       └── silkwave.properties
│   ├── Dockerfile
│   ├── example
│   │   ├── docker-compose.yml
│   │   └── silkwave.env
│   ├── README.md
│   └── zcomp
│       ├── Dockerfile
│       ├── README.md
│       └── staging
│           ├── container_exec.sh
│           ├── zcomp-1.4.3-1.tar.gz
│           └── zcompd.conf
├── eslint.config.mjs
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── new_tree.md
├── package.json
├── package-lock.json
├── README.md
├── settings.gradle.kts
└── tools
    └── eslint
        ├── cli.mjs
        ├── report.mjs
        └── report.test.mjs

287 directories, 919 files
