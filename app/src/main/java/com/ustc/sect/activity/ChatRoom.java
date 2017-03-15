package com.ustc.sect.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ustc.sect.R;
import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.MyServiceConnect;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lenovo on 2017/1/24.
 */

public class ChatRoom extends Activity implements TextWatcher
{
    private String name;
    private Button sendButton;
    private EditText editText;
    private General.ChatRoom_update update;
    private ListView listView;
    public static Handler handler;
    public ChatRoom()
    {
        handler=new MyHandler();
    }
    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.chatroom);
        name=getIntent().getBundleExtra("bundle").getString("userID");
        General.Current_ChatRoom_Name=name;
        TextView chart_name=(TextView)findViewById(R.id.chart_room_name);
        chart_name.setText(name);
        listView=(ListView) findViewById(R.id.chart_room_listView);
        listView.setAdapter(new ChatRoom_ListView_Adaptor());
        sendButton=(Button) findViewById(R.id.sendButton);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(new ButtonAdaptor());
        editText=(EditText)findViewById(R.id.input_box);
        editText.addTextChangedListener(this);
        TextView returnButton=(TextView)findViewById(R.id.return_MainScreen);
        returnButton.setOnClickListener(new ReturnAdaptor() );
        update.updateView();
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        Log.d("s,start,count,after", "beforeTextChanged:s="+s+",start="+start+",count="+count+",after="+after);
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        Pattern pattern=Pattern.compile("^\\s+$");
        Matcher matcher=pattern.matcher(s);
       if (!s.toString().equals("")&&!matcher.find())
       {
           sendButton.setEnabled(true);
       }
        else
           sendButton.setEnabled(false);
        Log.d("s,start,before,count", "onTextChanged: s="+s+",start="+start+",before="+before+",count="+count);
    }
    @Override
    public void afterTextChanged(Editable s)
    {
        Log.d("s", "afterTextChanged: "+s);
    }
//listView的适配器类
    class ChatRoom_ListView_Adaptor extends BaseAdapter implements General.ChatRoom_update
    {
        private Message[] data;
        public ChatRoom_ListView_Adaptor()
        {
            data= General.pull.getMessageChain(name).toArray();
            update=this;
        }
        @Override
        public int getCount()
        {
            if (data==null)
                return 0;
            else
                return data.length;
        }
        @Override
        public void updateView()
        {
            data=General.pull.getMessageChain(name).toArray();
            notifyDataSetChanged();
            listView.setSelection(data.length-1);
        }
        @Override
        public Object getItem(int position)
        {
            return data[data.length-1-position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Message message=data[data.length-1-position];
            if (message.getFrom().equals(General.userID)||message.getFrom().equals("3001"))
            {
                View layoutView=LayoutInflater.from(ChatRoom.this).inflate(R.layout.chat_item,null);
                View nativeView=layoutView.findViewById(R.id.native_item);
                TextView name=(TextView) nativeView.findViewById(R.id.native_name);
                name.setText(message.getFrom());
                TextView time=(TextView)nativeView.findViewById(R.id.native_time);
                time.setText(General.dateToString(message.getDate()));
                TextView content=(TextView)nativeView.findViewById(R.id.native_content);
                content.setText(message.getTextMessage());
                return nativeView;
            }
            else
            {
                View layoutView=LayoutInflater.from(ChatRoom.this).inflate(R.layout.chat_item,null);
                View targetView=layoutView.findViewById(R.id.target_item);
                TextView name=(TextView) targetView.findViewById(R.id.target_name);
                name.setText(message.getFrom());
                TextView time=(TextView)targetView.findViewById(R.id.target_time);
                time.setText(General.dateToString(message.getDate()));
                TextView content=(TextView)targetView.findViewById(R.id.target_content);
                content.setText(message.getTextMessage());
                return targetView;
            }
        }
    }
    class ButtonAdaptor implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //把消息放进消息队列中，并发送消息
            Message message=new Message(General.userID,name,new Date(),editText.getText().toString().trim(),null);
            General.pull.getMessageChain(name).insertMessage(message);
            update.updateView();
            General.sendMessage.send(message);
            editText.setText("");
        }
    }
    class ReturnAdaptor implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Intent intent=new Intent(ChatRoom.this,MainScreen.class);
            setResult(General.Activity_Result_OK,intent);
            finish();
        }
    }
    class MyHandler extends Handler
    {
        @Override
        public void handleMessage(android.os.Message message)
        {
            if (message.what==General.RequestUpdate)
            {
                update.updateView();
            }
        }
    }
}
