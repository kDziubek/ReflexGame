package com.example.hp.reflexgame.view;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hp.reflexgame.R;

import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.LogRecord;

public class ReflexView extends View {

    //Static instant variables
    private static final String HIGHSCORE = "HIGH_SCORE";
    private SharedPreferences preferences ;

    // Varaibles that manage the game
    private int spotsTouched;
    private int score;
    private int level;
    private int viewWidth;
    private int viewHigh;
    private long animationTime;
    private boolean gameOver;
    private boolean gamePaused;
    private boolean dialogSiplayed;
    private int highScore;

    //Collections types for our circles/spots (imageViews) and Animators

    private final Queue<ImageView> spots = new ConcurrentLinkedDeque<>();
    private final Queue<Animator> animators = new ConcurrentLinkedDeque<>();

    private TextView highScoreTextView;
    private TextView currentScoreTexetView;
    private TextView levelTextView;
    private LinearLayout livesLinearLayout;
    private RelativeLayout relativeLayout;
    private Resources resources;
    private LayoutInflater layoutInflater;


    private static final int INITIAL_ANIMATION_DURATION = 6000; //6sec
    public static final Random random = new Random();
    public static final int SPOT_DAIMETER = 100;
    public static final float SCALE_X = 0.25f;
    public static final float SCALE_Y = 0.25f;
    public static final int INITIAL_SPOTS = 5;
    public static final int SPOTS_DELAY = 500; //opoznienie
    public static final int Lives = 3;
    public static final int MAV_Lives = 7;
    public static final int NEW_LEVEL = 10;
    private Handler spotHandler;

    public static final int HIT_SOUND_ID = 1;
    public static final int MISS_SOUND_ID = 2;
    public static final int DISSAPEAR_SOUND_ID = 3;
    public static final int SOUND_PRIORITY = 1;
    public static final int SOUND_QUALITY = 1000;
    public static final int MAX_STREAMS = 4;

    private SoundPool.Builder soundPool;
    private SoundPool soundPool1;
    private int volume;
    private Map<Integer, Integer> soundMap;





    public ReflexView(Context context, SharedPreferences sharedPreferences, RelativeLayout parentLayout) {
        super(context);

        preferences = sharedPreferences;
        highScore = preferences.getInt(HIGHSCORE, 0);

        //save resources for loading external values
        resources = context.getResources();

        //save LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Setup UI component
        relativeLayout = parentLayout;
        livesLinearLayout = relativeLayout.findViewById(R.id.lifeLinearLayout);
        highScoreTextView = relativeLayout.findViewById(R.id.highScoreTextView);
        currentScoreTexetView = relativeLayout.findViewById(R.id.scoreTextView);
        levelTextView = relativeLayout.findViewById(R.id.levelTextview);

       // spotHandler = new Handler();
        
        //addNewSpot();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHigh = h;
    }

    private void initializeSoundEffects(Context context){

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder();
            soundPool.setAudioAttributes(audioAttributes).setMaxStreams(MAX_STREAMS).build();
    }

    private void displayScores(){
        //highScoreTextView.setText(R.string.high_score);
        highScoreTextView.setText(resources.getString(R.string.high_score) + "" + highScore);
        currentScoreTexetView.setText(resources.getString(R.string.score) + "" + score);
        levelTextView.setText(resources.getString(R.string.level) + "" + level);
    }

    private Runnable addSpotRunnable = new Runnable() {
        @Override
        public void run() {
            addNewSpot();
        }
    };

    public void addNewSpot(){

        //Create the actual spot / circle
        final ImageView spot = (ImageView) layoutInflater.inflate(R.layout.untouched,null);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) spot.getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height  = metrics.heightPixels;

        // choose two random coordinates for the starting and ending point
        int x = random.nextInt(width - SPOT_DAIMETER);
        int y = random.nextInt(height  - SPOT_DAIMETER);
        int x2 = random.nextInt(width - SPOT_DAIMETER);
        int y2 = random.nextInt(height - SPOT_DAIMETER);


        spots.add(spot);
        // Diameter, diameter
        spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DAIMETER,SPOT_DAIMETER));

        // if random number between 0 nad 2 is equal 0 than we gonna make a green spot, if not true, red spot.
        spot.setImageResource(random.nextInt(2)== 0? R.drawable.green_spot : R.drawable.red_spot);
        spot.setX(x);// set spot's starting x location
        spot.setY(y); // set spot's starting y location

        spot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
               touchedSpot(spot);
            }
        });

        // adding circle to the screen
        relativeLayout.addView(spot);    }

    private void touchedSpot(ImageView spot) {
        //after touch remove spot view
        relativeLayout.removeView(spot);
        // after touch remove spot from spots queue
        spots.remove(spot);
        //level = 1;

        ++spotsTouched; //increment the number of spots touched
        score += 10 * level;

        //currentScoreTexetView.setText("Score:" + score);
    }
}
