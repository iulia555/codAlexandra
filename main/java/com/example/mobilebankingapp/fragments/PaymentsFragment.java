package com.example.mobilebankingapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.classes.Provider;
import com.example.mobilebankingapp.classes.cards.Card;
import com.example.mobilebankingapp.classes.cards.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PaymentsFragment extends Fragment {

    private Spinner spinner;
    private Switch aSwitch;

    private EditText name;
    private EditText iban;
    private EditText description;
    private EditText sum;
    private boolean isUtility;
    public FirebaseFirestore database = FirebaseFirestore.getInstance();

    private Button sendBtn;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payments,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initialAnimations();

        spinner=view.findViewById(R.id.spinner);
        aSwitch=view.findViewById(R.id.switch1);
        name= view.findViewById(R.id.editTextName);
        iban= view.findViewById(R.id.editTextIBAN);
        sum= view.findViewById(R.id.editTextSum);
        description= view.findViewById(R.id.editTextDescription);
        sendBtn=view.findViewById(R.id.btnsend);


        initializeazaSpinner("");
        spinner.setEnabled(false);


        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   initializeazaSpinner("Alege un furnizor");
                   spinner.setEnabled(true);
                   isUtility=true;
               }else{
                   initializeazaSpinner("");
                   spinner.setEnabled(false);
                   name.setText("");
                   iban.setText("");
                   description.setText("");
                   isUtility=false;

               }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String provider = (String) parent.getItemAtPosition(position);
                for(Provider provider1:HomeFragment.providersList.values()){
                    if(provider1.getName().equals(provider)){

                        name.setText(provider1.getName());
                        iban.setText(provider1.getIBAN());
                        description.setText(provider1.getDomain());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validari()) {

                    float suma = -Float.parseFloat(sum.getText().toString());
                    String beneficiar = name.getText().toString();
                    String Iban = iban.getText().toString();
                    String descriere = description.getText().toString();
                    String cardFrom = HomeFragment.cardId;
                    String currency = "RON";
                    String status = "Approved";
                    Calendar calendar = Calendar.getInstance();
                    int an = calendar.get(Calendar.YEAR);
                    int luna = calendar.get(Calendar.MONTH) + 1;
                    int zi = calendar.get(Calendar.DAY_OF_MONTH);
                    String date = zi + "/" + luna + "/" + an;
                    String cardTo = null;


                    for (Map.Entry<String, Provider> entry : HomeFragment.providersList.entrySet()) {
                        if (entry.getValue().getName().equals(beneficiar)) {
                            cardTo = entry.getKey();
                        }
                    }

                    if (TextUtils.isEmpty(cardTo)) {

                        Provider providerNou = new Provider(descriere, Iban, beneficiar, isUtility);

                        String idNouProvider = "p" + (HomeFragment.providersList.size() + 1);
                        Toast.makeText(getContext(), idNouProvider, Toast.LENGTH_SHORT).show();

                        HomeFragment.providersList.put(idNouProvider,providerNou);
                        cardTo = idNouProvider;

                        database.collection("Providers").document(idNouProvider)
                                .set(providerNou)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "S-a adaugat provider", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "NU s-a adaugat provider"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }

                    int transactionListSize=0;
                    for(Transaction transaction:HomeFragment.transactionList){
                        transactionListSize+=1;
                    }
                    String idNouTransaction = "t" + (transactionListSize + 1);
                    Toast.makeText(getContext(), idNouTransaction, Toast.LENGTH_SHORT).show();


                    Transaction tranzactieNoua= new Transaction((int) suma,cardFrom,cardTo,currency,date,descriere,status,isUtility);
                    HomeFragment.transactionList.add(tranzactieNoua);
                    HomeFragment.card.setBalance((int) (HomeFragment.card.getBalance()+suma));


                    database.collection("Cards/"+HomeFragment.cardId +"/Transactions").document(idNouTransaction)
                            .set(tranzactieNoua)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "A fost adaugata tranzactia", Toast.LENGTH_SHORT).show();

                                    database.document("Cards/"+HomeFragment.cardId)
                                            .update("Balance",HomeFragment.card.getBalance()+suma)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(view.getContext(), "S-a actualizat soldul", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            });





                }
            }

        });
}


    public boolean validari(){
        if(sum.getText().toString().equals("")){
            sum.setError("Completati suma !");
            return false;
        }
        if(Double.parseDouble(sum.getText().toString()) > HomeFragment.card.getBalance()){
            sum.setError("Sold insuficient pentru suma completata!");
            return false;
        }
        if(name.getText().toString().equals("")) {
            name.setError("Completati beneficiarul !");
            return false;
        }
        if(iban.getText().toString().equals("")) {
            iban.setError("Completati IBAN-ul !");
            return false;
        }
        if(iban.getText().toString().length()!=24){
            iban.setError("IBAN-ul trebuie sa aiba 24 de caractere!");
            return false;
        }
        return true;
    }

    private void initializeazaSpinner(String mesaj) {
        List<String> providers=new ArrayList<>();
        providers.add(0, mesaj);
        for(Provider provider: HomeFragment.providersList.values()){
            if(provider.getIsUtility()){
                providers.add(provider.getName());
            }
        }

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(getContext(),R.layout.style_spinner,providers);
        arrayAdapter.setDropDownViewResource(R.layout.style_spinner_dropdown);
        spinner.setAdapter(arrayAdapter);
    }

    private void initialAnimations() {

        View view = this.getView();
        ImageView transBackground;
        Animation fromBottomAnimation;
        ConstraintLayout transactionsInfo;

        transBackground= view.findViewById(R.id.payments_background);
        transactionsInfo=view.findViewById(R.id.payments_info);

        fromBottomAnimation= AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_faster);

        transBackground.animate().translationY(-1250).setDuration(500);
        transactionsInfo.startAnimation(fromBottomAnimation);
    }
}
