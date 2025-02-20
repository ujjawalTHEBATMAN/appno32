package com.example.examtimetablemanagement;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.examtimetablemanagement.authenTication.login.loginActivity;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private View particlesView;
    private View logoContainer;
    private TextView appTitle;
    private CircularProgressIndicator loadingIndicator;
    private TextView versionText;
    private Random random = new Random();
    private AnimatorSet particleAnimator;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get screen dimensions
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Initialize views
        initializeViews();

        // Post layout to ensure views are measured
        new Handler().postDelayed(() -> {
            try {
                startSplashAnimations();
                animateBackground();
                createParticleAnimation();
            } catch (Exception e) {
                Toast.makeText(this, "Animation Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, 100);

        // Handle navigation after animations
        new Handler().postDelayed(this::navigateToMain, 3500);
    }

    private void initializeViews() {
        particlesView = findViewById(R.id.particlesView);
        logoContainer = findViewById(R.id.logoContainer);
        appTitle = findViewById(R.id.appTitle);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        versionText = findViewById(R.id.versionText);

        // Initially hide views for animations
        logoContainer.setScaleX(0);
        logoContainer.setScaleY(0);
        appTitle.setAlpha(0);
        versionText.setAlpha(0);
        loadingIndicator.setAlpha(0);
    }

    private void startSplashAnimations() {
        // Logo reveal animation
        int centerX = logoContainer.getWidth() / 2;
        int centerY = logoContainer.getHeight() / 2;
        float finalRadius = (float) Math.hypot(centerX, centerY);

        Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                logoContainer, centerX, centerY, 0, finalRadius);
        circularReveal.setDuration(1000);
        circularReveal.setInterpolator(new AccelerateDecelerateInterpolator());

        // Scale animation for logo
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0f, 1.2f, 1f);
        scaleX.setDuration(1200);
        scaleY.setDuration(1200);
        scaleX.setInterpolator(new AnticipateOvershootInterpolator());
        scaleY.setInterpolator(new AnticipateOvershootInterpolator());

        // Fade in animations
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f);
        titleFade.setDuration(800);
        titleFade.setStartDelay(500);

        ObjectAnimator loadingFade = ObjectAnimator.ofFloat(loadingIndicator, "alpha", 0f, 1f);
        loadingFade.setDuration(500);
        loadingFade.setStartDelay(800);

        ObjectAnimator versionFade = ObjectAnimator.ofFloat(versionText, "alpha", 0f, 1f);
        versionFade.setDuration(500);
        versionFade.setStartDelay(1000);

        // Play animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(circularReveal, scaleX, scaleY, titleFade, loadingFade, versionFade);
        animatorSet.start();
    }

    private void animateBackground() {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] {
                        ContextCompat.getColor(this, R.color.gradientStart),
                        ContextCompat.getColor(this, R.color.gradientMiddle),
                        ContextCompat.getColor(this, R.color.gradientEnd)
                }
        );
        getWindow().setBackgroundDrawable(gradient);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            int[] newColors = new int[3];
            for (int i = 0; i < 3; i++) {
                newColors[i] = evaluateColor(fraction,
                        ContextCompat.getColor(this, R.color.gradientStart),
                        ContextCompat.getColor(this, R.color.gradientEnd));
            }
            gradient.setColors(newColors);
        });
        animator.start();
    }

    private int evaluateColor(float fraction, int startColor, int endColor) {
        float[] startHsv = new float[3];
        float[] endHsv = new float[3];
        float[] outHsv = new float[3];

        Color.colorToHSV(startColor, startHsv);
        Color.colorToHSV(endColor, endHsv);

        for (int i = 0; i < 3; i++) {
            outHsv[i] = startHsv[i] + (endHsv[i] - startHsv[i]) * fraction;
        }

        return Color.HSVToColor(outHsv);
    }

    private void createParticleAnimation() {
        particleAnimator = new AnimatorSet();
        for (int i = 0; i < 20; i++) {
            View particle = new View(this);
            particle.setBackgroundResource(R.drawable.particle_background);
            addContentView(particle, new android.view.ViewGroup.LayoutParams(10, 10));

            ObjectAnimator moveX = ObjectAnimator.ofFloat(particle, "translationX",
                    random.nextFloat() * screenWidth);
            ObjectAnimator moveY = ObjectAnimator.ofFloat(particle, "translationY",
                    random.nextFloat() * screenHeight);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(particle, "alpha",
                    0f, 0.8f, 0f);

            AnimatorSet particleSet = new AnimatorSet();
            particleSet.playTogether(moveX, moveY, alpha);
            particleSet.setDuration(2000 + random.nextInt(1000));
            particleSet.setStartDelay(random.nextInt(1000));
            particleSet.setInterpolator(new AccelerateDecelerateInterpolator());
            particleAnimator.playTogether(particleSet);
        }
        particleAnimator.start();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (particleAnimator != null) {
            particleAnimator.cancel();
        }
    }
}