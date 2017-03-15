package com.ustc.sect.crytography;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Encode
{
    private String plaintext;
    public Encode(String plaintext)
    {
        this.plaintext=plaintext;
    }
    public String encode()
    {
        return  this.plaintext;
    }
}
