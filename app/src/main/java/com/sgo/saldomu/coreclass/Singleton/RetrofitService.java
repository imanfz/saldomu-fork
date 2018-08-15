package com.sgo.saldomu.coreclass.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.RetrofitInterfaces;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.securities.SHA;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.realm.RealmObject;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by SGOUser on 07/12/2017.
 */

public class RetrofitService {
    private static String hostname = "mobile-dev.espay.id";

    public static final String idService = "dev.api.mobile";
    public static final String passService = "590@dev.api.mobile!";
    public static final String PRIVATE_KEY = "590mobil3";

    public static final int DEFAULT_RETRIES_REQUEST = 3;

    private static Context mContext;
    private static RetrofitService singleton;
    Retrofit retrofit;

    private static HttpLoggingInterceptor interceptorLogging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    //
    static CompositeDisposable compositeDisposable;

    public static RetrofitService getInstance(){
        if (singleton == null){
            singleton = new RetrofitService();
        }
        return singleton;
    }

    private static CompositeDisposable getCompositeDisposable() {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        return compositeDisposable;
    }

    public static void add(Disposable disposable) {
        getCompositeDisposable().add(disposable);
    }

    public static void dispose() {
        getCompositeDisposable().dispose();
    }

    private RetrofitInterfaces BuildRetrofit(){
        return BuildRetrofit2(false);
    }

    private RetrofitInterfaces BuildRetrofit2(boolean inApps){
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(BuildGSON()))
                .baseUrl(BuildConfig.HEAD_ADDRESSS)
                .client(BuildOkHttpClients(inApps))
                .build();
        return retrofit.create(RetrofitInterfaces.class);
    }

    private Gson BuildGSON(){
        return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass() == RealmObject.class;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
    }

    private static String getBasicAuth() {
//        String stringEncode = "dev.api.mobile"+":"+"590@dev.api.mobile!";
        String stringEncode = "s4LD0mu"+":"+"WPtK9YBa?4g,rfvm(^XD/M]{25TJF8";
        byte[] encodeByte = Base64.encodeBase64(stringEncode.getBytes());
        String encode = new String(encodeByte);
        return encode.replace('+','-').replace('/','_');
    }

    private OkHttpClient BuildOkHttpClients(final boolean inApps){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(600, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.writeTimeout(600, TimeUnit.SECONDS);
        builder.connectTimeout(600, TimeUnit.SECONDS);
        builder.addInterceptor(interceptorLogging);
        builder.certificatePinner(certificatePinner);

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();

        List<ConnectionSpec> specs = new ArrayList<>();
        specs.add(spec);
        specs.add(ConnectionSpec.CLEARTEXT);

        builder.connectionSpecs(specs);

        TLSSocket sf;
        try {
            sf = new TLSSocket();
            builder.sslSocketFactory(sf, sf.systemDefaultTrustManager());
        }catch (Exception e) {
            Timber.w("exception tls socket:"+e.toString());
            throw new AssertionError(e);
        }

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {


                Request.Builder builder1 = chain.request().newBuilder();

                builder1.header("Content-Type", "application/x-www-form-urlencoded");
                if (inApps){
                    builder1.addHeader("Authorization", "Basic "+getBasicAuth());
                }

                Request url = builder1.build();

                return chain.proceed(url);
            }
        });
        return builder.build();
    }

    private static CertificatePinner certificatePinner
            = new CertificatePinner.Builder()
            .add(hostname,"sha256/UUsUINnnxiyFSr9zQdrGG9kfl9er17hIN56rmbF1LMg=")
            .add(hostname,"sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=")
            .add(hostname,"sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=")
            .add(hostname,"sha256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU")
            .build();


//    public static <S> S createService(Class<S> serviceClass,String authToken){
//
//        if(!TextUtils.isEmpty(authToken)){
//            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);
//
//            if(!client.interceptors().contains(interceptor)){
//
//                client.addInterceptor(interceptor);
//
//                client .connectTimeout(600, TimeUnit.SECONDS);
//                client .readTimeout(600, TimeUnit.SECONDS);
//                client .addInterceptor(interceptorLogging);
//                client .certificatePinner(certificatePinner);
//                client.retryOnConnectionFailure(true);
//                //OkHttp Configuration
//                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                        .tlsVersions(TlsVersion.TLS_1_2)
//                        .build();
//
//                List<ConnectionSpec> specs = new ArrayList<>();
//                specs.add(spec);
//                specs.add(ConnectionSpec.CLEARTEXT);
////                specs.add(ConnectionSpec.COMPATIBLE_TLS);
//
////                client.connectionSpecs(Collections.singletonList(spec));
//                client.connectionSpecs(specs);
//
//                TLSSocket sf;
//                try {
//                    sf = new TLSSocket();
//                    client.sslSocketFactory(sf, sf.systemDefaultTrustManager());
//                }catch (Exception e) {
//                    Timber.w("exception tls socket:"+e.toString());
//                    throw new AssertionError(e);
//                }
//
//                builder.client(client.build());
//                retrofit = builder.build();
//                Timber.d("Success build retrofit");


// #bks
//
//                try {
//                    // Get an instance of the Bouncy Castle KeyStore format
//                    KeyStore trusted = KeyStore.getInstance("BKS");
//                    // Get the raw resource, which contains the keystore with
//                    // your trusted certificates
//                    InputStream in = getmContext().getResources().openRawResource(R.raw.espayid);
//                    try {
//                        // Initialize the keystore with the provided trusted certificates
//                        // Also provide the password of the keystore
//                        trusted.load(in, PRIVATE_KEY.toCharArray());
//                        Timber.d("x5019 socket open");
//                    } finally {
//                        Timber.d("x5019 socket closed");
//                        in.close();
//                    }
//                    // Pass the keystore to the SSLSocketFactory. The factory is responsible
//                    // for the verification of the server certificate.
//                    TLSSocket sf;
//                    try {
//                        sf = new TLSSocket(trusted);
//                        client.sslSocketFactory(sf,sf.customTrustManager());
//                    } catch (KeyManagementException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    } catch (KeyStoreException e) {
//                        e.printStackTrace();
//                    }
//                    // Hostname verification from certificate
//                    // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
//                } catch (Exception e) {
//                    Timber.w("exception tls socket:"+e.toString());
//                    throw new AssertionError(e);
//                }
//                builder.client(client.build());
//                retrofit = builder.build();
//                Timber.d("Success build retrofit");
//            }
//        }
//        return retrofit.create(serviceClass);
//    }

//    /** Return the value mapped by the given key, or {@code null} if not present or null. */
//    public static String optString(JsonObject json, String key)
//    {
//        Timber.d("optString Key="+key.toString());
//        if(json.get(key).equals(null)){
//            return null;
//        }
//        else
//            return json.get(key).getAsString();
//    }

    public static HashMap<String,String> getSignatureWithParamsFCM(String gcmID, String deviceId, String appID){

        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = Md5.hashMd5(uuidnya+dtime+gcmID+deviceId+appID);
        Timber.d("isi messageSignatureFCM : " + msgnya);


        String hash = SHA.SHA1(msgnya);
        Timber.d("isi sha1 signatureFCM : " + hash);

        HashMap<String,String> params = new HashMap<>();
        params.put(WebParams.RQ_UUID, uuidnya.toString());
        params.put(WebParams.RQ_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }

    public static HashMap<String,String> getSignatureWithParams(String commID, String linknya, String user_id, String access_key){

        String webServiceName = getWebserviceName(linknya);
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = uuidnya+dtime+BuildConfig.APP_ID+webServiceName+ commID + user_id;
//        Timber.d("isi access_key :" + access_key);
//
//        Timber.d("isisnya signature :"+  webServiceName +" / "+commID+" / " +user_id);

        String hash = SHA.SHA256(access_key,msgnya);

        HashMap<String,String> params = new HashMap<>();
        params.put(WebParams.RC_UUID, uuidnya.toString());
        params.put(WebParams.RC_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);
        return params;
    }

    public static String getSignature(UUID uuidnya, String date, String WebServiceName, String noID, String apinya){
        String msgnya = uuidnya+date+BuildConfig.APP_ID+WebServiceName+noID;
        String hash = SHA.SHA256(apinya,msgnya);
        return hash;
    }

    public static UUID getUUID(){
        return UUID.randomUUID();
    }

    public static String getWebserviceName(String link){
        StringTokenizer tokens = new StringTokenizer(link, "/");
        int index = 0;
        while(index<3) {
            tokens.nextToken();
            index++;
        }
        return tokens.nextToken();
    }

//    public static String encodeURL(String url){
//        try {
//            address = URLEncoder.encode(url, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return address;
//    }

    public static boolean callSuccess(Response response){
        int code = response.code();
        return (code >= 200 && code<=400);
    }


    public static void notSuccesResponse(int responsecode){
        switch (responsecode) {
            case 401:
                Toast.makeText(mContext, mContext.getString(R.string.authentication_problem), Toast.LENGTH_SHORT).show();
                break;
            case 404:
                Toast.makeText(mContext, mContext.getString(R.string.url_not_found), Toast.LENGTH_SHORT).show();
                break;
            case 500:
                Toast.makeText(mContext, mContext.getString(R.string.server_is_broken), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mContext, mContext.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    public static String checkErrorResponseCode(int responsecode){
        String error = null;
        switch (responsecode) {
            case 401:
                error = mContext.getString(R.string.authentication_problem);
                break;
            case 404:
                error = mContext.getString(R.string.url_not_found);
                break;
            case 500:
                error = mContext.getString(R.string.server_is_broken);
                break;
            default:
                error = mContext.getString(R.string.unknown_error);
                break;
        }
        return error;
    }

    public static void networkError(Throwable throwable, Call call){
        int retryCount = 0;
        int totalRetries = 3;
        if(throwable instanceof IOException){
            Toast.makeText(mContext, mContext.getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(mContext, mContext.getString(R.string.conversion_retrofit_error), Toast.LENGTH_SHORT).show();
        }
    }



 }
