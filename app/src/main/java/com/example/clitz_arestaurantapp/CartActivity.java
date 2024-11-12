package com.example.clitz_arestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clitz_arestaurantapp.Adapter.MyCartAdapter;
import com.example.clitz_arestaurantapp.Listener.ICartLoadListener;
import com.example.clitz_arestaurantapp.Model.CartModel;
import com.example.clitz_arestaurantapp.Model.DrinkModel;
import com.example.clitz_arestaurantapp.databinding.ActivityCartBinding;
import com.example.clitz_arestaurantapp.eventBus.myUpdateCartEvent;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements ICartLoadListener {

    public ActivityCartBinding binding;
    private ICartLoadListener cartLoadListener;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(myUpdateCartEvent.class)) {
            EventBus.getDefault().removeStickyEvent(myUpdateCartEvent.class);
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onUpdateCart(myUpdateCartEvent event) {
        loadCartFromFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        loadCartFromFirebase();

        binding.goToPlaceOrder.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), PlaceYourOrder.class);
            startActivity(i);
        });
    }

    void loadCartFromFirebase() {
        List<CartModel> cartModels = new ArrayList<>();

        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                                CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                                cartModel.setKey(cartSnapshot.getKey());
                                cartModels.add(cartModel);
                            }
                            cartLoadListener.onCartLoadSuccess(cartModels);
                        } else {
                            cartLoadListener.onCartLoadFailed("Cart Empty");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    private void init() {
        cartLoadListener = this;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerCart.setLayoutManager(layoutManager);
        binding.recyclerCart.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onDrinkLoadSuccess(List<DrinkModel> drinkModelList) {
        // Not used in this activity
    }

    @Override
    public void onDrinkLoadFailed(String message) {
        // Not used in this activity
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        double sum = 0;
        for (CartModel cartModel : cartModelList) {
            sum += cartModel.getTotalPrice();
        }

        // Format sum to two decimal places
        String formattedTotal = String.format("Total : $ %.2f", sum);
        binding.txtTotal.setText(formattedTotal);

        MyCartAdapter adapter = new MyCartAdapter(this, cartModelList);
        binding.recyclerCart.setAdapter(adapter);
    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
    }
}