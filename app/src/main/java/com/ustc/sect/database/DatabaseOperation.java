package com.ustc.sect.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ustc.sect.mode.General;
import com.ustc.sect.mode.Message;
import com.ustc.sect.mode.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 2017/1/14.
 */

public class DatabaseOperation
{
    private static DatabaseHelper databaseHelper;
    private static DatabaseOperation databaseOperation;
    private  SQLiteDatabase db;
    private DatabaseOperation()
    {
        if (databaseHelper ==null)
            databaseHelper =new DatabaseHelper(General.context, General.userID+"");
        this.databaseOperation=this;
    }
    public static DatabaseOperation getDatabaseOperation()
    {
        if (databaseOperation==null)databaseOperation=new DatabaseOperation();
        return databaseOperation;
    }
    //这个函数是检查对应的表是否存在，如果不存在就创建一个表
    public boolean isTableExist(String tableName,Message message)
    {
        db= databaseHelper.getReadableDatabase();
        StringBuilder buffer=new StringBuilder();
        buffer.append("select count(*) from ").append("sqlite_master").append(" where type='table' and name='")
                .append(tableName).append("';");
        Cursor cursor=db.rawQuery(buffer.toString(),null);
        boolean bln=cursor.moveToNext();
        Log.d("cursor.moveToNext", "isTableExist: "+bln);
        if (message==null)return bln;
        if (bln)
        {
            insertMessage(message);
        }
        else
        {
            createTable(tableName).insertMessage(message);
        }
        return bln;
    }
    public DatabaseOperation createTable(String tableName)
    {//创建表
        StringBuilder buffer=new StringBuilder();
        buffer.append("create table '").append(tableName)
                .append("' (userID varchar(50),sessionDate long,message varchar(500),primary key(userID,sessionDate)," +
                "foreign key(userID) references friends on delete cascade);");
        db= databaseHelper.getWritableDatabase();
        db.execSQL(buffer.toString());
        db.close();
        return this;
    }
    private DatabaseOperation insertMessage(Message message)
    {//插入消息
//        insert(message.getFrom(),"userID","sessionDate","message",message.getFrom(),message.getDate().getTime(),message.getTextMessage());
        db=databaseHelper.getWritableDatabase();
        StringBuilder buffer=new StringBuilder();
        buffer.append("insert into '").append(message.getFrom()+"").append("'(userID,sessionDate,message)values('")
                .append(message.getFrom()).append("',").append(message.getDate()).append(",'")
                .append(message.getContent()).append("');");
        db.execSQL(buffer.toString());
        db.close();
        return this;
    }
    public DatabaseOperation deleteMessage(String userID)
    {
        db=databaseHelper.getWritableDatabase();
        db.execSQL(" drop table ? ;",new Object[]{userID});
        db.close();
        return this;
    }
    public DatabaseOperation update(User user)
    {
        StringBuffer buffer=new StringBuffer();
        buffer.append("update friends set describe=").append(user.getDescribe())
                .append("where userID=").append(user.getUserID()).append(";");
        db= databaseHelper.getWritableDatabase();
        db.execSQL(buffer.toString());
        db.close();
        return this;
    }
    public DatabaseOperation insertUser(User user)
    {//这里没有检查返回值
        insert("friends","describe","userID","userName","department","email",user.getDescribe(),user.getUserID(),user.getUserName(),user.getDepartment(),user.getEmail());
        return this;
    }
    //columns数组前面的是列属性后面的是值属性
    public boolean insert(String tableName,Object...columns)
    {
        if(columns==null||columns.length%2!=0||columns.length==0)return false;
        StringBuffer buffer=new StringBuffer("insert into '").append(tableName).append("'");
        int half=columns.length/2;
        for(int i=0;i<half;i++)
        {
            if(i==0)buffer.append("(");
            buffer=i==half-1?buffer.append(columns[i]).append(")"):buffer.append(columns[i]).append(",");
        }
        for (int i=half;i<columns.length;i++)
        {
            if(i==half)buffer.append("values(");
            buffer=i==columns.length-1?buffer.append(columns[i]).append(");"):buffer.append(columns[i]).append(",");
        }
        db= databaseHelper.getWritableDatabase();
        Log.d("insertMessage",buffer.toString());
        db.execSQL(buffer.toString());
        db.close();
        return true;
    }
    public List<Message> queryMessage(String userID)
    {
        db=databaseHelper.getReadableDatabase();
        StringBuffer buffer=new StringBuffer();
        buffer.append("select * from '").append(userID).append("';");
        Cursor cursor=db.rawQuery(buffer.toString(),null);
        ArrayList<Message> lists=null;
        while(cursor.moveToNext())
        {
            int from=Integer.parseInt(cursor.getString(cursor.getColumnIndex("userID")));
            int to;
            if (from==General.userID)
            {
                to=Integer.parseInt(userID);
            }
            else
            {
                to=General.userID;
            }
            long sessionDate=cursor.getLong(cursor.getColumnIndex("sessionDate"));
            String s=cursor.getString(cursor.getColumnIndex("message"));
            Message message=new Message(from,to,General.ONLY_TEXT);
            message.setContent(s);
            message.setDate(sessionDate);
            if (lists==null)
            {
                lists=new ArrayList<>();
            }
            lists.add(message);
        }
        return lists;
    }
    //这里使用按用户的ID查找，也可以按用户名查找
    public User queryUser(String userID)
    {
        db=databaseHelper.getReadableDatabase();
        StringBuilder buffer=new StringBuilder();
        buffer.append("select * from friends where userID=").append(userID);
        Cursor cursor=db.rawQuery(buffer.toString(),null);
        User user=null;
        if (cursor.moveToNext())
        {
            String userName=cursor.getString(cursor.getColumnIndex("userName"));
            String describe=cursor.getString(cursor.getColumnIndex("describe"));
            String department=cursor.getString(cursor.getColumnIndex("department"));
            String email=cursor.getString(cursor.getColumnIndex("email"));
            user= new User(Integer.parseInt(userID),userName,describe,department,email);
        }
        cursor.close();
        db.close();
        return user;
    }
    //获取session中的最近一条消息
    public Message getLastMessage(String userID)
    {
        if (userID==null)return null;
        db=databaseHelper.getReadableDatabase();
        StringBuilder buffer=new StringBuilder();
        buffer.append("select * from '").append(userID).append("' order by sessionDate desc limit 1;");
        Cursor cursor=db.rawQuery(buffer.toString(),null);
        Message message=null;
        if (cursor.moveToNext())
        {
            int from=Integer.parseInt(cursor.getString(cursor.getColumnIndex("userID")));
            int to;
            if (from==General.userID)
            {
                to=Integer.parseInt(userID);
            }
            else
            {
                to=General.userID;
            }
            long date=cursor.getLong(cursor.getColumnIndex("sessionDate"));
            String mg=cursor.getString(cursor.getColumnIndex("message"));
            message=new Message(from,to,date,mg);
            message.setType(General.ONLY_TEXT);
        }
        cursor.close();
        db.close();
        if (message!=null)
            Log.d("message", "getLastMessage: "+message.toString());
        return message;
    }
    public void loadSessionMap()
    {
        db=databaseHelper.getReadableDatabase();
        StringBuilder buffer=new StringBuilder();
        buffer.append("select * from sessionPosition ;");
        Cursor cursor=db.rawQuery(buffer.toString(),null);
        String[] s=null;
        if (cursor.getCount()!=0)
        {
            s=new String[cursor.getCount()];
        }
        while (cursor.moveToNext())
        {
            int position=cursor.getInt(1);
            String userID=cursor.getString(0);
            if (General.sessionPositionMap==null)
            {
                General.sessionPositionMap=new ArrayList<>();
            }
            assert s!=null;
            Log.d("position", "loadSessionMap: "+position);
           s[position]=userID;

        }
        if (s!=null)
        {Log.d("s[]", "loadSessionMap: "+Arrays.toString(s));
            General.sessionPositionMap= new ArrayList<>(Arrays.asList(s));
        }
    }
    //保存sessionItem的位置
    public void saveSessionMap()
    {
        if (General.sessionPositionMap!=null)
        {
            db=databaseHelper.getWritableDatabase();
            db.execSQL("delete from sessionPosition;");
            StringBuilder buffer=new StringBuilder();
            buffer.append("insert into sessionPosition(userID,position) values(?,?);");
            for (int i=0;i<General.sessionPositionMap.size();i++)
            {
                db.execSQL(buffer.toString(),new Object[]{General.sessionPositionMap.get(i),i});
            }
            db.close();
        }
    }
    public void deleteSessionMap(String userID)
    {
        db=databaseHelper.getWritableDatabase();
        db.execSQL("delete from sessionPosition where userID=? ;",new Object[]{userID});
        db.close();
    }
}
