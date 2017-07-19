package com.example.jonathan.applicationtest2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;


/*
    MainActivity définie la classe de la première activité que l'utilisateur voit au lancement de l'application.

    Les intéractions définies ici :
        -Saisie de l'adresse ip et du port du controleur pour la connexion grâce à des TextEdit
        -Validation de la saisie grâce à un Button

    Intéractions à prévoir :
        -Bouton de contact
        -Bouton vers site web
        -Mentions légales
        -Tutoriel rapide pour l'utilisateur
 */
public class MainActivity extends AppCompatActivity
{

    Handler handler ;
    /*
        nb_ips et liste_ips vont être obtenues à la connexion avec le controleur.
        Celles-ci permettent de savoir si il y a effectivement des dalles en écoute sur le réseau
     */
    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler() ;
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Accès à la mémoire de stockage")
                        .setMessage("L'utilisation de cette application requiert l'accès au stockage de l'appareil pour uploader des fichiers")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_STORAGE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
            }
        }
        /*
            btn_ping est le bouton qui permet d'intéroger le controleur quant à l'état du réseau
         */
        final FloatingActionButton btn_ping = (FloatingActionButton) findViewById(R.id.btn_ping);
        btn_ping.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickBtnPing();
                // Code here executes on main thread after user presses button
            }
        });

    }


    /*
        @requires Nothing
        @assigns nb_ips et liste_ips
        @ensures assigne nb_ips >0 et liste_ips non vide si il y a des dalles en écoute sur le réseau
                 assigne nb_ips = -1 et liste_ips vide si le controleur n'a pas pu scanner le réseau
                 assigne nb_ips = 0 et liste_ips vide si aucune dalle est en écoute sur le réseau
     */
    public void onClickBtnPing()
    {

        //Récupération du contenu des deux TextEdit pour la connexion
        EditText et2 = (EditText) findViewById(R.id.EditText02);
        final String ip_address =et2.getText().toString();
        EditText et3 = (EditText) findViewById(R.id.EditText03);
        final Integer ip_port = Integer.parseInt(et3.getText().toString());
            //La connexion est effectuée dans un thread
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        //Tentative de connexion au controleur
                        Socket s = new Socket(ip_address, ip_port);
                        s.close();
                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                        intent.putExtra("EXTRA_MESSAGE3",ip_address);
                        intent.putExtra("EXTRA_MESSAGE4",ip_port.toString());
                        startActivity(intent);
                    } catch (IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), "Une erreur de connexion est survenue. Veuillez réessayer", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    }
                }
            };
            t.start();
    }

}
