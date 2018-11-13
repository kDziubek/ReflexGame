package com.example.hp.reflexgame.view;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    public ReflexView(Context context, SharedPreferences sharedPreferences, RelativeLayout parentLayout) {
        super(context);
    }


}
