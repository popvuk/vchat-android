package com.example.popvuk.chatapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.popvuk.vchat.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import custom.FileHandler;
import custom.ImageTask;
import custom.LoadingDialog;
import database.ContactsTableQueries;
import entity.Contact;
import entity.Message;
import entity.User;
import roundedimageview.CircleImageView;

/**
 * Created by MiljanaMilena on 12/20/2017.
 */

public class ChatActivity extends AppCompatActivity {

    private String chatId;
    private String username;
    private Firebase ref , upRef, notification;
    private ChildEventListener listener;
    private SharedPreferences my_prefs;
    private String myId;
    private String contactId;
    private String contactPhone;
    private Boolean myMssg = true;// flag za poruku sa slikom
    private Boolean contactMssg = true;//flag za poruku sa slikom
    private View view;
    private LinearLayout ll;
    private String path;
    private TextView text;
    private CircleImageView civ;
    private int counter;
    private String myPhoto = null;
    private String contactPhoto = null;
    private Map<String, Object> readed = new HashMap<>();
    private Message mssg;
    private ScrollView scrollView;
    private LoadingDialog loadingDialog;
    private Dialog loading;
    private int mul = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        loadingDialog = new LoadingDialog(this);
        loading = loadingDialog.createLoadingSpinner();
        loading.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        loading.show();

        counter = 1;
        my_prefs = getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE);
        myId = my_prefs.getString(getString(R.string.my_prefs_id),null);
        myPhoto = my_prefs.getString(getString(R.string.my_prefs_photo),null);

        Bundle extras = getIntent().getExtras();
        chatId = extras.getString("chat");
        username = extras.getString("username");
        contactId = extras.getString("contactId");
        contactPhone = extras.getString("phone");

        if(extras.getString("contactPhoto") != null)
        {
            contactPhoto = extras.getString("contactPhoto");
        }

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ChatApp";

        if(chatId != null)
        {
            ContactsTableQueries queries = new ContactsTableQueries(this);
            queries.open();
            Cursor c = queries.getContact(contactId);
            if (c.getCount() < 1)
            {
                addContactInDb();
            }
            queries.close();
        }

        TextView textView = (TextView) findViewById(R.id.chat_name);
        textView.setText(username);

        ll = (LinearLayout) findViewById(R.id.chat_box);

        setChatListener();

        final EditText textField = (EditText) findViewById(R.id.text_box);
        ImageView send = (ImageView) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(textField.getText()))
                {
                    if(chatId != null)
                    {
                        sendMessage(textField);
                    }
                    else
                    {
                        createChat();
                        sendMessage(textField);
                    }

                }
            }
        });
        scrollView = (ScrollView) findViewById(R.id.scroll_chat);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void createChat()
    {
        addFirebaseContact();
        addContactInDb();
        setChatListener();
    }

    private void addContactInDb()
    {
        ContactsTableQueries queries = new ContactsTableQueries(this);
        queries.open();
        long id = queries.insertContact(username, contactPhoto, contactPhone, contactId);
        if(contactPhoto != null)
        {
            FileHandler fh = new FileHandler(this);
            fh.downloadPhotoFromFirebase(contactPhoto, path);
        }
        queries.addChatId(contactId, chatId);
        queries.close();
    }

    private void addFirebaseContact()
    {
        //create contact in firebase
        Firebase contact1 = new Firebase(getString(R.string.firebase_users)+"/"+myId+"/contacts");
        Contact contact = new Contact(contactId, "yes");
        Firebase id = contact1.push();
        chatId = id.getKey();
        id.setValue(contact);

        Firebase contact2= new Firebase(getString(R.string.firebase_users)+"/"+contactId+"/contacts/"+chatId);
        contact = new Contact(myId, "no");
        contact2.setValue(contact);
    }

    private void setChatListener()
    {
        if (chatId != null)
        {
            setHeader();
            notification = new Firebase(getString(R.string.firebase_notifications));
            ref = new Firebase(getString(R.string.firebase_chats)+"/"+chatId);
            Query query = ref.limitToLast(50*mul);
            listener = query.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    mssg = dataSnapshot.getValue(Message.class);
                    displayMessage(mssg.getFrom(), mssg.getMessage());

                    if(contactId.equals(mssg.getFrom()))
                    {
                        upRef = new Firebase(getString(R.string.firebase_chats)+"/"+chatId+"/"+dataSnapshot.getKey());
                        readed.put("readed", "yes");
                        upRef.updateChildren(readed);
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    private void sendMessage(final EditText editText)
    {
        Message message = new Message(myId, editText.getText().toString(), "no", contactId);
        Firebase id = ref.push();
        id.setValue(message);
        notification.push().setValue(message);//upis u notifikacije radi slanja iste
    }

    private void setHeader()
    {
        view = LayoutInflater.from(this).inflate(R.layout.more, ll, true );
        LinearLayout more = (LinearLayout) view.findViewById(R.id.load_more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll.removeAllViews();
                mul = mul + 1;
                setChatListener();
            }
        });
    }

    private void displayMessage(String sender, String message)
    {
        if (loading.isShowing())
            loading.dismiss();

        if (sender.equals(myId))
        {
            if (myMssg)
            {//poruka sa slikom
                view = LayoutInflater.from(this).inflate(R.layout.message_user_layout, ll, true );
                text = (TextView) view.findViewById(R.id.user_layout_text);
                text.setText(message);
                if(myPhoto != null)
                {
                    civ = (CircleImageView) view.findViewById(R.id.user_layout_civ);
                    civ.setId(counter);
                    new ImageTask(this, path+"/"+myPhoto, civ, 40, 40).execute();
                }
                text.setId(counter);
                counter = counter + 1;
                myMssg = false;
                contactMssg = true;
            }
            else
            {//poruka bez slike
                view = LayoutInflater.from(this).inflate(R.layout.message_user_empty_layout, ll, true );
                text = (TextView) view.findViewById(R.id.user_empty_text);
                text.setText(message);
                text.setId(counter);
                counter = counter + 1;
            }

        }
        else
        {
            if(contactMssg)
            {
                view = LayoutInflater.from(this).inflate(R.layout.message_contact_layout, ll, true );
                text = (TextView) view.findViewById(R.id.contact_layout_text);
                text.setText(message);
                if(contactPhoto != null)
                {
                    civ = (CircleImageView) view.findViewById(R.id.contact_layout_civ);
                    civ.setId(counter);
                    new ImageTask(this, path+"/"+contactPhoto, civ, 40, 40).execute();
                }
                text.setId(counter);
                counter = counter + 1;
                contactMssg = false;
                myMssg = true;
            }
            else
            {
                view = LayoutInflater.from(this).inflate(R.layout.message_contact_empty_layout, ll, true );
                text = (TextView) view.findViewById(R.id.contact_empty_text);
                text.setText(message);
                text.setId(counter);
                counter = counter + 1;
            }

        }

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(listener != null)
        ref.removeEventListener(listener);
    }

    @Override
    public void onBackPressed() {

        setResult(Activity.RESULT_OK);
        finish();
    }
}
