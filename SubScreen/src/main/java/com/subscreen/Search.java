package com.subscreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Search extends Activity {
    String path = "";
    static SubDownloader down = null;
    Button searchButton;
    Button loginButton;
    static URL downloadPath;
    EditText searchString;
    ListView resultList;
    ArrayAdapter adp;
    Spinner languageSelect;
    SubDownloader.Result[] results;
    String[] fileNames;
    AsyncTask<String, String, String> searchTask;
    AsyncTask<String, String, String> downloadTask;
    String username;
    String password;
    Activity currentActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Bundle b = getIntent().getExtras();
        path = b.getString("path");
        username = b.getString("username");
        password = b.getString("password");
        SubDownloader tmpDownloader = (SubDownloader) b.getSerializable("downloader");
        if (tmpDownloader != null)
            down = tmpDownloader;
        searchString = (EditText) findViewById(R.id.searchString);
        loginButton = (Button) findViewById(R.id.doLoginButton);
        searchButton = (Button) findViewById(R.id.startButton);
        resultList = (ListView) findViewById(R.id.resultsList);
        languageSelect = (Spinner) findViewById(R.id.languages);
        currentActivity = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try {
            downloadPath = new URL("http://api.opensubtitles.org:80/xml-rpc");
        } catch (MalformedURLException e) {
            return;
        }
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Disable the search button while running the search so users know not
                // to click on it
                searchButton.setEnabled(false);
                hideSoftKeyboard(currentActivity);
                runSearchThread();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gotoLoginScreen();
            }
        });
        initLanguages();
    }
    @Override
    // Override the Back button behavior to make sure that we clean up any remaining threads
    public void onBackPressed() {
        returnToSelectScreen();
    }
    private void gotoLoginScreen() {
        Intent intent = new Intent(Search.this, Login.class);
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
    private void initLanguages() {
        Languages.Language[] allLanguages = Languages.allLanguages;
        ArrayAdapter langAdp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, allLanguages);
        languageSelect.setAdapter(langAdp);
        languageSelect.setSelection(1);
        langAdp.notifyDataSetChanged();
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
    public void runSearchThread() {
        // Don't do anything if a search is already running
        if (searchTask != null)
            return;
        searchTask = new SearchTaskRunner();
        searchTask.execute();
    }
    private void askToPlay(final String path, final String fileName) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Launch the subtitle playing activity
                        playSubtitles(path + fileName);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(fileName + " was downloaded successfully. Play now?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .setTitle("Play now?").setIcon(android.R.drawable.ic_dialog_alert).show();

    }
    private void playSubtitles(String path)
    {
        Intent intent = new Intent(Search.this, ShowText.class);
        Bundle b = new Bundle();
        if (down != null)
            down.Disconnect();
        b.putString("fileName", path);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }
    private void doSearch() {
        if (!checkNetworkState())
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    displayMessage("No network connection found ", "No connection");
                }
            });
            return;
        }
        final String toSearch = searchString.getText().toString();
        if (down == null) {
            down = new SubDownloader();
            down.setUser(username, password);
            if (down.Connect(downloadPath) == false) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        displayMessage("Unable to connect", "No connection");
                    }
                });
                return;
            }
        }
        Languages.Language currentLanguage = (Languages.Language) languageSelect.getSelectedItem();
        results = down.Search(toSearch, currentLanguage.shortName);
        if (results.length == 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    displayMessage("No results found for \"" + toSearch + "\"", "No results found");
                }
            });
            return;
        }
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
        // Don't start another download if one is already running
        if (downloadTask != null)
            return;
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
            // Do your long operations here and return the result
            try {
                doSearch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    searchButton.setEnabled(true);
                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            searchTask = null;
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
            downloadTask = null;
            runOnUiThread(new Runnable() {
                public void run() {
                    askToPlay(path, toDownload.fileName);
                }
            });
            // execution of result of Long time consuming operation
        }

        @Override
        protected void onPreExecute() {
        }
    }
    public boolean checkNetworkState() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = cm.getActiveNetworkInfo();
        return (n != null && n.isConnectedOrConnecting());
    }
    void displayMessage(String message, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    public void returnToSelectScreen(){
        if (searchTask != null)
            searchTask.cancel(true);
        if (downloadTask != null)
            downloadTask.cancel(true);
        Intent intent = new Intent(Search.this, SelectFile.class);
        startActivity(intent);
        finish();
    }
}
