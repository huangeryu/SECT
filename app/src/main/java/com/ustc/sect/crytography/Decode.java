package com.ustc.sect.crytography;

/**
 * Created by Lenovo on 2017/1/13.
 */

public class Decode
{
    private String ciphertext;
    public Decode(String ciphertext)
    {
        this.ciphertext=ciphertext;
    }
    public String decode()
    {
        return this.ciphertext;
    }
}
