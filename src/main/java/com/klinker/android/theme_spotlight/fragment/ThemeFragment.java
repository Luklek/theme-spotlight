/*
 * Copyright (C) 2014 Klinker Apps, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.theme_spotlight.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market;
import com.klinker.android.theme_spotlight.R;

public class ThemeFragment extends AuthFragment {

    private static final String TAG = "ThemeFragment";
    public static final String ARG_PACKAGE_NAME = "package_name";

    private Handler mHandler;

    private View mLayout;
    private String mPackageName;

    public static ThemeFragment newInstance(String packageName) {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PACKAGE_NAME, packageName);
        fragment.setArguments(args);
        return fragment;
    }

    public ThemeFragment() {
        // all fragments should always have a default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        // get the package name that we want to load in the fragment
        mPackageName = getArguments().getString(ARG_PACKAGE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_theme, null);
        loadApp(mPackageName);
        return mLayout;
    }

    // load the app by creating a new session and requesting by the package name. we can then load
    // just one result and get what we are looking for
    public void loadApp(final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // create our session to look at themes from
                    MarketSession session = new MarketSession();
                    session.getContext().setAuthSubToken(getAuthActivity().getAuthToken().getAuthToken());
                    session.getContext().setAndroidId(getAuthActivity().getAuthToken().getAndroidId());

                    // create a simple query
                    String query = getPackageQuery(packageName);
                    Market.AppsRequest appsRequest = Market.AppsRequest.newBuilder()
                            .setQuery(query)
                            .setStartIndex(0)
                            .setEntriesCount(1)
                            .setWithExtendedInfo(true)
                            .build();

                    // post our request
                    session.append(appsRequest, new MarketSession.Callback<Market.AppsResponse>() {
                        @Override
                        public void onResult(Market.ResponseContext context, Market.AppsResponse response) {
                            final Market.App app = response.getAppList().get(0);

                            // post back to the ui thread to update the view
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setApp(app);
                                }
                            });
                        }
                    });
                    session.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // set up the view we want to show finally, need to post back to the ui thread
    // since we queried on a separate thread
    public void setApp(Market.App app) {
        // TODO
        Log.v(TAG, app.toString());
    }

    public String getPackageQuery(String packageName) {
        return "pname:" + packageName;
    }
}