package com.ustc.sect.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.ustc.sect.R;

/**
 * Created by Lenovo on 2016/11/13.
 */

public class Welcome extends Activity
{
    private float down=0;
    @Override
    protected  void onCreate(Bundle s)
    {
        super.onCreate(s);
        setContentView(R.layout.welcome);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        int event=e.getAction();
        if(event==MotionEvent.ACTION_DOWN)
        {
            down=e.getX();
        }
        if(event==MotionEvent.ACTION_UP) {
            float up = e.getX();
            if ((down-up)> Float.parseFloat(getString(R.string.slide_distance)))
            {
                startActivity(new Intent(this, Login.class));
                finish();
            }
        }
       return true;
    }
}
