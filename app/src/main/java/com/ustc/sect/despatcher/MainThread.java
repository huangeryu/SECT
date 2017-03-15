package com.ustc.sect.despatcher;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ustc.sect.R;
import com.ustc.sect.crytography.Decode;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.SocketObj;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
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
            while (General.NetWorkAvailable)
            {
                InputStream inputStream = socketObj.getSocket().getInputStream();
                Scanner in = new Scanner(inputStream);
                StringBuffer buffer=new StringBuffer();
                if (in.hasNextLine())
                {
                    buffer.append(in.nextLine());
                    Log.d("接收的消息", "call: " + new String(buffer));
                    pool.submit(new Process(buffer.toString()));
                }
            }
        }
        catch (IOException e)
        {
            //错误处理,不允许该线程停止
            if (General.checkNetWork(General.context))
                run();
            else
                Toast.makeText(General.context,General.context.getText(R.string.netUnavailable),Toast.LENGTH_LONG).show();
        }
    }
}
//把消息进行解码并且将待处理的消息放入阻塞队列，供分发服务使用
class Process implements Runnable
{
    private String message;
     Process(String message)
    {
        this.message=message;
    }
    @Override
    public void run()
    {

        message= new Decode(message).decode();//解码
        if(message!=null)
        {
            Log.d("收到的message", "run: "+message);
            //入口
            Message messageObject=new Message(message);
            BlockingQueue queue= General.getBlockingQueue();
            try
            {
                queue.put(messageObject);
            }catch (InterruptedException e)
            {
                //被其他线程中断处理
                e.printStackTrace();
            }
        }
    }
}