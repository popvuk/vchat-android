package com.example.popvuk.chatapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import custom.LoadingDialog;
import entity.User;
import com.example.popvuk.vchat.R;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by POPVUK on 8/25/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;
    private Dialog loading;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = this;

        final EditText name = (EditText) findViewById(R.id.name);
        final EditText phone = (EditText) findViewById(R.id.phone);
        final Button submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(name.getText()))
                {
                    name.setError("This field is empty!");
                    return ;
                }
                if (TextUtils.isEmpty(phone.getText()))
                {
                    phone.setError("This field is empty!");
                    return ;
                }
                loadingDialog = new LoadingDialog(context);
                loading = loadingDialog.createLoadingSpinner();
                loading.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
                loading.show();

                final String usernseme = name.getText().toString();
                final String phoneNumber = phone.getText().toString();
                final SharedPreferences my_prefs = getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE);
                String token = my_prefs.getString("my_token", null);
                Firebase ref = new Firebase(getResources().getString(R.string.firebase_users));
                User user = new User(usernseme, phoneNumber, token);
                Firebase id = ref.push();
                final String userId = id.getKey();
                id.setValue(user, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {


                        SharedPreferences.Editor editor = my_prefs.edit();
                        editor.putString(getString(R.string.my_prefs_id), userId);
                        editor.putString(getString(R.string.my_prefs_username), usernseme);
                        editor.putString(getString(R.string.my_prefs_phone), phoneNumber);
                        editor.commit();

                        if (loading.isShowing())
                            loading.dismiss();

                        finish();

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {

        setResult(Activity.RESULT_OK);
        finish();
    }
}
