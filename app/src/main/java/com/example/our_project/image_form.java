package com.example.our_project;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;
import androidx.gridlayout.widget.GridLayout;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class image_form extends Fragment {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private GridLayout imageGridLayout;
    private List<Uri> pendingImageUris;
    private  Uri photoUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_input, container, false);

        ImageView upload_image = view.findViewById(R.id.upload_image);

        ImageView camera = view.findViewById(R.id.camera);
        imageGridLayout = view.findViewById(R.id.image_view_grid);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            pendingImageUris = new ArrayList<>();
                            if (data.getClipData() != null) {
                                int count = Math.min(data.getClipData().getItemCount(), 5);
                                for (int i = 0; i < count; i++) {
                                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                    pendingImageUris.add(imageUri);
                                }
                            } else if (data.getData() != null) {
                                pendingImageUris.add(data.getData());
                            }
                            addImagesWhenLayoutReady();
                        }
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        if (pendingImageUris == null) {
                            pendingImageUris = new ArrayList<>();
                        }
                        pendingImageUris.add(photoUri);
                        addImagesWhenLayoutReady();
                    }
                }
        );

        upload_image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select up to 5 images"));
        });

        camera.setOnClickListener(v->{
            File photoFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "IMG_" + System.currentTimeMillis() + ".jpg");
            photoUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", photoFile);

            takePictureLauncher.launch(photoUri);
        });

        Button uploadButton = view.findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> {
            if (pendingImageUris != null && !pendingImageUris.isEmpty()) {
                Intent intent = new Intent(requireContext(), UploadService.class);
                intent.putParcelableArrayListExtra(UploadService.EXTRA_IMAGE_URIS, new ArrayList<>(pendingImageUris));
                requireContext().startService(intent);
            }
        });

        return view;
    }

    private final BroadcastReceiver uploadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra("success", false);
            if (success) {
                Toast.makeText(requireContext(), "Images successfully uploaded", Toast.LENGTH_SHORT).show();
                // Clear the grid layout and pending image list
                pendingImageUris = new ArrayList<>();
                imageGridLayout.removeAllViews();
            } else {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(uploadReceiver,new IntentFilter("UPLOAD_COMPLETE"), Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(uploadReceiver, new IntentFilter("UPLOAD_COMPLETE"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(uploadReceiver);
    }

    private void addImagesWhenLayoutReady() {
        if (imageGridLayout.getWidth() > 0) {
            // GridLayout is ready, add images now
            addImagesToGrid(pendingImageUris);
        } else {
            // Wait for layout
            imageGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (imageGridLayout.getWidth() > 0) {
                        imageGridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        addImagesToGrid(pendingImageUris);
                    }
                }
            });
        }
    }

    private void addImagesToGrid(List<Uri> imageUris) {
        imageGridLayout.removeAllViews();

        int columns = 2;
        int marginDp = 8;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int marginPx = (int) (marginDp * displayMetrics.density + 0.5f);
        int gridWidthPx = imageGridLayout.getWidth();
        int totalHorizontalMargins = marginPx * (columns + 1);
        int imageWidthPx = (gridWidthPx - totalHorizontalMargins) / columns;
        int imageCount = 0;
        for (Uri imageUri : imageUris) {
            ImageView imageView = new ImageView(requireContext());

            int row = imageCount / columns;
            int col = imageCount % columns;

            GridLayout.Spec rowSpec = GridLayout.spec(row, 1);
            GridLayout.Spec colSpec = GridLayout.spec(col, 1);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
            params.width = imageWidthPx;
            params.height = imageWidthPx;
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(imageUri).centerCrop().into(imageView);
            imageGridLayout.addView(imageView);
            imageCount++;
        }
    }

}