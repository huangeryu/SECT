package com.ustc.sect.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.ustc.sect.R;
import com.ustc.sect.despatcher.MainThread;

import java.util.concurrent.ExecutorService;

/**
 * Created by Lenovo on 2017/3/14.
 */

public class NetWorkListener extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if (info!=null&&info.isConnectedOrConnecting())
        {
            General.NetWorkAvailable = true;
            ExecutorService pool=General.getThreadPool();
            pool.submit(new MainThread());
        }
        else
        {
            General.NetWorkAvailable = false;
            Toast.makeText(context,context.getString(R.string.netUnavailable),Toast.LENGTH_LONG).show();
        }
    }
}
