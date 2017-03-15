package com.ustc.sect.mode;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Lenovo on 2017/1/15.
 */
//MessageQueue主要作为缓冲使用，当MessageQueue没有消息是需要到数据库中加载
public class MessageQueue
{
    private String queueName;
    private Integer capacity;//该变量表示队列的大小
    private Queue<Message> queue;
    private boolean firstLoad;
    private Integer lastCapacity;
    public MessageQueue(String queueName)
    {
        this.queueName = queueName;
        queue = new PriorityQueue<>();
        this.capacity = 0;
        this.lastCapacity=0;
        this.firstLoad=true;
    }
    public void setFirstLoad(boolean firstLoad)
    {
        this.firstLoad=firstLoad;
    }
    public boolean getFirstLoad()
    {
        return firstLoad;
    }
    public String getQueueName()
    {
        return this.queueName;
    }
    public Integer getCapacity()
    {
        return this.capacity;
    }
    public Integer getAvailableCount()
    {
        return this.capacity-this.lastCapacity;
    }

    public void setLastCapacity(Integer lastCapacity)
    {
        this.lastCapacity = lastCapacity;
    }
    public void setLastCapacity()
    {
        this.lastCapacity=this.capacity;
    }
    public MessageQueue insertMessage(Message message)
    {
        queue.add(message);
        capacity++;
        return this;
    }
    public MessageQueue insertMessage(List<Message> lists)
    {
        for(Message e:lists)
        {
            queue.add(e);
            capacity++;
            lastCapacity++;
        }
        return this;
    }
    public Message removeMessage()
    {//这里的删除操作存在问题，这个操作需要重写
        try
        {
            Message message=queue.remove();
            capacity--;
            return message;
        }catch (NoSuchElementException e)
        {
            capacity=0;
            return null;
        }
    }
    public Message peekMessage()
    {//弹出但是不移除
        return queue.peek();
    }
    public void clearQueue()
    {
        queue.clear();
        lastCapacity=0;
        capacity=0;
    }
    public Message[] toArray()
    {
        Message [] a=null;
        if (!this.queue.isEmpty())
        {
            a=new Message[queue.size()];
            this.queue.toArray(a);
            Arrays.sort(a);
        }
        return a;
    }
}
