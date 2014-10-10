package com.cesidiodibenedetto.filechooser;

import com.fivetouchsolutions.<$event_directory$>.R;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

import com.ipaulpro.afilechooser.utils.FileUtils;
import android.util.Base64;

/**
 * FileChooser is a PhoneGap plugin that acts as polyfill for Android KitKat and web
 * applications that need support for <input type="file">
 * 
 */
public class FileChooser extends CordovaPlugin {

    private CallbackContext callbackContext = null;
    private static final String TAG = "FileChooser";
    private static final int REQUEST_CODE = 6666; // onActivityResult request code

    private void showFileChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, this.cordova.getActivity().getString(R.string.chooser_title));
        try {
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_CODE) {
                // If the file selection was successful
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        JSONObject obj = new JSONObject();
                        try {
                            // from http://stackoverflow.com/questions/19882331/html-file-input-in-android-webview-android-4-4-kitkat
                            obj.put("filepath", FileUtils.getPath(this.cordova.getActivity(), uri));
                            obj.put("name", FileUtils.getFile(this.cordova.getActivity(), uri).getName());
                            obj.put("type", FileUtils.getMimeType(this.cordova.getActivity(), uri));

                            // attach the actual file content as base64 encoded string
                            byte[] content = FileUtils.readFile(this.cordova.getActivity(), uri);
                            String base64Content = Base64.encodeToString(content, Base64.DEFAULT);
                            obj.put("content", base64Content);

                            this.callbackContext.success(obj);
                        } catch (Exception e) {
                            Log.e("FileChooser", "File select error", e);
                            this.callbackContext.error(e.getMessage());
                        }
                    }
                }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
            if (action.equals("open")) {
                showFileChooser();
                return true;
            }
            else {
                return false;
            }
    }

}
