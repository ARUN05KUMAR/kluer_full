package com.example.our_project;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageVolume;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.UUID;

public class MemoryUsageFragment extends Fragment {

    private ProgressBar progressAppData, progressCache, progressOther;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory_usage, container, false);

        progressAppData = view.findViewById(R.id.progress_app_data);
        progressCache = view.findViewById(R.id.progress_cache);
        progressOther = view.findViewById(R.id.progress_other);

        updateMemoryInfo();

        return view;
    }

    private void updateMemoryInfo() {
        Context context = getContext();
        if (context == null) return;

        File appDir = context.getFilesDir();
        File cacheDir = context.getCacheDir();

        long appSize = getFolderSize(appDir);
        long cacheSize = getFolderSize(cacheDir);
        long total = appSize + cacheSize;

        // Add fake "other" usage for visualization
        long otherSize = total / 3;

        long fullTotal = appSize + cacheSize + otherSize;

        int appPercent = (int) (appSize * 100 / fullTotal);
        int cachePercent = (int) (cacheSize * 100 / fullTotal);
        int otherPercent = (int) (otherSize * 100 / fullTotal);

        progressAppData.setProgress(appPercent);
        progressCache.setProgress(cachePercent);
        progressOther.setProgress(otherPercent);
    }

    private long getFolderSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file);
                }
            }
        }
        return size;
    }
}
