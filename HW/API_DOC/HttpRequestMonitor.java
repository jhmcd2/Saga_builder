/*
 * The overall classification of this file is: UNCLASSIFIED
 */
package gov.ic.silkwave.common.files;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gov.ic.silkwave.common.utils.Constants;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated included for compatibility with 2.4.1.4
 */
@Deprecated
public class HttpRequestMonitor {
    private static Logger log = LoggerFactory.getLogger(HttpRequestMonitor.class);

    private ConcurrentMap<String, HttpClient> clients = new ConcurrentHashMap<>();
    private ConcurrentMap<HttpClient, Set<Request>> requestMap = new ConcurrentHashMap<>();

    private Collection<HttpClient> listForDeletion = new ConcurrentLinkedQueue<>();

    private Object httpClientLock = new Object();
    private Object requestLock = new Object();

    private ScheduledExecutorService executor;

    private static String getURL(URI uri) {
        return uri.getHost() + ':' + uri.getPort();
    }

    private class CleanupTask implements Runnable {
        @Override
        public void run() {
            Iterator<HttpClient> itr = listForDeletion.iterator();
            while (itr.hasNext()) {
                HttpClient client = itr.next();
                if (!requestMap.containsKey(client) || (requestMap.containsKey(client) && requestMap.get(client)
                        .isEmpty())) {
                    // close the client
                    try {
                        client.stop();
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                    client.destroy();
                    // remove the client from the requestMap
                    requestMap.remove(client);
                    // remove the client from the listForDeletion
                    itr.remove();
                }
            }
        }
    }

    public HttpRequestMonitor() {
        long requestRate = Long.parseLong(Constants.props.getProperty("http.request.monitor.rate", "60"));

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(new CleanupTask(), 0, requestRate, TimeUnit.SECONDS);

    }

    @Override
    protected void finalize() throws Throwable {
        executor.shutdownNow();
        super.finalize();
    }

    public void addRequest(HttpClient client, Request req) {
        synchronized (requestLock) {
            if (requestMap.containsKey(client)) {
                requestMap.get(client).add(req);
            } else {
                Set<Request> requests = new HashSet<>();
                requests.add(req);
                requestMap.put(client, requests);
            }
        }
    }

    public void removeRequest(HttpClient client, Request req) {
        synchronized (requestLock) {
            if (requestMap.containsKey(client)) {
                requestMap.get(client).remove(req);
            }
        }
    }

    public HttpClient getHttpClient(URI uri) {
        String url = getURL(uri);

        HttpClient client = null;

        synchronized (httpClientLock) {
            if (clients.containsKey(url)) {
                client = clients.get(url);
            } else {
                // Instantiate and configure the SslContextFactory
                // TODO: compilation error
                // error: SslContextFactory is abstract; cannot be instantiated
                //        SslContextFactory sslContextFactory = new SslContextFactory();
                //
                //        // configure the SSL context factory
                //        String keyStore = Constants.props.getProperty("silkwave.connections.keystore");
                //        String keyStorePass = Constants.props.getProperty("silkwave.connections.keystorePass");
                //        String trustStore = Constants.props.getProperty("silkwave.connections.truststore");
                //        String trustStorePass = Constants.props.getProperty("silkwave.connections.truststorePass");
                //
                //        if (keyStore != null && keyStorePass != null) {
                //          sslContextFactory.setKeyStorePath(keyStore);
                //          sslContextFactory.setKeyStorePassword(keyStorePass);
                //        }
                //
                //        if (trustStore != null && trustStorePass != null) {
                //          sslContextFactory.setTrustStorePath(trustStore);
                //          sslContextFactory.setTrustStorePassword(trustStorePass);
                //        }
                //
                //        client = new HttpClient(sslContextFactory);
                //
                //        client.addLifeCycleListener(new LifeCycle.Listener() {
                //
                //          @Override
                //          public void lifeCycleStopping(LifeCycle arg0) {
                //            log.warn("HttpClient STOPPING " + arg0);
                //          }
                //
                //          @Override
                //          public void lifeCycleStopped(LifeCycle arg0) {
                //            log.warn("HttpClient STOPPED " + arg0);
                //          }
                //
                //          @Override
                //          public void lifeCycleStarting(LifeCycle arg0) {
                //            log.debug("HttpClient STARTING " + arg0);
                //          }
                //
                //          @Override
                //          public void lifeCycleStarted(LifeCycle arg0) {
                //            log.debug("HttpClient STARTED " + arg0);
                //          }
                //
                //          @Override
                //          public void lifeCycleFailure(LifeCycle arg0, Throwable arg1) {
                //            log.warn("HttpClient FAILURE " + arg0, arg1);
                //          }
                //        });
                //        client.start();
                //        clients.put(url, client);
                //        log.info("Starting HttpClient({}) to URL({})", client, url);

            }
        }

        return client;
    }

    public void removeHttpClient(URI uri, HttpClient client) {
        String key = getURL(uri);
        synchronized (httpClientLock) {
            // check to make sure that the map still points the key to the idletimeout client code,
            // it may have already been removed from the map by another idletimeout
            if (clients.containsKey(key) && clients.get(key).equals(client)) {
                clients.remove(key);

                listForDeletion.add(client);
                executor.execute(new CleanupTask());
                log.info(
                        "Closing out HttpClient({}) to URL({}) due to error, next request will create a new HttpClient",
                        client, key);
            }
        }
    }
}
/*
 * The overall classification of this file is: UNCLASSIFIED
 */
