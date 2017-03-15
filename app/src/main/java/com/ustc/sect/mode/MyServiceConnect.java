package com.ustc.sect.mode;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Lenovo on 2017/1/26.
 */

public class MyServiceConnect implements ServiceConnection
{

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        //服务连接
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        //服务断开
    }
}
