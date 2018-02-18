package Tab;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SignalStrength;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.popvuk.chatapp.MainActivity;
import com.example.popvuk.vchat.R;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.lang.ref.WeakReference;

import custom.FileHandler;
import database.ContactsTable;
import database.ContactsTableQueries;
import entity.Contact;
import entity.User;
import roundedimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by POPVUK on 8/25/2017.
 */

public class TabContacts extends Fragment{

    private TextView contactName, contactPhone;
    private View view;
    private ImageView mail;
    private SharedPreferences my_prefs;
    private String userKey;
    private Firebase ref;
    private String username, tel, photo;


    public TabContacts() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int rBr = 1;
        View rootView = inflater.inflate(R.layout.tab_contacts_layout, container,false);
        LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.contact_tab);

        ContentResolver cr = getContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                final String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        final String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        view = inflater.inflate(R.layout.contact_layout, ll, true);

                        contactName = (TextView) view.findViewById(R.id.name);
                        contactPhone = (TextView) view.findViewById(R.id.phone);
                        mail = (ImageView) view.findViewById(R.id.imageViewMail);
                        contactName.setText(name);
                        contactName.setId(rBr);

                        contactPhone.setText(phoneNo);
                        contactPhone.setId(rBr);

                        mail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                checkFirebaseUsers(phoneNo, name);//if ChatUp user?
                            }
                        });
                        mail.setId(rBr);

                        rBr++;
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
        return rootView;
    }

    private void checkFirebaseUsers(final String phone, final String name)//
    {
        final String phoneS = phone.replaceAll("\\s","");
        ref = new Firebase(getString(R.string.firebase_users));
        Query queryRef = ref.orderByChild("phone");
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()) {

                    User user = data.getValue(User.class);
                    if(PhoneNumberUtils.compare(user.getPhone(), phoneS))
                    {
                        userKey = data.getKey();
                        username = user.getUsername();
                        tel = user.getPhone();
                        photo = user.getPhoto();
                        isInDb();//

                        return;
                    }

                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
                intent.putExtra("address", phone);
                intent.putExtra("sms_body", "Hi "+name+", please join ChatApp!");
                intent.setType("vnd.android-dir/mms-sms");
                startActivity(intent);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void isInDb()
    {
        ContactsTableQueries queries = new ContactsTableQueries(getContext());
        queries.open();
        Cursor c = queries.getContact(userKey);

        //if is in db open chat
        if (c.moveToFirst())
        {
            openChat(c.getString(1), c.getString(3), c.getString(4), c.getString(2));
            queries.close();
        }
        else
        {
            isFirebaseContact();
            queries.close();
        }
    }

    private void isFirebaseContact()
    {
        my_prefs = getActivity().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        String myId = my_prefs.getString("userId", null);
        ref = new Firebase(getString(R.string.firebase_users)+"/"+myId+"/contacts");

        Query query = ref.orderByChild("id_contact").equalTo(userKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren())
                {
                    Contact contact = data.getValue(Contact.class);;
                    if(userKey.equals(contact.getId_contact()))
                    {
                        openChat(username, data.getKey(), photo, tel);
                        return;
                    }
                }

                openChat1();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void openChat(String username, String chatId,String photo, String phoNo)
    {
        Intent chat = new Intent("android.intent.action.CHAT");
        chat.putExtra("chat", chatId);
        chat.putExtra("username", username);
        chat.putExtra("contactId", userKey);
        chat.putExtra("contactPhoto", photo);
        chat.putExtra("phone", phoNo);
        getActivity().startActivityForResult(chat , 2);
    }

    private void openChat1()
    {
        Intent chat = new Intent("android.intent.action.CHAT");
        chat.putExtra("username", username);
        chat.putExtra("contactId", userKey);
        chat.putExtra("contactPhoto", photo);
        chat.putExtra("phone", tel);
        getActivity().startActivityForResult(chat, 3);
    }

}

