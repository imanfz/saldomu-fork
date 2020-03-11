package com.sgo.saldomu.dialogs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.sgo.saldomu.FingerprintHandler;
import com.sgo.saldomu.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.support.v4.content.ContextCompat.getDrawable;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintDialog extends DialogFragment {
    View v;
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private FingerprintHandler helper;
    private TextView tv_status, tv_use_pin;
    private ImageView iv_finger;
    private static final String KEY_NAME = "saldomuFingerprint";
    private int attempt;
    private FingerprintDialogListener listener;


    public interface FingerprintDialogListener {
        void onFinishFingerprintDialog(boolean result);
    }

    public static FingerprintDialog newDialog(FingerprintDialogListener listener) {
        FingerprintDialog dialog = new FingerprintDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.dialog_fingerprint, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        tv_status = v.findViewById(R.id.tv_error_finger);
        tv_use_pin = v.findViewById(R.id.tv_usepin);
        iv_finger = v.findViewById(R.id.iv_finger);
//        listener = (FingerprintDialogListener) getTargetFragment();

        tv_use_pin.setOnClickListener(v -> {
            listener.onFinishFingerprintDialog(false);
            getDialog().dismiss();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getActivity().getSystemService(FINGERPRINT_SERVICE);

            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                tv_status.setText(getString(R.string.enable_fingerprint_permission_text));
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                tv_status.setText(getString(R.string.register_fingerprint_text));
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                tv_status.setText(getString(R.string.enable_lockscreen_security_text));
            } else {
                try {
                    generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    helper = new FingerprintHandler(getActivity(), this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onPause() {
        super.onPause();
        helper.stopListening();
    }

    public void setStatusSuccess() {
//        iv_finger.setImageDrawable(getDrawable(getActivity(), R.drawable.ic_check));
        iv_finger.setBackground(getDrawable(getActivity(), R.drawable.ic_fingerprint_success));
        tv_status.setText(getString(R.string.fingerprint_success));


        // Setelah 1 detik dialog didismiss dan arahin ke halaman utama
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (getDialog().isShowing())
                    getDialog().dismiss();
                listener.onFinishFingerprintDialog(true);
//                ActivityLogin activityLogin = (ActivityLogin) getActivity();
//                activityLogin.showDashboard();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public void setStatusFailed() {
//        iv_finger.setImageDrawable(getDrawable(getActivity(),R.drawable.ic_priority_high));
        attempt++;
        iv_finger.setBackground(getDrawable(getActivity(), R.drawable.ic_fingerprint_failed));
        if (attempt == 1)
            tv_status.setText(getString(R.string.fingerprint_attempt) + " (" + attempt + ")");
        if (attempt == 2)
            tv_status.setText(getString(R.string.fingerprint_attempt) + " (" + attempt + ")");
        if (attempt == 3)
            tv_status.setText(getString(R.string.fingerprint_attempt) + " (" + attempt + ")");
        if (attempt == 4)
            tv_status.setText(getString(R.string.fingerprint_attempt) + " (" + attempt + ")");
        if (attempt == 5) {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (getDialog().isShowing())
                        getDialog().dismiss();
                    listener.onFinishFingerprintDialog(false);
//                ActivityLogin activityLogin = (ActivityLogin) getActivity();
//                activityLogin.showDashboard();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    public void setStatusError(String message) {
        iv_finger.setBackground(getDrawable(getActivity(), R.drawable.ic_fingerprint_failed));
        tv_status.setText(message);
    }

    public void setStatusHelp(String message) {
        iv_finger.setBackground(getDrawable(getActivity(), R.drawable.ic_fingerprint_failed));
        tv_status.setText(message);
    }
}
