package com.ustc.sect.adaptors;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ustc.sect.activity.ChatRoom;
import com.ustc.sect.mode.General;

/**
 * Created by Lenovo on 2017/1/22.
 */
//这里是SessionItemAdaptor的适配器
public class SessionItemClickAdaptor implements View.OnClickListener
{
    private Fragment session;
    public SessionItemClickAdaptor(Fragment session)
    {
        this.session=session;
    }
    @Override
    public void onClick(View v)
    {
        int viewID=v.getId();
        String userID= General.sessionPositionMap.get(viewID);
        Intent intent=new Intent(General.mainActivity, ChatRoom.class);
        Bundle bundle=new Bundle();
        bundle.putString("userID",userID);
        intent.putExtra("bundle",bundle);
        session.startActivityForResult(intent,General.Activity_ChatRoom);
    }
}
