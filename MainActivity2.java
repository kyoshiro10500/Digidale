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

public class MainActivity2 extends AppCompatActivity
{
    private String [] listFichier ;
    private boolean error ;
    private Handler handler ;
    private ProgressBar firstBar = null ;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 6384; // onActivityResult request
    public String path="";
    boolean loading = true ;
    public String extension ;
    private String nb_ips ="Default" ;
    private String ip_address="";
    private String ip_port="";
    private TextAdapter grid_adapt ;
    private ListView listFile ;

   /* protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        ArrayList saved = new ArrayList();
        saved.add(0,ip_address);
        saved.add(1,nb_ips) ;
        saved.add(2,ip_port) ;
        savedInstanceState.putStringArrayList("Ip_array",saved);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        if(intent != null)
        {
            nb_ips = intent.getStringExtra("EXTRA_MESSAGE2");
            ip_address=intent.getStringExtra("EXTRA_MESSAGE3");
            ip_port=intent.getStringExtra("EXTRA_MESSAGE4");
        }

        /*if (savedInstanceState != null){
            ArrayList ip_array = savedInstanceState.getStringArrayList("Ip_array") ;
            ip_port = (String) ip_array.get(2) ;
            ip_address = (String) ip_array.get(0) ;
            nb_ips = (String) ip_array.get(1) ;
            Toast toast = Toast.makeText(getApplicationContext(), ip_address, Toast.LENGTH_SHORT);
            toast.show();
        }
*/
        firstBar = (ProgressBar)findViewById(R.id.firstBar);

        handler = new Handler() ;


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
                            intent.putExtra("EXTRA_MESSAGE4",ip_port.toString());
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

        final Button btn_choosefile = (Button) findViewById(R.id.btn_choosefile);
        btn_choosefile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showChooser();
                // Code here executes on main thread after user presses button
            }
        });

        final Button btn_chooseserver = (Button) findViewById(R.id.btn_chooseserver);
        btn_chooseserver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showFileServer() ;
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
                            String message = "" ;
                            extension = FileUtils.getExtension(path) ;
                            if(extension.equals(".jpg"))
                            {
                                message = "begin-image/"+Integer.toString(b.length)+"-end" ;
                            }
                            else if(extension.equals(".mp4"))
                            {
                                message = "begin-video/"+Integer.toString(b.length)+"-end" ;
                            }

                            if(!message.equals(""))
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
                                error = true ;
                            }
                            loading = false ;

                        } catch (UnknownHostException e) {
                            loading = false ;
                            error = true ;
                        } catch (IOException e) {
                            loading = false ;
                            error = true ;
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
                                    if(!error)
                                    {
                                        Toast toast = Toast.makeText(getApplicationContext(), "fichier envoyé  : "+path, Toast.LENGTH_SHORT);
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
        });
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

    private void showFileServer() {
        // Use the GET_CONTENT intent from the utility class
        //lecture nomfichier + extension
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


                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };

        thread.start();
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
