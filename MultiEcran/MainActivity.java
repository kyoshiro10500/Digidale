package com.example.jonathan.applicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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
    /*
        nb_ips et liste_ips vont être obtenues à la connexion avec le controleur.
        Celles-ci permettent de savoir si il y a effectivement des dalles en écoute sur le réseau
     */
    private String nb_ips ="0" ;
    private String[] liste_ips={};

    boolean available =true ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if(available) {
            //La connexion est effectuée dans un thread
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        //Tentative de connexion au controleur
                        Socket s = new Socket(ip_address, ip_port);
                        PrintWriter out = new PrintWriter(
                                new BufferedWriter(
                                        new OutputStreamWriter(s.getOutputStream())
                                )
                                , true);
                        //Message standardisé pour que le controleur sache qu'il a effectivement des informations à récupérer et ce qu'il doit effectuer
                        out.println("begin-ping-end");

                        //Récupération de la réponse du controleur qui renvoit d'abord le nombre d'ip détecté puis la liste des ips
                        //Format du message :
                        //3
                        //192.XXX.XXX.XXX
                        //192.XXX.XXX.XXX
                        //192.XXX.XXX.XXX
                        BufferedReader bfr = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        nb_ips = bfr.readLine();
                        for (int i = 0; i < Integer.parseInt(nb_ips); i++) {
                            liste_ips = Arrays.copyOf(liste_ips, liste_ips.length + 1);
                            liste_ips[liste_ips.length - 1] = bfr.readLine();
                        }
                        s.close();
                    } catch (IOException e) {
                        nb_ips = "-1";
                    }
                }
            };
            t.start();
        }
        available = false ;
        //Délai d'attente pour la réponse du controleur. Fixé arbitrairement.
        SystemClock.sleep(200);
        //Traitement selon la réponse obtenue par le controleur
        //nb_ips > 0 on lance l'activité suivante
        //nb_ips = -1 le controleur n'a pas pu se connecter au réseau
        //nb_ips = 0 le controleur n'a pas détecté de dalles
        if(Integer.parseInt(nb_ips) > 0 )
        {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("EXTRA_MESSAGE2",nb_ips);
            intent.putExtra("EXTRA_MESSAGE3",ip_address);
            intent.putExtra("EXTRA_MESSAGE4",ip_port.toString());
            startActivity(intent);
        }
        else if(Integer.parseInt(nb_ips) == -1)
        {
            available = true ;
            Toast toast = Toast.makeText(getApplicationContext(), "Une erreur est survenue, veuillez réessayer", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            available = true ;
            Toast toast = Toast.makeText(getApplicationContext(), "Aucun écran détecté, veuillez réessayer", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
