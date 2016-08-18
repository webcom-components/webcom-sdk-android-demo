package com.orange.datasync.chat;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.orange.webcom.sdk.Config;
import com.orange.webcom.sdk.DataSnapshot;
import com.orange.webcom.sdk.OnAuth;
import com.orange.webcom.sdk.OnQuery;
import com.orange.webcom.sdk.Query;
import com.orange.webcom.sdk.Webcom;
import com.orange.webcom.sdk.WebcomError;
import com.orange.webcom.sdk.WebcomException;
import com.thedeanda.lorem.Lorem;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ChatActivity extends ListActivity {

    private static final String TAG = "ChatActivity";

    EditText mNewMessage;
    Button mNewMessageSend;
    ViewHolderAdapter mAdapter;
    Webcom ref;

    String userName;

    class ChatQuery implements OnQuery {
        Query.Event type;
        public ChatQuery(Query.Event type) {
            this.type = type;
        }
        @Override
        public void onComplete(DataSnapshot snapData, @Nullable String prevName) {
            try {
                Message message = snapData.value(Message.class);
                if(message == null) {
                    message = new Message();
                }
                message.setSnapName(snapData.name());
                switch (this.type) {
                    case CHILD_ADDED:
                        addMessage(message);
                        break;
                    case CHILD_REMOVED:
                        removeMessage(message);
                        break;
                    case CHILD_CHANGED:
                        updateMessage(message);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        @Override
        public void onCancel(WebcomError error) {
            Log.e(TAG, error.getMessage());
        }

        @Override
        public void onError(WebcomError error) {
            Log.e(TAG, error.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        registerForContextMenu(this.getListView());

        mAdapter = new ViewHolderAdapter(this, R.layout.list_message);
        setListAdapter(mAdapter);

        mNewMessage = (EditText) findViewById(R.id.newmsg);
        if(mNewMessage != null) {
            mNewMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage();
                    }
                    return false;
                }
            });
        }
        mNewMessageSend = (Button) findViewById(R.id.newmsgsend);
        if (mNewMessageSend!=null){
            mNewMessageSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessage();
                }
            });
        }

        SharedPreferences prefs = getSharedPreferences("SimpleChat", Context.MODE_MULTI_PROCESS);

        userName = prefs.getString("chatUserName", null);

        try {
            Config.setLogLevel(Config.LogLevel.valueOf(BuildConfig.LOG_LEVEL));
            // Create ref
            ref = new Webcom(BuildConfig.WEBCOM_URL);
            // No error when creating => enable button
            mNewMessageSend.setEnabled(true);
            // Listen to changes
            ref.on(Query.Event.CHILD_ADDED, new ChatQuery(Query.Event.CHILD_ADDED));
            ref.on(Query.Event.CHILD_CHANGED, new ChatQuery(Query.Event.CHILD_CHANGED));
            ref.on(Query.Event.CHILD_REMOVED, new ChatQuery(Query.Event.CHILD_REMOVED));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void addMessage(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.add(message);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void removeMessage(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.remove(message);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void updateMessage(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int position = mAdapter.getPosition(message);
                mAdapter.remove(message);
                mAdapter.insert(message, position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void notifyChanges() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void sendMessage() {
        // Get message
        String message = mNewMessage.getText().toString();
        if(message.length() == 0){
            //Dialog
            new AlertDialog.Builder(this)
                    .setTitle("Empty message")
                    .setMessage("Send a random message ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                ref.push(new Message(userName, Lorem.getWords(3, 10)));
                            } catch (WebcomException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            try {
                ref.push(new Message(userName, message));
                mNewMessage.setText("");
            } catch (WebcomException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_user_create).setVisible(userName == null);
        menu.findItem(R.id.action_user_change).setVisible(userName != null);
        menu.findItem(R.id.action_user_delete).setVisible(userName != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_clean:
                try {
                    ref.remove();
                } catch (WebcomException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return true;
            case R.id.action_log_populate:
                try {
                    String n1 = email();
                    String n2 = email();
                    String n3 = email();
                    String[] users = {userName, n1, n2, userName, n3};
                    int count = 0;
                    while(count++ < 10) {
                        ref.push(new Message(users[ThreadLocalRandom.current().nextInt(0, users.length)], Lorem.getWords(3, 10)));
                    }
                } catch (WebcomException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return true;
            case R.id.action_user_create:
                createUser();
                return true;
            case R.id.action_user_delete:
                deleteUser(false);
                return true;
            case R.id.action_user_change:
                deleteUser(true);
                return true;
            case R.id.action_about:
                Pair<String, String>[] p = new Pair[]{
                        new Pair("Webcom Server", BuildConfig.WEBCOM_URL),
                        new Pair("Log Level", BuildConfig.LOG_LEVEL),
                        new Pair("SDK", String.format("%s / %s / %s", com.orange.webcom.sdk.BuildConfig.VERSION_NAME, com.orange.webcom.sdk.BuildConfig.FLAVOR, com.orange.webcom.sdk.BuildConfig.BUILD_TYPE)),
                        new Pair("App", String.format("%s / %s / %s", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE))
                };
                StringBuilder msg = new StringBuilder();
                for (Pair<String, String> mm: Arrays.asList(p)) {
                    msg.append(String.format("%s: \n\n\t\t%s\n\n", mm.first, mm.second));
                }
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(msg.toString())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat_item, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Message message = (Message) this.getListView().getItemAtPosition(acmi != null ? acmi.position : 0);
        switch (item.getItemId()) {
            case R.id.action_message_user:
                try {
                    message.setName(email());
                    ref.child(message.snapName).update(message);
                } catch (WebcomException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return true;
            case R.id.action_message_random:
                try {
                    message.setText(Lorem.getWords(3, 10));
                    ref.child(message.snapName).update(message);
                } catch (WebcomException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return true;
            case R.id.action_message_remove:
                try {
                    ref.child(message.snapName).remove();
                } catch (WebcomException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    Message createMessage(@Nullable String user, @Nullable String text){
        return new Message(user, text != null ? text : Lorem.getWords(3, 10));
    }

    public void setUserName(@Nullable String name) {
        this.userName = name;
        SharedPreferences prefs = getSharedPreferences("SimpleChat", Context.MODE_MULTI_PROCESS);
        prefs.edit().putString("chatUserName", userName).apply();
    }


    public String getUserName() {
        return userName;
    }

    String email() {
        return (Lorem.getFirstName() + "." + Lorem.getLastName() + "@webcom.com").toLowerCase();
    }
    public static String capitalize(String s){
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
    String emailToName(String mail){
        String[] p = mail.replaceAll("@.*", "").split("\\.");
        return capitalize(p[0]) + " " + capitalize(p[1]);
    }
    void createUser(){
        try {
            final Context mContext = this;
            final String email = email();
            ref.createUser(email, "Passwd123!", new OnAuth() {
                @Override
                public void onComplete(@Nullable AuthResponse response) {
                    //Dialog
                    setUserName(email);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("User creation")
                                    .setMessage(String.format("The user \"%s\" has been created", emailToName(email)))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            notifyChanges();
                                        }
                                    })
                                    .show();
                        }
                    });
                }
                @Override
                public void onError(WebcomError error) {
                    final String errorMessage = error.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("User creation")
                                    .setMessage(String.format("The user \"%s\" wasn't created.\nReason: %s", emailToName(email), errorMessage))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            notifyChanges();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
                }
            });
        } catch (WebcomException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    void deleteUser(final boolean create){
        try {
            final Context mContext = this;
            final String email = userName;
            ref.removeUser(email, "Passwd123!", new OnAuth() {
                @Override
                public void onComplete(@Nullable AuthResponse response) {
                    //Dialog
                    setUserName(null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("User deletion")
                                    .setMessage(String.format("The user \"%s\" has been deleted", emailToName(email)))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // nothing
                                            if (create) {
                                                createUser();
                                            } else {
                                                notifyChanges();
                                            }
                                        }
                                    })
                                    .show();
                        }
                    });
                }

                @Override
                public void onError(WebcomError error) {
                    final String errorMessage = error.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(mContext)
                                    .setTitle("User deletion")
                                    .setMessage(String.format("The user \"%s\" wasn't deleted.\nReason: %s", emailToName(email), errorMessage))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // nothing
                                            if (create) {
                                                createUser();
                                            } else {
                                                notifyChanges();
                                            }
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
                }
            });
        } catch (WebcomException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}

