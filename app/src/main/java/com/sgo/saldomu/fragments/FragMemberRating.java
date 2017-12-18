package com.sgo.saldomu.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.R;
import com.sgo.saldomu.activities.MainPage;
import com.sgo.saldomu.coreclass.CurrencyFormat;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DateTimeFormat;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.GlideManager;
import com.sgo.saldomu.coreclass.HashMessage;
import com.sgo.saldomu.coreclass.MyApiClient;
import com.sgo.saldomu.coreclass.RoundImageTransformation;
import com.sgo.saldomu.coreclass.WebParams;
import com.sgo.saldomu.dialogs.DefinedDialog;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import timber.log.Timber;

import static com.sgo.saldomu.R.id.decor_content_parent;
import static com.sgo.saldomu.R.id.rlApproval;
import static com.sgo.saldomu.R.id.tvTotalTrx;
import static com.sgo.saldomu.R.id.txtName;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FragMemberRating extends Fragment {
    public final static String TAG = "com.sgo.saldomu.fragments.Frag_Member_Rating";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SecurePreferences sp;

    // TODO: Rename and change types of parameters
    private String categoryName, txId, imageURL, shopName, amount, maximumRating, defaultRating, comment, customerId;
    private TextView tvNameKategori,tvDescAmount, tvAgentName;
    private Button btnSubmit, btnCancel;
    private RatingBar ratingBar;
    private ImageView agentProfilePic;
    private int userRating = 0;
    ProgressDialog progdialog;
    private SharedPreferences.Editor mEditor;

    public FragMemberRating() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp                  = CustomSecurePref.getInstance().getmSecurePrefs();
        mEditor             = sp.edit();

        customerId          = sp.getString(DefineValue.USERID_PHONE, "");



        if (getArguments() != null) {
            categoryName    = getArguments().getString(DefineValue.CATEGORY_NAME);
            amount          = getArguments().getString(DefineValue.AMOUNT);
            imageURL        = getArguments().getString(DefineValue.IMG_MEDIUM_URL);
            txId            = getArguments().getString(DefineValue.BBS_TX_ID);
            shopName        = getArguments().getString(DefineValue.BBS_SHOP_NAME);

            maximumRating   = getArguments().getString(DefineValue.BBS_MAXIMUM_RATING);
            defaultRating   = getArguments().getString(DefineValue.BBS_DEFAULT_RATING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v          = inflater.inflate(R.layout.frag_member_rating, container, false);
        comment         = "";

        ratingBar       = (RatingBar) v.findViewById(R.id.ratingBar);
        tvNameKategori  = (TextView) v.findViewById(R.id.tvNameKategori);
        tvDescAmount    = (TextView) v.findViewById(R.id.tvDescAmount);
        tvAgentName     = (TextView) v.findViewById(R.id.tvAgentName);
        btnSubmit       = (Button) v.findViewById(R.id.btnSubmit);
        btnCancel       = (Button) v.findViewById(R.id.btnCancel);
        agentProfilePic = (ImageView) v.findViewById(R.id.agentProfilePic);

        ratingBar.setNumStars(Integer.valueOf(maximumRating));
        ratingBar.setMax(Integer.valueOf(maximumRating));
        ratingBar.setRating(userRating);
        tvNameKategori.setText(categoryName);
        tvAgentName.setText(shopName);
        tvDescAmount.setText(DefineValue.IDR + " " + CurrencyFormat.format(amount) );

        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.user_unknown_menu);
        RoundImageTransformation roundedImage = new RoundImageTransformation(bm);

        if ( imageURL.equals("") ) {
            GlideManager.sharedInstance().initializeGlide(getContext(), R.drawable.user_unknown_menu, roundedImage, agentProfilePic);
//
        } else {
            GlideManager.sharedInstance().initializeGlide(getContext(), imageURL, roundedImage, agentProfilePic);
//
        }

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                userRating = (int) rating;

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment     = "";
                userRating  = Integer.valueOf(defaultRating);
                submitData();

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( userRating > 0 ) {
                    submitData();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle(getString(R.string.warning));
                    alertDialog.setMessage(getString(R.string.err_rating_empty));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        return v;
    }


    private void submitData() {
        progdialog              = DefinedDialog.CreateProgressDialog(getContext(), "");

        RequestParams params    = new RequestParams();

        UUID rcUUID             = UUID.randomUUID();
        String  dtime           = DateTimeFormat.getCurrentDateTime();

        params.put(WebParams.RC_UUID, rcUUID);
        params.put(WebParams.RC_DATETIME, dtime);
        params.put(WebParams.APP_ID, BuildConfig.AppID);
        params.put(WebParams.SENDER_ID, DefineValue.BBS_SENDER_ID);
        params.put(WebParams.RECEIVER_ID, DefineValue.BBS_RECEIVER_ID);
        params.put(WebParams.TX_ID, txId);
        params.put(WebParams.KEY_PHONE, customerId);
        params.put(WebParams.MESSAGE, comment);
        params.put(WebParams.RATING, userRating);

        String signature = HashMessage.SHA1(HashMessage.MD5(rcUUID + dtime + DefineValue.BBS_SENDER_ID + DefineValue.BBS_RECEIVER_ID + BuildConfig.AppID + txId + customerId ));

        params.put(WebParams.SIGNATURE, signature);

        Timber.d("isi params updatefeedback:" + params.toString());

        MyApiClient.updateFeedback(getContext(), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if ( progdialog.isShowing() )
                    progdialog.dismiss();

                try {
                    Timber.d("isi params response updatefeedback:"+response.toString());
                    String code = response.getString(WebParams.ERROR_CODE);

                    if (code.equals(WebParams.SUCCESS_CODE)) {

                    } else {
                        Toast.makeText(getContext(), response.getString(WebParams.ERROR_MESSAGE), Toast.LENGTH_LONG).show();
                    }

                    // Now remove tvalue from shared preferences
                    mEditor.remove(DefineValue.BBS_MODULE);
                    mEditor.remove(DefineValue.BBS_TX_ID);
                    mEditor.remove(DefineValue.IMG_MEDIUM_URL);
                    mEditor.remove(DefineValue.BBS_MAXIMUM_RATING);
                    mEditor.remove(DefineValue.BBS_DEFAULT_RATING);
                    mEditor.remove(DefineValue.BBS_SHOP_NAME);
                    mEditor.apply();

                    getActivity().finish();


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

                if ( progdialog.isShowing() )
                    progdialog.dismiss();
                Timber.w("Error Koneksi updateFeedback:" + throwable.toString());

            }

        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


}
