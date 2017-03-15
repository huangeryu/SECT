package com.ustc.sect.adaptors;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ustc.sect.R;
import com.ustc.sect.activity.ChatRoom;
import com.ustc.sect.mode.General;

/**
 * Created by Lenovo on 2017/1/22.
 */

public class SessionItemLongClickListener implements View.OnLongClickListener
{
    private Fragment session;
    public SessionItemLongClickListener(Fragment session)
    {
        this.session=session;
    }
    @Override
    public boolean onLongClick(View v)//这里的v指的是sessionItem
    {
        int viewId=v.getId();
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(General.mainActivity);
        View view= LayoutInflater.from(General.context).inflate(R.layout.session_dialog,null);
        TextView top=(TextView)view.findViewById(R.id.toTop);
        if (viewId>General.toppedSum)
        {
            top.setText(General.context.getString(R.string.ToTop));
        }
        else
        {
            top.setText(General.context.getString(R.string.cancel_toTop));
        }
        alertDialogBuilder.setView(view);
        AlertDialog alertDialog=alertDialogBuilder.create();
//        view.setOnClickListener(new AlertDialogClickListener(viewId,alertDialog));
        TextView entry=(TextView) view.findViewById(R.id.enter_chat);
        entry.setOnClickListener(new AlertDialogClickListener(viewId,alertDialog));
        top.setOnClickListener(new AlertDialogClickListener(viewId,alertDialog));
        TextView delete=(TextView)view.findViewById(R.id.delete_session);
        delete.setOnClickListener(new AlertDialogClickListener(viewId,alertDialog));
        alertDialog.show();
        return true;
    }
    //监听点击事件
    private class AlertDialogClickListener implements View.OnClickListener
    {
        private int viewPosition;
        private AlertDialog alertDialog;
         AlertDialogClickListener(int viewPosition,AlertDialog alertDialog)
        {
            this.viewPosition=viewPosition;
            this.alertDialog=alertDialog;
        }
        @Override
        public void onClick(View v)
        {
            int viewID=v.getId();
            switch (viewID)
            {
                case R.id.toTop://这里是置顶操作
                    if (viewPosition>General.toppedSum)
                    {//未置顶状态，置顶操作
                        General.toppedSum++;
                        String s=General.remove(General.sessionPositionMap,viewPosition);
                        assert s!=null;
                        General.sessionPositionMap.add(0,s);
                    }
                    else
                    {//已经置顶状态，取消置顶操作
                        General.toppedSum--;
                        String s=General.remove(General.sessionPositionMap,viewPosition);
                        General.sessionPositionMap.add(General.toppedSum+1,s);
                        General.sessionItemSort.sort();
                    }
                    General.updateSession.updateSession();
                    alertDialog.dismiss();
                    break;
                case R.id.delete_session://删除sessionItem操作
                    if (viewPosition<=General.toppedSum)
                    {//删除置顶sessionItem
                        General.toppedSum--;
                        General.remove(General.sessionPositionMap,viewPosition);
                        Log.d("toppedSum", "onClick: "+General.toppedSum);
                    }
                    else
                    {//删除非置顶sessionItem
                        General.remove(General.sessionPositionMap,viewPosition);
                    }
                    General.updateSession.updateSession();
                    alertDialog.dismiss();
                    break;
                case R.id.enter_chat://进入另一个Activity
                    //在进入Activity前需要保持fragment的状态
                    //保存的操作在session中完成
                    alertDialog.dismiss();
                    Intent intent=new Intent(General.mainActivity, ChatRoom.class);
                    Bundle bundle=new Bundle();
                    String userID=General.sessionPositionMap.get(viewPosition);
                    bundle.putString("userID",userID);
                    intent.putExtra("bundle",bundle);
                    session.startActivityForResult(intent,General.Activity_ChatRoom);//这里的是验证码，用来区分Activity
                    break;
            }
        }
    }
}
