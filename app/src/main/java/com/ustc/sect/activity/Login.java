package com.ustc.sect.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.*;
import com.ustc.sect.R;
import com.ustc.sect.crytography.Decode;
import com.ustc.sect.crytography.Encode;
import com.ustc.sect.database.DatabaseOperation;
import com.ustc.sect.despatcher.Despatcher;
import com.ustc.sect.despatcher.MainThread;
import com.ustc.sect.mode.General;

/**
 * Created by Lenovo on 2016/11/13.
 */

public class Login extends Activity
{
    private SharedPreferences sharedPreferences;
    private final String Paths="login";
    private final String USERNAME_EXPRESS="^[0-9]{8}$";
    private final String PASSWORD_EXPRESS ="^[a-zA-Z0-9]{1,9}";
    private EditText username;
    private EditText password;
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            String isCorrect=(String) message.obj;
            if("true".equals(isCorrect))//这里的“true”仅仅表示验证成功，测试时使用
            {
                //登入成功后开始初始化
                General.userID=username.getText().toString().trim();
                General.context=getApplicationContext();
                DatabaseOperation.getDatabaseOperation().loadSessionMap();//这里导入数据库中的sessionPosition数据
                SharedPreferences user=getSharedPreferences(".user",MODE_PRIVATE);
                General.toppedSum=Integer.parseInt(user.getString("toTop",-1+""));
                new Thread(new MainThread()).start();
                new Thread(new Despatcher()).start();
                startActivity(new Intent(Login.this,MainScreen.class));
                finish();
            }
            else
            {Toast.makeText(Login.this,"核对用户错误",Toast.LENGTH_LONG).show();}
        }
    };
    protected void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.login);

        username=(EditText)findViewById(R.id.login_username);
        password =(EditText)findViewById(R.id.login_passwd);
        Button login_loginButton=(Button) findViewById(R.id.login_loginbutton);
        Button login_exitButton=(Button)findViewById(R.id.login_exitbutton);
        sharedPreferences=getSharedPreferences(".user", Context.MODE_PRIVATE);
        if(sharedPreferences!=null&&sharedPreferences.contains("username"))
        {
            username.setText(sharedPreferences.getString("username",null));
        }
        login_loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(check(username,USERNAME_EXPRESS,true)&&check(password, PASSWORD_EXPRESS,false))
                {
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("username",username.getText().toString());
                    editor.apply();
                    //检查是否联网
                    if(General.checkNetWork(Login.this))
                    {
                        //连接服务器核对用户名和密码；
                        checkUsernameAndPasswd();
                    }
                    else
                    {
                        Toast.makeText(Login.this,getString(R.string.netUnavailable),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        login_exitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                System.exit(0);
            }
        });
    }
    private boolean check(EditText text,String regex,boolean isName)
    {
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(text.getText().toString());
        boolean isFind=matcher.find();
        if(!isFind)
        {
            if (isName)text.setError(getString(R.string.nameFormError));
            else text.setError(getString(R.string.passwdFormError));
        }
        return isFind;
    }
    public void checkUsernameAndPasswd()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                String base=getString(R.string.URL);
                PrintWriter out;
                Scanner in=null;
                try
                {
                    URL url=new URL(base+Paths);
                    System.out.println(url);
                    HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();
                    try
                    {
                        out=new PrintWriter(httpURLConnection.getOutputStream(),true);
                        //把用户名和密码加密
                        String cipherText=new Encode("name="+username.getText().toString()+"&password="+ password.getText().toString()).encode();
                        out.write(cipherText);
                        out.close();
                        in=new Scanner(httpURLConnection.getInputStream());
                        if(in.hasNextLine()) {
                            Message message = Message.obtain();
                            //在这里约定服务器对用户和密码的验证采用别的格式
                            //这里也许可以设计为携带验证码和消息
                            StringBuilder buffer=new StringBuilder();
                            while(in.hasNext())
                            {
                                buffer.append(in.nextLine());
                            }
                            message.obj=new Decode(buffer.toString()).decode();
                            System.out.println(message.obj);
                            handler.sendMessage(message);
                        }

                    }finally
                    {
                        if (in!=null)
                            in.close();
                    }
                }
                catch (MalformedURLException e)
                {
                    //URL异常
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    //连接异常
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
