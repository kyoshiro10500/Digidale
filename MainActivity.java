package com.example.jonathan.applicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private String nb_ips ="0" ;
    private String[] liste_ips={};


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final FloatingActionButton btn_ping = (FloatingActionButton) findViewById(R.id.btn_ping);
        btn_ping.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBtnPing();
                // Code here executes on main thread after user presses button
            }
        });


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickBtnPing()
    {
        EditText et2 = (EditText) findViewById(R.id.EditText02);
        final String ip_address =et2.getText().toString();

        EditText et3 = (EditText) findViewById(R.id.EditText03);
        final Integer ip_port = Integer.parseInt(et3.getText().toString());

        Thread t = new Thread() {

            @Override
            public void run() {
                try
                {
                    Socket s = new Socket(ip_address,ip_port);
                    PrintWriter out = new PrintWriter(
                                        new BufferedWriter(
                                                new OutputStreamWriter(s.getOutputStream())
                                        )
                            , true);
                    out.println("begin-ping-end");

                    BufferedReader bfr = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    nb_ips = bfr.readLine();
                    for(int i=0;i < Integer.parseInt(nb_ips);i++)
                    {
                        liste_ips= Arrays.copyOf(liste_ips, liste_ips.length + 1);
                        liste_ips[liste_ips.length - 1] = bfr.readLine();
                    }
                    s.close();
                }
                catch(IOException e)
                {
                    nb_ips="-1" ;
                }
            }
        };
        t.start();

        SystemClock.sleep(200);
        if(Integer.parseInt(nb_ips) >= 0)
        {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("EXTRA_MESSAGE2",nb_ips);
            intent.putExtra("EXTRA_MESSAGE3",ip_address);
            intent.putExtra("EXTRA_MESSAGE4",ip_port.toString());
            startActivity(intent);
        }
        else if(Integer.parseInt(nb_ips) == -1)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Connection error, please retry", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(), "No IPs detected, please retry", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera)
        {
            // Handle the camera action
        }
        else if (id == R.id.nav_gallery)
        {

        }
        else if (id == R.id.nav_slideshow)
        {

        }
        else if (id == R.id.nav_manage) {

        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_send)
        {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}