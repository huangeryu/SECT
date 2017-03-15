package com.ustc.sect.mode;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Lenovo on 2017/1/21.
 */

public class SessionViewHolder
{
    private ImageView headImage;
    private TextView name;
    private TextView lastMessage;
    private TextView time;
    private TextView count;//这里是是未读消息的数量

    public void setTime(TextView time)
    {
        this.time = time;
    }

    public void setName(TextView name)
    {
        this.name = name;
    }

    public ImageView getHeadImage()
    {
        return headImage;
    }

    public TextView getCount()
    {
        return count;
    }

    public TextView getLastMessage()
    {
        return lastMessage;
    }

    public TextView getName()
    {
        return name;
    }

    public TextView getTime()
    {
        return time;
    }

    public void setCount(TextView count)
    {
        this.count = count;
    }

    public void setHeadImage(ImageView headImage)
    {
        this.headImage = headImage;
    }

    public void setLastMessage(TextView lastMessage)
    {
        this.lastMessage = lastMessage;
    }
}
