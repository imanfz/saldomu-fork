package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BBSActivity;
import com.sgo.saldomu.activities.BbsApprovalAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
import com.sgo.saldomu.activities.BbsMapViewByMemberActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.models.ShopDetail;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.sgo.saldomu.coreclass.GlobalSetting.RC_LOCATION_PERM;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragApprovalAgent.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragApprovalAgent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragApprovalAgent extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, EasyPermissions.PermissionCallbacks{
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Approval_Agent";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SecurePreferences sp;
    ProgressDialog progdialog, progdialog2;
    String flagApprove, customerId, title, gcmId, flagTxStatus, txId, memberId, shopId;
    ShopDetail shopDetail;
    List<ShopDetail> shopDetails;
    TextView tvCategoryName, tvMemberName, tvAmount, tvShop, tvCountTrx, tvTotalTrx;
    RelativeLayout rlApproval;
    Spinner spPilihan;
    ArrayAdapter<String> SpinnerAdapter;
    Button btnApprove, btnReject;
    int itemId;
    Double currentLatitude, currentLongitude;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;

    private OnFragmentInteractionListener mListener;
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int RC_GPS_REQUEST = 1;

    public FragApprovalAgent() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragApprovalAgent.
     */
    // TODO: Rename and change types and number of parameters
    public static FragApprovalAgent newInstance(String param1, String param2) {
        FragApprovalAgent fragment = new FragApprovalAgent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        title                   = getString(R.string.menu_item_title_trx_agent);

        gcmId                   = "";
        flagTxStatus            = "";
        txId                    = "";
        flagApprove             = DefineValue.STRING_NO;
        customerId              = sp.getString(DefineValue.USERID_PHONE, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_approval_agent, container, false);


        btnApprove              = (Button) v.findViewById(R.id.btnApprove);
        //btnReject               = (Button) v.findViewById(R.id.btnReject);
        tvCategoryName          = (TextView) v.findViewById(R.id.tvCategoryName);
        tvMemberName            = (TextView) v.findViewById(R.id.tvMemberName);
        tvAmount                = (TextView) v.findViewById(R.id.tvAmount);
        rlApproval              = (RelativeLayout) v.findViewById(R.id.rlApproval);
        rlApproval.setVisibility(View.GONE);

        tvCountTrx              = (TextView) v.findViewById(R.id.tvCountTrx);
        tvTotalTrx              = (TextView) v.findViewById(R.id.tvTotalTrx);

        shopDetail              = new ShopDetail();
        shopDetails             = new ArrayList<>();



        if ( !sp.getBoolean(DefineValue.IS_AGENT, false) ) {
            //is member
            Intent i = new Intent(getContext(), MainPage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }



        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");
        RequestParams params    = new RequestParams();

        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_PHONE, customerId);
        params.put(WebParams.SHOP_REMARK, gcmId);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + customerId ));

        params.put(WebParams.SIGNATURE, signature);

        MyApiClient.getListTransactionAgent(getContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {

                    String code = response.getString(WebParams.ERROR_CODE);

                    if (code.equals(WebParams.SUCCESS_CODE)) {
                        progdialog.dismiss();
                        rlApproval.setVisibility(View.VISIBLE);

                        shopDetail.setAmount(response.getString(DefineValue.KEY_AMOUNT));
                        shopDetail.setTxId(response.getString(DefineValue.TX_ID2));
                        shopDetail.setCategoryId(response.getString(DefineValue.CATEGORY_ID));
                        shopDetail.setCategoryName(response.getString(DefineValue.CATEGORY_NAME));
                        shopDetail.setCategoryCode(response.getString(DefineValue.CATEGORY_CODE));
                        shopDetail.setKeyName(response.getString(DefineValue.KEY_NAME));
                        shopDetail.setKeyAddress(response.getString(DefineValue.KEY_ADDRESS));
                        //shopDetail.setKeyDistrict(response.getString(DefineValue.KEY_DISTRICT));
                        shopDetail.setKeyAddress(response.getString(DefineValue.KEY_ADDRESS));
                        //shopDetail.setKeyProvince(response.getString(DefineValue.KEY_PROVINCE));
                        //shopDetail.setKeyCountry(response.getString(DefineValue.KEY_COUNTRY));
                        shopDetail.setCommId(response.getString(WebParams.COMM_ID));

                        shopDetail.setMemberId(response.getString(WebParams.MEMBER_ID));
                        shopDetail.setMemberCode(response.getString(WebParams.MEMBER_CODE));
                        shopDetail.setMemberName(response.getString(WebParams.MEMBER_NAME));
                        shopDetail.setMemberType(response.getString(WebParams.MEMBER_TYPE));
                        shopDetail.setShopId(response.getString(WebParams.SHOP_ID));
                        shopDetail.setShopName(response.getString(WebParams.SHOP_NAME));

                        shopDetails.add(shopDetail);

                        tvCategoryName.setText(shopDetail.getCategoryName());
                        tvMemberName.setText(response.getString(WebParams.KEY_NAME));
                        //tvShop.setText(shopDetail.getShopName());
                        tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(shopDetail.getAmount()));

                        tvCountTrx.setText(response.getString(WebParams.COUNT_TRX));
                        tvTotalTrx.setText(DefineValue.IDR + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_TRX)));



                    } else {
                        progdialog.dismiss();
                        code = response.getString(WebParams.ERROR_MESSAGE);
                        //Toast.makeText(getApplicationContext(), code, Toast.LENGTH_LONG).show();

                        rlApproval.setVisibility(View.GONE);

                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setTitle(getString(R.string.alertbox_title_information));
                        alertDialog.setMessage(getString(R.string.alertbox_message_information));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        getActivity().finish();

                                    }
                                });
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                ifFailure(throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                ifFailure(throwable);
            }

            private void ifFailure(Throwable throwable) {
                if (MyApiClient.PROD_FAILURE_FLAG)
                    Toast.makeText(getContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                progdialog.dismiss();
                Timber.w("Error Koneksi login:" + throwable.toString());

            }

        });


        //rlApproval.setVisibility(View.VISIBLE);

        /*
        tvCategoryName.setText(shopDetail.getCategoryName());
        tvMemberName.setText(shopDetail.getKeyName());
        tvAmount.setText(shopDetail.getCcyId()+" "+ CurrencyFormat.format(shopDetail.getAmount()));

        if ( shopDetails.size() == 1 ) {
            tvShop.setVisibility(View.VISIBLE);
            spPilihan.setVisibility(View.GONE);
            shopDetails.get(0).getMemberName();
            tvShop.setText(shopDetails.get(0).getMemberName());
        } else {
            tvShop.setVisibility(View.GONE);
            spPilihan.setVisibility(View.VISIBLE);
        }
        */

        btnApprove.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        flagTxStatus = DefineValue.STRING_ACCEPT;

                        if ( shopDetails.size() > 1 ) {
                            itemId  = spPilihan.getSelectedItemPosition();

                        } else {
                            itemId = 0;
                        }

                        if ( shopDetails.size() > 0 ) {
                            shopId = shopDetails.get(itemId).getShopId();
                            memberId = shopDetails.get(itemId).getMemberId();
                            gcmId = "";

                            updateTrxAgent();
                        }
                    }
                }
        );

        /*
        btnReject.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle(getString(R.string.alertbox_title_information));


                        alertDialog.setMessage(getString(R.string.message_notif_cancel_trx_by_agent));



                        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        progdialog2              = DefinedDialog.CreateProgressDialog(getContext(), "");
                                        flagTxStatus = DefineValue.STRING_CANCEL;

                                        if ( shopDetails.size() > 1 ) {
                                            itemId  = spPilihan.getSelectedItemPosition();

                                        } else {
                                            itemId = 0;
                                        }

                                        if ( shopDetails.size() > 0 ) {
                                            shopId = shopDetails.get(itemId).getShopId();
                                            memberId = shopDetails.get(itemId).getMemberId();
                                            updateTrxAgent();
                                        }

                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();



                                    }
                                });
                        alertDialog.show();


                    }
                }
        );
        */

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            //googleApiClient.disconnect();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void updateTrxAgent() {


        //startActivity(new Intent(getApplicationContext(), BbsMapViewByAgentActivity.class));

        if ( currentLatitude == null || currentLongitude == null ) {
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(getString(R.string.alertbox_title_information));
            alertDialog.setCancelable(false);

            alertDialog.setMessage(getString(R.string.message_notif_latitude_not_found));



            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                           dialog.dismiss();
                        }
                    });

            alertDialog.show();

        } else {

            progdialog2              = DefinedDialog.CreateProgressDialog(getContext(), "");
            RequestParams params3 = new RequestParams();
            UUID rcUUID = UUID.randomUUID();
            String dtime = DateTimeFormat.getCurrentDateTime();

            params3.put(WebParams.RC_UUID, rcUUID);
            params3.put(WebParams.RC_DATETIME, dtime);
            params3.put(WebParams.APP_ID, BuildConfig.AppID);
            params3.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params3.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params3.put(WebParams.TX_ID, shopDetails.get(itemId).getTxId());
            params3.put(WebParams.MEMBER_ID, memberId);
            params3.put(WebParams.SHOP_ID, shopId);
            params3.put(WebParams.TX_STATUS, flagTxStatus);

            if (flagTxStatus.equals(DefineValue.STRING_ACCEPT)) {
                params3.put(WebParams.KEY_VALUE, gcmId);
                params3.put(WebParams.LATITUDE, currentLatitude);
                params3.put(WebParams.LONGITUDE, currentLongitude);
            }

            String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + shopDetails.get(itemId).getTxId() + memberId + shopId + flagTxStatus));

            params3.put(WebParams.SIGNATURE, signature);

            MyApiClient.updateTransactionAgent(getContext(), params3, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    progdialog2.dismiss();

                    try {

                        String code = response.getString(WebParams.ERROR_CODE);
                        if (code.equals(WebParams.SUCCESS_CODE)) {

                            if (flagTxStatus.equals(DefineValue.STRING_ACCEPT)) {
                                SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
                                SecurePreferences.Editor mEditor = prefs.edit();
                                mEditor.putString(DefineValue.BBS_MEMBER_ID, memberId);
                                mEditor.putString(DefineValue.BBS_SHOP_ID, shopId);
                                mEditor.putString(DefineValue.BBS_TX_ID, shopDetails.get(itemId).getTxId());
                                mEditor.putDouble(DefineValue.AGENT_LATITUDE, currentLatitude);
                                mEditor.putDouble(DefineValue.AGENT_LONGITUDE, currentLongitude);
                                mEditor.putString(DefineValue.KEY_CCY, response.getString(DefineValue.KEY_CCY));
                                mEditor.putString(DefineValue.KEY_AMOUNT, response.getString(DefineValue.KEY_AMOUNT));
                                mEditor.putString(DefineValue.KEY_ADDRESS, response.getString(DefineValue.KEY_ADDRESS));
                                mEditor.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));
                                mEditor.putString(DefineValue.KEY_NAME, response.getString(DefineValue.KEY_NAME));
                                mEditor.putDouble(DefineValue.BENEF_LATITUDE, response.getDouble(DefineValue.KEY_LATITUDE));
                                mEditor.putDouble(DefineValue.BENEF_LONGITUDE, response.getDouble(DefineValue.KEY_LONGITUDE));
                                mEditor.apply();

                                Intent i = new Intent(getContext(), BbsMapViewByAgentActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                getActivity().finish();
                            } else {
                            /*Bundle bundle = new Bundle();
                            bundle.putInt(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);

                            Intent intent = new Intent(getContext(), BBSActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtras(bundle);
                            startActivity(intent);*/

                                Intent i = new Intent(getContext(), MainPage.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                getActivity().finish();
                            }
                        } else {
                            code = response.getString(WebParams.ERROR_MESSAGE);
                            Toast.makeText(getContext(), code, Toast.LENGTH_LONG).show();

                        /*Bundle bundle = new Bundle();
                        bundle.putInt(DefineValue.INDEX, BBSActivity.BBSTRXAGENT);

                        Intent intent = new Intent(getContext(), BBSActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtras(bundle);
                        startActivity(intent);*/

                            Intent i = new Intent(getContext(), MainPage.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    ifFailure(throwable);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    ifFailure(throwable);
                }

                private void ifFailure(Throwable throwable) {
                    if (MyApiClient.PROD_FAILURE_FLAG)
                        Toast.makeText(getContext(), getString(R.string.network_connection_failure_toast), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), throwable.toString(), Toast.LENGTH_SHORT).show();

                    progdialog2.dismiss();
                    Timber.w("Error Koneksi login:" + throwable.toString());

                }

            });

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        //startLocationUpdate();

        if ( googleApiClient != null ) {
            try {
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if (lastLocation == null) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
                } else {

                    currentLatitude = lastLocation.getLatitude();
                    currentLongitude = lastLocation.getLongitude();

                    Timber.d("Location Found" + lastLocation.toString());
                    btnApprove.setEnabled(true);
                    //googleApiClient.disconnect();
                }
            } catch (SecurityException se) {
                se.printStackTrace();
                //Timber.d(se.printStackTrace());
            }
        }
        if (bundle!=null) {
            Timber.d(bundle.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        currentLatitude = lastLocation.getLatitude();
        currentLongitude = lastLocation.getLongitude();
        btnApprove.setEnabled(true);
        //googleApiClient.disconnect();

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getContext());
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result, DefineValue.REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            }

            return false;
        }

        return true;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setFastestInterval(DefineValue.INTERVAL_LOCATION_REQUEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DefineValue.DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        switch(requestCode) {
            //case RC_LOCATION_PERM:
            case RC_LOCATION_PERM:
                if ( !GlobalSetting.isLocationEnabled(getContext()) ) {
                    showAlertEnabledGPS();
                } else {
                    runningApp();
                }
                break;
        }


    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        startActivity(new Intent(getActivity(), MainPage.class));
        getActivity().finish();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            if ( !GlobalSetting.isLocationEnabled(getContext()) ) {
                showAlertEnabledGPS();
            } else {
                //runningApp();
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, perms);
        }




        if ( checkPlayServices() ) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        try {
            googleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( !GlobalSetting.isLocationEnabled(getContext()) )
        {
            showAlertEnabledGPS();
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }

            try {
                googleApiClient.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }


            /*Fragment currentFragment = getFragmentManager().findFragmentByTag(FragApprovalAgent.TAG);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.detach(currentFragment);
            fragmentTransaction.attach(currentFragment);
            fragmentTransaction.commit();*/

        }
    }

    private void showAlertEnabledGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivityForResult(ilocation, RC_GPS_REQUEST);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        getActivity().startActivity(new Intent(getContext(), MainPage.class));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void runningApp() {
        Fragment frg = null;
        frg = getFragmentManager().findFragmentByTag(FragApprovalAgent.TAG);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commitAllowingStateLoss();
    }
}
