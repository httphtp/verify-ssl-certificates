package com.ersan.androidhttpsclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });

    }


    private void fetchData() {
        SSLSocketFactory sf= SSLHelper.createSocketFactory(getApplicationContext());

       /* ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256)
                .build();*/

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sf,SSLHelper.mTrustManager)//获取SSLSocketFactory
                .hostnameVerifier(new UnSafeHostnameVerifier())//添加hostName验证器
                /*.connectionSpecs(Collections.singletonList(spec))*/
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://192.168.12.64:8081")//填写自己服务器IP
                //.addConverterFactory(GsonConverterFactory.create())//添加 json 转换器
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//添加 RxJava 适配器
                .client(okHttpClient)
                .build();
        ReqInterface reqInterface = retrofit.create(ReqInterface.class);

        reqInterface.test()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String response) {
                        Log.i("HttpDroidReqGoServer", "response=" + response);
                        final TextView msg = (TextView) findViewById(R.id.msg);
                        msg.setText("response=" + response);
                    }
                });
    }

    private class UnSafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            Log.i("AndroidHttpsclient", "verify hostname="+hostname);
            return  true;//自行添加判断逻辑，true->Safe，false->unsafe
        }
    }
}
