package com.example.fintrack.TransactionService.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.CategoryEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
public class CategoryPickerBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSelect(CategoryEntity category);
    }

    private static final String ARG_TYPE = "tx_type";

    private String txType;
    private Listener listener;

    public static CategoryPickerBottomSheet newInstance(
            String txType,
            Listener listener
    ) {
        CategoryPickerBottomSheet sheet = new CategoryPickerBottomSheet();
        sheet.txType = txType;
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(
                R.layout.bottom_sheet_category_picker,
                container,
                false
        );

        RecyclerView rv = v.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        CategoryChildAdapter adapter =
                new CategoryChildAdapter(
                        new ArrayList<>(),
                        new CategoryChildAdapter.Listener() {
                            @Override
                            public void onEdit(CategoryEntity c) {
                                // KHÔNG DÙNG
                            }

                            @Override
                            public void onDelete(CategoryEntity c) {
                                // KHÔNG DÙNG
                            }

                            @Override
                            public void onClick(CategoryEntity c) {
                                if (listener != null) {
                                    listener.onSelect(c);
                                    dismiss();
                                }
                            }
                        },
                        false
                );

        rv.setAdapter(adapter);

        loadData(adapter);

        return v;
    }

    private void loadData(CategoryChildAdapter adapter) {
        new Thread(() -> {

            FintrackDatabase db =
                    FintrackDatabase.getInstance(requireContext());

            UserRepository userRepo =
                    new UserRepository(requireContext());

            UserEntity currentUser =
                    userRepo.getCurrentUser();

            if(currentUser == null) return;

            String currentUserId = currentUser.user_id;

            List<CategoryEntity> list =
                    db.categoryDao().getAll(currentUserId);

            List<CategoryEntity> result = new ArrayList<>();

            for (CategoryEntity c : list) {
                if (txType.equals(c.tx_type_id)) {
                    result.add(c);
                }
            }

            requireActivity().runOnUiThread(() ->
                    adapter.update(result)
            );

        }).start();
    }
}
