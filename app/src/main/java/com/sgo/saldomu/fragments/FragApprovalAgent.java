package com.sgo.saldomu.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.BbsMapViewByAgentActivity;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlobalSetting;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;
import com.sgo.saldomu.coreclass.Singleton.RetrofitService;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;
import com.sgo.saldomu.interfaces.ObjListeners;
import com.sgo.saldomu.models.ShopDetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

import static com.sgo.saldomu.coreclass.GlobalSetting.RC_LOCATION_PERM;

public class FragApprovalAgent extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, EasyPermissions.PermissionCallbacks {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Approval_Agent";
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SecurePreferences sp;
    ProgressDialog progdialog, progdialog2;
    String customerId, gcmId, flagTxStatus, memberId, shopId;
    ShopDetail shopDetail;
    List<ShopDetail> shopDetails;
    TextView tvCategoryName, tvMemberName, tvAmount, tvCountTrx, tvTotalTrx, tvBbsNote, tvAcctLabel, tvAcctName;
    RelativeLayout rlApproval;
    Button btnApprove;
    int itemId;
    Double currentLatitude, currentLongitude;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest mLocationRequest;

    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int RC_GPS_REQUEST = 1;

    public FragApprovalAgent() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("Flag Login Approvalagent ");
        sp = CustomSecurePref.getInstance().getmSecurePrefs();

        gcmId = "";
        flagTxStatus = "";

        customerId = sp.getString(DefineValue.USERID_PHONE, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_approval_agent, container, false);

        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if (flagLogin == null)
            flagLogin = DefineValue.STRING_NO;

        if (flagLogin.equals(DefineValue.STRING_NO)) {
            getActivity().finish();
        } else {
            String notifDataNextLogin = sp.getString(DefineValue.NOTIF_DATA_NEXT_LOGIN, "");
            if (!notifDataNextLogin.equals("")) {
                sp.edit().remove(DefineValue.NOTIF_DATA_NEXT_LOGIN).commit();
            }
        }

        btnApprove = (Button) v.findViewById(R.id.btnApprove);
        //btnReject               = (Button) v.findViewById(R.id.btnReject);
        tvCategoryName = (TextView) v.findViewById(R.id.tvCategoryName);
        tvMemberName = (TextView) v.findViewById(R.id.tvMemberName);
        tvAmount = (TextView) v.findViewById(R.id.tvAmount);
        tvBbsNote = (TextView) v.findViewById(R.id.tvBbsNote);
        rlApproval = (RelativeLayout) v.findViewById(R.id.rlApproval);
        rlApproval.setVisibility(View.GONE);

        tvCountTrx = (TextView) v.findViewById(R.id.tvCountTrx);
        tvTotalTrx = (TextView) v.findViewById(R.id.tvTotalTrx);
        tvAcctLabel = (TextView) v.findViewById(R.id.tvAcctLabel);
        tvAcctName = (TextView) v.findViewById(R.id.tvAcctName);
        //rbMemberRating          = (RatingBar) v.findViewById(R.id.rbMemberRating);
        //ivPPMember              = (ImageView) v.findViewById(R.id.ivPPMember);

        //LayerDrawable stars = (LayerDrawable) rbMemberRating.getProgressDrawable();
        // Filled stars
        //setRatingStarColor(stars.getDrawable(2), ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));


        shopDetail = new ShopDetail();
        shopDetails = new ArrayList<>();


        if (!sp.getBoolean(DefineValue.IS_AGENT, false)) {
            //is member
            Intent i = new Intent(getContext(), MainPage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }


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

                        itemId = 0;

                        if (shopDetails.size() > 0) {
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

    private void updateTrxAgent() {
        if (currentLatitude == null || currentLongitude == null) {
            androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
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

            progdialog2 = DefinedDialog.CreateProgressDialog(getContext(), getString(R.string.please_wait));
            String extraSignature = shopDetails.get(itemId).getTxId() + memberId + shopId + flagTxStatus;
            HashMap<String, Object> params3 = RetrofitService.getInstance().getSignature(MyApiClient.LINK_UPDATE_APPROVAL_TRX_AGENT,
                    extraSignature);

            params3.put(WebParams.APP_ID, BuildConfig.APP_ID);
            params3.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
            params3.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
            params3.put(WebParams.TX_ID, shopDetails.get(itemId).getTxId());
            params3.put(WebParams.MEMBER_ID, memberId);
            params3.put(WebParams.SHOP_ID, shopId);
            params3.put(WebParams.TX_STATUS, flagTxStatus);
            params3.put(WebParams.USER_ID, sp.getString(DefineValue.USERID_PHONE, ""));

            if (flagTxStatus.equals(DefineValue.STRING_ACCEPT)) {
                params3.put(WebParams.KEY_VALUE, gcmId);
                params3.put(WebParams.LATITUDE, currentLatitude);
                params3.put(WebParams.LONGITUDE, currentLongitude);
            }

            Log.d("paarm", "param: " + params3.toString());

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_UPDATE_APPROVAL_TRX_AGENT, params3,
                    new ObjListeners() {
                        @Override
                        public void onResponses(JSONObject response) {
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
                                        mEditor.putString(DefineValue.CATEGORY_NAME, shopDetails.get(itemId).getCategoryName());
                                        mEditor.putString(DefineValue.KEY_CCY, response.getString(DefineValue.KEY_CCY));
                                        mEditor.putString(DefineValue.KEY_AMOUNT, response.getString(DefineValue.KEY_AMOUNT));
                                        mEditor.putString(DefineValue.KEY_ADDRESS, response.getString(DefineValue.KEY_ADDRESS));
                                        mEditor.putString(DefineValue.KEY_CODE, response.getString(DefineValue.KEY_CODE));
                                        mEditor.putString(DefineValue.KEY_NAME, response.getString(DefineValue.KEY_NAME));
                                        mEditor.putDouble(DefineValue.BENEF_LATITUDE, response.getDouble(DefineValue.KEY_LATITUDE));
                                        mEditor.putDouble(DefineValue.BENEF_LONGITUDE, response.getDouble(DefineValue.KEY_LONGITUDE));
                                        mEditor.apply();

                                        Intent i = new Intent(getContext(), BbsMapViewByAgentActivity.class);
                                        i.putExtra(DefineValue.AOD_TX_ID, shopDetails.get(itemId).getTxId());
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
                        public void onError(Throwable throwable) {

                        }

                        @Override
                        public void onComplete() {
                            progdialog2.dismiss();

                        }
                    });

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected Started");
        //startLocationUpdate();

        if (googleApiClient != null) {
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
        if (bundle != null) {
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

    private boolean checkPlayServices() {
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

    protected synchronized void buildGoogleApiClient() {
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

        switch (requestCode) {
            //case RC_LOCATION_PERM:
            case RC_LOCATION_PERM:
                if (!GlobalSetting.isLocationEnabled(getContext())) {
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
    }

    @Override
    public void onStart() {
        super.onStart();

        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            if (!GlobalSetting.isLocationEnabled(getContext())) {
                showAlertEnabledGPS();
            } else {
                runningApp();
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM, perms);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!GlobalSetting.isLocationEnabled(getContext())) {
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
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Intent ilocation = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivityForResult(ilocation, RC_GPS_REQUEST);

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        getActivity().startActivity(new Intent(getContext(), MainPage.class));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void runningApp() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        try {
            googleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Fragment frg = null;
        frg = getFragmentManager().findFragmentByTag(FragApprovalAgent.TAG);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commitAllowingStateLoss();*/

        progdialog = DefinedDialog.CreateProgressDialog(getContext(), getString(R.string.please_wait));

        HashMap<String, Object> params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_TRANSACTION_AGENT);


        params.put(WebParams.APP_ID, BuildConfig.APP_ID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.SHOP_PHONE, customerId);
        params.put(WebParams.SHOP_REMARK, gcmId);
        params.put(WebParams.USER_ID, customerId);

        RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_TRANSACTION_AGENT, params,
                new ObjListeners() {
                    @Override
                    public void onResponses(JSONObject response) {
                        try {

                            String code = response.getString(WebParams.ERROR_CODE);

                            if (code.equals(WebParams.SUCCESS_CODE)) {
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

                                shopId = response.getString(WebParams.SHOP_ID);
                                memberId = response.getString(WebParams.MEMBER_ID);
                                shopDetails.add(shopDetail);

                                tvCategoryName.setText(shopDetail.getCategoryName());
                                tvMemberName.setText(response.getString(WebParams.KEY_NAME));
                                //tvShop.setText(shopDetail.getShopName());
                                tvAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(shopDetail.getAmount()));

                                if (response.getString(WebParams.BBS_NOTE) != null) {
                                    tvBbsNote.setText(response.getString(WebParams.BBS_NOTE));
                                } else {
                                    tvBbsNote.setText("");
                                }
                                tvCountTrx.setText(response.getString(WebParams.COUNT_TRX));
                                tvTotalTrx.setText(DefineValue.IDR + " " + CurrencyFormat.format(response.getString(WebParams.TOTAL_TRX)));

                                if (response.getString(WebParams.SCHEME_CODE).equals(DefineValue.CTA)) {
                                    tvAcctLabel.setText(getString(R.string.bbs_setor_ke));
                                } else {
                                    tvAcctLabel.setText(getString(R.string.bbs_tarik_dari));
                                }

                                tvAcctName.setText(response.getString(WebParams.PRODUCT_NAME));
                            } else {
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
                        progdialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        progdialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        progdialog.dismiss();
                    }
                });
    }
}
