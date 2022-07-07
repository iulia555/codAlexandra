package com.example.mobilebankingapp.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.adapters.TransactionAdapter;
import com.example.mobilebankingapp.adapters.WithdrawalAdapter;
import com.example.mobilebankingapp.classes.Provider;
import com.example.mobilebankingapp.classes.User;
import com.example.mobilebankingapp.classes.cards.Card;
import com.example.mobilebankingapp.classes.cards.Transaction;
import com.example.mobilebankingapp.customDialogs.AddDepositDialog;
import com.example.mobilebankingapp.customDialogs.ExtrasDeContDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.example.mobilebankingapp.R.*;

public class TransactionsFragment extends Fragment {

    private ListView transactionsListView;
    private SearchView searchView;

    private Button filterBtn;
    private Button sortBtn;

    boolean sortHidden= true;
    boolean filterHidden= true;

    private LinearLayout filterView1;
    private LinearLayout filterView2;
    private Button toateBtn;
    private Button facturiBtn;
    private Button dateBtn;
    private String dataCautata;
    private Button venituriBtn;
    private Button cheltuieliBtn;
    private Button extrasDeCont;
    private ArrayList<String> filtreSelectate= new ArrayList<>();
    private String cuvantCautat="";

    private LinearLayout sortView1;
    private Button crescatorBtn;
    private Button descrescatorBtn;

    private Dialog dialog;

    public  FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_transactions,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //initializari
        initialAnimations();
        initToolbar(view);
        setupListView(view);

        selecteazaButon(toateBtn);
        filtreSelectate.add("toate");

        //modul de sortare si filtrare
        hideFilter();
        hideSort();
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortTapped(view);
            }
        });
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterTapped(view);
            }
        });

        extrasDeCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtrasDeContDialog extrasDeContDialog=new ExtrasDeContDialog();
                extrasDeContDialog.show(getFragmentManager(),"ExtrasDeContDialog");
            }
        });


        //toate filtrarile
        cautaDupaDescriere(view);

        toateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrareToate(view);
            }
        });
        facturiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrareFacturi(view,true);
            }
        });
        venituriBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrareVenituri(view,true);
            }
        });
        cheltuieliBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrareCheltuieli(view,true);
            }
        });
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar= Calendar.getInstance();
                int an=calendar.get(Calendar.YEAR);
                int luna=calendar.get(Calendar.MONTH);
                int zi=calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog;
                datePickerDialog=new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                dataCautata = dayOfMonth + "/" + (month + 1) + "/" + year;
                                filtrareData(view,true);
                            }

                        },an,luna,zi);
                datePickerDialog.show();
            }
        });

        //toate sortarile
        crescatorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortareCrescator(view);
            }
        });
        descrescatorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortareDescrescator(view);
            }
        });

        //la click pe transactiile apar detaliile ei
        showTransaction(view);



    }

    private void showTransaction(@NonNull View view) {
        dialog=new Dialog(getContext());
        dialog.setContentView(layout.transaction_dialog);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().windowAnimations=R.style.dialog_animation;

        Button closeBtn =dialog.findViewById(R.id.btnClose);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView description= dialog.findViewById(R.id.transDescription);
        TextView amount= dialog.findViewById(R.id.transAmount);
        TextView fromOrTo= dialog.findViewById(R.id.transFrom);
        TextView fromText= dialog.findViewById(id.fromtext);
        TextView iban= dialog.findViewById(id.transIban);
        TextView date= dialog.findViewById(id.transDate);
        TextView bill= dialog.findViewById(id.transBill);

        transactionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Transaction transaction = (Transaction) parent.getItemAtPosition(position);
                //Toast.makeText(getContext(), transaction.toString(), Toast.LENGTH_SHORT).show();
                String beneficiarOrExpeditor;

                description.setText(transaction.getDescription());
                if(transaction.getAmount()<0){
                    fromText.setText("To: ");
                    amount.setTextColor(Color.RED);
                    amount.setText(String.valueOf(transaction.getAmount()));
                    beneficiarOrExpeditor=transaction.getCardTo();
                }
                else{
                    fromText.setText("From: ");
                    amount.setTextColor(Color.rgb(0, 124, 0));
                    amount.setText(String.valueOf(transaction.getAmount()));
                    beneficiarOrExpeditor=transaction.getCardFrom();

                }
                date.setText(transaction.getDate());

                if (transaction.getIsUtilityBill()){
                    bill.setText("Este factura utilitati");
                }
                else{
                    bill.setText("");
                    fromOrTo.setText(beneficiarOrExpeditor);
                }

                if(beneficiarOrExpeditor.contains("p"))
                {
                    database.document("Providers/"+beneficiarOrExpeditor)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Provider provider=documentSnapshot.toObject(Provider.class);
                                    fromOrTo.setText(provider.getName());
                                    iban.setText(provider.getIBAN());
                                }
                            });
                }
                else if(beneficiarOrExpeditor.contains("c")){

                    database.document("Cards/"+beneficiarOrExpeditor)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Card card=documentSnapshot.toObject(Card.class);
                                    iban.setText(card.getIBAN());

                                    //trebuie sa caut numele detinatorului dupa id-ul sau in tabela users
                                    String owner=card.getOwner();

                                    database.document("Users/"+owner)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                   User user=documentSnapshot.toObject(User.class);
                                                    fromOrTo.setText(String.format("%s %s", user.getLastName(), user.getFirstName()));
                                                }
                                            });
                                }
                            });

                }
                dialog.show();
            }
        });

    }

    //sortari
    public void sortareCrescator(View view){
        Collections.sort(HomeFragment.transactionList,Transaction.esteCrescator);
        checkForFilter(view);

        deselecteazaToateButoaneleSortare();
        selecteazaButon(crescatorBtn);
    }

    public void sortareDescrescator(View view){
        Collections.sort(HomeFragment.transactionList,Transaction.esteCrescator);
        Collections.reverse(HomeFragment.transactionList);
        checkForFilter(view);

        deselecteazaToateButoaneleSortare();
        selecteazaButon(descrescatorBtn);
    }

    private void checkForFilter(View view) {
        if(filtreSelectate.contains("toate")){
            if(cuvantCautat.equals("")){
                setAdapter(view, HomeFragment.transactionList);
            }
            else{
                List<Transaction> transactiiDupaDescriere= new ArrayList<>();
                for(Transaction transaction:HomeFragment.transactionList){
                     if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())) {
                         transactiiDupaDescriere.add(transaction);
                     }
                }
                setAdapter(view, transactiiDupaDescriere);
            }
        }
        else {

            if(filtreSelectate.contains("facturi")){
                filtrareFacturi(view,false);
            }
            if(filtreSelectate.contains("cheltuieli")){
                filtrareCheltuieli(view,false);
            }
            if(filtreSelectate.contains("venituri")){
                filtrareVenituri(view,false);
            }
            if(filtreSelectate.contains(dataCautata)) {
                filtrareData(view,false);
            }
        }

    }


    //filtrari
    public void filtrareToate(View view){

        filtreSelectate.clear();
        dataCautata="";
        searchView.setQuery("",false);
        searchView.clearFocus();
        filtreSelectate.add("toate");

        deselecteazaToateButoaneleFiltru();
        selecteazaButon(toateBtn);

        Collections.sort(HomeFragment.transactionList,Transaction.esteCronologic);
        setAdapter(view, HomeFragment.transactionList);
    }

    public void filtrareFacturi(View view, boolean b){

        if(b && !filtreSelectate.contains("facturi"))
            filtreSelectate.add("facturi");

        selecteazaButon(facturiBtn);
        deselecteazaButon(toateBtn);
        filtreSelectate.remove("toate");

        List<Transaction> transactiiFacturi= new ArrayList<>();
        for(Transaction transaction:HomeFragment.transactionList){
            if(filtreSelectate.size()==1){
                if(transaction.getIsUtilityBill()){
                    if(cuvantCautat.equals("")){
                        transactiiFacturi.add(transaction);
                    }
                    else {
                        if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                            transactiiFacturi.add(transaction);
                        }
                    }
                }
            }else{
                for(String filter:filtreSelectate){
                    if(filter.equals(dataCautata) && dataCautata.equals(transaction.getDate())){
                        if(transaction.getIsUtilityBill() && !transactiiFacturi.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiFacturi.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiFacturi.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("venituri") && transaction.getAmount()>0){
                        if(transaction.getIsUtilityBill() && !transactiiFacturi.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiFacturi.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiFacturi.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("cheltuieli") && transaction.getAmount()<0){
                        if(transaction.getIsUtilityBill() && !transactiiFacturi.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiFacturi.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiFacturi.add(transaction);
                                }
                            }
                        }
                    }

                }
                if(filtreSelectate.contains("venituri") && filtreSelectate.contains("cheltuieli")){
                    transactiiFacturi.clear();
                }
            }

        }
        setAdapter(view, transactiiFacturi);
    }
    private void filtrareCheltuieli(View view, boolean b) {

        if(b && !filtreSelectate.contains("cheltuieli"))
            filtreSelectate.add("cheltuieli");

        selecteazaButon(cheltuieliBtn);
        deselecteazaButon(toateBtn);
        filtreSelectate.remove("toate");


        List<Transaction> transactiiCheltuieli= new ArrayList<>();
        for(Transaction transaction:HomeFragment.transactionList){
            if(filtreSelectate.size()==1){
                if(transaction.getAmount()<0){
                    if(cuvantCautat.equals("")){
                        transactiiCheltuieli.add(transaction);
                    }
                    else {
                        if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                            transactiiCheltuieli.add(transaction);
                        }
                    }
                }
            }
            else{
                for(String filter:filtreSelectate){
                    if(filter.equals("facturi") && filtreSelectate.contains(dataCautata)){
                        if(transaction.getAmount()<0 && transaction.getIsUtilityBill() && transaction.getDate().equals(dataCautata) && !transactiiCheltuieli.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiCheltuieli.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiCheltuieli.add(transaction);
                                }
                            }
                        }
                    }
                    else if(filter.equals("facturi") && transaction.getIsUtilityBill()){
                        if(transaction.getAmount()<0 && !transactiiCheltuieli.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiCheltuieli.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiCheltuieli.add(transaction);
                                }
                            }
                        }
                    }
                    else if(filter.equals(dataCautata) && dataCautata.equals(transaction.getDate())) {
                        if(transaction.getAmount()<0 && !transactiiCheltuieli.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiCheltuieli.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiCheltuieli.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("venituri") && transaction.getAmount()>0){
                        if(transaction.getAmount()<0 && !transactiiCheltuieli.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiCheltuieli.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiCheltuieli.add(transaction);
                                }
                            }
                        }
                    }

                }
                if(filtreSelectate.contains("venituri") && filtreSelectate.contains("facturi")){
                    transactiiCheltuieli.clear();
                }
            }

        }
        setAdapter(view, transactiiCheltuieli);
    }

    private void filtrareVenituri(View view, boolean b) {

        if(b && !filtreSelectate.contains("venituri"))
            filtreSelectate.add("venituri");

        selecteazaButon(venituriBtn);
        deselecteazaButon(toateBtn);
        filtreSelectate.remove("toate");


        List<Transaction> transactiiVenituri= new ArrayList<>();
        for(Transaction transaction:HomeFragment.transactionList){
            if(filtreSelectate.size()==1){
                if(transaction.getAmount()>0){
                    if(cuvantCautat.equals("")){
                        transactiiVenituri.add(transaction);
                    }
                    else {
                        if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                            transactiiVenituri.add(transaction);
                        }
                    }
                }
            }
            else{
                for(String filter:filtreSelectate){
                    if(filter.equals("cheltuieli") && transaction.getAmount()<0){
                        if(transaction.getAmount()>0 && !transactiiVenituri.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiVenituri.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiVenituri.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("facturi") && transaction.getIsUtilityBill()){
                        if(transaction.getAmount()>0 && !transactiiVenituri.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiVenituri.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiVenituri.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals(dataCautata) && dataCautata.equals(transaction.getDate())){
                        if(transaction.getAmount()>0 && !transactiiVenituri.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiVenituri.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiVenituri.add(transaction);
                                }
                            }
                        }
                    }
                }
            }
        }
        setAdapter(view, transactiiVenituri);
    }
    private void filtrareData(View view, boolean b) {

        if(b && !filtreSelectate.contains(dataCautata))
            filtreSelectate.add(dataCautata);

        selecteazaButon(dateBtn);
        deselecteazaButon(toateBtn);
        filtreSelectate.remove("toate");


        List<Transaction> transactiiData= new ArrayList<>();
        for(Transaction transaction:HomeFragment.transactionList){
            if(filtreSelectate.size()==1){
                if(dataCautata.equals(transaction.getDate())){
                    if(cuvantCautat.equals("")){
                        transactiiData.add(transaction);
                    }
                    else {
                        if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                            transactiiData.add(transaction);
                        }
                    }
                }
            }else{
                for(String filter:filtreSelectate){
                    if(filter.equals("facturi") && transaction.getIsUtilityBill()){
                        if(dataCautata.equals(transaction.getDate()) && !transactiiData.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiData.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiData.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("cheltuieli") && transaction.getAmount()<0){
                        if(dataCautata.equals(transaction.getDate()) && !transactiiData.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiData.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiData.add(transaction);
                                }
                            }
                        }
                    }
                    if(filter.equals("venituri") && transaction.getAmount()>0){
                        if(dataCautata.equals(transaction.getDate()) && !transactiiData.contains(transaction)){
                            if(cuvantCautat.equals("")){
                                transactiiData.add(transaction);
                            }
                            else {
                                if(transaction.getDescription().toLowerCase().contains(cuvantCautat.toLowerCase())){
                                    transactiiData.add(transaction);
                                }
                            }
                        }
                    }
                }
                if(filtreSelectate.contains("venituri") && filtreSelectate.contains("cheltuieli")){
                    transactiiData.clear();
                }
                if(filtreSelectate.contains("venituri") && filtreSelectate.contains("facturi")){
                    transactiiData.clear();
                }
            }
        }
        setAdapter(view, transactiiData);
        //filtreSelectate.remove(dataCautata);
    }

    private void cautaDupaDescriere(View view){

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                cuvantCautat=newText;
                List<Transaction> transactiiDupaDescriere= new ArrayList<>();

                for(Transaction transaction:HomeFragment.transactionList){
                    if(transaction.getDescription().toLowerCase().contains(newText.toLowerCase())){

                        if(filtreSelectate.contains("toate")){
                            transactiiDupaDescriere.add(transaction);
                        }
                        else{
                            for(String filter:filtreSelectate){
                                if(filter.equals("facturi")){
                                    if(transaction.getIsUtilityBill())
                                    {
                                        transactiiDupaDescriere.add(transaction);
                                    }
                                }
                                if(filter.equals("cheltuieli")){
                                    if(transaction.getAmount()<0)
                                    {
                                        transactiiDupaDescriere.add(transaction);
                                    }
                                }
                                if(filter.equals("venituri")){
                                    if(transaction.getAmount()>0)
                                    {
                                        transactiiDupaDescriere.add(transaction);
                                    }
                                }
                                if(filter.equals(transaction.getDate()))
                                {
                                    transactiiDupaDescriere.add(transaction);
                                }

                            }
                        }
                    }
                }

                setAdapter(view, transactiiDupaDescriere);

                return false;
            }
        });

    }

    //filtrare multipla
    private void selecteazaButon(Button butonApasat){
        butonApasat.setBackgroundColor(Color.WHITE);
        butonApasat.setTextColor(ContextCompat.getColor(getContext(), color.purple_500));
    }
    private void deselecteazaButon(Button butonApasat){
        butonApasat.setBackgroundColor(ContextCompat.getColor(getContext(), color.purple_500));
        butonApasat.setTextColor(Color.WHITE);
    }
    private void deselecteazaToateButoaneleFiltru(){
        deselecteazaButon(toateBtn);
        deselecteazaButon(facturiBtn);
        deselecteazaButon(dateBtn);
        deselecteazaButon(venituriBtn);
        deselecteazaButon(cheltuieliBtn);
    }
    private void deselecteazaToateButoaneleSortare(){
        deselecteazaButon(descrescatorBtn);
        deselecteazaButon(crescatorBtn);
    }


    //toolbar
    public void initToolbar(View view){

        //butoane filtrare
        searchView=view.findViewById(id.searchView);
        toateBtn=view.findViewById(id.toateFiltrare);
        facturiBtn=view.findViewById(id.facturiFiltrare);
        venituriBtn=view.findViewById(id.venituriFiltrare);
        cheltuieliBtn=view.findViewById(id.cheltuieliFiltrare);
        dateBtn=view.findViewById(id.dataFiltrare);
        extrasDeCont=view.findViewById(id.btnExtras);

        //butoane sortare
        crescatorBtn=view.findViewById(id.crescator);
        descrescatorBtn=view.findViewById(id.descrescator);

        //management ptr toolbar
        sortBtn=view.findViewById(id.sortBtn);
        filterBtn=view.findViewById(id.filterBtn);
        filterView1=view.findViewById(id.filterTabsLayout);
        filterView2=view.findViewById(id.filterTabsLayout2);
        sortView1=view.findViewById(id.sortTabsLayout);
    }

    public void showFilterTapped(View view){
        if(filterHidden==true){
            filterHidden=false;
            showFilter();
        }
        else {
            filterHidden=true;
            hideFilter();
        }
    }
    public void showSortTapped(View view){
        if(sortHidden==true){
            sortHidden=false;
            showSort();
        }
        else {
            sortHidden=true;
            hideSort();
        }
    }

    private void hideFilter() {
        searchView.setVisibility(View.GONE);
        filterView1.setVisibility(View.GONE);
        filterView2.setVisibility(View.GONE);
        filterBtn.setText("filter");
    }
    private void showFilter() {
        searchView.setVisibility(View.VISIBLE);
        filterView1.setVisibility(View.VISIBLE);
        filterView2.setVisibility(View.VISIBLE);
        filterBtn.setText("hide");
    }
    private void hideSort() {
        deselecteazaToateButoaneleSortare();
        sortView1.setVisibility(View.GONE);
        sortBtn.setText("sort");
    }
    private void showSort() {
        sortView1.setVisibility(View.VISIBLE);
        sortBtn.setText("hide");
    }


    //management
    public void setupListView(View view){
        transactionsListView=view.findViewById(id.transListView);
        setAdapter(view, HomeFragment.transactionList);
    }

    private void setAdapter(View view, List<Transaction> transactionList) {
        Collections.sort(transactionList,Transaction.esteCronologic);
        TransactionAdapter transAdapter = new TransactionAdapter(view.getContext(), layout.item_transaction, transactionList, getLayoutInflater());
        transactionsListView.setAdapter(transAdapter);
        transAdapter.notifyDataSetChanged();
    }
    private void initialAnimations() {

        View view = this.getView();
        ImageView transBackground;
        Animation fromBottomAnimation;
        ConstraintLayout transactionsInfo;

        transBackground= view.findViewById(id.trans_background);
        transactionsInfo=view.findViewById(id.transactions_info);

        fromBottomAnimation= AnimationUtils.loadAnimation(getContext(), anim.from_bottom_faster);

        transBackground.animate().translationY(-1250).setDuration(500);
        transactionsInfo.startAnimation(fromBottomAnimation);
    }
}
