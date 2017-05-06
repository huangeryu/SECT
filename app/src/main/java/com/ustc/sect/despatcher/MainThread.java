package com.ustc.sect.despatcher;

import android.util.Log;
import android.widget.Toast;

import com.ustc.sect.R;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.SocketObj;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class MainThread implements Runnable
{
    public void run()
    {
        SocketObj socketObj=SocketObj.getSocketObj();
        Socket socket= socketObj.getSocket();
        if (!socket.isBound()||!socket.isConnected())socketObj.connect();
        ExecutorService pool=General.getThreadPool();
        try
        {
            Log.e("测试", "run: 接收前");
            if (General.NetWorkAvailable)
            {
                InputStream in=null;
                if(socketObj.getSocket().isConnected()&&!socketObj.getSocket().isClosed())
                    in = socketObj.getSocket().getInputStream();
                byte[] head=new byte[24];
                while (socketObj.getSocket().isConnected()&&!socketObj.getSocket().isClosed())
                {
                    if(in.read(head)==24)
                    {
                        int length=General.bytesToInt(head,20);
                        if(length>0)
                        {
                            byte[] content=new byte[length];
                            if(in.read(content)!=-1)
                            {
                                pool.submit(new Process(head,content));
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            //错误处理,不允许该线程停止
            if (General.checkNetWork(General.context)&&!General.isLogout)
                run();
            else
                if (!General.isLogout)
                    Toast.makeText(General.context,General.context.getText(R.string.netUnavailable),Toast.LENGTH_LONG).show();
        }
    }
}
//把消息进行解码并且将待处理的消息放入阻塞队列，供分发服务使用
class Process implements Runnable
{
     private Message message;
     Process(byte[] head,byte[] content)
    {
        this.message=new Message(head,content);
    }
    @Override
    public void run()
    {
//        message= new Decode(message).decode();//解码
        if(message.getFrom()!=Integer.parseInt(General.context.getString(R.string.Server_ID)))
        {
            Log.d("收到的message", "run: "+message);
            //入口,这里进行重构
            BlockingQueue queue= General.getBlockingQueue();
            try
            {
                queue.put(message);
            }catch (InterruptedException e)
            {
                //被其他线程中断处理
                e.printStackTrace();
            }
        }
        else
        {//这里处理服务器响应或请求类的处理

        }
    }
}