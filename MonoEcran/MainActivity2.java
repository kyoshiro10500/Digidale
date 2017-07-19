package com.example.jonathan.applicationtest2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
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

    private String ip_address="";
    private String ip_port="";

    private String [] listFichier ;
    private ListView listFile ;


    public String fileChosen="" ;
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
            ip_address=intent.getStringExtra("EXTRA_MESSAGE3");
            ip_port=intent.getStringExtra("EXTRA_MESSAGE4");
        }

        //Bouton permettant d'envoyer un message au controleur pour afficher la vidéo
        final FloatingActionButton btn_launch = (FloatingActionButton) findViewById(R.id.btn_launch);
        btn_launch.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnLaunch();
                // Code here executes on main thread after user presses button
            }
        });

        //Bouton permettant d'envoyer un message au controleur pour arreter l'affichage
        //Problème connu : Si l'utilisateur quitte l'application pendant la lecture, celle-ci ne s'arrete pas
        //TODO Protéger la lecture pour qu'elle s'arrete quand on quittte l'appli/activité
        final FloatingActionButton btn_stop = (FloatingActionButton) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnStopServer();
                // Code here executes on main thread after user presses button
            }
        });

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
                            fileChosen = grid_adapt.getThumb(position) ;
                            s.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Fichier :  " + fileChosen + " sélectionné", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de la requête. Veuillez réessayer", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
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
                                        Toast toast = Toast.makeText(getApplicationContext(), "fichier envoyé :  " + path, Toast.LENGTH_SHORT);
                                        path = "";
                                        toast.show();
                                    }
                                    else
                                    {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de l'upload. Veuillez réessayer", Toast.LENGTH_SHORT);
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

    public void onClickBtnLaunch()
    {
        if(fileChosen!="") {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        //Le message standard est de la forme begin-launch-position/position/nbligne|nbcolonne-end
                        Socket s = new Socket(ip_address, Integer.parseInt(ip_port));
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(s.getOutputStream())),
                                true);
                        String chaine = "begin-launch-end";

                        out.println(chaine);
                        s.close();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), "Affichage lancé", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        else
        {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), "Veuillez choisir un fichier", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }

    public void onClickBtnStopServer()
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket s = new Socket(ip_address,Integer.parseInt(ip_port));
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(s.getOutputStream())
                            ),
                            true);
                    out.println("begin-stop-end");
                    s.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        Toast toast = Toast.makeText(getApplicationContext(), "Affichage arrêté", Toast.LENGTH_SHORT);
        toast.show();
    }

}
