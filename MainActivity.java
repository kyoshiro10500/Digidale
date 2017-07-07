package com.example.jonathan.exemplepagination;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String EXTRA_LOGIN ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickButton();
                // Code here executes on main thread after user presses button
            }
        });


    }

    public void onClickButton()
    {
        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        intent.putExtra(EXTRA_LOGIN, "Activité 1 vers 2");
        startActivity(intent);
    }

}
