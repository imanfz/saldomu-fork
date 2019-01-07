package com.sgo.saldomu.coreclass.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.CoreApp;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.OkHttpTLSSocketFactory;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.interfaces.ArrListeners;
import com.sgo.saldomu.interfaces.ObjListener;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.interfaces.ResponseListener;
import com.sgo.saldomu.interfaces.RetrofitInterfaces;
import com.sgo.saldomu.securities.Md5;
import com.sgo.saldomu.securities.SHA;
import com.sgo.saldomu.widgets.TLSSocket;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmObject;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.HttpException;
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
    Gson gson;
    private SecurePreferences sp;

    //URL
    public static String LINK_LOGIN = "saldomu/MemberLogin/SignIn";
    public static String LINK_LIST_MEMBER = "saldomu/Member/Retrieve";

    private static HttpLoggingInterceptor interceptorLogging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    //
    static CompositeDisposable compositeDisposable;

    public static RetrofitService getInstance() {
        if (singleton == null) {
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

    private RetrofitInterfaces BuildRetrofit() {
        return BuildRetrofit2();
    }

    private RetrofitInterfaces BuildRetrofit2() {
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(BuildGSON()))
                .baseUrl(BuildConfig.HEAD_ADDRESSS)
                .client(BuildOkHttpClients())
                .build();
        return retrofit.create(RetrofitInterfaces.class);
    }

    private Gson BuildGSON() {
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
        String stringEncode = "s4LD0mu" + ":" + "WPtK9YBa?4g,rfvm(^XD/M]{25TJF8";
        byte[] encodeByte = Base64.encodeBase64(stringEncode.getBytes());
        String encode = new String(encodeByte);
        return encode.replace('+', '-').replace('/', '_');
    }

    private OkHttpClient BuildOkHttpClients() {
        return BuildOkHttpClient("application/x-www-form-urlencoded");
    }

    private OkHttpClient BuildOkHttpClient2() {
        return BuildOkHttpClient("application/x-www-form-urlencoded");
    }

    private OkHttpClient BuildOkHttpClient(final String content_type) {
        TrustManager[] trustManagers = new TrustManager[0];
        try {

            KeyStore keyStore = KeyStore.getInstance("BKS");
            InputStream is = CoreApp.getAppContext().getResources().openRawResource(R.raw.saldomucom);

            keyStore.load(is, PRIVATE_KEY.toCharArray());

            is.close();

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, PRIVATE_KEY.toCharArray());

            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            trustManagers = tmf.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException(
                        "Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(10, TimeUnit.MINUTES);
        builder.retryOnConnectionFailure(true);
        builder.writeTimeout(10, TimeUnit.MINUTES);
        builder.connectTimeout(10, TimeUnit.MINUTES);
        builder.addInterceptor(interceptorLogging);
        builder.certificatePinner(certificatePinner);
        try {
            builder.sslSocketFactory(new OkHttpTLSSocketFactory(CoreApp.getAppContext()), (X509TrustManager) trustManagers[0]);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

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
        } catch (Exception e) {
            Timber.w("exception tls socket:" + e.toString());
            throw new AssertionError(e);
        }

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {


                Request.Builder builder1 = chain.request().newBuilder();

                builder1.header("Content-Type", content_type);
//                if (inApps){
                builder1.addHeader("Authorization", "Basic " + getBasicAuth());
//                }

                Request url = builder1.build();

                return chain.proceed(url);
            }
        });
        return builder.build();
    }


    private static CertificatePinner certificatePinner
            = new CertificatePinner.Builder()
            .add(hostname, "sha256/UUsUINnnxiyFSr9zQdrGG9kfl9er17hIN56rmbF1LMg=")
            .add(hostname, "sha256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=")
            .add(hostname, "sha256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=")
            .add(hostname, "sha256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU")
            .build();

    public static UUID getUUID() {
        return UUID.randomUUID();
    }

    public static String getWebserviceName(String link) {
        return link.substring(link.indexOf("saldomu/"));
    }

    public HashMap<String, Object> getSignatureSecretKey(String linknya, String extraSignature) {
        return getSignatures(MyApiClient.COMM_ID, "", linknya, BuildConfig.SECRET_KEY, extraSignature);
    }

    public HashMap<String, Object> getSignature(String linknya) {
        return getInstance().getSignatures(getCommIdLogin(), getUserPhoneId(), linknya, getAccessKey(), "");
    }

    public HashMap<String, Object> getSignature(String linknya, String extraSignature) {
        return getInstance().getSignatures(getCommIdLogin(), getUserPhoneId(), linknya, getAccessKey(), extraSignature);
    }

    public HashMap<String, Object> getSignaturePulsa(String linknya, String extraSignature) {
        return getInstance().getSignatures(MyApiClient.COMM_ID_PULSA, getUserPhoneId(), linknya, getAccessKey(), extraSignature);
    }

    private HashMap<String, Object> getSignatures(String commid, String userphoneid, String linknya, String secretKey
            , String extraSignature) {
        String webServiceName = getWebserviceName(linknya);
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = uuidnya + dtime + BuildConfig.APP_ID + webServiceName + commid + userphoneid + extraSignature;
        String hash = SHA.SHA256(secretKey, msgnya);

        Log.d("myapiclient retrofit", "msg : " + msgnya + ", hashed : " + hash + ", access key : " + secretKey);

        HashMap<String, Object> params = new HashMap<>();
        params.put(WebParams.RC_UUID, uuidnya);
        params.put(WebParams.RC_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }

    public HashMap<String, Object> getSignatureWithParamsFCM(String gcmID, String deviceId, String appID) {


        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = Md5.hashMd5(uuidnya + dtime + gcmID + deviceId + appID);
        Timber.d("isi messageSignatureFCM : " + msgnya);


        String hash = SHA.SHA1(msgnya);
        Timber.d("isi sha1 signatureFCM : " + hash);

        HashMap<String, Object> params = new HashMap<>();
        params.put(WebParams.RQ_UUID, uuidnya);
        params.put(WebParams.RQ_DTIME, dtime);
        params.put(WebParams.SIGNATURE, hash);

        return params;
    }

    public HashMap<String, RequestBody> getSignature2(String linknya, String extra) {
        return getInstance().getSignatures2(getCommIdLogin(), getUserPhoneId(), linknya, getAccessKey(), extra);
    }

    private HashMap<String, RequestBody> getSignatures2(String commid, String userphoneid, String linknya, String secretKey
            , String extraSignature) {
        String webServiceName = getWebserviceName(linknya);
        UUID uuidnya = getUUID();
        String dtime = DateTimeFormat.getCurrentDateTime();
        String msgnya = uuidnya + dtime + BuildConfig.APP_ID + webServiceName + commid + userphoneid + extraSignature;
        String hash = SHA.SHA256(secretKey, msgnya);

        Log.d("myapiclient retrofit", "msg : " + msgnya + ", hashed : " + hash);

        HashMap<String, RequestBody> params = new HashMap<>();
        RequestBody req1 = RequestBody.create(MediaType.parse("text/plain"),
                uuidnya.toString());
        RequestBody req2 = RequestBody.create(MediaType.parse("text/plain"),
                dtime);
        RequestBody req3 = RequestBody.create(MediaType.parse("text/plain"),
                hash);
        params.put(WebParams.RC_UUID, req1);
        params.put(WebParams.RC_DTIME, req2);
        params.put(WebParams.SIGNATURE, req3);

        return params;
    }

    public String getAccessKey() {
        return getInstance().getSecurePref().getString(DefineValue.ACCESS_KEY, "");
    }

    public String getCommIdLogin() {
        return getInstance().getSecurePref().getString(DefineValue.COMMUNITY_ID, "");
    }

    public String getUserPhoneId() {
        return getInstance().getSecurePref().getString(DefineValue.USERID_PHONE, "");
    }

    private SecurePreferences getSecurePref() {
        if (sp == null)
            sp = CustomSecurePref.getInstance().getmSecurePrefs();
        return sp;
    }

    private JsonObject getErrorMessage(Throwable e) {
        JsonObject error = new JsonObject();
        if (e instanceof HttpException) {

            ResponseBody body = ((HttpException) e).response().errorBody();
            Response resp = ((HttpException) e).response().raw();
            Converter<ResponseBody, JsonObject> errorConverter = retrofit.responseBodyConverter(JsonObject.class, new Annotation[0]);

            assert body != null;
            try {
                error = errorConverter.convert(body);
            } catch (IOException e1) {
                error.addProperty("error_code", resp.code());
                error.addProperty("error_message", resp.message());

                e1.printStackTrace();
            }
        } else {
            error.addProperty("error_code", "1111");
            error.addProperty("error_message", e.getMessage());

        }

        error.addProperty("on_error", true);

        return error;
    }

    public void PostObjectRequest(String link, HashMap<String, Object> param, final ObjListener listener) {
        BuildRetrofit().PostObjectInterface(link, param).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);
                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        listener.onResponses(obj);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onResponses(getErrorMessage(e));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void PostObjectRequest(String link, HashMap<String, Object> param, final ResponseListener listener) {
        BuildRetrofit().PostObjectInterface(link, param).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);
                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        listener.onResponses(obj);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Context context = CoreApp.getAppContext();
                        if (context != null) {
                            if (MyApiClient.PROD_FAILURE_FLAG)
                                Toast.makeText(CoreApp.getAppContext(), CoreApp.getAppContext().getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                });
    }

    public void PostJsonObjRequest(String link, HashMap<String, Object> param, final ObjListeners listener) {
        BuildRetrofit().PostObjectInterface(link, param).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);
                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        try {
                            listener.onResponses(new JSONObject(getGson().toJson(obj)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Toast.makeText(CoreApp.getAppContext(),
                                    CoreApp.getAppContext().getResources().getString(R.string.network_connection_failure_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                });
    }

    public void GetObjectRequest(String link, final ObjListeners listener) {
        BuildRetrofit().GetObjectInterface(link).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);

                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        try {
                            listener.onResponses(new JSONObject(getGson().toJson(obj)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Toast.makeText(CoreApp.getAppContext(),
                                    CoreApp.getAppContext().getResources().getString(R.string.network_connection_failure_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                });
    }

    public void QueryRequest(String link, HashMap<String, Object> queryMap, final ObjListeners listener) {
        BuildRetrofit().QueryInterface(link, queryMap).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);

                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        try {
                            listener.onResponses(new JSONObject(getGson().toJson(obj)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Toast.makeText(CoreApp.getAppContext(),
                                    CoreApp.getAppContext().getResources().getString(R.string.network_connection_failure_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                });
    }

    public void GetArrayRequest(String link, final ArrListeners listener) {
        BuildRetrofit().GetArrayInterface(link).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonArray>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);

                    }

                    @Override
                    public void onNext(JsonArray obj) {
                        try {
                            listener.onResponses(new JSONArray(getGson().toJson(obj)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Toast.makeText(CoreApp.getAppContext(),
                                    CoreApp.getAppContext().getResources().getString(R.string.network_connection_failure_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        listener.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        listener.onComplete();
                    }
                });
    }

    public void GetObjectRequest(String link, final ResponseListener listener) {
        BuildRetrofit().GetObjectInterface(link).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);

                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        listener.onResponses(obj);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (MyApiClient.PROD_FAILURE_FLAG) {
                            Toast.makeText(CoreApp.getAppContext(),
                                    CoreApp.getAppContext().getResources().getString(R.string.network_connection_failure_toast),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CoreApp.getAppContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        listener.onResponses(getErrorMessage(e));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void MultiPartRequest(String link, HashMap<String, RequestBody> param,
                                 MultipartBody.Part file, final ObjListener listener) {
        BuildRetrofit().MultiPartInterface(link, param, file).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(2)
                .subscribe(new Observer<JsonObject>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        getCompositeDisposable().add(d);

                    }

                    @Override
                    public void onNext(JsonObject obj) {
                        listener.onResponses(obj);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onResponses(getErrorMessage(e));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }



    public Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

}
