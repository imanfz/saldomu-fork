package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.TagihReasonModel;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FragCancelTransactionDGI extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    View v;
    Button btnProses, btnCancel;
    TextView tv_next_visit;
    Spinner sp_reason;
    EditText et_reason;
    String memberCode, commCode;
    String reason;
    private DateFormat fromFormat;
    private DateFormat toFormat;
    private DateFormat toFormat2;
    private String dateNow;
    private String dedate;
    private String date_visit;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd;

    Double latitude, longitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private ArrayList<TagihReasonModel> reasonDataList = new ArrayList<>();
    private ArrayList<String> reasonNameArrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_cancel_transaction_dgi, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        runningApp();

        et_reason = v.findViewById(R.id.et_reason);
        tv_next_visit = v.findViewById(R.id.tv_nextVisit);
        sp_reason = v.findViewById(R.id.sp_reason);
        btnProses = v.findViewById(R.id.btn_submit);
        btnCancel = v.findViewById(R.id.btn_cancel);

        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }

        fromFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("ID", "INDONESIA"));
        toFormat = new SimpleDateFormat("dd-MM-yyyy", new Locale("ID", "INDONESIA"));
        toFormat2 = new SimpleDateFormat("dd-M-yyyy", new Locale("ID", "INDONESIA"));

        Calendar c = Calendar.getInstance();
        dateNow = fromFormat.format(c.getTime());
        Timber.d("date now profile:" + dateNow);

        dpd = DatePickerDialog.newInstance(
                dobPickerSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        tv_next_visit.setOnClickListener(view -> dpd.show(getActivity().getFragmentManager(), "asd"));

        try {
            reasonDataList.clear();
            JSONArray reasonArray = new JSONArray(sp.getString(DefineValue.REJECT_REASON, ""));
            if (reasonArray.length() > 0) {
                for (int i = 0; i < reasonArray.length(); i++) {
                    JSONObject jsonObjectReason = reasonArray.getJSONObject(i);
                    reasonNameArrayList.add(formatStringCamelCase(jsonObjectReason.getString("DESCRIPTION")));
                }
            }
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, reasonNameArrayList);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_reason.setAdapter(spinAdapter);
            sp_reason.setOnItemSelectedListener(spinnerReason);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnCancel.setOnClickListener(cancelListener);
        btnProses.setOnClickListener(prosesListener);
    }

    Button.OnClickListener cancelListener = view -> getFragmentManager().popBackStack();

    Button.OnClickListener prosesListener = view -> {
        if (inputValidation()) {
            cancelDGI();
        }
    };

    private Spinner.OnItemSelectedListener spinnerReason = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            reason = sp_reason.getItemAtPosition(i).toString();
            if (reason.equalsIgnoreCase("LAINNYA")) {
                et_reason.setVisibility(View.VISIBLE);
            } else et_reason.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private DatePickerDialog.OnDateSetListener dobPickerSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            dedate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            Timber.d("masuk date picker dob");
            try {
                date_visit = fromFormat.format(toFormat2.parse(dedate));
                Timber.d("masuk date picker dob masuk tanggal : " + date_visit);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_next_visit.setText(dedate);
        }
    };

    public Boolean inputValidation() {
        int compare = 100;
        if (date_visit != null) {
            Date visit = null;
            Date now = null;
            try {
                visit = fromFormat.parse(date_visit);
                now = fromFormat.parse(dateNow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (visit != null) {
                if (now != null) {
                    compare = visit.compareTo(now);
                }
            }
            Timber.d("compare date:" + Integer.toString(compare));
        }

        if (et_reason.getVisibility() == View.VISIBLE && reason.isEmpty()) {
            et_reason.requestFocus();
            et_reason.setError("Alasan dibutuhkan!");
            return false;
        } else if (compare == 100 || tv_next_visit.getText().toString().equalsIgnoreCase(getString(R.string.choose_date))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.validation_date_empty))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else if (compare <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setMessage(getString(R.string.validation_date))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        return true;
    }

    public void cancelDGI() {
        showProgressDialog();

        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_SEARCH_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        if (reason.equalsIgnoreCase("lainnya")) {
            params.put(WebParams.REASON, et_reason.getText().toString());
        } else
            params.put(WebParams.REASON, reason);
        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.COMM_CODE, commCode);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.NEXT_VISIT_DATE, date_visit);
        params.put(WebParams.LATITUDE, latitude);
        params.put(WebParams.LONGITUDE, longitude);
        Timber.d("params cancel search DGI : " + params.toString());

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_CANCEL_SEARCH_DGI, params,
                new ObjListeners() {

                    @Override
                    public void onResponses(JSONObject response) {
                        try {
                            dismissProgressDialog();

                            Gson gson = new Gson();
                            jsonModel model = gson.fromJson(response.toString(), jsonModel.class);
                            Timber.d("response cancel search DGI : " + response.toString());
                            String code = response.getString(WebParams.ERROR_CODE);
                            String error_message = response.getString(WebParams.ERROR_MESSAGE);
                            if (code.equals(WebParams.SUCCESS_CODE)) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Alert")
                                        .setMessage(getString(R.string.cancel_transaction_dgi))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                getActivity().finish();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            } else if (code.equals(DefineValue.ERROR_9333)) {
                                Timber.d("isi response app data:" + model.getApp_data());
                                final AppDataModel appModel = model.getApp_data();
                                AlertDialogUpdateApp alertDialogUpdateApp = AlertDialogUpdateApp.getInstance();
                                alertDialogUpdateApp.showDialogUpdate(getActivity(), appModel.getType(), appModel.getPackageName(), appModel.getDownloadUrl());
                            } else if (code.equals(DefineValue.ERROR_0066)) {
                                Timber.d("isi response maintenance:" + response.toString());
                                AlertDialogMaintenance alertDialogMaintenance = AlertDialogMaintenance.getInstance();
                                alertDialogMaintenance.showDialogMaintenance(getActivity(), model.getError_message());
                            } else {
                                Toast.makeText(getActivity(), error_message, Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void runningApp() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        Timber.d("GPS Test googleapiclient : " + mGoogleApiClient.toString());
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            Timber.d("GPS Test googleapiclient connect : " + mGoogleApiClient.toString());
        }

    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2 * 8000);
        mLocationRequest.setFastestInterval(1 * 8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(DefineValue.AGENT_DISPLACEMENT);

    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        Timber.d("GPS Test checkPlayServices : " + String.valueOf(result));
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Toast.makeText(getActivity(), "GOOGLE API LOCATION CONNECTION FAILED", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        //startLocationUpdate();

        if (mGoogleApiClient != null) {
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {

                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();

                    Timber.d("Location Found" + mLastLocation.toString());
                    //googleApiClient.disconnect();
                }
            } catch (SecurityException se) {
                se.printStackTrace();
                //Timber.d(se.printStackTrace());
            }
        }
        if (bundle != null) {
            Timber.d(bundle.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatStringCamelCase(String description) {
        String[] words = description.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0))).append(words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }
}
