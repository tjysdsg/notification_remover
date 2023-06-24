package com.tjysdsg.notification_remover;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.app.Instrumentation;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.UiAutomation;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class HighLevelTests {
    private static final String CHANNEL_ID = "Fake notification channel";
    private static final String NOTIFICATION_TITLE = "Fake notification title";

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }
        };
    }

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    public Context ctx() {
        return ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testSpotNewNotifications() {
        var activityScenario = activityRule.getScenario();
        activityScenario.moveToState(Lifecycle.State.RESUMED);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // create a notification
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText("Much longer text that cannot fit one line...")
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx());
        if (!notificationManager.areNotificationsEnabled()) {
            throw new RuntimeException("Needs notification permission to test properly");
        }
        notificationManager.notify(100, builder.build());

        // check if the app recognizes the notification
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(isRoot()).perform(waitFor(5000));

        activityScenario.moveToState(Lifecycle.State.DESTROYED);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Fake notification channel description";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = ctx().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}