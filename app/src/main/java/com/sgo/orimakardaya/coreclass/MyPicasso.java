package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 3/30/2015.
 */

import android.content.Context;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import javax.net.ssl.*;
import java.security.cert.CertificateException;

public class MyPicasso {

    private static Picasso sPicasso;

    private MyPicasso() {
    }

    public static Picasso getImageLoader(final Context context) {
        if (sPicasso == null) {
            Picasso.Builder builder = new Picasso.Builder(context);
            builder.downloader(new OkHttpDownloader(getUnsafeOkHttpClient()));
            sPicasso = builder.build();
        }
        return sPicasso;
    }


    static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            } };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class TrustEverythingTrustManager implements X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
        }
    }

    public static class VerifyEverythingHostnameVerifier implements
            HostnameVerifier {

        public boolean verify(String string, SSLSession sslSession) {
            return true;
        }
    }

}
