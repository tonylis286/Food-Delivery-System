package com.example.clitz_arestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clitz_arestaurantapp.Adapter.MyDrinkAdapter;
import com.example.clitz_arestaurantapp.Listener.ICartLoadListener;
import com.example.clitz_arestaurantapp.Listener.IDrinkLoadListener;
import com.example.clitz_arestaurantapp.Model.CartModel;
import com.example.clitz_arestaurantapp.Model.DrinkModel;
import com.example.clitz_arestaurantapp.Utils.SpaceItemDecoration;
import com.example.clitz_arestaurantapp.databinding.ActivityMenuListBinding;
import com.example.clitz_arestaurantapp.eventBus.myUpdateCartEvent;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MenuList extends AppCompatActivity implements ICartLoadListener, IDrinkLoadListener {

    private ActivityMenuListBinding binding;

    private IDrinkLoadListener drinkLoadListener;
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
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        loadDrinkFromFirebase();
        countCartItem();
    }

    private void loadDrinkFromFirebase() {
        List<DrinkModel> drinkModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Drink")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot drinkSnapshot : snapshot.getChildren()) {
                                DrinkModel drinkModel = drinkSnapshot.getValue(DrinkModel.class);
                                assert drinkModel != null;
                                drinkModel.setKey(drinkSnapshot.getKey());
                                drinkModels.add(drinkModel);
                            }
                            drinkLoadListener.onDrinkLoadSuccess(drinkModels);
                        } else {
                            drinkLoadListener.onDrinkLoadFailed("Can't find drinks");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        drinkLoadListener.onDrinkLoadFailed(error.getMessage());
                    }
                });
    }

    public void init() {
        drinkLoadListener = this;
        cartLoadListener = this;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.recyclerDrink.setLayoutManager(gridLayoutManager);
        binding.recyclerDrink.addItemDecoration(new SpaceItemDecoration());

        binding.btnCart.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), CartActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onDrinkLoadSuccess(List<DrinkModel> drinkModelList) {
        MyDrinkAdapter adapter = new MyDrinkAdapter(this, drinkModelList, cartLoadListener);
        binding.recyclerDrink.setAdapter(adapter);
    }

    @Override
    public void onDrinkLoadFailed(String message) {
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        int cartSum = 0;
        for (CartModel cartModel : cartModelList)
            cartSum += cartModel.getQuantity();
        binding.badge.setNumber(cartSum);
    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(binding.mainLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    private void countCartItem() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot cartSnapshot : snapshot.getChildren()) {
                            CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                            cartModel.setKey(cartSnapshot.getKey());
                            cartModels.add(cartModel);
                        }
                        cartLoadListener.onCartLoadSuccess(cartModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // No operation needed
    }
}