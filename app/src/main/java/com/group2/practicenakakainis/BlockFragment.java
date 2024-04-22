package com.group2.practicenakakainis;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;


public class BlockFragment extends Fragment {
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_block, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button button = view.findViewById(R.id.button_blk);
        MediaPlayer buttonSound = MediaPlayer.create(getActivity(), R.raw.button_sound1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();

                if (AccessibilityUtils.isAccessibilityServiceRunning(getActivity(), MyAccessibilityService.class)) {
                    blockListView(v);
                    buttonSound.start();
                }else{
                    checkAccessibility();
                    buttonSound.start();
                }
            }

            public void blockListView(View v) {
                Intent intent = new Intent(getActivity(), AppListView.class);
                startActivity(intent);
            }

            public void checkAccessibility() {
                Toast.makeText(getActivity(), "Accessibility Service is not enabled", Toast.LENGTH_SHORT).show();

                // Create the dialog
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_accessibility_settings);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                dialog.setCancelable(false);


                // Get the OK button from the layout and set a click listener
                Button okButton = dialog.findViewById(R.id.ok_button);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Open the accessibility settings
                        AccessibilityUtils.openAccessibilitySettings(getActivity());
                        dialog.dismiss();
                    }
                });

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(layoutParams);

                dialog.show();
            }


            public void showProgressDialog() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
