package com.example.mobilebankingapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilebankingapp.R;
import com.example.mobilebankingapp.classes.cards.Deposit;
import com.example.mobilebankingapp.classes.cards.Withdrawal;

import java.util.List;

public class DepositAdapter extends ArrayAdapter<Deposit> {
    private Context context;
    private List<Deposit> depositList;
    private LayoutInflater inflater;
    private int resource;

    public DepositAdapter(@NonNull Context context, int resource,
                          @NonNull List<Deposit> objects, LayoutInflater inflater) {
        super(context, resource, objects);
        this.context = context;
        this.depositList = objects;
        this.inflater = inflater;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view= inflater.inflate(resource,parent,false);

        Deposit deposit=depositList.get(position);
        if(deposit!=null){

            TextView depositname=view.findViewById(R.id.tv_depositname);
            TextView depositsum=view.findViewById(R.id.tv_depositsum);
            TextView depositrate=view.findViewById(R.id.tv_depositrate);
            TextView deposittimeleft=view.findViewById(R.id.tv_deposittimeleft);

            depositname.setText(deposit.getName());
            depositsum.setText(String.valueOf(deposit.getAmount()));
            depositrate.setText(String.valueOf(deposit.getInterestRate()));
            deposittimeleft.setText(deposit.getTimeLeft() +" luni");
        }
        return view;
    }
}
