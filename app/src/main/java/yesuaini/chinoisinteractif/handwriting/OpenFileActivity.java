/****************************************************************************
 * @project     EvaluateHandWritingActivity
 * @copyright   Copyright(C), 2015~, SCUT HCII-Lab(http://www.hcii-lab.net/gpen) All Rights Reserved.
 * @package     com.example.evaluatehandwritingaccuracy
 * @title       OpenFileActivity.java
 *
 * @model 		 open the folder of  hciiTestAccuracy 
 * @description TODO
 *
 * @date        2015-5-13
 * @author      Xuefeng Xiao
 * @mail   xiaoxuefengchina@gmail.com
 * @version     1.0.0
 *
 ****************************************************************************/
package yesuaini.chinoisinteractif.handwriting;;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import yesuaini.chinoisinteractif.R;

public class OpenFileActivity extends Activity
{
    public static final String FILENAME = "FILENAME";
    ListView listView;
    TextView textView;
    File currentParent;
    File[] currentFiles;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handwriting_open);

        listView = (ListView) findViewById(R.id.list);
        textView = (TextView) findViewById(R.id.path);

        final String pathname = Environment.getExternalStorageDirectory().getPath() + "/hciiTestAccuracy";
        try{
            File dir = new File(pathname);
            if(!dir.exists()){
                dir.mkdirs();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        File root = new File(pathname);
        if (root.exists())
        {
            currentParent = root;
            currentFiles = root.listFiles();
            inflateListView(currentFiles);
        }

        listView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                if (currentFiles[position].isFile())
                {
                    String  getFileAddress =   currentFiles[position].getName();
                    Intent intent = new Intent();
                    intent.putExtra(FILENAME, getFileAddress);
                    setResult(1, intent);
                    finish();
                }
            }
        });

    }
    private void inflateListView(File[] files)
    {
        List<Map<String, Object>> listItems =
                new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++)
        {
            Map<String, Object> listItem =
                    new HashMap<String, Object>();

              if (files[i].isDirectory())
            {
                listItem.put("icon", R.drawable.folder);
            }
            else
            {
                listItem.put("icon", R.drawable.file);
            }
            listItem.put("fileName", files[i].getName());
            listItems.add(listItem);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.handwriting_line, new String[]{ "icon", "fileName" }
                , new int[]{R.id.icon, R.id.file_name });
        listView.setAdapter(simpleAdapter);
        try
        {
            textView.setText(" "+ currentParent.getCanonicalPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}