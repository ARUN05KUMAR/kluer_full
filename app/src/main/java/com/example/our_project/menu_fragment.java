package com.example.our_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class menu_fragment extends Fragment {

    public menu_fragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment, container, false);

        TextView memoryUsageTextView = view.findViewById(R.id.textView2);

        memoryUsageTextView.setOnClickListener(v -> {
            // Replace the main fragment with MemoryUsageFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container, new MemoryUsageFragment());
            transaction.addToBackStack(null);
            transaction.commit();

            // Call MainActivity's method to close menu and fade out background
            ((MainActivity) requireActivity()).closeMenu();
        });

        return view;
    }
}
