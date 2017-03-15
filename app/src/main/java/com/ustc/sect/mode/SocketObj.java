package com.ustc.sect.mode;

import com.ustc.sect.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Lenovo on 2017/2/11.
 */

//定义一该用于接收消息和发送消息的Socket
public class SocketObj
{
    private static SocketObj socketObj;
    private volatile Socket socket;
    private InetSocketAddress address;
    private int connectCount=10;
    private SocketObj()
    {
        this.socket=new Socket();
        address=new InetSocketAddress(General.context.getString(R.string.server_ip),
                Integer.parseInt(General.context.getString(R.string.Server_port)));
    }
    public static SocketObj getSocketObj()
    {
        if (socketObj==null)
        {
            socketObj=new SocketObj();
        }
        return socketObj;
    }
    public void connect()
    {
        try
        {
            connectCount--;
            socket.connect(address);
        }catch (IOException e)
        {
            //尝试重新连接
            if (connectCount>0)
            {
                this.socket=new Socket();
                connect();
            }
            else
            {
                e.printStackTrace();
            }
        }
    }
    public Socket getSocket()
    {
        return socket;
    }
}
