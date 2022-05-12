package com.hoogercoin.fragments.bottomsheet;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hoogercoin.BuyCoinActivity;
import com.hoogercoin.R;
import com.hoogercoin.SellCoinActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwapFragment extends BottomSheetDialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SwapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SwapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SwapFragment newInstance(String param1, String param2) {
        SwapFragment fragment = new SwapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private LinearLayout buyHGR;
    private LinearLayout sellHGR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    private void detectUIElements(View root) {
        buyHGR = root.findViewById(R.id.buyHGR);
        sellHGR = root.findViewById(R.id.sellHGR);
    }

    private void defineUIBehaviours() {
        buyHGR.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), BuyCoinActivity.class));
            dismiss();
        });

        sellHGR.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), SellCoinActivity.class));
            dismiss();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_swap, container, false);

        detectUIElements(view);
        defineUIBehaviours();

        return view;
    }
}