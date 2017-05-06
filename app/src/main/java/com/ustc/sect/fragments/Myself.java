package com.ustc.sect.fragments;

import android.app.Dialog;
import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.sect.R;
import com.ustc.sect.activity.Login;
import com.ustc.sect.database.DatabaseOperation;
import com.ustc.sect.mode.FileDialog;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.SocketObj;
import com.ustc.sect.mode.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lenovo on 2017/1/20.
 */

public class Myself extends Fragment
{
    private String path=null;
    private enum Switch
    {
        SAVE_BEFORE,SAVE,SAVE_AFTER;//枚举类型用来控制各个控件的状态
    }
    private Switch editView;
    public Myself()
    {
        this.editView=Switch.SAVE_BEFORE;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState)
    {
        final View myselfView=inflater.inflate(R.layout.myself_fragment,null);
        init(myselfView);
        TextView clear=(TextView)myselfView.findViewById(R.id.clear_record);
        TextView exit=(TextView)myselfView.findViewById(R.id.myself_send_addFriend_Exit);
        final ImageView headImage=(ImageView)myselfView.findViewById(R.id.myself_headImage);
        TextView editButton=(TextView)myselfView.findViewById(R.id.myself_edit);
        clear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sharedPreferences=General.mainActivity.getSharedPreferences(".user", Context.MODE_PRIVATE);
                if (General.sessionPositionMap!=null)
                {
                    for (int i = 0; i < General.sessionPositionMap.size(); i++)
                    {
                        String s = General.sessionPositionMap.get(i);
                        General.updateSession.deleteSession(s);
                        DatabaseOperation.getDatabaseOperation().deleteSessionMap(s);
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    General.toppedSum = -1;
                    editor.putString("toTop", General.toppedSum + "");
                    editor.apply();
                    Set<String> keys = General.messageQueueMap.keySet();
                    for (String s : keys)
                    {
                        General.pull.deleteMessageQueue(s);
                        DatabaseOperation.getDatabaseOperation().deleteMessage(s);
                    }
                }
                Toast.makeText(getActivity(),R.string.clear_finished,Toast.LENGTH_SHORT).show();
            }
        });//清除缓存记录
        exit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                General.getThreadPool().shutdown();
                Intent intent=new Intent(getActivity(), Login.class);
                General.isLogout=true;
                try
                {
                    if (!SocketObj.getSocketObj().getSocket().isClosed())
                        SocketObj.getSocketObj().getSocket().close();
                }
                catch (IOException e)
                {
                    //socket关闭异常
                }
                General.cancel_sendMessage.cancel();
                startActivity(intent);
                getActivity().finish();
            }
        });//退出登入
        //当头像状态是可编辑时用户点击头像可以更换头像
        headImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Map<String,Integer> map=new HashMap<>();
                map.put(FileDialog.root,R.drawable.filedialog_root);
                map.put(FileDialog.empty, R.drawable.default_img);
                map.put(FileDialog.current,R.drawable.filedialog_folder);
                map.put(FileDialog.parent,R.drawable.filedialog_folder_up);
                map.put("jpg",R.drawable.jpg_form);
                map.put("png",R.drawable.pen_form);
                Dialog dialog=FileDialog.createDialog(getActivity(),"打开文件", new General.CallBack()
                {
                    @Override
                    public Bundle callBackBundle(Bundle bundle)
                    {
                        path=bundle.getString("path");
                        if(path!=null)
                        {
                            Bitmap bm = BitmapFactory.decodeFile(path);
                            headImage.setImageBitmap(bm);
                        }
                        return bundle;
                    }
                },".png.jpg",map);
                dialog.show();
            }
        });//设置编辑头像
        headImage.setClickable(false);
        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (editView.equals(Switch.SAVE_BEFORE)||editView.equals(Switch.SAVE_AFTER))
                {
                    editView=Switch.SAVE;
                    init(myselfView);
                }else if (editView.equals(Switch.SAVE))
                {
                    editView=Switch.SAVE_AFTER;
                    User user=init(myselfView);
                    Message message=new Message(General.userID,Integer.parseInt(getString(R.string.Server_ID)),General.ONLY_PERSON);
                    message.setInformation(user);
                    General.sendMessage.send(message);
                }
            }
        });//设置编辑按钮点击监听
        return myselfView;
    }
     User init(View view)
    {
        User user=new User(General.userID);
        TextView editText=(TextView) view.findViewById(R.id.myself_edit);
        EditText userName=(EditText)view.findViewById(R.id.myself_userName);
        user.setUserName(userName.getText().toString().trim());
        EditText describe=(EditText)view.findViewById(R.id.myself_describe);
        user.setDescribe(describe.getText().toString().trim());
        ImageView headImage=(ImageView) view.findViewById(R.id.myself_headImage);
        EditText userID=(EditText)view.findViewById(R.id.myself_userID);
        userID.setText(General.userID+"");
        userID.setFocusable(false);
        EditText department=(EditText)view.findViewById(R.id.myself_department);
        user.setDepartment(department.getText().toString().trim());
        EditText email=(EditText)view.findViewById(R.id.myself_email);
        user.setEmail(email.getText().toString().trim());
        switch (this.editView)
        {
            case SAVE_BEFORE:
                editText.setText("编辑");
                DatabaseOperation dbo=DatabaseOperation.getDatabaseOperation();
                user=dbo.queryUser(General.userID+"");
                if(user!=null)
                {
                    if(user.getUserName()!=null)userName.setText(user.getUserName());
                    else userName.setText(General.userID+"");
                    userName.setEnabled(false);
                    if(user.getDescribe()!=null)describe.setText(user.getDescribe());
                    describe.setEnabled(false);
                    department.setText(user.getDepartment());
                    department.setEnabled(false);
                    email.setText(user.getEmail());
                    email.setEnabled(false);
                }
                break;
            case SAVE:
                editText.setText(R.string.save);
                userName.setEnabled(true);
                userName.setHint(R.string.m_userName);
                describe.setEnabled(true);
                describe.setHint(R.string.m_describe);
                department.setEnabled(true);
                department.setHint(R.string.m_department);
                email.setEnabled(true);
                email.setHint(R.string.m_email);
                Toast.makeText(this.getActivity(),R.string.m_headImage,Toast.LENGTH_SHORT).show();
                headImage.setClickable(true);
                break;
            case SAVE_AFTER:
                editText.setText(R.string.edit);
                user=new User(General.userID);
                userName.setHint("");
                userName.setEnabled(false);
                describe.setHint("");
                describe.setEnabled(false);
                department.setHint("");
                department.setEnabled(false);
                email.setHint("");
                email.setEnabled(false);
                headImage.setClickable(false);
                break;
        }
        return user;
    }

}
