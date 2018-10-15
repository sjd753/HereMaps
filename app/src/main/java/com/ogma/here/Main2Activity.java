package com.ogma.here;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.truizlop.fabreveallayout.FABRevealLayout;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final FABRevealLayout revealLayout = findViewById(R.id.fab_reveal_layout);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealLayout.revealSecondaryView();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        revealLayout.revealMainView();
                    }
                }, 1500);
            }
        });


    }
}
