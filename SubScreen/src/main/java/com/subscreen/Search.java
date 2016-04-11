package com.subscreen;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Search extends Activity {
    String path = "";
    SubDownloader down = null;
    Button searchButton;
    static URL downloadPath;
    EditText searchString;
    ListView resultList;
    ArrayAdapter adp;
    SubDownloader.Result[] results;
    String[] fileNames;
    AsyncTask<String, String, String> searchTask;
    AsyncTask<String, String, String> downloadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Bundle b = getIntent().getExtras();
        path = b.getString("path");
        searchString = (EditText) findViewById(R.id.searchString);
        searchButton = (Button) findViewById(R.id.startButton);
        resultList = (ListView) findViewById(R.id.resultsList);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                runSearchThread();
            }
        });
    }
    public void runSearchThread() {
        checkNetworkState();
        searchTask = new SearchTaskRunner();
        searchTask.execute();
    }

    private void doSearch() {
        if (down == null) {
            down = new SubDownloader();
            try {
                downloadPath = new URL("http://api.opensubtitles.org:80/xml-rpc");
            } catch (MalformedURLException e) {
                return;
            }
            if (down.Connect(downloadPath) == null) {
                // error here
                return;
            }
        }
        results = down.Search(searchString.getText().toString(), "eng");
        fileNames = new String[results.length];
        for (int i = 0; i < fileNames.length; i++) {
            fileNames[i] = results[i].fileName;
        }
        runOnUiThread(new Runnable()
        {
            public void run() {
                updateAdapter();
            }
        });
    }
    private void updateAdapter() {
        adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames);
        resultList.setAdapter(adp);
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(position);
            }
        });
        adp.notifyDataSetChanged();
    }
    private void itemClicked(int position) {
        SubDownloader.Result result = results[position];
        downloadTask = new DownloadTaskRunner();
        ((DownloadTaskRunner) downloadTask).setToDownload(result);
        try {
            downloadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class SearchTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            publishProgress("testing"); // Calls onProgressUpdate()
            // Do your long operations here and return the result
            try {
                doSearch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
        }

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }
    }
    private class DownloadTaskRunner extends AsyncTask<String, String, String> {
        SubDownloader.Result toDownload = null;
        public void setToDownload(SubDownloader.Result result) {
            toDownload = result;
        }
        @Override
        protected String doInBackground(String... params) {
            android.os.Debug.waitForDebugger();
            publishProgress("testing"); // Calls onProgressUpdate()
            // Do your long operations here and return the result
            try {
                down.Download(toDownload.id, new FileOutputStream(path + toDownload.fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
        }

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }
    }
    public void checkNetworkState() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }
}
