package com.ustc.sect.mode;


import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Message implements Comparable<Message>
{
    private int  from;
    private int to;
    private long date;
    private int type;
    private int length;
    private String content;
    private String pictureDescribe;
    private byte[] picture;
    private User information;
    public Message(int from,int to,long date,String content)
    {
        this.from=from;
        this.to=to;
        this.date=date;
        this.content=content;
    }
    public Message(byte[] head,byte[] content)
    {
        from=General.bytesToInt(head,0);
        to=General.bytesToInt(head,4);
        date=General.bytesToLong(head,8);
        type=General.bytesToInt(head,16);
        length=General.bytesToInt(head,20);
        switch(type)
        {
            case General.ONLY_PICTURE:
                int pictureDescribeLength=General.bytesToInt(content,4);
                int pictureLength=General.bytesToInt(content,8+pictureDescribeLength);
                try
                {
                    pictureDescribe=new String(content,8,pictureDescribeLength,General.ENCODE_MODE);
                    picture=new byte[pictureLength];
                    System.arraycopy(content,12+pictureDescribeLength,picture,0,pictureLength);
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("create String ERROR", "Message: "+General.ENCODE_MODE+" unsupported!");
                }
                break;
            case General.ONLY_PERSON:
                this.information=new User();
                this.information.setUserID(General.bytesToInt(content,4));
                try
                {
                    this.information.setUserName(new String(content,8,32,General.ENCODE_MODE))
                            .setDescribe(new String(content,40,128,General.ENCODE_MODE))
                            .setDepartment(new String(content,168,64,General.ENCODE_MODE))
                            .setEmail(new String(content,232,64,General.ENCODE_MODE));
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("create String ERROR", "Message: "+General.ENCODE_MODE+" unsupported!");
                }
                break;
            case General.ONLY_TEXT:
                int TextAreaLength=General.bytesToInt(content,0);
                try
                {
                    this.content=new String(content,4,TextAreaLength-4,General.ENCODE_MODE);
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("create String ERROR", "Message: "+General.ENCODE_MODE+" unsupported!");
                }
                break;
            default:
                break;
        }
    }
    public Message(int from,int to,int type)
    {
        this.from=from;
        this.to=to;
        this.type=type;
    }
    //setter方法
    public Message setDate(long date)
    {
        this.date=date;
        return this;
    }
    public Message setFrom(int from)
    {
        this.from=from;
        return this;
    }
    public Message setTo(int to)
    {
        this.to=to;
        return this;
    }
    public Message setContent(String str)
    {
        this.content=str;
        return this;
    }
    public Message setInformation(User u)
    {
        this.information=u;
        return this;
    }
    public Message setPicture(byte[] p,String pictureDescribe)
    {
        this.picture=p;
        this.pictureDescribe=pictureDescribe;
        return this;
    }

    public Message setType(int type)
    {
        this.type = type;
        return this;
    }

    //getter
    public int getFrom()
    {
        return this.from;
    }
    public long getDate()
    {
        return date;
    }
    public int getTo()
    {
        return to;
    }
    public int getType()
    {
        return this.type;
    }

    public String getContent()
    {
        return content;
    }

    public User getInformation()
    {
        return information;
    }

    public String getPictureDescribe()
    {
        return pictureDescribe;
    }

    public byte[] getPicture()
    {
        return picture;
    }

    public int getLength()
    {
        return length;
    }
    public byte[] messageTobyte()
    {
        byte[] head=new byte[24];
        General.intToBytes(head,0,this.from);
        General.intToBytes(head,4,this.to);
        General.intToBytes(head,16,this.type);
        General.longToBytes(head,8,new Date().getTime());
        byte[] a;
        switch (this.type)
        {
            case General.ONLY_PERSON:
                this.length=296;
                General.intToBytes(head,20,length);
                a=new byte[296+24];
                Arrays.fill(a,(byte)0);
                System.arraycopy(head,0,a,0,24);
                General.intToBytes(a,24,this.length);
                General.intToBytes(a,28,this.information.getUserID());
                try
                {
                    if(this.information.getUserName()!=null)
                    {
                        byte[] userName = this.information.getUserName().getBytes(General.ENCODE_MODE);
                        System.arraycopy(userName, 0, a, 32, userName.length);
                    }
                    if(this.information.getDescribe()!=null)
                    {
                        byte[] selfDes = this.information.getDescribe().getBytes(General.ENCODE_MODE);
                        System.arraycopy(selfDes, 0, a, 64, selfDes.length);
                    }
                    if(this.information.getDepartment()!=null)
                    {
                        byte[] department = this.information.getDepartment().getBytes(General.ENCODE_MODE);
                        System.arraycopy(department, 0, a, 192, department.length);
                    }
                    if(this.information.getEmail()!=null)
                    {
                        byte[] email = this.information.getEmail().getBytes(General.ENCODE_MODE);
                        System.arraycopy(email, 0, a, 256, email.length);
                    }
                    return  a;
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("String_to_byte error", "messageTobyte: "+General.ENCODE_MODE+" unsupported!");
                }
                return null;
            case General.ONLY_PICTURE:
                try
                {
                    byte[] pdl=this.pictureDescribe.getBytes(General.ENCODE_MODE);
                    int pictureLength=this.picture.length;
                    this.length=12+pdl.length+pictureLength;
                    General.intToBytes(head,20,this.length);
                    a=new byte[24+this.length];
                    Arrays.fill(a,(byte)0);
                    System.arraycopy(head,0,a,0,24);
                    General.intToBytes(a,24,this.length);
                    General.intToBytes(a,28,pdl.length);
                    System.arraycopy(pdl,0,a,32,pdl.length);
                    General.intToBytes(a,32+pdl.length,pictureLength);
                    System.arraycopy(this.picture,0,a,36+pdl.length,pictureLength);
                    return a;
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("String_to_byte error", "messageTobyte: "+General.ENCODE_MODE+" unsupported!");
                }
                return null;
            case General.ONLY_TEXT:
                try
                {
                    byte[] cb=this.content.getBytes(General.ENCODE_MODE);
                    this.length=4+cb.length;
                    a=new byte[this.length+24];
                    Arrays.fill(a,(byte)0);
                    General.intToBytes(head,20,this.length);
                    System.arraycopy(head,0,a,0,24);
                    General.intToBytes(a,24,this.length);
                    System.arraycopy(cb,0,a,28,cb.length);
                    return  a;
                }catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    Log.d("String_to_byte error", "messageTobyte: "+General.ENCODE_MODE+" unsupported!");
                }
                return null;
            case General.HEARTBEAT:
                this.length=0;
                General.intToBytes(head,20,this.length);
                return head;
            default:
                return null;
        }
    }
    @Override
    //该函数用来比较消息的时间的前后
    public int compareTo(Message message)
    {
        if (this.date>message.date)
            return -1;
        else if (this.date<message.date)
            return 1;
        else
            return 0;
    }
    @Override
    public String toString()
    {
        return "from:"+this.from+" to:"+this.to+" date:"+General.dateToString(new Date(this.date))+" message："+this.content;
    }
}
