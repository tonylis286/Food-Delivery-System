package com.example.clitz_arestaurantapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clitz_arestaurantapp.Listener.ICartLoadListener;
import com.example.clitz_arestaurantapp.Listener.IRecyclerViewClickListener;
import com.example.clitz_arestaurantapp.Model.CartModel;
import com.example.clitz_arestaurantapp.Model.DrinkModel;
import com.example.clitz_arestaurantapp.R;
import com.example.clitz_arestaurantapp.databinding.LayoutDrinkItemBinding;
import com.example.clitz_arestaurantapp.eventBus.myUpdateCartEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDrinkAdapter extends RecyclerView.Adapter<MyDrinkAdapter.MyDrinkViewHolder> {

    private final Context context;
    private final List<DrinkModel> drinkModelList;
    private final ICartLoadListener iCartLoadListener;

    public MyDrinkAdapter(Context context, List<DrinkModel> drinkModelList, ICartLoadListener iCartLoadListener) {
        this.context = context;
        this.drinkModelList = drinkModelList;
        this.iCartLoadListener = iCartLoadListener;
    }

    @NonNull
    @Override
    public MyDrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutDrinkItemBinding binding = LayoutDrinkItemBinding.inflate(inflater, parent, false);
        return new MyDrinkViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDrinkViewHolder holder, int position) {
        DrinkModel drinkModel = drinkModelList.get(position);

        Glide.with(context)
                .load(drinkModel.getImage())
                .into(holder.binding.imageView);

        holder.binding.txtPrice.setText(new StringBuilder("$").append(drinkModel.getPrice()));
        holder.binding.txtName.setText(drinkModel.getName());

        holder.setListener((view, adapterPosition) -> addTocart(drinkModel));
    }

    private void addTocart(DrinkModel drinkModel) {
        DatabaseReference userCart = FirebaseDatabase
                .getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID");

        userCart.child(drinkModel.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CartModel cartModel = snapshot.getValue(CartModel.class);
                    cartModel.setQuantity(cartModel.getQuantity() + 1);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("quantity", cartModel.getQuantity());
                    updateData.put("totalPrice", cartModel.getQuantity() * Float.parseFloat(cartModel.getPrice()));

                    userCart.child(drinkModel.getKey())
                            .updateChildren(updateData)
                            .addOnSuccessListener(aVoid -> iCartLoadListener.onCartLoadFailed("Add to cart Success"))
                            .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                } else {
                    CartModel cartModel = new CartModel();
                    cartModel.setName(drinkModel.getName());
                    cartModel.setImage(drinkModel.getImage());
                    cartModel.setKey(drinkModel.getKey());
                    cartModel.setPrice(drinkModel.getPrice());
                    cartModel.setQuantity(1);
                    cartModel.setTotalPrice(Float.parseFloat(drinkModel.getPrice()));

                    userCart.child(drinkModel.getKey())
                            .setValue(cartModel)
                            .addOnSuccessListener(aVoid -> iCartLoadListener.onCartLoadFailed("Add to cart Success"))
                            .addOnFailureListener(e -> iCartLoadListener.onCartLoadFailed(e.getMessage()));
                }
                EventBus.getDefault().postSticky(new myUpdateCartEvent());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                iCartLoadListener.onCartLoadFailed(error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return drinkModelList.size();
    }

    public static class MyDrinkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final LayoutDrinkItemBinding binding;
        private IRecyclerViewClickListener listener;

        public MyDrinkViewHolder(@NonNull LayoutDrinkItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
        }

        public void setListener(IRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onRecycleerClick(v, getAdapterPosition());
            }
        }
    }
}