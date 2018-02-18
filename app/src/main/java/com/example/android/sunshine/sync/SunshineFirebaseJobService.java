/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.sync;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.sunshine.models.ForecastResult;
import com.example.android.sunshine.models.ForecastRequest;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SunshineFirebaseJobService extends JobService {

    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d("Service", "StartFromService");
        Context context = getApplicationContext();
        RestApiWeather.getInstance().getForecast(new ForecastRequest(context)).enqueue(new Callback<ForecastResult>() {
            @Override
            public void onResponse(Call<ForecastResult> call, Response<ForecastResult> response) {
                mFetchWeatherTask = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Log.d("SUC", "SUC: " + response.body().getCurrently().getSummary());
                        SunshineSyncTask.syncWeather(context,response.body());
                        jobFinished(jobParameters, false);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        jobFinished(jobParameters, false);
                    }
                };
                mFetchWeatherTask.execute();
            }

            @Override
            public void onFailure(Call<ForecastResult> call, Throwable t) {
                jobFinished(jobParameters, false);
                Log.d("FAIL", "Failure: " + t.getMessage());
            }
        });

        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mFetchWeatherTask != null) {
            mFetchWeatherTask.cancel(true);
        }
        return true;
    }


}