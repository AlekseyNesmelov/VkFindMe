package com.nesmelov.alexey.vkfindme.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.nesmelov.alexey.vkfindme.utils.Utils;

import org.json.JSONObject;

public class HTTPManager {
    public static final int CACHE_SIZE = 10;
    public static final int PARSE_ERROR_CODE = 0;
    public static final int SERVER_ERROR_CODE = 1;

    public static final int REQUEST_IDLE = -1;
    public static final int REQUEST_ADD_USER = 0;
    public static final int REQUEST_SET_VISIBILITY_TRUE = 1;
    public static final int REQUEST_SET_VISIBILITY_FALSE = 2;
    public static final int REQUEST_SET_POSITION = 3;

    private static final String SERVER_URL = "http://findmeapp-nesmelov.rhcloud.com/FindMe/Server";
    private static final String ADD_USER_ACTION_URL = "?action=add";
    private static final String SET_VISIBLE_ACTION_URL = "?action=set_visible";
    private static final String SET_POSITION_ACTION_URL = "?action=set_pos";
    private RequestQueue mQueue;

    private JsonRequest mAddUserRequest = null;
    private JsonRequest mSetVisibilityTrueRequest = null;
    private JsonRequest mSetVisibilityFalseRequest = null;
    private JsonRequest mSetPositionRequest = null;

    private ImageLoader mImageLoader;

    public HTTPManager(final Context context) {
        mQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(CACHE_SIZE);
            @Override
            public void putBitmap(final String url, final Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            @Override
            public Bitmap getBitmap(final String url) {
                return mCache.get(url);
            }
        });
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public synchronized void executeRequest(final int request, final int requestToCancel, final OnUpdateListener listener,
                                            final String... params) {
        cancelRequest(requestToCancel);
        switch (request) {
            case REQUEST_ADD_USER:
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("{\"user\": ").append(params[0]).append("}");
                    addRequest(request, Request.Method.POST, listener, new JSONObject(sb.toString()));
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_VISIBILITY_TRUE:
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("{\"user\": ").append(params[0]).append(", \"visible\": true, \"lat\": ")
                            .append(params[1]).append(", \"lon\": ").append(params[2]).append("}");
                    addRequest(request, Request.Method.POST, listener, new JSONObject(sb.toString()));
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_VISIBILITY_FALSE:
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("{\"user\": ").append(params[0]).append(", \"visible\": false}");
                    addRequest(request, Request.Method.POST, listener, new JSONObject(sb.toString()));
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_POSITION:
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("{\"user\": ").append(params[0]).append(", \"lat\": ").append(params[1])
                            .append(", \"lon\": ").append(params[2]).append("}");
                    addRequest(request, Request.Method.POST, listener, new JSONObject(sb.toString()));
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
        }
    }

   /* private class CheckUsersThread extends Thread {

        private List<String> mUsersToCheck;
        private List<String> mCheckedUsers = null;
        private boolean mResultFound = false;

        public CheckUsersThread(List<String> usersToCheck) {
            mUsersToCheck = usersToCheck;
        }

        @Override
        public void run() {
            mCheckedUsers = checkFriends();
        }

        public List<String> checkFriends() {
            List<String> result = new ArrayList<>();
            if (mUsersToCheck.size() > 0) {
                HttpURLConnection urlConnection = null;
                try {
                    String urlStr =;
                    for (final String id : mUsersToCheck) {
                        urlStr += id + ":";
                    }
                    urlStr = urlStr.substring(0, urlStr.length() - 1);
                    final URL url = new URL(urlStr);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    final BufferedReader r = new BufferedReader(new InputStreamReader(in));
                    final StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    final int start = total.indexOf("<body>") + "<body>".length();
                    final int end = total.indexOf("</body>", start);
                    final String body = total.substring(start, end).replace("\n", "");
                    final String[] users = body.split(":");
                    for (final String user : users) {
                        result.add(user);
                    }
                    mResultFound = true;
                } catch (Exception e) {
                    result = null;
                } finally {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        public boolean resultWasFound() {
            return mResultFound;
        }

        public List<String> getResult() {
            return mCheckedUsers;
        }
    }

    private class AddUserThread extends Thread {

        private String mUser;
        private boolean mResultFound = false;
        private boolean mResult = false;

        public AddUserThread(String user) {
            mUser = user;
        }

        @Override
        public void run() {
            mResult = addUser();
        }

        public boolean resultWasFound() {
            return mResultFound;
        }

        public boolean getResult() {
            return mResult;
        }

        private boolean addUser() {
            HttpURLConnection urlConnection = null;
            try {
                final String urlStr = "http://findmeapp-nesmelov.rhcloud.com/FindMe/Server?action=add&user=" + mUser;
                final URL url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader r = new BufferedReader(new InputStreamReader(in));
                final StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                final int start = total.indexOf("<body>") + "<body>".length();
                final int end = total.indexOf("</body>", start);
                final String body = total.substring(start, end).replace("\n", "");
                mResult = body == "true";
                mResultFound = true;
            } catch (Exception e) {
            } finally {
                urlConnection.disconnect();
            }
            return mResult;
        }
    }

    private class SendPosThread extends Thread {
        private Location mPosition;
        private String mUser;
        private boolean mResultFound = false;
        private boolean mResult = false;

        public SendPosThread(final String user, final Location position) {
            mUser = user;
            mPosition = position;
        }

        @Override
        public void run() {
            mResult = sendPos();
        }

        public boolean sendPos() {
            HttpURLConnection urlConnection = null;
            try {
                final String urlStr = "http://findmeapp-nesmelov.rhcloud.com/FindMe/Server?action=set_pos&user=" + mUser +
                        "&lat=" + mPosition.getLatitude() + "&lon=" + mPosition.getLongitude();
                final URL url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader r = new BufferedReader(new InputStreamReader(in));
                final StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                final int start = total.indexOf("<body>") + "<body>".length();
                final int end = total.indexOf("</body>", start);
                final String body = total.substring(start, end).replace("\n", "");
                mResultFound = true;
                return  body == "true";
            } catch (Exception e) {
            } finally {
                urlConnection.disconnect();
            }
            return false;
        }

        public boolean resultWasFound() {
            return mResultFound;
        }

        public boolean getResult() {
            return mResult;
        }
    }

    private class SendVisibleThread extends Thread {
        private boolean mVisible;
        private String mUser;
        private boolean mResultFound = false;
        private boolean mResult = false;

        public SendVisibleThread(final String user, final boolean visible) {
            mUser = user;
            mVisible = visible;
        }

        @Override
        public void run() {
            mResult = sendVisible();
        }

        public boolean sendVisible() {
            HttpURLConnection urlConnection = null;
            try {
                final String urlStr = "http://findmeapp-nesmelov.rhcloud.com/FindMe/Server?action=set_visible&user="
                        + mUser + "&visible=" + mVisible;
                final URL url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader r = new BufferedReader(new InputStreamReader(in));
                final StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                final int start = total.indexOf("<body>") + "<body>".length();
                final int end = total.indexOf("</body>", start);
                final String body = total.substring(start, end).replace("\n", "");
                mResultFound = true;
                return  body == "true";
            } catch (Exception ee) {
            } finally {
                urlConnection.disconnect();
            }
            return false;
        }

        public boolean resultWasFound() {
            return mResultFound;
        }

        public boolean getResult() {
            return mResult;
        }
    }*/

    private void cancelRequest(final int requestToCancel) {
        switch (requestToCancel) {
            case REQUEST_ADD_USER:
                if (mAddUserRequest != null) {
                    mAddUserRequest.cancel();
                }
                break;
            case REQUEST_SET_VISIBILITY_TRUE:
                if (mSetVisibilityTrueRequest != null) {
                    mSetVisibilityTrueRequest.cancel();
                }
                break;
            case REQUEST_SET_VISIBILITY_FALSE:
                if (mSetVisibilityFalseRequest != null) {
                    mSetVisibilityFalseRequest.cancel();
                }
                break;
            case REQUEST_SET_POSITION:
                if (mSetPositionRequest != null) {
                    mSetPositionRequest.cancel();
                }
                break;
        }
    }

    private void addRequest(final int request, final int method, final OnUpdateListener listener, final JSONObject data) {
        if (request != REQUEST_IDLE) {
            final Response.Listener responseListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(final JSONObject response) {
                    listener.onUpdate(request, response);
                }
            };

            final Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onError(request, SERVER_ERROR_CODE);
                }
            };

            switch (request) {
                case REQUEST_ADD_USER: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + ADD_USER_ACTION_URL, data, responseListener, errorListener);
                    mAddUserRequest = jsonRequest;
                    mQueue.add(mAddUserRequest);
                    break;
                }
                case REQUEST_SET_VISIBILITY_TRUE: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + SET_VISIBLE_ACTION_URL, data, responseListener, errorListener);
                    mSetVisibilityTrueRequest = jsonRequest;
                    mQueue.add(mSetVisibilityTrueRequest);
                    break;
                }
                case REQUEST_SET_VISIBILITY_FALSE: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + SET_VISIBLE_ACTION_URL, data, responseListener, errorListener);
                    mSetVisibilityFalseRequest = jsonRequest;
                    mQueue.add(mSetVisibilityFalseRequest);
                    break;
                }
                case REQUEST_SET_POSITION: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + SET_POSITION_ACTION_URL, data, responseListener, errorListener);
                    mSetPositionRequest = jsonRequest;
                    mQueue.add(mSetPositionRequest);
                    break;
                }
                default:
                    break;
            }
        }
    }
}
