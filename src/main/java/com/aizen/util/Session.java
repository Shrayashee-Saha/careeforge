package com.aizen.util;

import com.aizen.model.Resume;
import com.aizen.model.User;

import java.util.prefs.Preferences;

/** In-memory session state plus a persisted "last username" preference. */
public final class Session {
    private static final Preferences PREFS = Preferences.userNodeForPackage(Session.class);
    private static final String KEY_LAST_USER = "lastUsername";

    private static User currentUser;
    private static Resume resumeToEdit;
    private static Resume resumeToPreview;

    private Session() {
    }

    public static void login(User user) {
        currentUser = user;
        PREFS.put(KEY_LAST_USER, user.getUsername());
    }

    public static void logout() {
        currentUser = null;
        resumeToEdit = null;
        resumeToPreview = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getLastUsername() {
        return PREFS.get(KEY_LAST_USER, "");
    }

    public static Resume getResumeToEdit() {
        return resumeToEdit;
    }

    public static void setResumeToEdit(Resume resume) {
        resumeToEdit = resume;
    }

    public static Resume getResumeToPreview() {
        return resumeToPreview;
    }

    public static void setResumeToPreview(Resume resume) {
        resumeToPreview = resume;
    }
}
