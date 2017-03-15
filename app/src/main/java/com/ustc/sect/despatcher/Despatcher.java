package com.ustc.sect.despatcher;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustc.sect.activity.ChatRoom;
import com.ustc.sect.activity.MainScreen;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.MessageQueue;
import com.ustc.sect.mode.ProxyFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by Lenovo on 2017/1/14.
 */
//这类需要在登入成功后被实例化
public class Despatcher  implements Runnable
{
    private  Map<String,MessageQueue> messageQueueMap;
    public Despatcher()
    {
        if (messageQueueMap==null)
        messageQueueMap= new HashMap<>();
        General.messageQueueMap=messageQueueMap;
        new ServiceBinder();
    }
    @Override
    public void run()
    {
        while (true)
        {
            ArrayBlockingQueue<Message> queue= General.getBlockingQueue();
            try
            {
                final Message message=queue.take();
                dispatch(message);
                ExecutorService pool=General.getThreadPool();
                pool.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        General.saveMessage(message);
                    }
                });
                //这里需要通知mainScreen来进行更新，从而实现自动更新
                //更新的机制使用handler进行传递更新信号
                if (General.isRefresh)
                {
                    android.os.Message msg=android.os.Message.obtain();
                    msg.what=General.Session_Fragment;
                    MainScreen.handler.sendMessage(msg);
                }
            }catch (InterruptedException e)
            {
                //被打断处理
            }
        }
    }
    private class  ServiceBinder  implements General.Pull
    {
         ServiceBinder()
        {
            General.pull=(General.Pull)new ProxyFactory(this).bind();
        }
        //这里被用来手动更新消息,Map<String,Integer>中的string代表用户名
        //如果没有消息需要到数据库中取
        @Override
        public Map<String,Map> getMessageFromMessageQueue()
        {
            HashMap<String,Map> map=new HashMap<>();
            Set<String> keySet=messageQueueMap.keySet();
            for (Iterator<String> it=keySet.iterator();it.hasNext();)
            {
                String userID=it.next();
                int capacity=messageQueueMap.get(userID).getAvailableCount();
                if (capacity!=0)
                {
                    Map<String,Object> objectMap=new HashMap<>();
                    objectMap.put("updateSum",capacity);
                    objectMap.put("lastMessage",messageQueueMap.get(userID).peekMessage());//如果获得的消息不是最新的检查这里
                    map.put(userID,objectMap);
                }
            }
            if (map.isEmpty())return null;//返回空代表没有要更新的数据
            else return map;
        }
        @Override
        public MessageQueue getMessageChain(String userID)
        {//获取消息队列
            return messageQueueMap.get(userID);
        }
        @Override
        public void deleteMessageQueue(String userID)
        {//删除消息队列
            messageQueueMap.get(userID).clearQueue();
        }
    }
    private void dispatch(Message message)
    {
        String name=message.getFrom();
        if (name.equals(General.userID))
        {
            name=message.getTo();
        }
        if(messageQueueMap.containsKey(name))
        {
            MessageQueue messageQueue=messageQueueMap.get(name);
            messageQueue.insertMessage(message);
        }
        else
        {
            messageQueueMap.put(name,new MessageQueue(name).insertMessage(message));
        }
        if (General.Current_ChatRoom_Name!=null)
        {
            if (name.equals(General.Current_ChatRoom_Name))
            {
                android.os.Message msg= android.os.Message.obtain();
                msg.what=General.RequestUpdate;
                ChatRoom.handler.sendMessage(msg);
            }
        }
    }
}
