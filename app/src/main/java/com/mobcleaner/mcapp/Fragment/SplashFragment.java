package com.mobcleaner.mcapp.Fragment;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mobcleaner.mcapp.Activity.AntivirusActivity;
import com.mobcleaner.mcapp.R;

public class SplashFragment extends Fragment {

    Context context;
    ImageView back_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_splash, container, false);

        ((AntivirusActivity)getActivity()).toolbar.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        back_btn = view.findViewById(R.id.backBtn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous screen
                getActivity().onBackPressed();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
