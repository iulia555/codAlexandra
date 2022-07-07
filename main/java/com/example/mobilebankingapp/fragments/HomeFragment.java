package com.example.mobilebankingapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.mobilebankingapp.MainActivity;
import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.adapters.DepositAdapter;
import com.example.mobilebankingapp.adapters.WithdrawalAdapter;
import com.example.mobilebankingapp.classes.Provider;
import com.example.mobilebankingapp.classes.User;
import com.example.mobilebankingapp.classes.cards.Card;
import com.example.mobilebankingapp.classes.cards.CardType;
import com.example.mobilebankingapp.classes.cards.Deposit;
import com.example.mobilebankingapp.classes.cards.Transaction;
import com.example.mobilebankingapp.classes.cards.Withdrawal;
import com.example.mobilebankingapp.customDialogs.AddDepositDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class HomeFragment extends Fragment {

    public static String cardId;  //id card curent
    public static Card card; //card curent
    public static User user;  //user curent
    public static CardType cardType;  //tipul cardului curent
    public static List<Withdrawal> withdrawalList=new ArrayList<>();  //toate retragerile
    public static List<Deposit> depositList=new ArrayList<>();  //toate depozitele
    public static List<Transaction> transactionList=new ArrayList<>();  //toate tranzatiile
    public static HashMap<String,Provider> providersList= new HashMap<>(); //toti providerii
    public static HashMap<String,User> userList= new HashMap<>(); //toti userii
    public static HashMap<String,Card> cardList= new HashMap<>(); //toate cardurile


    Dialog dialog1; //dialog ptr informatii card
    Dialog dialog2;  //dialog ptr aifsare retrageri

    boolean areDetailsShown=false;
    boolean areDepositsShown=false;

    public static DepositAdapter depositAdapter;

    public static FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //animatii
        initialAnimations();

        //luam userul curent
        getCurrentUser();

        //gasim id-ul ptr cardul userului curent
        //preluam informatiile si le afisam pe card
        findCardID();

        //eye button cu informatiile de pe card
        ImageButton showDetailsButton=view.findViewById(R.id.btn_show_details);
        showDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails(view);
            }
        });

        //Custom dialog cu withdrawals la atm la short click pe card
        showWithdrawals(view);

        //Custom dialog cu card informations la long click pe card
        showInformationsDialog(view);


        //biblioteca externa ptr slide si delete pe listview
        //se creaza menu ptr fiecare item din listview
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        view.getContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(232,
                        0, 0)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };


        //afiseaza si ascunde depozitele la click pe sagetica
        ImageButton arrowBtn=view.findViewById(R.id.arrow);
        arrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeposits(view, creator);
            }
        });

        //buton adauga depozit care deschide un custom dialog cu formularul de completat
        ImageButton addDeposit=view.findViewById(R.id.addDeposit);
        addDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDepositDialog depositDialog=new AddDepositDialog();
                depositDialog.show(getFragmentManager(),"AddDepositDialog");
            }
        });



    }

    private void showWithdrawals(@NonNull View view) {
        dialog2=new Dialog(getContext());
        dialog2.setContentView(R.layout.withdrawals_dialog);
        dialog2.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog2.setCancelable(true);
        dialog2.getWindow().getAttributes().windowAnimations=R.style.dialog_animation;

        Button closeBtn =dialog2.findViewById(R.id.btn_close);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog2.dismiss();
            }
        });



        TextView tv_cardNumber= view.findViewById(R.id.tv_card_number);
        tv_cardNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WithdrawalAdapter adapter=new WithdrawalAdapter(dialog2.getContext(),
                        R.layout.item_withdrawal,withdrawalList,getLayoutInflater());

                ListView listView=dialog2.findViewById(R.id.lv_withdrawal);
                listView.setAdapter(adapter);

                dialog2.show();


            }
        });
    }

    private void showInformationsDialog(@NonNull View view) {

        dialog1 =new Dialog(getContext());
        dialog1.setContentView(R.layout.cardinfo_dialog);
        dialog1.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog1.setCancelable(true);
        dialog1.getWindow().getAttributes().windowAnimations=R.style.dialog_animation;

        Button copyBtn= dialog1.findViewById(R.id.btn_copy);
        Button closeBtn= dialog1.findViewById(R.id.btn_close);

        TextView tvName= dialog1.findViewById(R.id.tv_name);
        TextView tvIban= dialog1.findViewById(R.id.tv_iban);
        TextView tvCurrency= dialog1.findViewById(R.id.tv_currency);
        TextView tvBank= dialog1.findViewById(R.id.tv_bank);

        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                String informations="Name:"+tvName.getText()+" IBAN:"+tvIban.getText()+" Currency: "+tvCurrency.getText()+" Bank:"+tvBank.getText();
                ClipData clip = ClipData.newPlainText("card info",informations );
                clipboard.setPrimaryClip(clip);

//              ClipData.Item item=clipboard.getPrimaryClip().getItemAt(0);
//              String myInfo =item.getText().toString();
                Toast.makeText(getContext(), informations, Toast.LENGTH_SHORT).show();

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog1.dismiss();
            }
        });

        View cardview=view.findViewById(R.id.cardview);
        cardview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                tvName.setText(" "+user.getLastName()+" "+user.getFirstName());
                tvIban.setText(" "+card.getIBAN());
                tvCurrency.setText(" RON");
                tvBank.setText(" Onyp Group");

                dialog1.show();
                return true;
            }
        });
    }

    private void showDeposits(@NonNull View view, SwipeMenuCreator creator) {

        SwipeMenuListView listView=(SwipeMenuListView) view.findViewById(R.id.depositListView);
        //ListView listView=view.findViewById(R.id.depositListView);

       depositAdapter =new DepositAdapter(view.getContext(),
                R.layout.item_deposit,depositList,getLayoutInflater());

        listView.setAdapter(depositAdapter);


        //implementare optiune de stergere la swipe
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                new AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_delete)
                        .setTitle("Esti sigur?")
                        .setMessage("Vrei sa inchizi acest depozit?")
                        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Deposit depositToErase=depositList.get(position);

                                //caut id-ul depozitului de sters
                                database.collection("Cards/"+cardId+"/Deposits").whereEqualTo("name",depositToErase.getName())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                                    String depositID= document.getId();

                                                    //sterg depozitul
                                                    database.collection("Cards/"+cardId+"/Deposits").document(depositID)
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(view.getContext(), "Depozit sters din baza de date", Toast.LENGTH_SHORT).show();

                                                                    //actualizez soldul din baza de date
                                                                    database.document("Cards/"+cardId)
                                                                            .update("Balance",card.getBalance()+depositToErase.getAmount())
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Toast.makeText(view.getContext(), "S-a actualizat soldul", Toast.LENGTH_SHORT).show();

                                                                                    //actualizez soldul local
                                                                                    card.setBalance((int) (card.getBalance()+depositToErase.getAmount()));
                                                                                     TextView myBalance=view.findViewById(R.id.mybalance);
                                                                                     myBalance.setText(String.format("%,.2f\n",card.getBalance()));
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(view.getContext(), "NU s-a actualizat soldul", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });


                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(view.getContext(), "Eroare la stergere", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });


                                                }
                                            }
                                        });

                                //sterg si depozitul din local
                                depositList.remove(position);
                                depositAdapter.notifyDataSetChanged();


                            }
                        })
                        .setNegativeButton("Nu",null)
                        .show();
                // false : close the menu; true : not close the menu
                return false;
            }
        });



        ImageButton arrowBtn=view.findViewById(R.id.arrow);

        if(!areDepositsShown){
            areDepositsShown=true;

            listView.setVisibility(View.VISIBLE);
            arrowBtn.setImageResource(R.drawable.arrow_up);
        }
        else if(areDepositsShown){
            areDepositsShown=false;

            listView.setVisibility(View.GONE);
            arrowBtn.setImageResource(R.drawable.arrow);

        }
    }

    private void showDetails(@NonNull View view) {
        if(!areDetailsShown){
            areDetailsShown=true;

            TextView cardNumber=view.findViewById(R.id.tv_card_number);
            cardNumber.setText(card.getNumber());

            TextView cvv=view.findViewById(R.id.tv_cvv);
            String cvv_string=String.valueOf(card.getCVV());
            cvv.setText(cvv_string);

        }
        else if(areDetailsShown){
            areDetailsShown=false;

            TextView cardNumber=view.findViewById(R.id.tv_card_number);
            String text=card.getNumber().substring(card.getNumber().length() - 4);
            String message="****  ****  ****  "+text;
            cardNumber.setText(message);

            TextView cvv=view.findViewById(R.id.tv_cvv);
            cvv.setText("***");

        }
    }

    private void getCurrentUser() {
        Thread thread = new Thread() {
            @Override
            public void run() {

                database.collection("Users")
                        .whereEqualTo("Phone", MainActivity.phone)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                    user =document.toObject(User.class);
                                }
                            }
                        });

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        };
        thread.start();
    }


    private void getTransactions() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                database.collection("Cards/"+cardId+"/Transactions")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                transactionList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    Transaction transaction=document.toObject(Transaction.class);
                                    transactionList.add(transaction);
                                    //Toast.makeText(getContext(), transaction.toString(), Toast.LENGTH_SHORT).show();
                                }
                                Collections.sort(transactionList,Transaction.esteCronologic);
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }

    private void findCardID() {
        Thread thread = new Thread() {
            @Override
            public void run() {

                database.collection("Cards").whereEqualTo("Owner","u1")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                    cardId=document.getId();
                                    //Toast.makeText(getContext(), "1->"+cardId, Toast.LENGTH_SHORT).show();

                                    //preluam datele cardului in variabila card si afisam
                                    findCardDetails(getView());
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();

    }
    private void findCardDetails(@NonNull View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {

                database.document("Cards/"+cardId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                card=documentSnapshot.toObject(Card.class);
                                //Toast.makeText(getContext(), "2->"+card.toString(), Toast.LENGTH_SHORT).show();

                                findCardType();
                                getWithdrawals();
                                getDeposits();
                                getTransactions();
                                getProviders();
                                getUsers();
                                getCards();
                                fillCardWithDetails(view);
                            }
                        });

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();

    }

    private void getProviders() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                database.collection("Providers")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                providersList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    Provider provider=document.toObject(Provider.class);
                                    providersList.put(document.getId(), provider);
                                    //Toast.makeText(getContext(), provider.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }
    private void getUsers() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                database.collection("Users")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                userList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    User user=document.toObject(User.class);
                                    userList.put(document.getId(), user);
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }
    private void getCards() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                database.collection("Cards")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                cardList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    Card card=document.toObject(Card.class);
                                    cardList.put(document.getId(), card);
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }

    private void findCardType() {
        Thread thread = new Thread() {
            @Override
            public void run() {

                database.document("CardTypes/"+card.getType())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                cardType=documentSnapshot.toObject(CardType.class);
                                //Toast.makeText(getContext(), cardType.toString(), Toast.LENGTH_LONG).show();

                                if(cardType.getIsMastercard()){
                                    View cardImage=getView().findViewById(R.id.cardview);
                                    cardImage.setBackgroundResource(R.drawable.mastercard);
                                }

                                if("Credit".equals(cardType.getType())){
                                    ConstraintLayout deposits=getView().findViewById(R.id.deposits_layout);
                                    deposits.setVisibility(View.GONE);
                                }
                            }
                        });

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();

    }
    private void getWithdrawals() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                database.collection("Cards/"+cardId+"/Withdrawals")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                withdrawalList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    Withdrawal withdrawal=document.toObject(Withdrawal.class);
                                    withdrawalList.add(withdrawal);
                                    //Toast.makeText(getContext(), withdrawal.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }
    private void getDeposits() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                database.collection("Cards/"+cardId+"/Deposits")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                depositList.clear();
                                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                                    Deposit deposit=document.toObject(Deposit.class);
                                    depositList.add(deposit);
                                    //Toast.makeText(getContext(), deposit.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        thread.start();
    }

    private void fillCardWithDetails(@NonNull View view) {
        TextView endDate=view.findViewById(R.id.tv_end_date);
        endDate.setText(card.getEndDate());

        TextView cardNumber=view.findViewById(R.id.tv_card_number);
        String text=card.getNumber().substring(card.getNumber().length() - 4);
        String message="****  ****  ****  "+text;
        cardNumber.setText(message);

        TextView owner=view.findViewById(R.id.tv_owner);
        String ownerName= user.getLastName().toUpperCase()+" "+ user.getFirstName().toUpperCase();
        owner.setText(ownerName);

        TextView balance=view.findViewById(R.id.mybalance);
        String mybalance= String.format("%,.2f\n",card.getBalance());
        //String mybalance=String.valueOf(card.getBalance());
        balance.setText(mybalance);
    }



    private void initialAnimations() {
        View view = this.getView();
        ImageView background;
        ImageView cardIcon;
        LinearLayout titleLayout;
        Animation fromBottomAnimation;
        ConstraintLayout homeInfo;

        background= view.findViewById(R.id.background);
        cardIcon= view.findViewById(R.id.card_icon);
        titleLayout= view.findViewById(R.id.titleLayout);
        homeInfo=view.findViewById(R.id.home_info);

        fromBottomAnimation= AnimationUtils.loadAnimation(getContext(),R.anim.from_bottom);

        background.animate().translationY(-1000).setDuration(800).setStartDelay(100);
        cardIcon.animate().translationX(-1100).setDuration(800).setStartDelay(600);

        titleLayout.startAnimation(fromBottomAnimation);
        homeInfo.startAnimation(fromBottomAnimation);
    }
}
