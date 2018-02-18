package com.example.popvuk.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.popvuk.vchat.R;
import com.firebase.client.Firebase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by MiljanaMilena on 1/31/2018.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        //zapamti token u sharedprefs
        SharedPreferences my_prefs = getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE);
        String userId = my_prefs.getString(getString(R.string.my_prefs_id), null);
        if(userId != null)
        {//upisi token u firebase
            Firebase ref = new Firebase(getString(R.string.firebase_users)+"/"+userId);
            Map<String, Object> token = new HashMap<String, Object>();
            token.put("token", refreshedToken);
            ref.updateChildren(token);
        }
        SharedPreferences.Editor editor = my_prefs.edit();
        editor.putString(getString(R.string.my_prefs_token),refreshedToken);
        editor.commit();

    }
}
