package com.subscreen;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SelectFile extends FragmentActivity {
    ListView lv;
    ArrayList<String> fileNames = null;
    String dirPath =  System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
    ArrayAdapter adp;
    boolean isMounted = true;
    FilenameFilter textFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            if (lowercaseName.endsWith(".md")) {
                return false;
            } else {
                return true;
            }
        }
    };
    void displayMessage(String message, String title)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    protected void onCreate(Bundle savedInstanceState) {
        try {
            isMounted = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if (!isMounted) {
                displayMessage("Sorry, we couldn't find the external device.\nMake sure it is inserted and that your Android device is not in Mass Storage Mode.", "Storage not Found");
            }
            File subDirectory = new File(dirPath);
// have the object build the directory structure, if needed.
            if (subDirectory.mkdirs()) {
                displayMessage("We've made a Subtitles folder on your external device.\nPlease place any subtitles you wish to play there.","Folder Created");
                isMounted = false;
            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_select_file);
            lv = (ListView) findViewById(R.id.file_list);
            fileNames = loadFileNames(dirPath);
            adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames);
            lv.setAdapter(adp);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String fileName = fileNames.get(position);
                    Intent intent = new Intent(SelectFile.this, ShowText.class);
                    Bundle b = new Bundle();
                    b.putString("fileName", fileName); //Your id
                    intent.putExtras(b); //Put your id to your next Intent
                    startActivity(intent);
                    finish();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public ArrayList<String> loadFileNames(String path) {
        File dir = new File(path);
        ArrayList<String> out = new ArrayList<String>();
        if (!isMounted || !dir.isDirectory())
            return out;
        for (File f : dir.listFiles(textFilter))
            out.add(f.getName());
        return out;
    }
}
