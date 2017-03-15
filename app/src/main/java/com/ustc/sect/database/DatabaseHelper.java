package com.ustc.sect.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    private String databaseName;
    protected DatabaseHelper(Context context, String name)
    {
        super(context,name,null,2);//这里的name是一串数字，即用户的ID
        this.databaseName=name;
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //创建朋友表和对话表
        db.execSQL("create table friends (userID varchar(50),userName varchar(50),describe varchar(500),primary key (userID));");
        db.execSQL("insert into friends(userID,userName,describe)values('3001','黄玉','他是世界上最帅的人'),('3002','黄二玉','世界上最具有智慧的人'),('3003','黄三亿','不仅仅只有三亿'),('huangyu','黄大玉','noting is impossible');");
        //创建sessionItem与位置的映射表
        db.execSQL("create table sessionPosition (userID varchar(50), position Integer,primary key (userID),foreign key(userID) references friends on delete cascade )");
        db.execSQL("insert into sessionPosition values('3001',1),('3003',0),('3002',2),('huangyu',3);");
        db.execSQL("create table '3001' (userID varchar(50),sessionDate long,message varchar(500),primary key(userID,sessionDate),"+
                        "foreign key(userID) references friends on delete cascade);");
        db.execSQL("insert into '3001' values('3001',1485437304326,'hello world!301'),('3001',1485487304326,'hello world!黄玉')");
        db.execSQL("create table '3002' (userID varchar(50),sessionDate long,message varchar(500),primary key(userID,sessionDate),"+
                "foreign key(userID) references friends on delete cascade);");
        db.execSQL("insert into '3002' values('3001',1485489305326,'hello world!3002'),('3002',1485487999326,'hello world!3001')");
        db.execSQL("create table '3003' (userID varchar(50),sessionDate long,message varchar(500),primary key(userID,sessionDate),"+
                "foreign key(userID) references friends on delete cascade);");
        db.execSQL("insert into '3003' values('3001',1485438304326,'hello world!3003'),('3003',1487497394326,'hello world!3001')");
        db.execSQL("create table 'huangyu' (userID varchar(50),sessionDate long,message varchar(500),primary key(userID,sessionDate),"+
                "foreign key(userID) references friends on delete cascade);");
        db.execSQL("insert into 'huangyu' values('huangyu',1485489305326,'hello world!黄玉'),('huangyu',1485487999326,'hello world!')");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //版本更新时候全部删除，用于开发
        db.execSQL("drop database "+databaseName+";");
        onCreate(db);
    }
}
