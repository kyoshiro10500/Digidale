package com.example.jonathan.applicationtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Stack;

public class MainActivity3 extends AppCompatActivity {

    private String nb_ips ="Default" ;
    private String ip_address="";
    private String ip_port="";
    private Integer[] tabChoice ;
    private Stack<Integer> pileChoix = new Stack<Integer>();
    private GridView gridview2 ;
    private Integer nbLigne ;
    private Integer nbColonne ;
    private Handler handler ;
    private ImageAdapter grid_adapt2 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Intent intent = getIntent();
        handler = new Handler() ;
        nb_ips = intent.getStringExtra("EXTRA_MESSAGE2");
        ip_address=intent.getStringExtra("EXTRA_MESSAGE3");
        ip_port=intent.getStringExtra("EXTRA_MESSAGE4");

        tabChoice = new Integer[Integer.parseInt(nb_ips)] ;
        for(int i=0;i< Integer.parseInt(nb_ips);i++)
        {
            tabChoice[i] = 0 ;
            pileChoix.push(Integer.parseInt(nb_ips)-i-1) ;
        }

        nbLigne = Integer.parseInt(nb_ips) ;
        nbColonne = Integer.parseInt(nb_ips) ;

        GridView gridview = (GridView) findViewById(R.id.gridViewScreens);
        ImageAdapter2 grid_adapt = new ImageAdapter2(this,Integer.parseInt(nb_ips));
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

        gridview2 = (GridView) findViewById(R.id.gridViewSelect);
        grid_adapt2 = new ImageAdapter(this);
        gridview2.setAdapter(grid_adapt2);
        gridview2.setNumColumns(nbColonne);
        grid_adapt2.updateThumbSelecter(nbLigne*nbColonne);
        ViewGroup.LayoutParams layoutParams = gridview2.getLayoutParams();
        layoutParams.width = (int) (nbColonne*grid_adapt2.scale*55 +0.5f); //this is in pixels
        gridview2.setLayoutParams(layoutParams);
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

        final Button btn_moinsLigne = (Button) findViewById(R.id.btn_moinsLigne) ;
        btn_moinsLigne.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnExpandGrid(true,false) ;
            }
        });

        final Button btn_plusLigne = (Button) findViewById(R.id.btn_plusLigne) ;
        btn_plusLigne.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnExpandGrid(true,true) ;
            }
        });

        final Button btn_moinsColonne = (Button) findViewById(R.id.btn_moinsColonne) ;
        btn_moinsColonne.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnExpandGrid(false,false) ;
            }
        });

        final Button btn_plusColonne = (Button) findViewById(R.id.btn_plusColonne) ;
        btn_plusColonne.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onClickBtnExpandGrid(false,true) ;
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
                        Socket s = new Socket(ip_address, Integer.parseInt(ip_port));
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(s.getOutputStream())),
                                true);
                        String chaine = "begin-launch-";
                        for (int i = 0; i < tabChoice.length; i++) {
                            chaine += tabChoice[i];
                            chaine += "/";
                        }
                        chaine+="|" ;
                        chaine+=nbLigne ;
                        chaine+="|" ;
                        chaine+=nbColonne ;
                        chaine += "-end";

                        out.println(chaine);
                        s.close();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), "Affichage lancé", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
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

    public void onClickBtnExpandGrid(boolean Ligne, boolean Plus)
    {
        if(Ligne)
        {
            if(Plus)
            {
                if(nbLigne != Integer.parseInt(nb_ips))
                {
                    nbLigne++ ;
                }
            }
            else
            {
                if(nbLigne != 1)
                {
                    nbLigne-- ;
                }
            }
        }
        else
        {
            if(Plus)
            {
                if(nbColonne != Integer.parseInt(nb_ips))
                {
                    nbColonne++ ;
                }
            }
            else
            {
                if(nbColonne != 1)
                {
                    nbColonne-- ;
                }
            }
        }
        int i ;
        pileChoix.clear();
        for(i=0;i<Integer.parseInt(nb_ips);i++)
        {
            tabChoice[i] = 0 ;
            pileChoix.push(Integer.parseInt(nb_ips)-i-1) ;
        }
        gridview2.setNumColumns(nbColonne);
        grid_adapt2.updateThumbSelecter(nbLigne*nbColonne);
        ViewGroup.LayoutParams layoutParams = gridview2.getLayoutParams();
        layoutParams.width = (int) (nbColonne*grid_adapt2.scale*55 +0.5f); //this is in pixels
        gridview2.setLayoutParams(layoutParams);
        gridview2.invalidateViews();
    }
}
