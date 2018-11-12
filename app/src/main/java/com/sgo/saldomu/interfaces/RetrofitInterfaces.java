package com.sgo.saldomu.interfaces;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface RetrofitInterfaces {
    @POST("{url}")
    @FormUrlEncoded
    Observable<JsonObject> PostObjectInterface(@Path(value = "url", encoded = true) String url,
                                               @FieldMap HashMap<String, Object> body);
//            , @FieldMap Map<String, Object> body);

    @POST("{url}")
//    @FormUrlEncoded
    Observable<JsonObject> GetObjectInterface(@Path(value = "url", encoded = true) String url);

    @POST("{url}")
    @FormUrlEncoded
    Observable<JsonArray> GetArrayInterface(@Path(value = "url", encoded = true) String url);

    @Multipart
    @POST("{url}")
    Observable<JsonObject> MultiPartInterface(@Path(value = "url", encoded = true) String url,
                                              @PartMap Map<String, RequestBody> param,
                                              @Part MultipartBody.Part filePart);
}
