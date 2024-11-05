package com.example.prm392_project.services;

import android.app.Activity;

public class NavigationService {
    public static void navigateBack(Activity activity) {
        activity.finish(); // Closes the current activity and returns to the previous one.
    }
}
