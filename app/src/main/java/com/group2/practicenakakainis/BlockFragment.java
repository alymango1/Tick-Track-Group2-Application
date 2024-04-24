package com.group2.practicenakakainis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class BlockFragment extends Fragment {
    private ProgressDialog progressDialog;
    private MediaPlayer buttonSound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_block, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button button = view.findViewById(R.id.button_blk);
        buttonSound = MediaPlayer.create(getActivity(), R.raw.button_sound1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(buttonSound);
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                if (AccessibilityUtils.isAccessibilityServiceRunning(getActivity(), MyAccessibilityService.class)) {
                    if (!MainActivity.isPickerShown) {
                        blockListView(v);
                    }
                } else if (!MainActivity.isPickerShown) {
                    MainActivity.isPickerShown = true;
                    checkAccessibility();
                }
            }

            public void blockListView(View v) {
                Intent intent = new Intent(getActivity(), AppListView.class);
                startActivity(intent);
                progressDialog.dismiss();
            }

            public void checkAccessibility() {
                Toast.makeText(getActivity(), "Accessibility Service is not enabled", Toast.LENGTH_SHORT).show();

                // Inflate the layout
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_accessibility_settings, null);

                // Set the maximum height of the ScrollView
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ScrollView scrollView = view.findViewById(R.id.scrollability);
                scrollView.getLayoutParams().height = 1000; // Or whatever max height you want

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(view);
                builder.setCancelable(false);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open the accessibility settings
                        MainActivity.isPickerShown = false;
                        AccessibilityUtils.openAccessibilitySettings(getActivity());
                        playSound(buttonSound);
                        dialog.dismiss();
                        progressDialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();

                // Set the background drawable resource for rounded corners
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

                dialog.show();
            }
        });
    }

    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (buttonSound != null) {
            buttonSound.release();
            buttonSound = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (buttonSound == null) {
            buttonSound = MediaPlayer.create(getActivity(), R.raw.button_sound1);
        }
    }
}

