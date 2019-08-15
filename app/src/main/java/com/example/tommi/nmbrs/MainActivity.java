package com.example.tommi.nmbrs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    public static String GAME_SIZE;
    private ArrayList<String> highScores;
    private ArrayList<Record> highRecords20;
    private ArrayList<Record> highRecords50;
    private ArrayList<Record> highRecords100;
    private ArrayAdapter<String> adapter;
    private final int MY_PERMISSIONS_REQUEST_INTERNET = 123;
    private FirebaseAnalytics mFirebaseAnalytics;
    private  Query highScore20Query;
    private  Query highScore50Query;
    private  Query highScore100Query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        highScores  = new ArrayList<>();
        highRecords20 = new ArrayList<>();
        highRecords50 = new ArrayList<>();
        highRecords100 = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("record");
        highScore20Query = myRef.child("record20");
        highScore50Query = myRef.child("record50");
        highScore100Query = myRef.child("record100");

        checkInternetPermission();

        adapter =
                new ArrayAdapter<>(this,
                        R.layout.highscorelayout,
                        R.id.highscoreTextView,
                        highScores
                );

        ListView listView = (ListView) findViewById(R.id.highScoreList);
        listView.setAdapter(adapter);

    }

    private void checkInternetPermission() {

        if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }else{
            setHighScoreListeners();
        }
    }

    /**
     * set listeners for high score from database
     */
    private void setHighScoreListeners() {

        highScore20Query.addChildEventListener(new custChildEventListener());
        highScore50Query.addChildEventListener(new custChildEventListener());
        highScore100Query.addChildEventListener(new custChildEventListener());

    }

    /**
     * Parse the response from database query
     * @param record Record object from database
     */
    private void parseResponse(Record record) {

        if(record == null){
            return;
        }

        switch (record.getLevel()){
            case 20:
                highRecords20.add(record);
                Collections.sort(highRecords20);
                if (highRecords20.size() > 3) {
                    highRecords20.remove(highRecords20.size() - 1);
                }
                updateHighScores();
                break;
            case 50:
                highRecords50.add(record);
                Collections.sort(highRecords50);
                if (highRecords50.size() > 3) {
                    highRecords50.remove(highRecords50.size() - 1);
                }
                updateHighScores();
                break;
            case 100:
                highRecords100.add(record);
                Collections.sort(highRecords100);
                if (highRecords100.size() > 3) {
                    highRecords100.remove(highRecords100.size() - 1);
                }
                updateHighScores();
                break;
        }
    }

    /**
     * Updates the high score list
     */
    private void updateHighScores() {
        adapter.clear();
        highScores.add("------- Level 20 ------");
        for (Record r : highRecords20){
            highScores.add(r.toString());
        }
        highScores.add("------- Level 50 ------");
        for (Record r : highRecords50){
            highScores.add(r.toString());
        }
        highScores.add("------- Level 100 ------");
        for (Record r : highRecords100){
            highScores.add(r.toString());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_INTERNET:{
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setHighScoreListeners();
                }
            }
        }
    }

    public void  startGame20(View view){
        startGame(20);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button 20");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void  startGame50(View view){
        startGame(50);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button 50");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void  startGame100(View view){
        startGame(100);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Button 100");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    private void startGame(int gameSize) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_SIZE, gameSize);
        startActivity(intent);
    }

    /**
     * Listener for events in high score database
     */
    private class custChildEventListener implements ChildEventListener {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                parseResponse(dataSnapshot.getValue(Record.class));
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
            public void onCancelled(DatabaseError error) {
                highScores.clear();
                adapter.clear();
                highScores.add("Couldn't retrieve high scores, sorry!");
                adapter.notifyDataSetChanged();
            }
    }
}
