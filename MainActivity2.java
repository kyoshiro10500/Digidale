package com.example.jonathan.exemplepagination;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity2 extends AppCompatActivity {

    String EXTRA_LOGIN ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();


        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickButton21();
            }
        });

        if (intent != null) {
            EXTRA = (intent.getStringExtra(EXTRA_LOGIN));
        }

    }


    public void onClickButton21()
    {
        Toast toast = Toast.makeText(getApplicationContext(), EXTRA_LOGIN, Toast.LENGTH_SHORT);
        toast.show();
    }

}
