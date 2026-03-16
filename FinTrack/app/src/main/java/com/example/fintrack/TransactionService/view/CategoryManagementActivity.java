package com.example.fintrack.TransactionService.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.CategoryEntity;
import com.example.fintrack.TransactionService.domain.usecase.AddCategoryUseCase;
import com.example.fintrack.TransactionService.domain.usecase.UpdateCategoryUseCase;
import com.example.fintrack.TransactionService.domain.usecase.DeleteCategoryUseCase;

import java.util.ArrayList;
import java.util.List;
import android.widget.ImageButton;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;

public class CategoryManagementActivity extends AppCompatActivity {
    private String currentUserId;
    // ===== UI =====
    private RecyclerView rvParent, rvChild;
    private Button btnExpense, btnIncome;

    // ===== ADAPTER =====
    private CategoryParentAdapter parentAdapter;
    private CategoryChildAdapter childAdapter;

    // ===== DB =====
    private FintrackDatabase db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_category_management_v2);

        // ===== INIT DB =====
        db = FintrackDatabase.getInstance(getApplicationContext());

        // ===== FIND VIEW =====
        rvParent = findViewById(R.id.rvParentCategories);
        rvChild = findViewById(R.id.rvChildCategories);
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);

        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        btnAddCategory.setOnClickListener(v -> showCategoryDialog(null));
        ImageButton btnBack = findViewById(R.id.btnBackCategory);

        btnBack.setOnClickListener(v -> finish());

        UserRepository userRepo = new UserRepository(this);
        UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser == null) return;

        currentUserId = currentUser.user_id;

        // ===== LAYOUT MANAGER =====
        rvParent.setLayoutManager(new GridLayoutManager(this, 2));
        rvChild.setLayoutManager(new LinearLayoutManager(this));

        // ===== CHILD ADAPTER =====
        childAdapter = new CategoryChildAdapter(
                new ArrayList<>(),
                new CategoryChildAdapter.Listener() {

                    @Override
                    public void onEdit(CategoryEntity c) {
                        showCategoryDialog(c);
                    }

                    @Override
                    public void onDelete(CategoryEntity c) {
                        deleteCategory(c);
                    }

                    @Override
                    public void onClick(CategoryEntity c) {
                        // KHÔNG LÀM GÌ – CategoryManagement không dùng click chọn
                    }
                }
        );

        rvChild.setAdapter(childAdapter);


        // ===== PARENT ADAPTER =====
        parentAdapter = new CategoryParentAdapter(
                new ArrayList<>(),
                new CategoryParentAdapter.Listener() {

                    @Override
                    public void onClick(CategoryEntity parent) {
                        loadChildren(parent.category_id);
                    }

                    @Override
                    public void onLongClick(CategoryEntity parent) {

                        new AlertDialog.Builder(CategoryManagementActivity.this)
                                .setTitle("Xoá danh mục")
                                .setMessage("Bạn có chắc muốn xoá \"" + parent.name + "\" ?")
                                .setPositiveButton("Xoá", (dialog, which) -> {
                                    deleteCategory(parent);
                                })
                                .setNegativeButton("Huỷ", null)
                                .show();
                    }

                }
        );

        rvParent.setAdapter(parentAdapter);

        // ===== TAB CLICK =====
        btnExpense.setOnClickListener(v -> loadParents("EXPENSE"));
        btnIncome.setOnClickListener(v -> loadParents("INCOME"));

        // ===== DEFAULT =====
        loadParents("EXPENSE");
    }

    // ================= LOAD PARENT =================
    private void loadParents(String type) {
        new Thread(() -> {
            List<CategoryEntity> list =
                    db.categoryDao().getParentByType(currentUserId, type);


            runOnUiThread(() -> {
                parentAdapter.update(list);
                childAdapter.update(new ArrayList<>()); // clear child
            });
        }).start();
    }

    // ================= LOAD CHILD =================
    private void loadChildren(String parentId) {
        new Thread(() -> {
            List<CategoryEntity> children =
                    db.categoryDao().getChildren(currentUserId, parentId);

            runOnUiThread(() -> childAdapter.update(children));
        }).start();
    }

    // ================= DELETE (GIỮ NGUYÊN LOGIC CŨ) =================
    private void deleteCategory(CategoryEntity c) {
        new Thread(() -> {
            try {
                new DeleteCategoryUseCase(db.categoryDao())
                        .execute(currentUserId, c);
                loadParents(c.tx_type_id);
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // ================= ADD / EDIT (GIỮ NGUYÊN LOGIC CŨ) =================
    private void showCategoryDialog(CategoryEntity editing) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_edit_category, null);

        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtIcon = v.findViewById(R.id.edtIcon);
        Spinner spinnerType = v.findViewById(R.id.spinnerType);
        Spinner spinnerParent = v.findViewById(R.id.spinnerParent);
        Button btnSave = v.findViewById(R.id.btnSave);

        spinnerType.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"EXPENSE", "INCOME"}
                )
        );

        new Thread(() -> {
            List<CategoryEntity> parents =
                    db.categoryDao().getParentCategories(currentUserId);

            CategoryEntity none = new CategoryEntity();
            none.category_id = "NONE";
            none.name = "Không có";
            none.parent_category_id = null;
            parents.add(0, none);


            runOnUiThread(() -> {
                ArrayAdapter<CategoryEntity> parentAdapter =
                        new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                parents
                        );
                spinnerParent.setAdapter(parentAdapter);
            });
        }).start();

        if (editing != null) {
            edtName.setText(editing.name);
            edtIcon.setText(editing.icon);
            spinnerType.setSelection(
                    "INCOME".equals(editing.tx_type_id) ? 1 : 0
            );
        }

        builder.setView(v);
        builder.setTitle(editing == null ? "Thêm danh mục" : "Sửa danh mục");
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(btn -> {
            String name = edtName.getText().toString().trim();
            String icon = edtIcon.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            CategoryEntity parent =
                    (CategoryEntity) spinnerParent.getSelectedItem();

            new Thread(() -> {
                if (editing == null) {

                    String parentId = "NONE".equals(parent.category_id)
                            ? null
                            : parent.category_id;

                    new AddCategoryUseCase(db.categoryDao()).execute(
                            name,
                            type,
                            icon,
                            parentId,
                            currentUserId
                    );
                }
                else {
                    editing.name = name;
                    editing.icon = icon;
                    editing.tx_type_id = type;
                    editing.parent_category_id =
                            "NONE".equals(parent.category_id)
                                    ? null
                                    : parent.category_id;

                    new UpdateCategoryUseCase(db.categoryDao()).execute(editing);
                }

                runOnUiThread(() -> {
                    dialog.dismiss();

                    if ("EXPENSE".equals(type)) {
                        btnExpense.performClick();
                    } else {
                        btnIncome.performClick();
                    }
                });

            }).start();
        });
    }
}
