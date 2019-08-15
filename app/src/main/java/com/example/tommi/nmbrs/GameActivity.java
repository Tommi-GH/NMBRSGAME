package com.example.tommi.nmbrs;


import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static android.graphics.Color.rgb;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class GameActivity extends AppCompatActivity {


    private int dataSetSize;
    private Integer[] dataSet;
    private GridView grid;
    private ArrayAdapter<Integer> adapter;
    private int columns;
    private ArrayList<Integer> occupiedCells = new ArrayList<>();
    private ArrayList<Integer> nextCells = new ArrayList<>();
    private Chronometer chronometer;
    private TextView score;
    private int lastClick;
    private int lastRow;
    private int lastColumn;
    private int nextInt;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("record");

    private void init(int dataSetSize, int columns,Bundle savedInstanceState){
        this.columns = columns;
        this.dataSetSize = dataSetSize;
        init(savedInstanceState);
    }


    private void init(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            occupiedCells.clear();
            nextCells.clear();
            dataSet = loop(dataSetSize);
            chronometer.setBase(SystemClock.elapsedRealtime());
            score.setText("0");
            lastClick = 0;
            lastRow = 0;
            lastColumn = 0;
            nextInt = 1;
        } else {
            ArrayList<Integer> dataList = savedInstanceState.getIntegerArrayList("dataSet");
            dataSet = dataList.toArray(new Integer[dataList.size()]);
            nextCells = savedInstanceState.getIntegerArrayList("nextCells");
            occupiedCells = savedInstanceState.getIntegerArrayList("occupiedCells");
            score.setText( (String) savedInstanceState.get("score"));
            chronometer.setBase((long) savedInstanceState.get("timeElapsed"));
            lastClick = (int) savedInstanceState.get("lastClick");
            lastRow = (int) savedInstanceState.get("lastRow");
            lastColumn = (int) savedInstanceState.get("lastColumn");
            nextInt = (int) savedInstanceState.get("nextInt");
        }
        adapter =
                new ArrayAdapter<Integer>(this,
                        R.layout.gamelayout,
                        R.id.textView,
                        dataSet
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                        View view = super.getView(position, convertView, parent);

                        int color = rgb(255, 255, 255);
                        if (occupiedCells.contains(position)) {
                            color = rgb(50, 205, 50);
                        } else if (nextCells.contains(position)) {
                            color = rgb(51, 255, 255);
                        }
                        view.setBackgroundColor(color);
                        view.setPadding(0, 20, 0, 20);


                        return view;
                    }
                };

        grid = (GridView) findViewById(R.id.gameGrid);
        grid.setAdapter(adapter);
        grid.setNumColumns(columns);
        grid.setBackgroundColor(rgb(224, 224, 224));
        grid.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        grid.setHorizontalSpacing(7);
        grid.setVerticalSpacing(7);
        grid.setPadding(70, 70, 70, 70);

        CustomOnItemClickListener listener = new CustomOnItemClickListener();
        grid.setOnItemClickListener(listener);

        chronometer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

            int gameSize = getIntent().getIntExtra(MainActivity.GAME_SIZE,-1);
            chronometer = (Chronometer) findViewById(R.id.chronometer);
            score = (TextView) findViewById(R.id.score);



             if(gameSize == 50 || gameSize == 100) {
                init(gameSize, (gameSize / 10),savedInstanceState);
            }else if (gameSize == 20){
                init(gameSize, 5,savedInstanceState);
            }
            else {
                finish();
            }

    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putIntegerArrayList("dataSet",new ArrayList<>(Arrays.asList(dataSet)));
        state.putIntegerArrayList("occupiedCells",occupiedCells);
        state.putIntegerArrayList("nextCells",nextCells);
        state.putLong("timeElapsed",chronometer.getBase());
        state.putString("score",(String) score.getText());
        state.putInt("lastClick",lastClick);
        state.putInt("lastColumn",lastColumn);
        state.putInt("lastRow",lastRow);
        state.putInt("nextInt",nextInt);
    }

    /**
     * Creates an array of zeros the size of the game
     * @param size Desired size of the array
     * @return array of zeros, size of max
     */
    private Integer[] loop ( int size){
        Integer[] a = new Integer[size];
        for (int i = 0; i < size; ++i) {
            a[i] = 0;
        }
        return a;
    }

    /**
     * saves score to database
     * @param score the player's score
     * @param time the player's time
     * @param name the player's name
     */
    private void postScore(final int score, final String time, final String name) {
        if (name.length() == 0){
            return;
        }

        Record record = new Record(dataSetSize,name,score,time);
        myRef.child("record"+dataSetSize).push().setValue(record);

    }

    @Override
    protected void onPause() {
        super.onPause();
        chronometer.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chronometer.start();
    }

    /**
     * Custom click listener
     */
    private class CustomOnItemClickListener implements AdapterView.OnItemClickListener {



        String time;
        Bundle bundle = new Bundle();

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int click, long l) {

            bundle.clear();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ""+click);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int row = ((int) Math.floor(click / columns));
            int column = (click % columns);

            if (isValidMove(click)){
                dataSet[click] = nextInt;
                score.setText(getString(R.string.nextInt,nextInt));
                nextInt++;
                lastColumn = column;
                lastRow = row;
                adapter.notifyDataSetInvalidated();
                occupiedCells.add(click);
                lastClick = click;


                if (!movesAvailable(click)){
                    chronometer.stop();
                    long millis = SystemClock.elapsedRealtime() - chronometer.getBase();
                    time = String.format("%d:%2d",
                            MILLISECONDS.toMinutes(millis),
                            MILLISECONDS.toSeconds(millis) -
                                    TimeUnit.MINUTES.toSeconds(MILLISECONDS.toMinutes(millis))
                    );

                    final AlertDialog.Builder gameOver = new AlertDialog.Builder(view.getContext());
                    if (nextInt<dataSetSize){
                        gameOver.setTitle(R.string.gameOver);
                        gameOver.setMessage(getString(R.string.gameOverLost,time));
                    } else {
                        gameOver.setTitle(getString(R.string.youWon));
                        gameOver.setMessage(getString(R.string.gameOverWon,time));
                        bundle.clear();
                        bundle.putString(FirebaseAnalytics.Param.LEVEL,""+dataSetSize);
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
                    }

                    final EditText input = new EditText(view.getContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    gameOver.setView(input);

                    gameOver.setPositiveButton(R.string.tryAgain, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            postScore(nextInt-1,time,input.getText().toString());
                            init(null);
                        }
                    });

                    gameOver.setNegativeButton(R.string.backToMenu, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            postScore(nextInt-1,time,input.getText().toString());
                            finish();
                        }
                    });

                    gameOver.show();

                }

            }else{
                CountDownTimer countDownTimer = new CountDownTimer(400, 200) {
                    @Override
                    public void onTick(long l) {
                        grid.setBackgroundColor(rgb(250,0,0));
                    }

                    @Override
                    public void onFinish() {
                        grid.setBackgroundColor(rgb(224,224,224));
                    }
                }.start();
            }

        }

        /**
         * Check if there's still moves available
         * @param click a press on a grid cell
         * @return true if there's still moves left, false otherwise
         */
        private boolean movesAvailable(int click) {

            int rowOffset = 3;
            int columnOffset = 3*columns;
            int cornerOffset = 2;
            int west = click - rowOffset;
            int east = click + rowOffset;
            int north = click - columnOffset;
            int south = click + columnOffset;
            int southwest = click - (cornerOffset*columns) - cornerOffset;
            int southeast = click - (cornerOffset*columns) + cornerOffset;
            int northwest = click + (cornerOffset*columns) - cornerOffset;
            int northeast = click + (cornerOffset*columns) + cornerOffset;
            nextCells.clear();


            int[] moves = {west,east,north,south,southwest,southeast,northeast,northwest};

            for (int move : moves){
                if (isValidMove(move)){
                    nextCells.add(move);
                }
            }

            return nextCells.size() != 0;

        }

        /**
         * Check if the given move is a valid move in the current situation
         * @param click a press on a grid cell
         * @return true if the click is a valid move, false otherwise
         */
        private boolean isValidMove(int click) {

            if (click < 0 || click > dataSet.length-1){
                return false;
            }

            int rowOffset = 3;
            int columnOffset = 3;
            int cornerOffset = 2;
            int row = ((int) Math.floor(click / columns));
            int column = (click % columns);

            if (nextInt == 1){
                return true;
            }else if ((dataSet[click] == 0) &&
                    (((row == lastRow) && (column == (lastColumn + columnOffset))) //East
                            || ((row == lastRow) && (column == (lastColumn - columnOffset))) //West
                            || ((column == lastColumn) && (row == (lastRow + rowOffset))) //South
                            || ((column == lastColumn) && (row == (lastRow - rowOffset))) //North
                            || ((column == lastColumn - cornerOffset) && (row == (lastRow - cornerOffset))) //Northwest
                            || ((column == lastColumn - cornerOffset) && (row == (lastRow + cornerOffset))) //Northeast
                            || ((column == lastColumn + cornerOffset) && (row == (lastRow - cornerOffset))) //Southwest
                            || ((column == lastColumn + cornerOffset) && (row == (lastRow + cornerOffset))))){ //Southeast
                return true;
            } else{
                return false;
            }}
    }
}
