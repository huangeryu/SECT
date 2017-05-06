package com.ustc.sect.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ustc.sect.R;
import com.ustc.sect.database.DatabaseOperation;
import com.ustc.sect.despatcher.SendMessageService;
import com.ustc.sect.fragments.Friends;
import com.ustc.sect.fragments.Myself;
import com.ustc.sect.fragments.Session;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.MyServiceConnect;

/**
 * Created by Lenovo on 2016/11/14.
 */
public class MainScreen extends Activity
{
    private FragmentManager fmg;
    private Friends friends;
    private Myself myself;
    private TextView headTitle;
    private Session session;
    public static Handler handler;
    public MainScreen()
    {
        fmg=getFragmentManager();
        handler=new MyHandler();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);
        headTitle=(TextView)findViewById(R.id.myTitle);
        init();
        final RadioButton sessionButton=(RadioButton) findViewById(R.id.session);
        sessionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectItem(R.id.session);
            }
        });
        RadioButton friendButton=(RadioButton)findViewById(R.id.friends);
        friendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectItem(R.id.friends);
            }
        });
        RadioButton myself=(RadioButton)findViewById(R.id.myself);
        myself.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectItem(R.id.myself);
            }
        });
    }
    private void init()
    {
        General.mainActivity=this;
        new SendMessageService().start();
        selectItem(R.id.session);
    }
    @Override
    public void onPause()
    {
        //这里处理保存持久化数据
        super.onPause();
        DatabaseOperation.getDatabaseOperation().saveSessionMap();
        SharedPreferences user=getSharedPreferences(".user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=user.edit();
        editor.putString("toTop",General.toppedSum+"");
        editor.apply();
       // Log.d("save", "onPause: "+General.sessionPositionMap.toString()+","+General.toppedSum);
    }
    public void hideAllFragment()
    {
        FragmentTransaction ft=fmg.beginTransaction();
        if (session!=null)
            ft.hide(session);
        if(myself!=null)ft.hide(myself);
        if(friends!=null)ft.hide(friends);
        ft.commit();
    }
    public void selectItem(int id)
    {
        //这里还没有添加更新listView语句
        hideAllFragment();
        FragmentTransaction ft=fmg.beginTransaction();
        switch (id)
        {
            case R.id.friends:
                if(friends==null)
                {
                    friends=new Friends();
                    ft.add(R.id.replace,friends);
                }
                else
                {
                    ft.show(friends);
                }
                ft.commit();
                headTitle.setText(R.string.friendsTitle);
                break;
            case R.id.session:
                if(session==null)
                {
                    session=new Session();
                    ft.add(R.id.replace,session);
                }
                else
                {
                    ft.show(session);
                    General.updateSession.updateSession();
                }
                ft.commit();
                headTitle.setText(R.string.sessionTitle);
                break;
            case R.id.myself:
                if(myself==null)
                {
                    myself=new Myself();
                    ft.add(R.id.replace,myself);
                }else
                {
                    ft.show(myself);
                }
                ft.commit();
                headTitle.setText(R.string.myselfTitle);
                break;
        }
    }
private static class  MyHandler extends Handler
{
    @Override
    public void handleMessage(Message message)
    {
        switch (message.what)
        {
            case General.Session_Fragment:
                General.updateSession.updateSession();
                break;
            case General.Friends_Fragment:
                break;
            case General.Myself_Fragment:
                break;
        }
    }
}
}
