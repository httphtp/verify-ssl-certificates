package com.ersan.androidhttpsclient;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okio.Buffer;

/**
 * Created by admin on 2017/6/20.
 */

public class SSLHelper {
    //https://gist.github.com/mtigas/952344   p12 证书生成
    //keytool -printcert -rfc -file ca.crt
    /*private static final String CA_STR = "-----BEGIN CERTIFICATE-----\n" +
            "MIICozCCAikCCQChAfTEz9z2WDAKBggqhkjOPQQDAjCBujELMAkGA1UEBhMCQ04x\n" +
            "EDAOBgNVBAgMB0JlaWppbmcxEDAOBgNVBAcMB0JlaWppbmcxDzANBgNVBBEMBjEw\n" +
            "MDAyMjEUMBIGA1UECQwLR3VvTWFvU2FuUWkxGDAWBgNVBAoMD2FwZmVsYm95bXNj\n" +
            "aHVsZTETMBEGA1UECwwKU3VwcG9ydF9DQTEdMBsGCSqGSIb3DQEJARYOaHR0cC5i\n" +
            "akBxcS5jb20xEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0xNzA5MTgwNzM2MjNaFw0y\n" +
            "NzA5MTYwNzM2MjNaMIG6MQswCQYDVQQGEwJDTjEQMA4GA1UECAwHQmVpamluZzEQ\n" +
            "MA4GA1UEBwwHQmVpamluZzEPMA0GA1UEEQwGMTAwMDIyMRQwEgYDVQQJDAtHdW9N\n" +
            "YW9TYW5RaTEYMBYGA1UECgwPYXBmZWxib3ltc2NodWxlMRMwEQYDVQQLDApTdXBw\n" +
            "b3J0X0NBMR0wGwYJKoZIhvcNAQkBFg5odHRwLmJqQHFxLmNvbTESMBAGA1UEAwwJ\n" +
            "bG9jYWxob3N0MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE4P5FWUVWab0jYh2pTuAV\n" +
            "VlcAL7SuYhVJFbCOgFovi7zC3+5KiUoRlBRfcsZtiVou7TQW6aOiq/HiwkqA5dS7\n" +
            "RL36CONSkey9A8eTPKc7irdGMQtsbTYro/zbBPhrCLv7MAoGCCqGSM49BAMCA2gA\n" +
            "MGUCMQCExbnq6mOsUwkXxovathOu0erfv1ZpvSr5v1HKJH91gU9XKsjYLtv0Et+q\n" +
            "osfcxukCMDxKUXrOBlfn1lM4gZIOOnF5F11LYxsiPmfaWGcu+JZl5j65lu110zZe\n" +
            "fdr6//vbrw==\n" +
            "-----END CERTIFICATE-----";*/

    public static SSLSocketFactory createSocketFactory(Context context) {
        SSLContext sslContext = null;
        InputStream caInput=null;
        try {
            //添加ca根证书
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //caInput = new Buffer().writeUtf8(CA_STR).inputStream();
            //caInput = context.getAssets().open("ca.crt");
            caInput = context.getResources().openRawResource(R.raw.ca);
            Certificate ca = cf.generateCertificate(caInput);
            //添加客户端证书
            sslContext = SSLContext.getInstance("TLS");
            String keyStoreType = "PKCS12";
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            String clientCertPassword = "123123";
            keyStore.load(context.getAssets().open("client.p12"), clientCertPassword.toCharArray());

            //为客户端证书添加根证书
            keyStore.setCertificateEntry("ca", ca);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, clientCertPassword.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();


            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            mTrustManager = (X509TrustManager) trustManagers[0];

            //If we don't use client certificate just use:
            //sslContext.init(null, tmf.getTrustManagers(), null);
            sslContext.init(keyManagers, new TrustManager[]{mTrustManager}, null);
        } catch (KeyManagementException | IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }finally {
            try {
                caInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sslContext.getSocketFactory();

    }

    public static X509TrustManager mTrustManager;
}
