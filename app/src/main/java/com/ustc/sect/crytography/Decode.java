package com.ustc.sect.crytography;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Decode
{
    private byte[] cipherText;
    private String str;
    public Decode(byte[] cipherText)
    {
        this.cipherText=cipherText;
    }
    public Decode(String str)
    {
        this.str=str;
    }
    public byte[] decode()
    {
        return this.cipherText;
    }
    public String decode_str()
    {
        return this.str;
    }
}
