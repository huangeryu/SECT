package com.ustc.sect.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ustc.sect.R;
import com.ustc.sect.adaptors.SessionItemClickAdaptor;
import com.ustc.sect.adaptors.SessionItemLongClickListener;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.SessionViewHolder;

import java.util.Date;
import java.util.Map;

/**
 * Created by Lenovo on 2017/1/20.
 */

public class Session extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View sessionView=inflater.inflate(R.layout.session_fragment,null);
        ListView listView=(ListView) sessionView.findViewById(R.id.session_ListView);
        listView.setAdapter(new SessionAdaptor());
        return sessionView;
    }
    @Override
    public void onResume()
    {
        super.onResume();
        General.updateSession.updateSession();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==General.Activity_ChatRoom)
        {
            if (resultCode==General.Activity_Result_OK)
            {
                General.updateSession.updateSession();
            }
        }
    }
    //sessionListView的适配器
    public class SessionAdaptor extends BaseAdapter implements General.UpdateSession
    {
        private Map<String,Map> lists;
        public SessionAdaptor()
        {
            General.updateSession=this;
            lists=General.pull.getMessageFromMessageQueue();
        }
        @Override
        public void updateSession()
        {
               if (General.pull!=null)
               {
                   lists=General.pull.getMessageFromMessageQueue();
                   notifyDataSetChanged();
               }
        }
        @Override
        public void deleteSession(String userID)
        {
            if (General.sessionPositionMap!=null)
            {
                if (General.sessionPositionMap.contains(userID))
                {
                    General.sessionPositionMap.remove(userID);
                    if (General.pull!=null)
                    {
                        lists=General.pull.getMessageFromMessageQueue();
                        notifyDataSetChanged();
                    }
                }
            }
        }
        @Override
        public int getCount()
        {
            if(General.sessionPositionMap!=null)
            {
                return General.sessionPositionMap.size();
            }else
            {
                return 0;
            }
        }
        @Override
        public Object getItem(int position)
        {
            //返回的是最近一条消息
            return lists.get(General.sessionPositionMap.get(position)).get("lastMessage");
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //这里没有添加图片支持
            SessionViewHolder holder;
            if (convertView==null)
            {
                convertView=LayoutInflater.from(General.context).inflate(R.layout.session_item,null);
                holder=new SessionViewHolder();
                holder.setCount((TextView)convertView.findViewById(R.id.notice));
                holder.setLastMessage((TextView)convertView.findViewById(R.id.lastMessage));
                holder.setTime((TextView)convertView.findViewById(R.id.sessionTime));
                holder.setName((TextView)convertView.findViewById(R.id.name));
                convertView.setTag(holder);
            }
            else
            {
                holder=(SessionViewHolder)convertView.getTag();
            }
            String userID=General.sessionPositionMap.get(position);//这里使用的是用户的ID，需要从数据库中查找对应的用户名
            Map viewMap=lists.get(userID);
            Integer count=(Integer) viewMap.get("updateSum");
            if (count<=General.LOW_NOTICE_NUM)
            {
                holder.getCount().setVisibility(View.GONE);
            }
            else
            {
                holder.getCount().setVisibility(View.VISIBLE);
                holder.getCount().setText(count+"");
            }
            Message message=(Message)viewMap.get("lastMessage");
            holder.getLastMessage().setText(message.getContent());
            holder.getName().setText(userID);
            holder.getTime().setText(General.dateToString(new Date(message.getDate())));
            convertView.setOnLongClickListener(new SessionItemLongClickListener(Session.this));//这里设置了长按监听
            convertView.setOnClickListener(new SessionItemClickAdaptor(Session.this));//这里设置点击监听
            if (General.toppedSum<position)
            {
                @SuppressWarnings("Deprecated")
                int id=getResources().getColor(R.color.sessionColor);
                convertView.setBackgroundColor(id);
            }
            else
            {
                @SuppressWarnings("Deprecated")
                int id=getResources().getColor(R.color.topSessionColor);
                convertView.setBackgroundColor(id);
            }
            convertView.setId(position);
            return convertView;
        }
    }
}
