package com.example.mobilebankingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilebankingapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.squareup.okhttp.internal.http.OkHeaders;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {


    private ActivityLoginBinding binding;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private static final String TAG="LOGIN_TAG";
    private FirebaseAuth firebaseAuth;

    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialAnimations();

        binding.phoneLl.setVisibility(View.VISIBLE);
        binding.codeLl.setVisibility(View.GONE);

        firebaseAuth=FirebaseAuth.getInstance();

        pd=new ProgressDialog(this);
        pd.setTitle("Va rugam asteptati...");
        pd.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.d(TAG, "onCodeSent: "+verificationId);
                mVerificationId=verificationId;
                forceResendingToken=token;
                pd.dismiss();

                binding.phoneLl.setVisibility(View.GONE);
                binding.codeLl.setVisibility(View.VISIBLE);

                Toast.makeText(Login.this, "S-a trimis codul de siguranta...", Toast.LENGTH_SHORT).show();
            }
        };

        binding.phoneContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone= binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(Login.this, "Introduceti numarul de telefon!", Toast.LENGTH_SHORT).show();
                }
                else{
                    startPhoneNumberVerification(phone);
                }
            }
        });

        binding.resendCodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone= binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(Login.this, "Introduceti numarul de telefon!", Toast.LENGTH_SHORT).show();
                }
                else{
                    resendVerificationCode(phone,forceResendingToken);
                }
            }
        });

        binding.codeSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code= binding.codeEt.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(Login.this, "Introduceti codul de verificare!", Toast.LENGTH_SHORT).show();
                }
                else{
                    verifyPhoneNumberWithCode(mVerificationId,code);
                }
            }
        });

    }

    private void startPhoneNumberVerification(String phone) {
        pd.setMessage("Se verifica numarul de telefon...");
        pd.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {
        pd.setMessage("Se retrimite codul de siguranta...");
        pd.show();

        PhoneAuthOptions options=
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        pd.setMessage("Se verifica codul de siguranta..");
        pd.show();

        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.setMessage("Se conecteaza...");

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        pd.dismiss();
                        startActivity(new Intent(Login.this,MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void initialAnimations() {
        RelativeLayout relativeLayout= findViewById(R.id.relativeLayout);
        LinearLayout linearLayout= findViewById(R.id.linearLayout7);
        TextView textView=findViewById(R.id.tvLogin);

        Animation fromBottomAnimation;

        fromBottomAnimation= AnimationUtils.loadAnimation(this, R.anim.from_bottom_faster);

        relativeLayout.startAnimation(fromBottomAnimation);
        textView.startAnimation(fromBottomAnimation);

        linearLayout.animate().translationY(-50).setDuration(1800);

    }
}