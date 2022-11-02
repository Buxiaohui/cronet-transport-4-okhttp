package com.bxh.cronet4okhttp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetProvider;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CronetEngine cronetEngine = null;
        for (CronetProvider provider : CronetProvider.getAllProviders(this)) {
            // We're not interested in using the fallback, we're better off sticking with
            // the
            // default OkHttp client in that case.
            if (!provider.isEnabled()
                    || provider.getName().equals(CronetProvider.PROVIDER_NAME_FALLBACK)) {
                continue;
            }
            try {
                cronetEngine = setupCronetEngine(provider.createBuilder()).build();

            } catch (Exception e) {
                Log.e(TAG, "decorateAsync,task-1,e:" + e);
            }
        }
        Log.d(TAG, "decorateAsync,task-1,newEngine:" + cronetEngine);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (cronetEngine != null) {
            CronetInterceptor cronetInterceptor = CronetInterceptor.newBuilder(cronetEngine).build();
            builder.addInterceptor(cronetInterceptor);
        }
        okHttpClient = builder.build();
        Call call = okHttpClient.newCall(new Request.Builder().url("https://www.baidu.com").build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure,e:"+ e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"onResponse,headers:"+ response.headers());
            }
        });


    }


    private CronetEngine.Builder setupCronetEngine(CronetEngine.Builder engineBuilder) {
        File file = new File(this.getApplication().getCacheDir(), "cronet");

        boolean isEnableQuic = false;
        boolean enableBrotli = false;
        engineBuilder
                .enableBrotli(enableBrotli)
                .enableQuic(isEnableQuic)
                .enableHttp2(true);
        int cacheSize = 1;
        int cacheMode = CronetEngine.Builder.HTTP_CACHE_DISK;
        if (cacheSize > 0 && (cacheMode >= CronetEngine.Builder.HTTP_CACHE_DISABLED && cacheMode <= CronetEngine.Builder.HTTP_CACHE_DISK)) {
            engineBuilder.enableHttpCache(cacheMode, cacheSize * 1024 * 1024)
                    .setStoragePath(file.getAbsolutePath()); // 缓存目录
        }
        return engineBuilder;
    }
}