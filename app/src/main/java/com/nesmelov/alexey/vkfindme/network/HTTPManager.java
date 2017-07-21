package com.nesmelov.alexey.vkfindme.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.nesmelov.alexey.vkfindme.storage.Const;
import org.json.JSONArray;
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
    public static final int REQUEST_CHECK_USERS = 4;
    public static final int REQUEST_GET_USERS_POS = 5;

    private static final String SERVER_URL = "http://findmeapp-nesmelov.rhcloud.com/FindMe/Server";
    private static final String ADD_USER_ACTION_URL = "?action=add";
    private static final String SET_VISIBLE_ACTION_URL = "?action=set_visible";
    private static final String SET_POSITION_ACTION_URL = "?action=set_pos";
    private static final String CHECK_USERS_ACTION_URL = "?action=check";
    private static final String GET_USERS_POS_ACTION_URL = "?action=get_pos";

    private RequestQueue mQueue;

    private JsonRequest mAddUserRequest = null;
    private JsonRequest mSetVisibilityTrueRequest = null;
    private JsonRequest mSetVisibilityFalseRequest = null;
    private JsonRequest mSetPositionRequest = null;
    private JsonRequest mCheckUsersRequest = null;
    private JsonRequest mGetUsersPosRequest = null;

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

    public void asyncLoadBitmap(final String url, final ImageView imageView) {
        mImageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageView.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    public void asyncLoadBitmap(final String url, final ImageLoader.ImageListener listener) {
        mImageLoader.get(url, listener);
    }

    public synchronized void executeRequest(final int request, final int requestToCancel, final OnUpdateListener listener,
                                            final String... params) {
        cancelRequest(requestToCancel);
        switch (request) {
            case REQUEST_ADD_USER:
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Const.USER, params[0]);
                    addRequest(request, Request.Method.POST, listener, jsonObject);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_VISIBILITY_TRUE:
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Const.USER, params[0]);
                    jsonObject.put(Const.VISIBLE, true);
                    jsonObject.put(Const.LAT, params[1]);
                    jsonObject.put(Const.LON, params[2]);
                    addRequest(request, Request.Method.POST, listener, jsonObject);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_VISIBILITY_FALSE:
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Const.USER, params[0]);
                    jsonObject.put(Const.VISIBLE, false);
                    addRequest(request, Request.Method.POST, listener, jsonObject);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_SET_POSITION:
                try {
                    final JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Const.USER, params[0]);
                    jsonObject.put(Const.LAT, params[1]);
                    jsonObject.put(Const.LON, params[2]);
                    addRequest(request, Request.Method.POST, listener, jsonObject);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_CHECK_USERS:
                try {
                    final JSONObject json = new JSONObject();
                    final JSONArray users = new JSONArray();
                    final String[] usersString = params[0].split(";");
                    for (final String userString : usersString) {
                        if (!userString.isEmpty()) {
                            users.put(userString);
                        }
                    }
                    json.put(Const.USERS, users);
                    addRequest(request, Request.Method.POST, listener, json);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
            case REQUEST_GET_USERS_POS:
                try {
                    final JSONObject json = new JSONObject();
                    final JSONArray users = new JSONArray();
                    final String[] usersString = params[0].split(";");
                    for (final String userString : usersString) {
                        if (!userString.isEmpty()) {
                            users.put(userString);
                        }
                    }
                    json.put(Const.USERS, users);
                    addRequest(request, Request.Method.POST, listener, json);
                } catch (Exception e) {
                    listener.onError(request, PARSE_ERROR_CODE);
                }
                break;
        }
    }

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
            case REQUEST_CHECK_USERS:
                if (mCheckUsersRequest != null) {
                    mCheckUsersRequest.cancel();
                }
                break;
            case REQUEST_GET_USERS_POS:
                if (mGetUsersPosRequest != null) {
                    mGetUsersPosRequest.cancel();
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
                case REQUEST_CHECK_USERS: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + CHECK_USERS_ACTION_URL, data, responseListener, errorListener);
                    mCheckUsersRequest = jsonRequest;
                    mQueue.add(mCheckUsersRequest);
                    break;
                }
                case REQUEST_GET_USERS_POS: {
                    final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                            method, SERVER_URL + GET_USERS_POS_ACTION_URL, data, responseListener, errorListener);
                    mGetUsersPosRequest = jsonRequest;
                    mQueue.add(mGetUsersPosRequest);
                    break;
                }
                default:
                    break;
            }
        }
    }
}
