package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.randomButton.setOnClickListener(view1 ->
                {
                    String countString = binding.showCountTextView.getText().toString();
                    int count = Integer.parseInt(countString);
                    Bundle bundle = new Bundle();
                    bundle.putInt("myArg", count);
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                }
        );

        binding.toastButton.setOnClickListener(view1 -> {
                    Toast myToast = Toast.makeText(getActivity(), "Hello toast!", Toast.LENGTH_SHORT);
                    myToast.show();
                }
        );

        binding.buttonCount.setOnClickListener(this::countMe);
    }

    private void countMe(View view) {
        String countString = binding.showCountTextView.getText().toString();
        int count = Integer.parseInt(countString);
        count++;
        binding.showCountTextView.setText(Integer.toString(count));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}