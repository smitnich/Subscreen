package com.subscreenplus;

import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.base64.Base64;

public class SubDownloader {
	final static String path = "http://api.opensubtitles.org:80/xml-rpc";
	XMLRPCClient cl;
	final static String testUser = "subscreen";
	String token;
	Boolean connected = false;
	String username;
	String password;

    public boolean Connect(URL url) {
		if (connected)
			return true;
		cl = new XMLRPCClient(url);
		try {
			token = Login(cl);
		} catch (XMLRPCException e) {
			System.out.println("Unable to connect to server: " + e.getMessage());
			return false;
		}
		if (token == null)
			return false;
		connected = true;
		return true;
	}
	private String Login(XMLRPCClient cl) throws XMLRPCException {
		@SuppressWarnings("unchecked")
		// This is what the status will be when an invalid username or password is entered
		final String badUsernameStatus = "401 Unauthorized";
		HashMap<String, Object> result;
		try {
			result = (HashMap<String, Object>) cl.call("LogIn", username, password, "english", testUser);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String status = (String) result.get("status");
		if (status.compareTo(badUsernameStatus) == 0) {
			return null;
		}
		return (String) result.get("token");
	}
	public void setUser(String _username, String _password) {
		username = _username;
		password = _password;
	}
	public void Disconnect() {
		new DisconnectTaskRunner().execute();
	}
	private void DisconnectInternal() {
		if (!connected)
			return;
		try {
			cl.call("LogOut", token);
		} catch (XMLRPCException e) {
			System.out.println("Unable to disconnect from server: " + e.getMessage());
		}
		connected = false;
	}
	public Result[] Search(String toSearch, String language) {
		Map<String, Map<String, String>> searchInfo = new HashMap<String, Map<String, String>>();
        Map<String, String> movieInfo = new HashMap<String, String>();
        Map<String, String> limit = new HashMap<String, String>();
        HashMap<String, Object[]> searchResult;
        limit.put("limit", "25");
        movieInfo.put("sublanguageid", language);
        movieInfo.put("query", toSearch);
        searchInfo.put("1", movieInfo);
        Object[] searchArgs = new Object[] { token, searchInfo, limit};
        try {
        	searchResult = (HashMap<String, Object[]>) cl.call("SearchSubtitles", searchArgs);
        }
        catch (XMLRPCException e) {
        	System.out.println("Unable to load search results: " + e.getMessage());
        	return null;
        } catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		Object[] tmpResult = (Object[]) searchResult.get("data");
		Result[] resultArray = new Result[tmpResult.length];
		for (int i = 0; i < tmpResult.length; i++) {
			resultArray[i] = new Result(tmpResult[i]);
		}
		return resultArray;
	}
	public Boolean Download(String id, FileOutputStream out) throws XMLRPCException {
			Map<String, String> ids = new HashMap<String, String>();
			ids.put("idsubtitlefile", id);
			Object downloadResult = cl.call("DownloadSubtitles", new Object[] {token, ids});
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (HashMap<String, Object>) downloadResult;
			if (dataMap.get("data") instanceof Boolean) {
				return false;
			}
			Object[] tmpData = (Object[]) dataMap.get("data");
			@SuppressWarnings("unchecked")
			HashMap<String, String> trueData = (HashMap<String, String>) tmpData[0];
			String result = trueData.get("data");
			try {
				decompressGZIP(result, out);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		public String decompressGZIP(String data, FileOutputStream outStream) throws IOException {
			byte[] output = new byte[1024];
			int num;
			GZIPInputStream input = new GZIPInputStream(new DataInputStream(new ByteArrayInputStream(Base64.decode(data))));
			while ((num = input.read(output)) > 0) {
				outStream.write(output, 0, num);
			}
			return "";
		}
	private class DisconnectTaskRunner extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			// Do your long operations here and return the result
			try {
				DisconnectInternal();
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
	public class Result {
		HashMap<String, String> data;
		public String name = "";
		public String fileName = "";
		public String id = "";
		Result(Object input) {
			data = (HashMap<String, String>) input;
			id = data.get("IDSubtitleFile");
			name = data.get("MovieName");
			fileName = data.get("SubFileName");
		}
	}
}
