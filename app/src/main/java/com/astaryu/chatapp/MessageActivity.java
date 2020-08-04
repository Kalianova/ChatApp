package com.astaryu.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.astaryu.chatapp.Adapter.MessageAdapter;
import com.astaryu.chatapp.Model.Chat;
import com.astaryu.chatapp.Model.Users;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView image;

    RecyclerView recyclerView;
    EditText message;
    ImageButton sendButton;

    FirebaseUser user;
    DatabaseReference database;
    Intent intent;

    MessageAdapter messageAdapter;
    List<Chat> chat;

    RecyclerView recyclerViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        username = findViewById(R.id.username_name);
        image = findViewById(R.id.username_image);

        sendButton = findViewById(R.id.button_send);
        message = findViewById(R.id.text_send);

        recyclerViewMessage = findViewById(R.id.recycle_view);
        recyclerViewMessage.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessage.setLayoutManager(linearLayoutManager);

        /*Toolbar toolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        intent = getIntent();
        final String userId = intent.getStringExtra("userid");
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user_this = snapshot.getValue(Users.class);
                username.setText(user_this.getUsername());

                if (user_this.getImage().equals("default")){
                    image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(MessageActivity.this).load(user_this.getImage()).into(image);
                }

                readMessages(user.getUid(), user_this.getId(), user_this.getImage());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_message = message.getText().toString();
                if (!text_message.equals("")){
                    sendMessage(user.getUid(), userId, text_message);
                }
                message.setText("");
            }
        });
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference databaseMessage = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        databaseMessage.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myid, final String userid, final String profile_image){
        chat = new ArrayList<>();

        database = FirebaseDatabase.getInstance().getReference("Chats");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat mes_chat = dataSnapshot.getValue(Chat.class);
                    if (mes_chat.getReceiver().equals(myid) && mes_chat.getSender().equals(userid) ||
                            mes_chat.getReceiver().equals(userid) && mes_chat.getSender().equals(myid)) {
                        chat.add(mes_chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, chat, profile_image);
                    recyclerViewMessage.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
