package com.sgo.saldomu.widgets;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import timber.log.Timber;

/**
 * Created by SGOUser on 1/9/2018.
 */

public class TLSSocket extends SSLSocketFactory implements HostnameVerifier{


    private static KeyStore keystore;

    // Field named delegate so okHttp 3.1.2 will be
    // able to get our trust manager as suggested here:
    // https://github.com/square/okhttp/issues/2323#issuecomment-185055040
    private SSLSocketFactory delegate;


    public TLSSocket() throws KeyManagementException, NoSuchAlgorithmException{
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, null, null);
        delegate = context.getSocketFactory();
    }

    public TLSSocket(KeyStore key) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        if (key == null) {
            throw new IllegalArgumentException("Keystore may not be null");
        }
        keystore  = key;

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, new TrustManager[]{systemDefaultTrustManager()},new SecureRandom());
        Timber.w("tls socket1");
        delegate = context.getSocketFactory();

    }

    @Override
    public String[] getDefaultCipherSuites() {

        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {

        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {

        return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {

        return enableTLSOnSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {

        return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {

        return enableTLSOnSocket(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {

        return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {

        if (socket != null && (socket instanceof SSLSocket)) {
            // Only use TLSv1.2,1.2
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});
            }
//            if(sock);
        }


        return socket;
    }

    private static X509TrustManager defaultTrustManager() {
                try {
            Timber.d("TLS 5");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore)null);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }

            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {

            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    private static X509TrustManager myTrustManager() {
        try {
            Timber.d("TLS 5a");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {

            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    public static X509TrustManager customTrustManager() {


        X509TrustManager customTM = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {

//                return new X509Certificate[0];
                return myTrustManager().getAcceptedIssuers();
            }
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try{

                    myTrustManager().checkClientTrusted(chain,authType);
                }catch(CertificateException e){

                    defaultTrustManager().checkClientTrusted(chain,authType);
                }
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try{

                    myTrustManager().checkServerTrusted(chain,authType);
                }catch(CertificateException e){

                    defaultTrustManager().checkServerTrusted(chain,authType);
                }
            }
        };
        return customTM;


    }

    public X509TrustManager systemDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}
