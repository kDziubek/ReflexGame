package com.example.hp.reflexgame.view;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.hp.reflexgame.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ReflexView extends View {

    //Static instant variables
    private static final String HIGHSCORE = "HIGH_SCORE";
    private SharedPreferences preferences;

    // Varaibles that manage the game
    private int spotsTouched;
    private int score;
    private int level;
    private int viewWidth;
    private int viewHigh;
    private long animationTime;
    private boolean gameOver;
    private boolean gamePaused;
    private int highScore;
    private boolean dialogSiplayed;

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
    public static final int SPOT_DAIMETER = 200;
    public static final float SCALE_X = 0.25f;
    public static final float SCALE_Y = 0.25f;
    public static final int INITIAL_SPOTS = 5;
    public static final int SPOTS_DELAY = 500; //opoznienie
    public static final int Lives = 3;
    public static final int MAV_Lives = 7;
    public static final int NEW_LEVEL = 10;
    public static final int HIT_SOUND_ID = 1;
    public static final int MISS_SOUND_ID = 2;
    public static final int DISSAPEAR_SOUND_ID = 3;
    public static final int SOUND_PRIORITY = 1;
    public static final int SOUND_QUALITY = 100;
    public static final int MAX_STREAMS = 4;


    private SoundPool soundPool1;
    private int volume;
    private Map<Integer, Integer> soundMap;
    private Handler spotHandler;


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

        spotHandler = new Handler();



    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHigh = h;
    }

    public void pause(){
        gamePaused = true;
        soundPool1.release();
        soundPool1 = null;
        cancelAnimations();
    }

    public void resume(Context context){
        gamePaused = false;
        initializeSoundEffects(context);
        if(!dialogSiplayed)
            resetGame();
    }

    public void resetGame() {


        spots.clear(); // empty the List of spots
        animators.clear(); // empty the List of Animators
        livesLinearLayout.removeAllViews(); // clear old lives from screen

        animationTime = INITIAL_ANIMATION_DURATION; // init animation length
        spotsTouched = 0; // reset the number of spots touched
        score = 0; // reset the score
        level = 1; // reset the level
        gameOver = false; // the game is not over
        displayScores(); // display scores and level

        // add lives
        for (int i = 0; i < Lives; i++)
        {
            // add life indicator to screen
            livesLinearLayout.addView(
                    layoutInflater.inflate(R.layout.life, null));
        } // end for

        // add INITIAL_SPOTS new spots at SPOT_DELAY time intervals in ms
        for (int i = 1; i <= INITIAL_SPOTS; ++i)
            spotHandler.postDelayed(addSpotRunnable, i * SPOTS_DELAY);
    } // end method resetGame

    // create the app's SoundPool for playing game audio



    private void initializeSoundEffects(Context context) {
/*
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

       soundPool = new SoundPool.Builder();
        soundPool.setAudioAttributes(audioAttributes).setMaxStreams(MAX_STREAMS).build();
 */

        soundPool1 = new SoundPool(MAX_STREAMS,AudioManager.STREAM_MUSIC,SOUND_QUALITY);
        // set sound effect volume
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Create a sound map
        soundMap = new HashMap<>();
        soundMap.put(HIT_SOUND_ID, soundPool1.load(context, R.raw.hit, SOUND_PRIORITY));
        soundMap.put(MISS_SOUND_ID, soundPool1.load(context, R.raw.miss, SOUND_PRIORITY));
        soundMap.put(DISSAPEAR_SOUND_ID, soundPool1.load(context, R.raw.disappear, SOUND_PRIORITY));
    }

    private void displayScores() {
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

    //we'll just fire up the moment we touch on the screen.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (soundPool1 != null){
            soundPool1.play(HIT_SOUND_ID,volume,volume,1,0,1f);
        }
        score -=15 * level;
        score = Math.max(score,0); // do not let score go below zero
        displayScores();
        return true;
    }

    public void addNewSpot() {

        // choose two random coordinates for the starting and ending point
        int x = random.nextInt(viewWidth - SPOT_DAIMETER);
        int y = random.nextInt(viewHigh - SPOT_DAIMETER);
        int x2 = random.nextInt(viewWidth - SPOT_DAIMETER);
        int y2 = random.nextInt(viewHigh - SPOT_DAIMETER);

        //Create the actual spot / circle
        final ImageView spot = (ImageView) layoutInflater.inflate(R.layout.untouched, null);

//        DisplayMetrics metrics = new DisplayMetrics();
//        WindowManager windowManager = (WindowManager) spot.getContext().getSystemService(Context.WINDOW_SERVICE);
//        windowManager.getDefaultDisplay().getMetrics(metrics);
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;


        spots.add(spot);
        // Diameter, diameter
        spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DAIMETER, SPOT_DAIMETER));

        // if random number between 0 nad 2 is equal 0 than we gonna make a green spot, if not true, red spot.
        spot.setImageResource(random.nextInt(2) == 0 ? R.drawable.green_spot : R.drawable.red_spot);
        spot.setX(x);// set spot's starting x location
        spot.setY(y); // set spot's starting y location

        spot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                touchedSpot(spot);
            }
        });

        // adding circle to the screen
        relativeLayout.addView(spot);

        //add spot animation

        spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y)
                .setDuration(animationTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animators.add(animation); //save for later time
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animators.remove(animation);
                if (!gamePaused && spots.contains(spot)) { //not touched
                    missedSpot(spot);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animators.remove(); //remote when done.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }

    public void missedSpot(ImageView spot) {
        spots.remove(spot); // remove spot from spots List
        relativeLayout.removeView(spot); // remove spot from screen

        if (gameOver) // if the game is already over, exit
            return;

        // play the disappear sound effect
        if (soundPool1 != null)
            soundPool1.play(DISSAPEAR_SOUND_ID, volume, volume,
                    SOUND_PRIORITY, 0, 1f);


        // if the game has been lost
        if (livesLinearLayout.getChildCount() == 0) {
            gameOver = true; // the game is over

            // if the last game's score is greater than the high score
            if (score > highScore) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGHSCORE, score);
                editor.apply(); // store the new high score
                highScore = score;
            } // end if

            cancelAnimations();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Game Over");
            builder.setMessage("Score: " + score);
            builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    displayScores();
                    dialogSiplayed = false;
                    resetGame();

                }
            });
            dialogSiplayed = true;
            builder.show();

        } else {
            livesLinearLayout.removeViewAt(
                    livesLinearLayout.getChildCount() - 1
            );
            addNewSpot();
        }
    }



    private void cancelAnimations(){

        for (Animator animator : animators){
            animator.cancel();
        }

        //remove remaining spots from the scream
        for (ImageView view : spots){
            relativeLayout.removeView(view);
        }

        spotHandler.removeCallbacks(addSpotRunnable);
        animators.clear();
        spots.clear();

    }


    private void touchedSpot(ImageView spot) {
        //after touch remove spot view
        relativeLayout.removeView(spot);
        // after touch remove spot from spots queue
        spots.remove(spot);
        //level = 1;

        ++spotsTouched; //increment the number of spots touched
        score += 10 * level;

        if(spotsTouched % NEW_LEVEL==0) {
            ++level;
            animationTime *= 0.95; //make game 5% faster every level

            if (livesLinearLayout.getChildCount()<MAV_Lives){
                ImageView life = (ImageView) layoutInflater.inflate(R.layout.life,null);
                livesLinearLayout.addView(life);
            }
        }
        displayScores();

        if (!gameOver){
            addNewSpot();
        }
    }
}
