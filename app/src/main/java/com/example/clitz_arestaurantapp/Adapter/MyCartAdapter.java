package com.example.clitz_arestaurantapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.clitz_arestaurantapp.Model.CartModel;
import com.example.clitz_arestaurantapp.R;
import com.example.clitz_arestaurantapp.databinding.LayoutCartItemBinding;
import com.example.clitz_arestaurantapp.eventBus.myUpdateCartEvent;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder> {

    private final Context context;
    private final List<CartModel> cartModelList;

    public MyCartAdapter(Context context, List<CartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
    }

    @NonNull
    @Override
    public MyCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LayoutCartItemBinding binding = LayoutCartItemBinding.inflate(inflater, parent, false);
        return new MyCartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCartViewHolder holder, int position) {
        CartModel cartModel = cartModelList.get(position);

        Glide.with(context)
                .load(cartModel.getImage())
                .into(holder.binding.imageView);

        holder.binding.txtPrice.setText(new StringBuilder("$").append(cartModel.getPrice()));
        holder.binding.txtName.setText(cartModel.getName());
        holder.binding.txtQuantity.setText(String.valueOf(cartModel.getQuantity()));
        holder.binding.btnMinus.setOnClickListener(v -> minusCartItem(holder, cartModel));
        holder.binding.btnPlus.setOnClickListener(v -> plusCartItem(holder, cartModel));
        holder.binding.btnDelete.setOnClickListener(v -> showDeleteDialog(position, cartModel));
    }

    private void showDeleteDialog(int position, CartModel cartModel) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete?")
                .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                .setPositiveButton("OK", (dialog12, which) -> {
                    // Temporary delete/remove
                    notifyItemRemoved(position);
                    deleteFromFirebase(cartModel);
                    dialog12.dismiss();
                })
                .create();
        dialog.show();
    }

    private void deleteFromFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .child(cartModel.getKey())
                .removeValue()
                .addOnSuccessListener(aVoid -> EventBus.getDefault().postSticky(new myUpdateCartEvent()));
    }

    private void plusCartItem(MyCartViewHolder holder, CartModel cartModel) {
        cartModel.setQuantity(cartModel.getQuantity() + 1);
        cartModel.setTotalPrice(cartModel.getQuantity() * Float.parseFloat(cartModel.getPrice()));

        // Update Quantity
        holder.binding.txtQuantity.setText(String.valueOf(cartModel.getQuantity()));
        updateFirebase(cartModel);
    }

    private void minusCartItem(MyCartViewHolder holder, CartModel cartModel) {
        if (cartModel.getQuantity() > 1) {
            cartModel.setQuantity(cartModel.getQuantity() - 1);
            cartModel.setTotalPrice(cartModel.getQuantity() * Float.parseFloat(cartModel.getPrice()));

            // Update Quantity
            holder.binding.txtQuantity.setText(String.valueOf(cartModel.getQuantity()));
            updateFirebase(cartModel);
        }
    }

    private void updateFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .child(cartModel.getKey())
                .setValue(cartModel)
                .addOnSuccessListener(aVoid -> EventBus.getDefault().postSticky(new myUpdateCartEvent()));
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public static class MyCartViewHolder extends RecyclerView.ViewHolder {
        private final LayoutCartItemBinding binding;

        public MyCartViewHolder(@NonNull LayoutCartItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}