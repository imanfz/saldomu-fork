package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.adapter.CancelInvoiceAdapter;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.Singleton.DataManager;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.AlertDialogMaintenance;
import com.sgo.saldomu.dialogs.AlertDialogUpdateApp;
import com.sgo.saldomu.dialogs.NextVisitDateDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.Invoice;
import com.sgo.saldomu.models.InvoiceDGI;
import com.sgo.saldomu.models.retrofit.AppDataModel;
import com.sgo.saldomu.models.retrofit.jsonModel;
import com.sgo.saldomu.widgets.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.Observable;
import timber.log.Timber;

public class CancelInvoiceFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    String memberCode, commCode;
    String reason;
    Double latitude, longitude;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private String date_visit;
    private ArrayList<InvoiceDGI> invoiceDGIModelArrayList;
    private ArrayList<Invoice> selectedCancelInvoice = new ArrayList<>();
    private ArrayList<String> reasonCodeArrayList = new ArrayList<>();
    private ArrayList<String> reasonNameArrayList = new ArrayList<>();
    CancelInvoiceAdapter adapter;

    RecyclerView recycler_view;
    Button cancelButton;
    View view;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cancel_invoice, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        runningApp();

        recycler_view = view.findViewById(R.id.recycler_view);
        cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> callCancelInvoice());

        Bundle bundle = getArguments();
        if (bundle != null) {
            memberCode = bundle.getString(DefineValue.MEMBER_CODE, "");
            commCode = bundle.getString(DefineValue.COMMUNITY_CODE, "");
        }

        JSONArray reasonArray = null;
        try {
            reasonArray = new JSONArray(sp.getString(DefineValue.REJECT_REASON, ""));
            if (reasonArray.length() > 0) {
                this.reasonCodeArrayList.add("00");
                this.reasonNameArrayList.add(getContext().getString(R.string.choose_reason));
                for (int i = 0; i < reasonArray.length(); i++) {
                    JSONObject jsonObjectReason;
                    jsonObjectReason = reasonArray.getJSONObject(i);
                    reasonCodeArrayList.add(formatStringCamelCase(jsonObjectReason.getString("CODE")));
                    reasonNameArrayList.add(formatStringCamelCase(jsonObjectReason.getString("DESCRIPTION")));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        initAdapter();
    }

    void initAdapter() {
        invoiceDGIModelArrayList = new ArrayList<>();
        invoiceDGIModelArrayList.addAll(DataManager.getInstance().getListInvoice());

        adapter = new CancelInvoiceAdapter(invoiceDGIModelArrayList, reasonCodeArrayList, reasonNameArrayList, new CancelInvoiceAdapter.OnItemClick() {
            @Override
            public void onEdit(Invoice obj) {
                Observable.fromIterable(selectedCancelInvoice)
                        .distinct()
                        .map(item -> {
                                    if (item.getInvoice_number().equals(obj.getInvoice_number())) {
                                        item.setReason_code(obj.getReason_code());
                                        item.setReason_description(obj.getReason_description());
                                    }
                                    return item;
                                }
                        )
                        .toList()
                        .subscribe(contactLists -> {
                            selectedCancelInvoice.clear();
                            selectedCancelInvoice.addAll(contactLists);
                        }, e -> {
                            Toast.makeText(getContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        }).dispose();
            }

            @Override
            public void onCheck(Invoice obj) {
                selectedCancelInvoice.add(obj);
            }

            @Override
            public void onUncheck(Invoice obj) {
                selectedCancelInvoice.remove(obj);
            }
        });
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(lm);
        recycler_view.setAdapter(adapter);
    }

    void callCancelInvoice() {
        Log.e("see", selectedCancelInvoice.size() + " callCancelInvoice: " + new Gson().toJson(selectedCancelInvoice.toArray()));
        if (selectedCancelInvoice.size() == 0) {
            Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.no_item_selected), Toast.LENGTH_SHORT).show();
            return;
        }


        for (Invoice invoice : selectedCancelInvoice) {
            if (invoice.getReason_code().equals("00")) {
                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.please_choose_reason), Toast.LENGTH_SHORT).show();
                return;
            }
            if (invoice.getReason_description().equals("") && invoice.getReason_code().equals(reasonCodeArrayList.get(reasonCodeArrayList.size()-1))) {
                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.please_input_description), Toast.LENGTH_SHORT).show();
                return;
            }
        }


        NextVisitDateDialog dialog = NextVisitDateDialog.newDialog((dialog1, date) -> {
            onSubmit(date);
            dialog1.dismiss();
        });

        dialog.show(getFragManager(), "Next Visit");
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

    private void onSubmit(String date) {
        showProgressDialog();

        params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_CANCEL_SEARCH_DGI, extraSignature);

        params.put(WebParams.APP_ID, BuildConfig.APP_ID);

        //params.put(WebParams.REASON, reason);
        params.put(WebParams.INVOICES, new Gson().toJson(selectedCancelInvoice.toArray()));
        params.put(WebParams.MEMBER_CODE, memberCode);
        params.put(WebParams.COMM_CODE, commCode);
        params.put(WebParams.USER_ID, userPhoneID);
        params.put(WebParams.NEXT_VISIT_DATE, date);
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
                                dismissProgressDialog();
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
                        dismissProgressDialog();
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
}
