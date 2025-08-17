package com.example.our_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class insert_fragment extends Fragment {

    public insert_fragment() {
    }
    private FragmentManager manager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.insert_fragment, container, false);

        ImageView imageView = view.findViewById(R.id.ip_design);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        imageView.startAnimation(rotate);

        CardView image_form = view.findViewById(R.id.image_input);
        CardView audio_form = view.findViewById(R.id.audio_input);
        CardView text_form = view.findViewById(R.id.text_input);

        manager = requireActivity().getSupportFragmentManager();

        image_form.setOnClickListener(v -> {
            FragmentTransaction transaction = manager.beginTransaction();
            remove_fragment(transaction);
            add_fragment_in_main_container(transaction, new image_form());
            transaction.commit();
        });

        audio_form.setOnClickListener(v->{
            FragmentTransaction transaction=manager.beginTransaction();
            remove_fragment(transaction);
            add_fragment_in_main_container(transaction,new audio_form());
            transaction.commit();
        });

        text_form.setOnClickListener(v->{
            FragmentTransaction transaction=manager.beginTransaction();
            remove_fragment(transaction);
            add_fragment_in_main_container(transaction,new text_form());
            transaction.commit();
        });

        return view;
    }

    private void add_fragment_in_main_container(FragmentTransaction transaction, Fragment fragment) {
        transaction.add(R.id.main_fragment_container, fragment, "main_frag");
    }

    private void remove_fragment(FragmentTransaction transaction) {
        Fragment fragment = manager.findFragmentById(R.id.main_fragment_container);
        if (fragment != null) {
            transaction.remove(fragment);
        }
    }

}
