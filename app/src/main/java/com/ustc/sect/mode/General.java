package com.ustc.sect.mode;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.ustc.sect.R;
import com.ustc.sect.database.DatabaseOperation;
import com.ustc.sect.fragments.Session;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lenovo on 2016/11/14.
 */

public class General
{
    //定义Message的type字段
    public static final int ONLY_PICTURE=151245;
    public static final int ONLY_TEXT=151234;
    public static final int ONLY_PERSON=161267;
    public static final int HEARTBEAT=150000;
    public static String ENCODE_MODE="UTF-16";//String 类的编码和解码方式
    public static volatile boolean isLogout=false;
    public static final int Activity_Result_OK = 1;
    public static final int Activity_ChatRoom = 1000;
    public static final int RequestUpdate=10;
    public static final int Session_Fragment = 0;
    public static final int Friends_Fragment = 1;
    public static final int Myself_Fragment = 2;
    public static final int LOW_NOTICE_NUM = -1;//用于最低提醒的消息数量，大于该值是会有提示出现
    public static boolean isRefresh = true;//自动更新开关，需要持久化保存
    public static volatile String Current_ChatRoom_Name;
    //记录sessionItem的位置状态，在登入成功后从数据库中载入到sessionPositionMap中
    public static volatile List<String> sessionPositionMap;
    //记录置顶的sessionItem的数量
    public static volatile int toppedSum = -1;
    //记录用户的ID，该ID也是数据库名
    public static volatile int userID;
    //记录上下文
    public static volatile Context context;
    //记录主界面的Activity，用于挂在alterDialog
    public static volatile Activity mainActivity;
    //消息队列集
    public static Map<String, MessageQueue> messageQueueMap;
    //声明一个线程池
    private static ExecutorService pool;
    public static ExecutorService getThreadPool()
    {
        if (pool == null)
        {
            pool = Executors.newCachedThreadPool();
        }
        else if (pool.isShutdown())
        {
            pool=Executors.newCachedThreadPool();
        }
        return pool;
    }
    public static Cancel_SendMessage cancel_sendMessage=null;
    public interface Cancel_SendMessage
    {
        void cancel();
    }
    public static volatile boolean NetWorkAvailable=true;
    //检查网络可用性
    public static boolean checkNetWork(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return  (networkInfo != null && networkInfo.isConnected());
    }

    //声明一个阻塞队列
    private static ArrayBlockingQueue<Message> queue;

    public static ArrayBlockingQueue<Message> getBlockingQueue()
    {
        if (queue == null)
        {
            queue = new ArrayBlockingQueue<>(100);
        }
        return queue;
    }

    //这个函数的返回值是用来显示在TextView上面
    public static String dateToString(Date date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        return simpleDateFormat.format(date);
    }

    //转换为日期格式，用于数据库的存储
    @org.jetbrains.annotations.Contract("_ -> !null")
    public static Date longToDate(long dateL)
    {
        return new Date(dateL);
    }

    //定义一个用来存放数据的静态同步方法
    public static synchronized void saveMessage(Message message)
    {
        int from = message.getFrom();
        if (userID==message.getFrom())
        {
            from = message.getTo();
        }
        DatabaseOperation databaseOperation = DatabaseOperation.getDatabaseOperation();
        databaseOperation.isTableExist(from+"", message);
    }

    //定义一个更新session的接口
    public static UpdateSession updateSession;

    public interface UpdateSession
    {
        void updateSession();

        void deleteSession(String userID);
    }

    //定义一个接口在 binder实现，在activity中被调用；
    public static Pull pull;

    public interface Pull
    {
        Map getMessageFromMessageQueue();

        MessageQueue getMessageChain(String userID);

        void deleteMessageQueue(String userID);
    }

    //定义一个发消息的接口
    public static SendMessage sendMessage;

    public interface SendMessage
    {
        void send(Message message);
    }

    //定义一个接口用于排序使用,该接口在代理工厂中被实现
    public static SessionItemSort sessionItemSort;

    public interface SessionItemSort
    {
        void sort();
    }

    //根据视图产生一矩形
    public static RectF createRectFFromView(View view)
    {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
    }

    //判断点（x,y）是否在视图中
    public static boolean isContainPoint(View view, int x, int y)
    {
        if ((view.getVisibility() == View.VISIBLE) && createRectFFromView(view).contains(x, y))
            return true;
        else
            return false;
    }

    //移除list中的第position个元素,返回该元素
    public static <T> T remove(List<T> list, int position)
    {
        if (list != null && !list.isEmpty() && position < list.size())
        {
            int i = 0;
            for (Iterator<T> it = list.iterator(); it.hasNext(); i++)
            {
                if (i == position)
                {
                    T temp = it.next();
                    it.remove();
                    return temp;
                }
                it.next();
            }
        }
        return null;
    }

    public static String[] ObjectToString(Object[] objects)
    {
        if (objects.length != 0)
        {
            String[] s = new String[objects.length];
            for (int i = 0; i < s.length; i++)
            {
                s[i] = (String) objects[i];
            }
            return s;
        }
        throw new RuntimeException();
    }

    public enum From
    {
        NATIVE(0), TARGET(1);
        private Integer num;

        private From(int num)
        {
            this.num = num;
        }

        public Integer getNum()
        {
            return this.num;
        }
    }

    //聊天面板的接口定义
    public interface ChatRoom_update
    {
        void updateView();
    }

    //获取本机的ip地址
    public static String getHostIP()
    {
        String hostIp = null;
        try
        {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements())
            {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address)
                    {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip))
                    {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e)
        {
            e.printStackTrace();
        }
        return hostIp;
    }
    //字节数组转int
    public static int bytesToInt(byte[] bytes, int off)
    {
        int value=0;
        for(int i=0;i<4;i++)
        {
            value|=(bytes[off+i]&0xFF)<<8*(3-i);
        }
        return value;
    }
    //字节转long
    public static long bytesToLong(byte[] bytes,int off)
    {
        long value=0L;
        for(int i=0;i<8;i++)
        {
            value|=(long) (bytes[i+off]&0xFF)<<8*(7-i);
        }
        return value;
    }
    //long转byte[]
   public static byte[] longToBytes(byte[] bytes,int off,long data)
    {
        for(int i=0;i<8;i++)
        {
            bytes[off+i]=(byte) ((data>>8*(7-i))&0xFF);
        }
        return bytes;
    }
    //int 转byte[]
    public static byte[] intToBytes(byte[] bytes,int off,int data)
    {
        for(int i=0;i<4;i++)
        {
            bytes[off+i]=(byte)((data>>8*(3-i))&0xFF);
        }
        return bytes;
    }
    public enum TriStatus
    {
        SEND_MESSAGE,ADD_FRIEND,EXIT;
    }
    public interface CallBack
    {
        Bundle callBackBundle(Bundle bundle);
    }
}
