package com.ustc.sect.despatcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.ustc.sect.R;
import com.ustc.sect.crytography.Encode;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.SocketObj;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lenovo on 2017/1/14.
 */
//这个类用来发送消息，发送心跳包，消息存储，对数据的存储使用了同步机制可能效率比较低，
//当该程序反应慢时需要检查该类
//需要确保该类在用户成功登入后，login activity销毁前实例化
public class SendMessageService extends Thread implements General.Cancel_SendMessage
{
    private Context context;
    private Message heartBeat;
    private Timer timer;
    private volatile long intervalTime;
    private static SocketObj socketObject;
    private   Handler handler;
    private Looper myLooper;
    //构造器
    public SendMessageService()
    {
        this.context= General.context;
        General.cancel_sendMessage=this;
        heartBeat=new Message(General.userID,Integer.parseInt(context.getString(R.string.Server_ID)),
                General.HEARTBEAT);
        timer=new Timer();
        intervalTime=0;
        timer.schedule(new HeartBeat(),500,300000);//这里设置了心跳包的间隔时间为5分钟
        socketObject= SocketObj.getSocketObj();
        General.sendMessage=new SendMessageBinder();
    }

    @Override
    public void run()
    {
        Looper.prepare();
        this.myLooper=Looper.myLooper();
        handler=new Handler()
        {
            @Override
            public void handleMessage(android.os.Message message)
            {
                //这里处理发送消息
                Bundle bundle = message.getData();
                byte[] content = bundle.getByteArray("message");
//                Log.d("jsonString","handleMessage:"+jsonString);
                General.getThreadPool().submit(new SendMessageThread(content));
            }
        };
        Looper.loop();
    }

    @Override
    public void cancel()
    {
        if(this.myLooper!=null)
            myLooper.quit();//强制退出looper
    }

    class SendMessageBinder  implements General.SendMessage
    {
        @Override
        public void send(final Message message)//这里的message为自定义的
        {//这个方法的作用是把message发送到looper中，并且放入数据库中
            Bundle bundle=new Bundle();
            byte[] content=new Encode(message.messageTobyte()).encode();
            bundle.putByteArray("message",content);
            android.os.Message sMessage= android.os.Message.obtain();
            sMessage.setData(bundle);
            handler.sendMessage(sMessage);
            ExecutorService pool=General.getThreadPool();
            pool.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    General.saveMessage(message);
                }
            });
        }
    }
//   private class  SocketObject
//    {//该类的建立主要是为了避免锁定一个非final的变量，导致锁失效
//        private Socket socket;
//        private InetSocketAddress address;
//        public SocketObject()
//        {
//            this.socket=new Socket();
//            address=new InetSocketAddress(context.getString(R.string.server_ip)
//                    ,Integer.parseInt(context.getString(R.string.Server_port)));
//        }
//        public void connect()
//        {
//            try
//            {
//                this.socket.connect(this.address);
//            }catch (IOException e)
//            {
//                this.socket=new Socket();
//                try
//                {
//                    this.socket.connect(this.address);
//                }catch (IOException e1)
//                {
//                    e1.printStackTrace();
//                }
//            }
//        }
//    }
    class SendMessageThread implements Runnable
    {
        private byte[] message;
        private Integer count=10;
        public SendMessageThread(byte[] s)
        {
            this.message=s;
        }
        public SendMessageThread(Message message)
        {
            this.message=message.messageTobyte();
        }
        @Override
        public void run()
        {
            if (!socketObject.getSocket().isConnected()&&!General.isLogout)socketObject.connect();
            printOut();
        }
        private void printOut()
        {
            try
            {
                OutputStream outputStream=socketObject.getSocket().getOutputStream();
                outputStream.write(message);
                outputStream.flush();
                intervalTime=new Date().getTime();
                Log.d("state", "printOut: 发送完毕");
            }catch (IOException e)
            {
                count--;
                if (count>0&&!General.isLogout)
                {
                    Log.d("连接", "printOut: "+"再一次连接");
                    socketObject.connect();
                    printOut();
                }
                else
                {
                    e.printStackTrace();
                }
            }
        }
    }
    class HeartBeat extends TimerTask
    {
        @Override
        public void run()
        {
            long now=new Date().getTime();
           if (now-intervalTime>=300000)
           {
//               Log.d("时间", "run: "+General.dateToString(new Date(now))+"    "+General.dateToString(new Date(intervalTime)));
               android.os.Message message= android.os.Message.obtain();
               Bundle bundle=new Bundle();
               heartBeat.setDate(new Date().getTime());
               bundle.putByteArray("message",heartBeat.messageTobyte());
               message.setData(bundle);
               handler.sendMessage(message);
           }
        }
    }
}
