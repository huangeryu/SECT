package com.ustc.sect.mode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ustc.sect.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lenovo on 2017/5/4.
 */

public class FileDialog
{
    public static final String root="/";
    public static final String parent="..";
    public static final String current=".";
    public static final String empty="";
    public static Dialog createDialog( Context context, String title, General.CallBack callBack,
                                      String suffix, Map<String,Integer> images)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        FileSelectView fsv=new FileSelectView(context,callBack,suffix,images);
        builder.setView(fsv);
        Dialog dialog=builder.create();
        fsv.setDialog(dialog);
        dialog.setTitle(title);
        return dialog;
    }
    static class  FileSelectView extends ListView implements AdapterView.OnItemClickListener
    {
        private General.CallBack callBack=null;
        private String path=root;
        private List<Map<String,Object>>list=null;
        private String suffix=null;
        private Map<String,Integer> imageMap=null;
        private Dialog iDialog=null;
        public FileSelectView(Context context, General.CallBack callBack,String suffix,
                              Map<String,Integer>images)
        {
            super(context);
            this.imageMap=images;
            this.suffix=(suffix==null?"":suffix.toLowerCase());
            this.callBack=callBack;
            this.setOnItemClickListener(this);
            refreshFileList();
        }
        public void setDialog(Dialog dialog)
        {
            this.iDialog=dialog;
        }
        private String getSuffix(String fileName)
        {
            int dix=fileName.lastIndexOf('.');
            if(dix<0)
            {
                return "";
            }
            else
            {
                return fileName.substring(dix+1);
            }
        }
        private int getImageId(String s)
        {
            if(imageMap==null) return 0;
            else if(imageMap.containsKey(s))
            {
                return imageMap.get(s);
            }
            else if(imageMap.containsKey(empty))
            {
                return imageMap.get(empty);
            }
            else
            {
                return 0;
            }
        }
        private int refreshFileList()
        {
            File[] files=null;
            Log.d("path","path="+path);
            try
            {
                files=new File(path).listFiles();
            }
            catch (Exception e)
            {
                files=null;
            }
            if(files==null)
            {
                Toast.makeText(getContext(), R.string.FileAccessError,Toast.LENGTH_LONG).show();
                return -1;
            }
            if(list!=null)
            {
                list.clear();
            }
            else
            {
                list=new ArrayList<>(files.length);
            }
            ArrayList<Map<String,Object>> folders=new ArrayList<>();
            ArrayList<Map<String,Object>> lFiles=new ArrayList<>();
            if(!this.path.equals(root))
            {
                Map<String,Object> map=new HashMap<>();
                map.put("name",root);
                map.put("path",root);
                map.put("img",getImageId(root));
                list.add(map);

                map=new HashMap<>();
                map.put("name",parent);
                map.put("path",path);
                map.put("img",getImageId(parent));
                list.add(map);
            }
            for(File file:files)
            {
                if(file.isDirectory()&&file.listFiles()!=null)
                {
                    Map<String,Object> map=new HashMap<>();
                    map.put("name",file.getName());
                    map.put("path",file.getPath());
                    map.put("img",getImageId(current));
                   folders.add(map);
                }
                else if(file.isFile())
                {
                    String sf=getSuffix(file.getName()).toLowerCase();
                    if(suffix==null || suffix.length()==0 || (sf.length()>0&&suffix.contains("."+sf)))
                    {
                        Map<String,Object> map=new HashMap<>();
                        map.put("name",file.getName());
                        map.put("path",file.getPath());
                        map.put("img",getImageId(sf));
                        lFiles.add(map);
                    }
                }
            }
            list.addAll(folders);
            list.addAll(lFiles);
            SimpleAdapter adapter=new SimpleAdapter(getContext(),list,R.layout.filedialogitem,new String[]{"img","name","path"},
                    new int[]{R.id.filedialogitem_img,R.id.filedialogitem_name,R.id.filedialogitem_path});
            this.setAdapter(adapter);
            return files.length;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            String pt=(String)list.get(position).get("path");
            String fn=(String)list.get(position).get("name");
            if(fn.equals(root)|| fn.equals(FileDialog.parent))
            {
                File fl=new File(pt);
                String ppt=fl.getParent();
                if(ppt!=null)
                {
                    path=ppt;
                }
                else
                {
                    path=root;
                }
            }
            else
            {
                File fl=new File(pt);
                if(fl.isFile())
                {
                    this.iDialog.dismiss();
                    Bundle bundle=new Bundle();
                    bundle.putString("path",pt);
                    bundle.putString("name",fn);
                    this.callBack.callBackBundle(bundle);
                    return;
                }
                else if(fl.isDirectory())
                {
                    path=pt;
                }
            }
            this.refreshFileList();
        }
    }
}
