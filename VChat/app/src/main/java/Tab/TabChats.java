package Tab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.popvuk.chatapp.MainActivity;
import com.example.popvuk.vchat.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import custom.FileHandler;
import custom.ImageTask;
import database.ContactsTableQueries;
import entity.Contact;
import entity.Message;
import entity.User;
import roundedimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by POPVUK on 8/25/2017.
 */

public class TabChats extends Fragment {

    private View view;
    private TextView username, counter;
    private TextView phoneNo;
    private CircleImageView civ;
    private LinearLayout ll;
    private HashMap<Firebase, ChildEventListener> listenerHashMap = new HashMap<>();
    private ChildEventListener newChatListener;
    private Firebase newChat;
    private FileHandler fh;
    private Firebase unreadedFirebase;
    private ChildEventListener unreadedListener;
    private Query query;
    private LayoutInflater layoutInflater;

    public TabChats() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab_chats_layout, container, false);
        layoutInflater = inflater;
        ll = (LinearLayout) rootView.findViewById(R.id.chat_tab);
        ContactsTableQueries queries = new ContactsTableQueries(getContext());
        queries.open();
        Cursor cursor = queries.getAllContacts();
        if(cursor.moveToFirst())
        {
            do
            {
                int id = Integer.valueOf(cursor.getString(0));
                displayChat(id, cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(3), cursor.getString(5));
                setUnreadedListener(cursor.getString(3), cursor.getString(5));
            }
            while (cursor.moveToNext());
        }
        queries.close();
        setNewChatListener();

        return rootView;
    }

    private void setUnreadedListener(String chatId, final String contactId)
    {
        counter = (TextView) view.findViewById(R.id.count_mssgs);
        unreadedFirebase = new Firebase(getString(R.string.firebase_chats)+"/"+chatId);
        query = unreadedFirebase.orderByChild("readed").equalTo("no");
        unreadedListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if(message.getFrom().equals(contactId))
                {
                    String br = counter.getText().toString();
                    int brmssg = Integer.valueOf(br);
                    brmssg = brmssg + 1;
                    counter.setText(String.valueOf(brmssg));
                    counter.setVisibility(View.VISIBLE);
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

        listenerHashMap.put(unreadedFirebase, unreadedListener);
    }

    private void setNewChatListener()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        final String myId = prefs.getString("userId", null);
        newChat = new Firebase(getString(R.string.firebase_users)+"/"+myId+"/contacts");
        Query query = newChat.orderByChild("readed").equalTo("no");
        newChatListener = query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Contact contact = dataSnapshot.getValue(Contact.class);
                ContactsTableQueries queries = new ContactsTableQueries(getContext());
                queries.open();
                Cursor c = queries.getContact(contact.getId_contact());
                if (!c.moveToFirst())
                {
                    addContactInDb(contact.getId_contact(), dataSnapshot.getKey());
                }
                queries.close();
                Firebase firebase = new Firebase(getString(R.string.firebase_users)+"/"+myId+"/contacts/"+dataSnapshot.getKey());
                Map<String, Object> readed = new HashMap<>();
                readed.put("readed", "yes");
                firebase.updateChildren(readed);
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

    private void addContactInDb(final String userKey, final String chatId)
    {
        Firebase user = new Firebase(getString(R.string.firebase_users));
        Query queryRef = user.orderByKey().equalTo(userKey);
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    User user = data.getValue(User.class);
                    if(user.getPhoto() != null)
                    {
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ChatApp";
                        fh = new FileHandler(getContext());
                        fh.downloadPhotoFromFirebase(user.getPhoto(), path);
                    }
                   //add contact in DB
                    ContactsTableQueries query = new ContactsTableQueries(getContext());
                    query.open();
                    int id = (int)query.insertContact(user.getUsername(), user.getPhoto(), user.getPhone(), userKey);
                    query.addChatId(userKey, chatId);
                    query.close();
                    displayChat(id, user.getUsername(),  user.getPhone(), user.getPhoto(), chatId, userKey);
                    setUnreadedListener(chatId, userKey);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void displayChat(final int id, final String name, String phone, final String photo, final String chatId, final String con)
    {
        view = layoutInflater.inflate(R.layout.chat_layout, ll, true);
        username = (TextView) view.findViewById(R.id.chatUsername);
        username.setText(name);
        username.setId(id);

        phoneNo = (TextView) view.findViewById(R.id.chatPhone);
        phoneNo.setText(phone);
        phoneNo.setId(id);

        if( photo != null)
        {
            civ = (CircleImageView) view.findViewById(R.id.circularImg);
            civ.setId(id);
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ChatApp";
            String fileName = path+"/"+photo;
            new ImageTask(getActivity(),fileName,civ, 60, 60).execute();
        }

        final LinearLayout chat = (LinearLayout) view.findViewById(R.id.chat_view);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = (TextView) view.findViewById(R.id.count_mssgs);
                counter.setVisibility(View.INVISIBLE);
                counter.setText("0");
                openChat(chatId, name, con, photo);
            }
        });
        chat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteChat(chat, id);
                return true;
            }
        });
    }

    private void openChat(String chatId, String username, String contact, String photo)
    {
        Intent chat = new Intent("android.intent.action.CHAT");
        chat.putExtra("chat", chatId);
        chat.putExtra("username", username);
        chat.putExtra("contactId", contact);
        chat.putExtra("contactPhoto", photo);
        startActivityForResult(chat, 4);
    }

    private void deleteChat(LinearLayout chatBox, int id)
    {
        //delete contact from db and remove from chat tab
        ContactsTableQueries queries = new ContactsTableQueries(getContext());
        queries.open();

        if(queries.deleteContact(id))
        {
            LinearLayout parent = (LinearLayout) chatBox.getParent();
            parent.removeView(chatBox);
        }

        queries.close();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        newChat.removeEventListener(newChatListener);

        for (Map.Entry<Firebase, ChildEventListener> entry : listenerHashMap.entrySet()) {
            Firebase ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }
}
