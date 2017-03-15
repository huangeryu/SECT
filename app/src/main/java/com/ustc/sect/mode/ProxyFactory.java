package com.ustc.sect.mode;
import com.ustc.sect.database.DatabaseOperation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lenovo on 2017/1/21.
 */
//这里使用动态代理，检查lists和数据库表的差异，例如，如果lists=null，并且没有更新，那么该代理会自动提取数据库中
// 的数据放入lists中
public class ProxyFactory implements InvocationHandler,General.SessionItemSort
{
    private Object beProxy;
    private MessageQueue messageQueue;//表示消息队列
    private Map<String,Map<String,Object>> lists;//用map类型存储sessionListView的数据源
    public ProxyFactory(Object beProxy)
    {
        this.beProxy=beProxy;
        General.sessionItemSort=this;
    }
    public  Object bind()
    {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),beProxy.getClass().getInterfaces(),this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        //这里只拦截getMessageFromMessageQueue
        if (method.getName().equals("getMessageFromMessageQueue"))
        {
            Map<String, Map<String, Object>> map = (Map) method.invoke(beProxy, args);
            if (lists == null)
            {//这里需要把数据库的消息加进lists中
                lists = new HashMap<>();
                if (General.sessionPositionMap != null)
                {
                    for (String s : General.sessionPositionMap)
                    {
                        Message message = DatabaseOperation.getDatabaseOperation().getLastMessage(s);
                        if (message != null)
                        {
                            Map<String, Object> objectMap = new HashMap<>();
                            objectMap.put("updateSum", 0);
                            objectMap.put("lastMessage", message);
                            lists.put(s, objectMap);
                        }
                    }
                }
            }
            if (map != null)
            {
                Set<String> keySet = map.keySet();
                for (String s : keySet)
                {
                    if (lists.containsKey(s))
                    {
                        lists.put(s, map.get(s));
                    } else
                    {
                        lists.put(s, map.get(s));
                        if (General.sessionPositionMap == null)
                        {
                            General.sessionPositionMap = new ArrayList<>();
                            General.sessionPositionMap.add(General.toppedSum+1,s);
                        }
                    }
                }
                sort();
            }
            assert lists!=null;
            return lists;
        }
        else if (method.getName().equals("getMessageChain"))
        {
            //这里获取消息链中的消息，如果数据库中有对应的会话需要加载进来
            messageQueue=(MessageQueue) method.invoke(beProxy,args);
            String userID=(String)args[0];
            if (messageQueue==null)
            {//加载数据库中的数据
                List<Message> lists=DatabaseOperation.getDatabaseOperation().queryMessage(userID);
                messageQueue=new MessageQueue(userID);
                messageQueue.insertMessage(lists);
                messageQueue.setFirstLoad(false);
                General.messageQueueMap.put(userID,messageQueue);
                return messageQueue;
            }
            else
            {
                if (messageQueue.getFirstLoad())
                {//返回更新的消息队列
                    List<Message> lists=DatabaseOperation.getDatabaseOperation().queryMessage(userID);
                    messageQueue=new MessageQueue(userID);
                    messageQueue.insertMessage(lists);
                    messageQueue.setFirstLoad(false);
                    General.messageQueueMap.put(userID,messageQueue);
                    return messageQueue;
                }
                else
                {//数据库中没有数据，并且没有更新的消息，返回null
                    return messageQueue;
                }
            }
        }
        else
        {
            return method.invoke(beProxy,args);
        }
    }
    @Override
    public void sort()
    {
        if (General.sessionPositionMap != null)
        {
            String[] strings = General.ObjectToString(General.sessionPositionMap.toArray()) ;
            Arrays.sort(strings,General.toppedSum+1,strings.length, new Comparator<String>()
            {
                @Override
                public int compare(String s1, String s2)
                {
                    if (lists != null)
                    {
                        Message message1 = (Message) lists.get(s1).get("lastMessage");
                        Message message2 = (Message) lists.get(s2).get("lastMessage");
                        return message1.compareTo(message2);//如果出现顺序不符合，需要变更message1和message2的顺序
                    }
                    return 0;
                }
            });
            General.sessionPositionMap = new ArrayList<>(Arrays.asList(strings));
        }
    }
}
