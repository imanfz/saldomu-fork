package com.sgo.saldomu.interfaces;

import com.sgo.saldomu.models.retrofit.ObjectModel;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterfaces {
    @POST("{url}")
    @FormUrlEncoded
    Observable<ObjectModel> PostObjectInterface(@Path(value = "url", encoded = true) String url
            , @FieldMap HashMap<String, Object> body);
}
