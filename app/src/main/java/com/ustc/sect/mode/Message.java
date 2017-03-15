package com.ustc.sect.mode;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Message implements Comparable<Message>
{
    private String from;
    private String to;
    private Date date;
    private String textMessage;
    private Object obj;
    public Message(String message)
    {
        try
        {
            Log.d("message is null", "Message: "+message);
            JSONObject jsonObject = new JSONObject(message);
            this.from = jsonObject.getString("from");
            this.to = jsonObject.getString("to");
            this.obj = jsonObject.get("obj");
            this.date=General.longToDate(jsonObject.getLong("date"));//这里名没有检查返回值
            this.textMessage = jsonObject.getString("textMessage");
        }catch (JSONException e)
        {
            System.out.println("解析json出错！");
            e.printStackTrace();
        }
    }
    public Message(String from,String to,Date date,String textMessage,Object obj)
    {
        this.from=from;
        this.to=to;
        this.date=date;
        this.textMessage=textMessage;
        if (obj==null)
            this.obj="default";
    }
    public String getJsonStringFromMessage()
    {
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put("from", this.from).put("to", this.to).put("date", this.date.getTime()+"").put("textMessage", textMessage)
                    .put("obj", obj);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    //setter方法
    public Message setDate(Date date)
    {
        this.date=date;
        return this;
    }
    public Message setFrom(String from)
    {
        this.from=from;
        return this;
    }
    public Message setTo(String to)
    {
        this.to=to;
        return this;
    }
    public Message setTextMessage(String textMessage)
    {
        this.textMessage=textMessage;
        return this;
    }
    public Message setObj(Object obj)
    {
        this.obj=obj;
        return this;
    }
    //getter
    public String getFrom()
    {
        return this.from;
    }
    public Date getDate()
    {
        return date;
    }
    public String getTo()
    {
        return to;
    }
    public String getTextMessage()
    {
        return textMessage;
    }
    public Object getObj()
    {
        return obj;
    }
    @Override
    //该函数用来比较消息的时间的前后
    public int compareTo(Message message)
    {
        if (this.date.after(message.date))
            return -1;
        else if (this.date.before(message.date))
            return 1;
        else
            return 0;
    }
    @Override
    public String toString()
    {
        return "from:"+this.from+" to:"+this.to+" date:"+General.dateToString(this.date)+" message："+this.textMessage;
    }
}
