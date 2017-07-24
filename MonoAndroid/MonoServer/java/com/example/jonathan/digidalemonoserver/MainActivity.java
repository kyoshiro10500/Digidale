package com.example.jonathan.digidalemonoserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    String toRead ;
    String stringListe ;
    private Handler handler ;
    private String path ;
    private String ip = "";
    private int port = 7000;
    private ServerSocket socket ;
    private String message = "" ;
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


        File f = new File(Environment.getExternalStorageDirectory().getPath()+"/digidale");
        if (!f.exists()) {
            f.mkdir();
        }
        path = f.getAbsolutePath();

        Scan() ;

    }

    public void Scan()
    {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                        socket = new ServerSocket(port) ;
                        CreateListener();
                    }
                }
            }
        } catch (SocketException e) {
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ip == "") {
            ip = "Pas de réseau";
        }

        TextView ipDisplay = (TextView) findViewById(R.id.ipDisplay);
        ipDisplay.setText(ip);
        ip="" ;
    }

    public void CreateListener()
    {
        Thread listener = new Thread() {

            @Override
            public void run() {
                while(true)
                {
                    try {
                        Socket socketConnexion = socket.accept() ;
                        BufferedReader bfr = new BufferedReader(new InputStreamReader(socketConnexion.getInputStream()));
                        message = bfr.readLine() ;
                        if(message == null)
                        {
                            message = "" ;
                        }
                        if(!message.isEmpty())
                        {
                            message = (message.split("begin-")[1]).split("-end")[0] ;
                            if(message.indexOf("launch")!=-1)
                            {
                                if(toRead != "")
                                {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ImageView img = (ImageView) findViewById(R.id.imageView) ;
                                            Bitmap mybit = BitmapFactory.decodeFile(path +"/"+toRead) ;
                                            img.setImageBitmap(mybit);
                                            img.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                            if(message.indexOf("lecture")!=-1)
                            {
                               toRead = message.split("/")[1] ;
                            }
                            else if(message.indexOf("stop")!=-1)
                            {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ImageView img = (ImageView) findViewById(R.id.imageView) ;
                                        img.setVisibility(View.INVISIBLE);
                                    }
                                });

                            }
                            else if(message.indexOf("image")!=-1)
                            {
                                socketConnexion.close();
                                Socket socketConnexion2 = socket.accept() ;
                                InputStream myInput = socketConnexion2.getInputStream();

                                File dossier = new File(path) ;
                                if(dossier.exists() && dossier.isDirectory())
                                {
                                    File[] liste = dossier.listFiles() ;
                                    int nombre = liste.length + 1 ;
                                    String outFileName = path+"/image"+nombre+".jpg";
                                    File image = new File(outFileName) ;
                                    image.createNewFile();
                                    FileOutputStream myOutput = new FileOutputStream(outFileName);
                                    byte[] buffer = new byte[2048];
                                    int length;
                                    while ((length = myInput.read(buffer))>0){
                                        myOutput.write(buffer, 0, length);
                                    }

                                    //Close the streams
                                    myOutput.close();
                                }
                                myInput.close();
                                socketConnexion2.close();


                            }
                            else {
                                if (message.indexOf("recherche") != -1) {
                                    SystemClock.sleep(1000);
                                    File dossier = new File(path);
                                    String ip = socketConnexion.getInetAddress().getHostAddress() ;
                                    socketConnexion.close();
                                    if (dossier.exists() && dossier.isDirectory()) {
                                        Socket sock = new Socket(ip,port) ;
                                        File[] liste = dossier.listFiles();
                                        int nbr = liste.length;
                                        stringListe = nbr + "\n";
                                        int i;
                                        for (i = 0; i < nbr; i++) {
                                            stringListe += liste[i].getName() + "\n";
                                        }
                                        PrintWriter out = new PrintWriter(
                                                new BufferedWriter(
                                                        new OutputStreamWriter(sock.getOutputStream())
                                                )
                                                , true);
                                        out.write(stringListe);
                                        out.flush();
                                        sock.close();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break ;
                    }
                }
               // Scan() ;
            }
        };
        listener.start();
    }

}




