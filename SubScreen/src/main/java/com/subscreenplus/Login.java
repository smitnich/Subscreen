package com.subscreenplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {
    Button loginButton;
    Button backButton;
    SubDownloader down;
    EditText usernameBox;
    EditText passwordBox;
    String curPath;
    LoginTaskRunner task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.subscreenplus.R.layout.activity_login);
        loginButton = (Button) findViewById(com.subscreenplus.R.id.doLoginButton);
        usernameBox = (EditText) findViewById(com.subscreenplus.R.id.userText);
        passwordBox = (EditText) findViewById(com.subscreenplus.R.id.passwordText);
        backButton = (Button) findViewById(com.subscreenplus.R.id.doBackButton);
        Bundle b = getIntent().getExtras();
        curPath = b.getString("path");
        down = (SubDownloader) b.getSerializable("downloader");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task == null)
                    task = new LoginTaskRunner();
                else
                    return;
                task.execute();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = null;
                runOnUiThread(new Runnable() {
                    public void run() {
                        returnToSearchActivity();
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        returnToSearchActivity();
    }
    private boolean tryLogin() {
        String username = usernameBox.getText().toString();
        String password = passwordBox.getText().toString();
        if (down == null)
            down = new SubDownloader();
        down.setUser(username, password);
        return down.Connect(Search.downloadPath);
    }

    private class LoginTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            task = null;
            if (!Search.checkNetworkState((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)))
            {
                runOnUiThread(new Runnable() {
                    public void run() {
                        displayMessage("No network connection found ", "No connection");
                    }
                });
                return "";
            }
            if (!tryLogin()) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        displayMessage("Sorry, that username and password is not valid.", "Invalid login");
                    }
                });
                return "";
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    returnToSearchActivity();
                }
            });
            return "";
        }
    }
    void returnToSearchActivity() {
        Intent intent = new Intent(Login.this, Search.class);
        Bundle bundle = new Bundle();
        bundle.putString("path", curPath);
        Search.down = down;
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
    void displayMessage(String message, String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(this.getString(com.subscreenplus.R.string.ok_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}