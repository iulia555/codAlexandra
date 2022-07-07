package com.example.mobilebankingapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobilebankingapp.MainActivity;
import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.classes.cards.Transaction;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ReportsFragment extends Fragment {
    private PieChart pieChart;
    private Button lunaBtn;
    private Button generalBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initialAnimations();

        pieChart=view.findViewById(R.id.pie_chart);

        setupPieChart();
        loadPieChartData("/");

        lunaBtn=view.findViewById(R.id.grafic_luna);
        generalBtn=view.findViewById(R.id.grafic_general);
        selecteazaButon(generalBtn);

        generalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunaBtn.setText("Alege luna");
                selecteazaButon(generalBtn);
                deselecteazaButon(lunaBtn);
                loadPieChartData("/");
            }
        });

        lunaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alegeLuna();
            }
        });



    }

    private void alegeLuna(){
        final Calendar today= Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getContext(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        String lunaSelectata=(selectedMonth+1)+"/"+selectedYear;
                        lunaBtn.setText(lunaSelectata);
                        selecteazaButon(lunaBtn);
                        deselecteazaButon(generalBtn);

                        Toast.makeText(getContext(), "/"+lunaSelectata, Toast.LENGTH_SHORT).show();
                        loadPieChartData("/"+lunaSelectata);
                    }

                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));

        builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(2021)
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(2021)
                .setTitle("Selectati luna")
                .build()
                .show();
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        //pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setHoleRadius(45);
        pieChart.setTransparentCircleRadius(50);
        pieChart.setCenterText("Cheltuieli per categorie");
        pieChart.setCenterTextSize(18);
        pieChart.getDescription().setEnabled(false);

        Legend legend= pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    private void loadPieChartData(String date){

        HashMap<String, Float> categorii=new HashMap<>();
        categorii.put("Utilitati",0.0f);
        categorii.put("Cumparaturi diverse",0.0f);
        categorii.put("Transport si masina",0.0f);
        categorii.put("Chirie",0.0f);
        categorii.put("Mancare si restaurant",0.0f);
        categorii.put("Transferuri",0.0f);

        for(Transaction transaction:HomeFragment.transactionList){
            if(transaction.getDate().contains(date)){
                if(transaction.getIsUtilityBill()){
                    float valoareAnterioara= categorii.get("Utilitati");
                    categorii.put("Utilitati",transaction.getAmount()+valoareAnterioara);
                }
                else if(transaction.getAmount()<0){

                    if(transaction.getDescription().contains("Cumparaturi")){
                        float valoareAnterioara= categorii.get("Cumparaturi diverse");
                        categorii.put("Cumparaturi diverse",transaction.getAmount()+valoareAnterioara);
                    }
                    else if(transaction.getDescription().contains("Mancare") || transaction.getDescription().contains("Restaurant")  ){
                        float valoareAnterioara= categorii.get("Mancare si restaurant");
                        categorii.put("Mancare si restaurant",transaction.getAmount()+valoareAnterioara);
                    }
                    else if(transaction.getDescription().contains("Chirie")){
                        float valoareAnterioara= categorii.get("Chirie");
                        categorii.put("Chirie",transaction.getAmount()+valoareAnterioara);
                    }
                    else if(transaction.getDescription().contains("Transfer")){
                        float valoareAnterioara= categorii.get("Transferuri");
                        categorii.put("Transferuri",transaction.getAmount()+valoareAnterioara);
                    }
                    else if(transaction.getDescription().contains("Transport") || transaction.getDescription().contains("Uber") || transaction.getDescription().contains("Benzina") || transaction.getDescription().contains("Motorina") || transaction.getDescription().contains("Masina")){
                        float valoareAnterioara= categorii.get("Transport si masina");
                        categorii.put("Transport si masina",transaction.getAmount()+valoareAnterioara);
                    }
                }
            }

        }

        ArrayList<PieEntry> entries= new ArrayList<>();
        for(String key: categorii.keySet()){
            if(-(categorii.get(key))>0){
                entries.add(new PieEntry(-(categorii.get(key)),key));
            }
        }

        ArrayList<Integer> culori= new ArrayList<>();
        for(int culoare: ColorTemplate.MATERIAL_COLORS){
            culori.add(culoare);
        }
        for(int culoare: ColorTemplate.VORDIPLOM_COLORS){
            culori.add(culoare);
        }

        PieDataSet dataSet = new PieDataSet(entries,"   ");
        dataSet.setColors(culori);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data= new PieData(dataSet);
        data.setDrawValues(true);
        //data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    private void selecteazaButon(Button butonApasat){
        butonApasat.setBackgroundColor(Color.WHITE);
        butonApasat.setTextColor(ContextCompat.getColor(getContext(), R.color.purple_500));
    }
    private void deselecteazaButon(Button butonApasat){
        butonApasat.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_500));
        butonApasat.setTextColor(Color.WHITE);
    }
    private void initialAnimations() {

        View view = this.getView();
        ImageView transBackground;
        Animation fromBottomAnimation;
        ConstraintLayout transactionsInfo;

        transBackground= view.findViewById(R.id.reports_background);
        transactionsInfo=view.findViewById(R.id.reports_info);

        fromBottomAnimation= AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_faster);

        transBackground.animate().translationY(-1250).setDuration(500);
        transactionsInfo.startAnimation(fromBottomAnimation);
    }
}
