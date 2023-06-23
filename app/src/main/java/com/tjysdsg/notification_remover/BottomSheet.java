package com.tjysdsg.notification_remover;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class BottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "BottomSheet";

    private BottomSheetBehavior<FrameLayout> dialogBehavior;
    private BottomSheetBehavior<View> behavior;
    private BottomSheetDialog dialog;
    private Button enableButton;
    private View rootView;
    private OnClickListener enableButtonCallback;

    private boolean hide = true;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.service_permission_request_view, container, false);
        behavior = BottomSheetBehavior.from(rootView.findViewById(R.id.service_permission_bottom_sheet));
        dialog = (BottomSheetDialog) getDialog();
        assert dialog != null;
        dialogBehavior = dialog.getBehavior();
        enableButton = rootView.findViewById(R.id.enable_button);

        dialogBehavior.setDraggable(false);
        dialog.setCancelable(false);
        behavior.setDraggable(false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // make sure everything is initialized
        assert enableButton != null;
        enableButton.setOnClickListener((View v) -> {
            if (enableButtonCallback != null)
                enableButtonCallback.onClick(v);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateVisibility();
    }

    public void setOnClickListener(OnClickListener l) {
        enableButtonCallback = l;
    }

    public void hide(boolean hide) {
        this.hide = hide;

        if (behavior != null) {
            updateVisibility();
        }
    }

    private void updateVisibility() {
        assert behavior != null && dialogBehavior != null;

        if (hide) {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            dialog.hide();
        } else {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.show();
        }

    }

}