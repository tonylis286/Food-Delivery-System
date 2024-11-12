package com.example.clitz_arestaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class AutoImageSlider extends AppCompatActivity {

    Button dbutton;
    ViewPager2 viewPager2;
    List<Integer> images = Arrays.asList(
            //R.drawable.auto_image_slider_1,
            //R.drawable.auto_image_slider_11,
            //R.drawable.auto_image_slider_8,
            //R.drawable.auto_image_slider_10,
            //R.drawable.auto_image_slider_5,
            //R.drawable.auto_image_slider_4
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_image_slider);

        viewPager2 = findViewById(R.id.image_slider);
        SliderAdapter sliderAdapter = new SliderAdapter(images);
        viewPager2.setAdapter(sliderAdapter);

        // Home Delivery Button
        dbutton = findViewById(R.id.homeDeliveryButton);
        dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Success!!!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), MenuList.class);
                startActivity(i);
                finish();
            }
        });
    }
}