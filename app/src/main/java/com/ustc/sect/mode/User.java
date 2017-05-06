package com.ustc.sect.mode;

/**
 * Created by Lenovo on 2017/1/15.
 */

public class User
{
    private Integer userID;
    private String userName;
    private String describe;
    private String department;
    private String email;
    public User(){}
    public User(Integer userID,String userName,String describe,String department,String email)
    {
        this.describe=describe;
        this.userID=userID;
        this.userName=userName;
        this.department=department;
        this.email=email;
    }
    public User(Integer userID)
    {
        this.userName=null;
        this.userID=userID;
        this.describe=null;
    }
    public User(Integer userID,String userName)
    {
        this.describe=null;
        this.userID=userID;
        this.userName=userName;
    }
    //setter
    public User setUserName(String userName)
    {
        this.userName = userName;
        return this;
    }
    public User setUserID(Integer userID)
    {
        this.userID = userID;
        return this;
    }
    public User setDescribe(String describe)
    {
        this.describe = describe;
        return this;
    }
    public User setDepartment(String department)
    {
        this.department=department;
        return this;
    }
    public User setEmail(String email)
    {
        this.email=email;
        return this;
    }
    //getter
    public String getUserName()
    {
        return userName;
    }

    public String getDescribe()
    {
        return describe;
    }

    public Integer getUserID()
    {
        return userID;
    }
    public String getDepartment()
    {
        return this.department;
    }
    public String getEmail()
    {
        return this.email;
    }
}
