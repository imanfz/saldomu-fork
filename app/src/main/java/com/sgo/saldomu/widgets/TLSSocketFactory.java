package com.sgo.saldomu.widgets;

import android.net.SSLCertificateSocketFactory;

import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import timber.log.Timber;

/**
 * Created by yuddistirakiki on 11/9/17.
 */

public class TLSSocketFactory extends SSLSocketFactory {

    private KeyStore keyStore;

    public TLSSocketFactory(String algorithm, KeyStore keystore, String keystorePassword, KeyStore truststore, SecureRandom random, HostNameResolver nameResolver) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(algorithm, keystore, keystorePassword, truststore, random, nameResolver);
    }

    public TLSSocketFactory(KeyStore keystore, String keystorePassword, KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(keystore, keystorePassword, truststore);
    }

    public TLSSocketFactory(KeyStore keystore, String keystorePassword) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(keystore, keystorePassword);
    }

    public TLSSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);
        Timber.d("isi truststore:%s", truststore.toString());
        this.keyStore = truststore;
    }

    @Override
    public Socket createSocket() throws IOException {
        return null;
    }

    private static TrustManager[] createTrustManagers(final KeyStore keystore)
            throws KeyStoreException, NoSuchAlgorithmException {
        if (keystore == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(keystore);
        return tmfactory.getTrustManagers();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        if (autoClose) {
            // we don't need the plainSocket
            socket.close();
        }

        SSLCertificateSocketFactory sslSocketFactory =
                (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);

        // For self-signed certificates use a custom trust manager
        try {
            sslSocketFactory.setTrustManagers(createTrustManagers(keyStore));
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        // create and connect SSL socket, but don't do hostname/certificate verification yet
        SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(InetAddress.getByName(host), port);

        // enable TLSv1.1/1.2 if available
        ssl.setEnabledProtocols(ssl.getSupportedProtocols());

        // set up SNI before the handshake
        sslSocketFactory.setHostname(ssl, host);

        // verify hostname and certificate
        SSLSession session = ssl.getSession();
        if (!getHostnameVerifier().verify(host, session)) {
            throw new SSLPeerUnverifiedException("Cannot verify hostname: " + host);
        }

		/*DLog.d(TlsSniSocketFactory.class.getSimpleName(),
				"Established " + session.getProtocol() + " connection with " + session.getPeerHost() +
						" using " + session.getCipherSuite());*/

        return ssl;
    }
}
