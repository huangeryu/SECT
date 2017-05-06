package com.ustc.sect.crytography;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Encode
{
    private byte[] plaintext;
    private String str;
    public Encode(byte[] plaintext)
    {
        this.plaintext=plaintext;
    }
    public Encode(String str)
    {
        this.str=str;
    }
    public byte[] encode()
    {
        return  this.plaintext;
    }
    public String encode_str()
    {
        return this.str;
    }
}
