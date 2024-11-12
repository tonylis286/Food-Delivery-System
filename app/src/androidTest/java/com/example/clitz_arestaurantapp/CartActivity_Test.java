package com.example.clitz_arestaurantapp;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.clitz_arestaurantapp.Model.CartModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CartActivity_Test {

    private CartActivity cartActivity;

    @Before
    public void setUp() {
        // Launch the CartActivity
        ActivityScenario<CartActivity> scenario = ActivityScenario.launch(CartActivity.class);
        scenario.onActivity(activity -> cartActivity = activity);
    }

    @Test
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void testLoadCartFromFirebase_WhenCartIsEmpty() {
        // Run UI updates on the main thread
        cartActivity.runOnUiThread(() -> {
            // Simulate an empty cart by providing an empty list
            List<CartModel> emptyCart = new ArrayList<>();
            cartActivity.onCartLoadSuccess(emptyCart);

            // Assert that the total text displays "$0.0" as the total for an empty cart
            String expectedText = "Total : $ 0.00";
            String actualText = cartActivity.binding.txtTotal.getText().toString();
            assertEquals(expectedText, actualText);
        });
    }

    @Test
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void testLoadCartFromFirebase_WhenCartHasItems() {
        cartActivity.runOnUiThread(() -> {
            // Simulate a cart with predefined items
            List<CartModel> cartModels = new ArrayList<>();

            CartModel item1 = new CartModel();
            item1.setKey("item1");
            item1.setName("Burger Meal");
            item1.setPrice("8.99");
            item1.setQuantity(2);
            item1.setTotalPrice(17.98f);

            CartModel item2 = new CartModel();
            item2.setKey("item2");
            item2.setName("Pasta Meal");
            item2.setPrice("10.49");
            item2.setQuantity(1);
            item2.setTotalPrice(10.49f);

            cartModels.add(item1);
            cartModels.add(item2);

            // Pass the simulated data to onCartLoadSuccess to populate the RecyclerView
            cartActivity.onCartLoadSuccess(cartModels);

            // Check that the total is calculated correctly
            String expectedText = "Total : $ 28.47";
            String actualText = cartActivity.binding.txtTotal.getText().toString();
            assertEquals(expectedText, actualText);
        });
    }
}
