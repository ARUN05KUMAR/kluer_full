package com.example.our_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText editName, editEmail, editGender, editAge, editOccupation, editCity;
    private Button editButton;
    private boolean isEditing = false;

    private SharedPreferences sharedPreferences;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        editName = view.findViewById(R.id.edit_name);
        editEmail = view.findViewById(R.id.edit_email);
        editGender = view.findViewById(R.id.edit_gender);
        editAge = view.findViewById(R.id.edit_age);
        editOccupation = view.findViewById(R.id.edit_occupation);
        editCity = view.findViewById(R.id.edit_city);
        editButton = view.findViewById(R.id.edit_button);

        sharedPreferences = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);

        loadUserData();

        profileImage.setOnClickListener(v -> {
            if (isEditing) {
                pickImageFromGallery();
            }
        });

        editButton.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                enableEditing(true);
                editButton.setText("Save");
            } else {
                saveUserData();
                isEditing = false;
                enableEditing(false);
                editButton.setText("Edit");
                Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                saveProfileImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfileImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        String encodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileImage", encodedImage);
        editor.apply();
    }

    private void loadProfileImage() {
        String encodedImage = sharedPreferences.getString("profileImage", null);
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            profileImage.setImageBitmap(bitmap);
        }
    }

    private void loadUserData() {
        editName.setText(sharedPreferences.getString("username", ""));
        editEmail.setText(sharedPreferences.getString("email", ""));
        editGender.setText(sharedPreferences.getString("gender", ""));
        editAge.setText(sharedPreferences.getString("age", ""));
        editOccupation.setText(sharedPreferences.getString("occupation", ""));
        editCity.setText(sharedPreferences.getString("city", ""));

        loadProfileImage();
        enableEditing(false);
    }

    private void saveUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", editName.getText().toString().trim());
        editor.putString("email", editEmail.getText().toString().trim());
        editor.putString("gender", editGender.getText().toString().trim());
        editor.putString("age", editAge.getText().toString().trim());
        editor.putString("occupation", editOccupation.getText().toString().trim());
        editor.putString("city", editCity.getText().toString().trim());
        editor.apply();
    }

    private void enableEditing(boolean enabled) {
        editName.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        editGender.setEnabled(enabled);
        editAge.setEnabled(enabled);
        editOccupation.setEnabled(enabled);
        editCity.setEnabled(enabled);

        int inputType = enabled ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL;
        editName.setInputType(inputType);
        editEmail.setInputType(enabled ? InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS : InputType.TYPE_NULL);
        editGender.setInputType(inputType);
        editAge.setInputType(enabled ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL);
        editOccupation.setInputType(inputType);
        editCity.setInputType(inputType);
    }
}
