package org.nrobert.joomlaconnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MyCookieStore implements CookieStore, Runnable {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_COOKIES = "cookies";
    private String domain;
    private URI uri;
    private CookieStore cookieStore;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String storedCookieString;

    public MyCookieStore(Context context, URI uri) {
        // get the default in memory cookie cookieStore
        this.uri = uri;
        this.domain = "." + uri.getHost();
        cookieStore = new CookieManager().getCookieStore();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        storedCookieString = sharedPreferences.getString(KEY_COOKIES, null);

        if (storedCookieString != null && !storedCookieString.isEmpty()) {

            List<HttpCookie> storedCookies = HttpCookie.parse(storedCookieString);
            List<HttpCookie> cookies = get(uri);

            for (HttpCookie cookie : storedCookies) {
                if (!cookies.contains(cookie)) {
                    add(uri, cookie);
                }
            }
        }

        // add a shutdown hook to write out the in memory cookies
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    public void run() {
        List<String> cookieArray = new ArrayList<>();
        List<HttpCookie> cookies = get(uri);

        if (!cookies.isEmpty()) {
            for (HttpCookie cookie : cookies) {
                cookieArray.add(cookie.toString() + "; domain=" + domain + "; path=/; HttpOnly");
            }
        }

        if (!cookieArray.isEmpty()) {
            editor.putString(KEY_COOKIES, TextUtils.join(",", cookieArray));
            editor.commit();
        }
    }

    public void add(URI uri, HttpCookie cookie) {
        cookieStore.add(uri, cookie);
    }

    public List<HttpCookie> get(URI uri) {
        return cookieStore.get(uri);
    }

    public List<HttpCookie> getCookies() {
        return cookieStore.getCookies();
    }

    public List<URI> getURIs() {
        return cookieStore.getURIs();
    }

    public boolean remove(URI uri, HttpCookie cookie) {
        return cookieStore.remove(uri, cookie);
    }

    public boolean removeAll() {
        return cookieStore.removeAll();
    }
}