package com.subscreen;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SelectFile extends FragmentActivity {
    ListView lv;
    ArrayList<String> fileNames = null;
    static final String dirPath =  System.getenv("EXTERNAL_STORAGE") + "/" + "Subtitles/";
    static String curPath = dirPath;
    String backString;
    ArrayAdapter adp;
    boolean zipOpened = false;
    boolean isMounted = true;
    FilenameFilter textFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            if (lowercaseName.endsWith(".nfo")) {
                return false;
            } else {
                return true;
            }
        }
    };
    public void onBackPressed() {
        goBackDirectory();
        updateMenu();
    }
    private void updateMenu() {
        zipOpened = false;
        fileNames = loadFileNames(curPath);
        adp.clear();
        for (String fileName : fileNames)
            adp.add(fileName);
        adp.notifyDataSetChanged();
        lv.setSelection(0);
    }
    void displayExitMessage(String message, String title)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(R.string.exit_text), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    void displayBackMessage(String message, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(R.string.back_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateMenu();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    protected void onCreate(Bundle savedInstanceState) {
        backString =  this.getString(R.string.back_folder);
        try {
            isMounted = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if (!isMounted) {
                displayExitMessage(this.getString(R.string.storage_not_found),
                        this.getString(R.string.storage_not_found_title));
            }
            File subDirectory = new File(dirPath);
// have the object build the directory structure, if needed.
            if (isMounted && subDirectory.mkdirs()) {
                //isMounted = false;
                writeHelpFiles();
            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_select_file);
            lv = (ListView) findViewById(R.id.file_list);
            fileNames = loadFileNames(curPath);
            if (isMounted && (fileNames == null || fileNames.size() == 0))
                displayExitMessage(this.getString(R.string.no_files_found).replace(
                                this.getString(R.string.subtitle_replace_string),dirPath),
                        this.getString(R.string.no_files_found_title));
            adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames);
            lv.setAdapter(adp);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    itemClicked(position);
                }
            });
        }
        catch (Exception e)
        {
            return;
        }
    }
    private void writeHelpFiles() {
        InputStream helpFileIn = null;
        FileOutputStream helpFileOut = null;
        try {
            helpFileIn = getResources().getAssets().open("BasicUsageTutorial.srt");
            helpFileOut = new FileOutputStream(curPath + "/Basic Usage");
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = helpFileIn.read(buffer)) > 0)
                helpFileOut.write(buffer,0,length);
        } catch (IOException e) {
            return;
        } finally {
            try {
                if (helpFileIn != null)
                    helpFileIn.close();
                if (helpFileOut != null)
                    helpFileOut.close();
            } catch (IOException e) {
                return;
            }
        }
    }
    private void goBackDirectory() {
        //Don't allow for going back past the subtitles directory
        if (curPath.compareTo(dirPath) == 0) {
            return;
        }
        int i;
        //Skip the last character, which will always be a directory symbol
        //Then go back to the previous directory symbol; we want only the part
        //of the string before this
        for (i = curPath.length() -2; i >= 0; i--)
        {
            if (curPath.charAt(i) == '/')
                break;
        }
        curPath = curPath.substring(0,i+1);
    }
    private void itemClicked(int position) {
        String fileName = fileNames.get(position);
        //If a zip file is loaded, this is the one to be used within it
        String zipFileName = null;

        if (fileName.charAt(0) == '/' || fileName.equals(backString))
        {
            //Take all but the first character of the fileName and add a directory
            //slash at the end in order to have the directory symbol at the end
            //of the string
            if (!fileName.equals(backString))
                curPath = curPath + fileName.substring(1) + "/";
            else
                goBackDirectory();
            updateMenu();
            return;
        }
        if (fileName.endsWith(".zip") || zipOpened) {
            ArrayList<String> zipFileNames = FileHelper.readZipFile(curPath + fileName);
            if (zipFileNames == null) {
                displayBackMessage(getText(R.string.bad_format_message).toString(), "Sorry");
                return;
            }
            else if (zipOpened) {
                zipFileName = fileName;
                //Akward hack, we're already storing the full path in the curPath variable, so
                //we don't want anything appended to the filename
                fileName = "";
            }
            else if (zipFileNames.size() == 1) {
                zipFileName = zipFileNames.get(0);
            }
            else {
                adp.clear();
                zipFileNames.add(0,backString);
                for (String tmpName : zipFileNames)
                    adp.add(tmpName);
                adp.notifyDataSetChanged();
                lv.setSelection(0);
                fileNames = zipFileNames;
                zipOpened = true;
                curPath = curPath + fileName;
                return;
            }
        }
        Intent intent = new Intent(SelectFile.this, ShowText.class);
        Bundle b = new Bundle();
        b.putString("fileName", curPath + fileName); //Your id
        if (zipFileName != null)
            b.putString("zipFileName",zipFileName);
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
        finish();
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
        ArrayList<String> out = new ArrayList<String>();
        if (path.endsWith(".zip")) {
            zipOpened = true;
            out.addAll(FileHelper.readZipFile(path));
            out.add(0,backString);
            return out;
        }
        File dir = new File(path);
        if (!isMounted || !dir.isDirectory())
            return out;
        //If we're at the root directory, don't allow the user to go back
        if (!path.equals(dirPath))
            out.add(backString);
        for (File f : dir.listFiles(textFilter)) {
            if (f.isDirectory())
                out.add("/" + f.getName());
            else
                out.add(f.getName());
        }
        return out;
    }
}
