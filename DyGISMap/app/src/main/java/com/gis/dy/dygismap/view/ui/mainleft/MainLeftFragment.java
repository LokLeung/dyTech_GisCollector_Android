package com.gis.dy.dygismap.view.ui.mainleft;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gis.dy.dygismap.R;

public class MainLeftFragment extends Fragment {

    private MainLeftViewModel mViewModel;

    public static MainLeftFragment newInstance() {
        return new MainLeftFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_left_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainLeftViewModel.class);
        // TODO: Use the ViewModel
    }

}
