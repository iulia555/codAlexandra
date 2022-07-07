package com.example.mobilebankingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mobilebankingapp.databinding.ActivityMainBinding;
import com.example.mobilebankingapp.fragments.HomeFragment;
import com.example.mobilebankingapp.fragments.ReportsFragment;
import com.example.mobilebankingapp.fragments.PaymentsFragment;
import com.example.mobilebankingapp.fragments.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    private ActivityMainBinding binding;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    public static String phone = "+40730423323";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        firebaseAuth= FirebaseAuth.getInstance();
        //checkUserStatus();


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();


    }

    private void checkUserStatus() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            phone =  firebaseUser.getPhoneNumber();
            Toast.makeText(this, phone, Toast.LENGTH_SHORT).show();

        }
        else{
            finish();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener=
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment=null;

                    switch (item.getItemId()){
                        case R.id.bottom_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.bottom_transactions:
                            selectedFragment = new TransactionsFragment();
                            break;
                        case R.id.bottom_pay:
                            selectedFragment = new PaymentsFragment();
                            break;
                        case R.id.bottom_reports:
                            selectedFragment = new ReportsFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

                    return true;
                }
            };


}