package com.example.popvuk.chatapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popvuk.vchat.R;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import custom.LoadingDialog;

/**
 * Created by POPVUK on 9/4/2017.
 */

public class EditUsernameDialog extends DialogFragment {

    private LoadingDialog loadingDialog;
    private Dialog loading;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_editusername_layout, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        final EditText editText = (EditText) root.findViewById(R.id.editTextUsername);

        Button cancel = (Button) root.findViewById(R.id.buttonCancel);
        Button ok = (Button) root.findViewById(R.id.buttonOk);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editText.getText()))
                {
                    editText.setError("This field is empty!");
                    return;
                }

                loadingDialog = new LoadingDialog(getActivity());
                loading = loadingDialog.createLoadingSpinner();
                loading.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dismiss();
                    }
                });
                loading.show();

                final String newUsername = editText.getText().toString();
                final SharedPreferences my_prefs = getActivity().getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE);
                final String userId = my_prefs.getString(getString(R.string.my_prefs_id), null);

                Firebase ref = new Firebase(getString(R.string.firebase_users)+"/"+userId);
                Map<String,Object> username = new HashMap<>();
                username.put("username", newUsername);
                ref.updateChildren(username, new Firebase.CompletionListener(){
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null) {
                            Toast.makeText(getActivity(), "Data could not be saved. " + firebaseError.getMessage(),Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            SharedPreferences.Editor editor = my_prefs.edit();
                            editor.putString(getString(R.string.my_prefs_username),newUsername);
                            editor.commit();

                            TextView username = (TextView) getActivity().findViewById(R.id.textViewUsername);
                            username.setText(newUsername);

                            if (loading.isShowing())
                                loading.dismiss();

                            dismiss();
                        }
                    }
                });

            }
        });
        return root;
    }
}
