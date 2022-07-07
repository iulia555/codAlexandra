package com.example.mobilebankingapp.customDialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.classes.cards.Deposit;
import com.example.mobilebankingapp.fragments.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddDepositDialog extends DialogFragment {

    public static final String TAG="AddDepositDialog";

    private EditText depositName;
    private EditText depositAmount;
    private Spinner depositPeriod;
    private TextView depositInterestRate;
    private TextView depositTimeLeft;
    private Button btnClose;
    private Button btnAdd;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.add_deposit_dialog,null);

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);


        depositName=view.findViewById(R.id.depositName);
        depositAmount=view.findViewById(R.id.depositAmount);
        depositInterestRate=view.findViewById(R.id.depositInterestRate);
        depositPeriod=view.findViewById(R.id.depositPeriod);
        depositTimeLeft=view.findViewById(R.id.depositTimeLeft);
        btnClose=view.findViewById(R.id.btnClose);
        btnAdd=view.findViewById(R.id.btnAdd);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        depositPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                depositTimeLeft.setText(depositPeriod.getSelectedItem().toString());


                float i= (float) 0.0;
                if(!depositAmount.getText().toString().equals("")){
                    int amount= Integer.parseInt((depositAmount.getText().toString()));
                    while(amount>10){
                        amount/=10;
                        i+=0.1;
                    }
                }
                float interestRate= (float) ((depositPeriod.getSelectedItemPosition()+4)/10.0+i);
                depositInterestRate.setText( String.valueOf(interestRate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        depositAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {

                float i= (float) 0.0;
                int amount= Integer.parseInt((depositAmount.getText().toString()));
                while(amount>100){
                    amount/=10;
                    i+=0.1;
                }
                float interestRate= (float) ((depositPeriod.getSelectedItemPosition()+4)/10.0 + i);
                depositInterestRate.setText( String.valueOf(interestRate));
            }
        });



        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validari()){
                    String nume= depositName.getText().toString();
                    int amount= Integer.parseInt(depositAmount.getText().toString());
                    String time=depositTimeLeft.getText().toString();
                    int timeLeft=Integer.parseInt(time.substring(0,time.length()-5));
                    int period=timeLeft;
                    float interestRate= Float.parseFloat(depositInterestRate.getText().toString());

                    Deposit newDeposit= new Deposit(amount,interestRate,nume,period,timeLeft);
                    Toast.makeText(getContext(), newDeposit.toString(), Toast.LENGTH_SHORT).show();

                    //adaug depozit local
                    HomeFragment.depositList.add(newDeposit);
                    HomeFragment.depositAdapter.notifyDataSetChanged();

                    //adaug depozit in firestore
                    HomeFragment.database.collection("Cards/"+HomeFragment.cardId+"/Deposits").document("d"+HomeFragment.depositList.size())
                            .set(newDeposit)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");

                                    //modific sold in firestore
                                    HomeFragment.database.document("Cards/"+HomeFragment.cardId)
                                            .update("Balance",HomeFragment.card.getBalance()-amount)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(view.getContext(), "S-a actualizat soldul", Toast.LENGTH_SHORT).show();
                                                    //actualizez soldul local
                                                    HomeFragment.card.setBalance((int) (HomeFragment.card.getBalance()-amount));
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    getDialog().dismiss();
                }
            }
        });
       return view;
    }
    public boolean validari(){
        if(depositName.getText().toString().equals("")){
            depositName.setError("Dati un nume depozitului !");
            return false;
        }
        if(depositAmount.getText().toString().equals("")) {
            depositAmount.setError("Alegeti o suma !");
            return false;
        }
        else if(Integer.parseInt(depositAmount.getText().toString())<0 ){
            depositAmount.setError("Suma trebuie sa fie pozitiva !");
            return false;
        }
        else if( Integer.parseInt(depositAmount.getText().toString())>HomeFragment.card.getBalance()){
            depositAmount.setError("Suma este mai mare decat soldul disponibil !");
            return false;
        }
        return true;
    }
}
