package com.example.jonathan.applicationtest;

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
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Stack;

public class MainActivity2 extends AppCompatActivity
{
    private Handler handler ;
    private ProgressBar firstBar = null ;
    private String nb_ips ="Default" ;
    private String ip_address="";
    private String ip_port="";
    private Integer[] tabChoice ;
    Stack<Integer> pileChoix = new Stack<Integer>();
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 6384; // onActivityResult request
    public String path;
    boolean loading = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        nb_ips = intent.getStringExtra("EXTRA_MESSAGE2");
        ip_address=intent.getStringExtra("EXTRA_MESSAGE3");
        ip_port=intent.getStringExtra("EXTRA_MESSAGE4");

        tabChoice = new Integer[Integer.parseInt(nb_ips)] ;
        for(int i=0;i< Integer.parseInt(nb_ips);i++)
        {
            tabChoice[i] = 0 ;
            pileChoix.push(Integer.parseInt(nb_ips)-i-1) ;
        }
        firstBar = (ProgressBar)findViewById(R.id.firstBar);
        handler = new Handler() ;

        GridView gridview = (GridView) findViewById(R.id.gridViewScreens);
        ImageAdapter grid_adapt = new ImageAdapter(this);
        gridview.setAdapter(grid_adapt);
        grid_adapt.updateThumb(Integer.parseInt(nb_ips));
        gridview.invalidateViews();
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener()
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
                            out.println("begin-idscreen"+Integer.toString(position)+"-end");
                            s.close();
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

        final GridView gridview2 = (GridView) findViewById(R.id.gridViewSelect);
        final ImageAdapter grid_adapt2 = new ImageAdapter(this);
        gridview2.setAdapter(grid_adapt2);
        grid_adapt2.updateThumbSelecter(4);
        gridview2.invalidateViews();
        gridview2.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id)
            {
                int i = 0 ;
                for(i=0;i<tabChoice.length;i++)
                {
                    if(tabChoice[i] == position+1)
                    {
                        break ;
                    }
                }
                if(i == tabChoice.length)
                {
                    if(pileChoix.empty())
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), "Pas assez d'écran", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else
                    {
                        int indice = pileChoix.pop() ;
                        tabChoice[indice] = position+1 ;
                        Toast toast = Toast.makeText(getApplicationContext(), (indice+1)+" Placé", Toast.LENGTH_SHORT);
                        toast.show();
                        grid_adapt2.showNumThumb(indice,position);
                        gridview2.invalidateViews();
                    }

                }
                else
                {
                    pileChoix.push(i) ;
                    Toast toast = Toast.makeText(getApplicationContext(), (i+1)+" Enlevé", Toast.LENGTH_SHORT);
                    toast.show();
                    tabChoice[i] = 0 ;
                    grid_adapt2.hideNumThumb(position);
                    gridview2.invalidateViews();
                }

            }
        });


        final FloatingActionButton btn_launch = (FloatingActionButton) findViewById(R.id.btn_launch);
        btn_launch.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnLaunch();
                // Code here executes on main thread after user presses button
            }
        });

        final FloatingActionButton btn_stopserver = (FloatingActionButton) findViewById(R.id.btn_stopserver);
        btn_stopserver.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnStopServer();
                // Code here executes on main thread after user presses button
            }
        });
        final Button btn_choosefile = (Button) findViewById(R.id.btn_choosefile);
        btn_choosefile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooser();
                // Code here executes on main thread after user presses button
            }
        });
        final Button btn_show_path = (Button) findViewById(R.id.show_path);
        btn_show_path.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File file=new File(path);
                loading = true ;
                final byte[] b = new byte[(int) file.length()];
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileInputStream.read(b);
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    loading=false ;
                }

                //final String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                Thread t = new Thread() {

                    @Override
                    public void run() {
                        try {
                            Socket sock = new Socket(ip_address, Integer.parseInt(ip_port));
                            PrintWriter out = new PrintWriter(
                                    new BufferedWriter(
                                            new OutputStreamWriter(sock.getOutputStream())
                                    )
                                    , true);
                            out.write("begin-image/"+Integer.toString(b.length)+"-end");
                            out.flush();
                            SystemClock.sleep(1000);
                            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                            dos.write(b);
                            dos.flush();
                            sock.shutdownInput();
                            loading = false ;
                        } catch (UnknownHostException e) {
                            loading = false ;
                            e.printStackTrace();
                        } catch (IOException e) {
                            loading = false ;
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                firstBar.setProgress(0);
                firstBar.setVisibility(View.VISIBLE);
                Thread thread = new Thread() {
                    @Override
                    public void run(){
                            int i = 0 ;
                            while( loading )
                            {
                                i = i + 5;
                                firstBar.setProgress(i);
                            }

                            // Update the progress bar
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    firstBar.setVisibility(View.INVISIBLE);
                                    Toast toast = Toast.makeText(getApplicationContext(), "fichier envoyé  : "+path, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });

                    }

                };

                thread.start();

            }
        });
    }

    public void onClickBtnLaunch()
    {
        Thread t = new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    Socket s = new Socket(ip_address,Integer.parseInt(ip_port));
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(s.getOutputStream())),
                            true);
                     String chaine = "begin-launch-" ;
                    for(int i=0; i < tabChoice.length;i++)
                    {
                        chaine+=tabChoice[i] ;
                        chaine+="/" ;
                    }
                    chaine+="-end" ;

                    out.println(chaine);
                    s.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        Toast toast = Toast.makeText(getApplicationContext(), "Affichage lancé", Toast.LENGTH_SHORT);
        toast.show();
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
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "chooser title");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        //Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            path = FileUtils.getPath(this, uri);

                        } catch (Exception e) {
                            //Log.e("MainActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
