package com.agromarket.ampl_chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agromarket.ampl_chat.MainActivity;
import com.agromarket.ampl_chat.R;

public class SplashFragment3 extends Fragment {

    public SplashFragment3() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_splash3, container, false);

        view.findViewById(R.id.btnRegister).setOnClickListener(v ->
                Toast.makeText(getContext(), "Register Clicked", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.btnLogin).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Optional: closes the current activity so user can't go back
        });

        return view;
    }
}
