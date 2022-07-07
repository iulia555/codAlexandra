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
import com.example.mobilebankingapp.classes.cards.Transaction;
import com.example.mobilebankingapp.classes.cards.Withdrawal;

import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private Context context;
    private List<Transaction> transactionList;
    private LayoutInflater inflater;
    private int resource;

    public TransactionAdapter(@NonNull Context context, int resource,
                              @NonNull List<Transaction> objects, LayoutInflater inflater) {
        super(context, resource, objects);
        this.context = context;
        this.transactionList = objects;
        this.inflater = inflater;
        this.resource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view= inflater.inflate(resource,parent,false);
        Transaction transaction=transactionList.get(position);

        if(transaction!=null){
            TextView trans_description=view.findViewById(R.id.trans_description);
            TextView trans_amount=view.findViewById(R.id.trans_amount);
            TextView trans_date=view.findViewById(R.id.trans_date);

            trans_description.setText(transaction.getDescription());
            trans_date.setText(transaction.getDate());
            trans_amount.setText(String.valueOf(transaction.getAmount()));
            if(transaction.getAmount()>0) {
                trans_amount.setTextColor(Color.rgb(0, 124, 0));
            }
            else {
                trans_amount.setTextColor(Color.RED);
            }

        }

        return view;
    }
}
