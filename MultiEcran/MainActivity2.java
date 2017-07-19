package com.example.jonathan.applicationtest;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/*
    MainActivity2 définie l'activité que l'utilisateur voit après avoir scanné le réseau

    Les intéractions définies ici :
        -Selection d'un fichier sur le stockage du téléphone (fichier photo ou vidéo)
        -Copie du fichier sélectionné vers le stockage du controleur
        -Accès au contenu stocké sur le controleur
        -Sélection d'un fichier du controleur pour l'afficher sur les dalles

    Intéractions éventuelles ?
        -Possibilité de choisir entre affichage de fichier ou affichage de flux continu (stream)
 */
public class MainActivity2 extends AppCompatActivity
{
    private boolean error ;
    boolean loading = true ;
    private Handler handler ;
    private ProgressBar firstBar = null ;
    private static final int REQUEST_CODE = 6384; // onActivityResult request

    private String nb_ips ="Default" ;
    private String ip_address="";
    private String ip_port="";

    private String [] listFichier ;
    private ListView listFile ;

    public String path="";
    public String extension ;
    private TextAdapter grid_adapt ;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        firstBar = (ProgressBar)findViewById(R.id.firstBar);
        handler = new Handler() ;

        // Récupération des infos passées par l'activité précédente -> nb_ips, ip_address et ip_port
        Intent intent = getIntent();
        if(intent != null)
        {
            nb_ips = intent.getStringExtra("EXTRA_MESSAGE2");
            ip_address=intent.getStringExtra("EXTRA_MESSAGE3");
            ip_port=intent.getStringExtra("EXTRA_MESSAGE4");
        }

        //Définition de la vue des fichiers du controleur
        //Les items de la liste sont clickables et permettent de spécifier au controleur quel fichier on veut envoyer sur les dalles
        listFile = (ListView) findViewById(R.id.listFile);
        grid_adapt = new TextAdapter(this);
        listFile.setAdapter(grid_adapt);
        listFile.invalidateViews();
        listFile.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Socket s = new Socket(ip_address, Integer.parseInt(ip_port));
                            PrintWriter out = new PrintWriter(
                                    new BufferedWriter(
                                            new OutputStreamWriter(s.getOutputStream())
                                    ),
                                    true);
                            out.println("begin-lecture/"+grid_adapt.getThumb(position)+"-end");
                            s.close();
                            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                            intent.putExtra("EXTRA_MESSAGE2",nb_ips);
                            intent.putExtra("EXTRA_MESSAGE3",ip_address);
                            intent.putExtra("EXTRA_MESSAGE4",ip_port);
                            startActivity(intent);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        });

        //Bouton de sélection de fichier sur le stockage du téléphone
        final Button btn_choosefile = (Button) findViewById(R.id.btn_choosefile);
        btn_choosefile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooser();
                // Code here executes on main thread after user presses button
            }
        });

        //Bouton permettant de voir la liste des fichiers sur le serveur
        final Button btn_chooseserver = (Button) findViewById(R.id.btn_chooseserver);
        btn_chooseserver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showFileServer() ;
                // Code here executes on main thread after user presses button
            }
        });

        /*Bouton permettant d'uploader le fichier sur le serveur
          L'utilisateur est notifié d'un message de la réussite de l'envoi

          Problème connu : Si l'utilisateur n'a pas doné l'autorisation d'accès au stockage de son téléphone
                           l'upload se soldera toujours par un "Une erreur est survenue lors de l'upload"

                          => Solution envisagée :  demander l'accès lors du premier lancement de l'appli
        */
        final Button btn_show_path = (Button) findViewById(R.id.show_path);
        btn_show_path.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //On vérifie que l'utilisateur a effectivement choisi un fichier sur son téléphone
                if(path != "")
                {
                    //On essaye d'ouvir le fichier pour l'envoi, celui-ci sera envoyer sous forme de tableau de byte
                    File file = new File(path);
                    loading = true;
                    final byte[] b = new byte[(int) file.length()];
                    try
                    {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        fileInputStream.read(b);
                        fileInputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        loading = false;
                    }

                    Thread t = new Thread() {

                        @Override
                        public void run() {
                            try
                            {
                                //Le serveur fait une distinction entre image et vidéo
                                //En effet, celui-ci a besoin de convertir l'image avant de pouvoir la lire
                                String message = "";
                                extension = FileUtils.getExtension(path);

                                //TODO : Rajouter + d'extension dispo (.png,.avi,.mkv,.bitmap)
                                if (extension.equals(".jpg"))
                                {
                                    message = "begin-image/" + Integer.toString(b.length) + "-end";
                                }
                                else if (extension.equals(".mp4"))
                                {
                                    message = "begin-video/" + Integer.toString(b.length) + "-end";
                                }

                                //Le message n'est pas créé si l'extension n'est ps reconnue
                                if (!message.equals(""))
                                {
                                    Socket sock = new Socket(ip_address, Integer.parseInt(ip_port));
                                    PrintWriter out = new PrintWriter(
                                            new BufferedWriter(
                                                    new OutputStreamWriter(sock.getOutputStream())
                                            )
                                            , true);
                                    out.write(message);
                                    out.flush();
                                    SystemClock.sleep(1000);
                                    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                                    dos.write(b);
                                    dos.flush();
                                    sock.shutdownInput();
                                }
                                else
                                {
                                    error = true;
                                }
                                loading = false;

                            }
                            catch (UnknownHostException e)
                            {
                                loading = false;
                                error = true;
                            }
                            catch (IOException e)
                            {
                                error = true;
                                loading = false;
                            }
                        }
                    };
                    t.start();

                    //Thread dédié à la barre de chargement lors de l'envoi
                    firstBar.setProgress(0);
                    firstBar.setVisibility(View.VISIBLE);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            int i = 0;
                            while (loading)
                            {
                                i = i + 5;
                                firstBar.setProgress(i);
                            }

                            // Update the progress bar
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    firstBar.setVisibility(View.INVISIBLE);
                                    if (!error)
                                    {
                                        String[] pathSplit = path.split("/");
                                        path = pathSplit[pathSplit.length - 1];
                                        Toast toast = Toast.makeText(getApplicationContext(), "fichier envoyé  : " + path, Toast.LENGTH_SHORT);
                                        path = "";
                                        toast.show();
                                    }
                                    else
                                    {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de l'upload", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }
                            });

                        }

                    };

                    thread.start();
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Veuillez choisir un fichier avant d'envoyer", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    //Méthode liée à la librairie aFileChooser
    private void showChooser()
    {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, "chooser title");
        try
        {
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e)
        {
            // The reason for the existence of aFileChooser
        }
    }

    private void showFileServer()
    {
        Thread thread = new Thread() {
            @Override
            public void run(){
                try{
                    Socket sock = new Socket(ip_address, Integer.parseInt(ip_port));
                    PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(sock.getOutputStream())
                        )
                        , true);
                    out.write("begin-recherche-end");
                    out.flush();

                    BufferedReader bfr = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String nb_fichiers = bfr.readLine();
                    listFichier = new String[Integer.parseInt(nb_fichiers)];
                    for(int i=0;i < Integer.parseInt(nb_fichiers);i++)
                    {
                        listFichier[i] = bfr.readLine() ;
                    }
                    sock.close();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            grid_adapt.updateThumb(listFichier);
                            listFile.invalidateViews();
                        }
                    });


                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        };

        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK)
                {
                    if (data != null)
                    {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        //Log.i(TAG, "Uri = " + uri.toString());
                        try
                        {
                            // Get the file path from the URI
                            path = FileUtils.getPath(this, uri);

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
