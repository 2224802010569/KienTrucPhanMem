package com.example.fintrack.TransactionService.view;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.db.FintrackDatabase;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.fintrack.UserService.data.UserRepository;
import com.example.fintrack.UserService.data.entity.UserEntity;
public class ImportBankStatementActivity extends AppCompatActivity {
    private String currentUserId;
    private static final int PICK_FILE_REQUEST = 1001;

    private Button btnSelectFile;
    private Button btnStartImport;

    private TextView txtFileName;

    // preview mapping
    private TextView txtPreviewDate;
    private TextView txtPreviewAmount;
    private TextView txtPreviewNote;

    private String previewDate;
    private double previewAmount;
    private String previewNote;

    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_bank_statement);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnStartImport = findViewById(R.id.btnStartImport);

        txtFileName = findViewById(R.id.txtFileName);

        txtPreviewDate = findViewById(R.id.txtPreviewDate);
        txtPreviewAmount = findViewById(R.id.txtPreviewAmount);
        txtPreviewNote = findViewById(R.id.txtPreviewNote);

        btnSelectFile.setOnClickListener(v -> openFilePicker());

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish(); // quay lại màn hình trước
        });
        UserRepository userRepo = new UserRepository(this);
        UserEntity currentUser = userRepo.getCurrentUser();

        if (currentUser == null) return;

        currentUserId = currentUser.user_id;

        btnStartImport.setOnClickListener(v -> {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
                return;
            }

            openAddTransaction(previewDate, previewAmount, previewNote);
        });
    }

    // ===============================
    // OPEN FILE PICKER
    // ===============================

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(
                Intent.createChooser(intent, "Select CSV File"),
                PICK_FILE_REQUEST
        );
    }

    // ===============================
    // RESULT FILE PICKER
    // ===============================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {

            selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                String fileName = getFileName(selectedFileUri);
                txtFileName.setText(fileName);

                // show preview mapping
                showCSVPreview();
            }
        }
    }

    // ===============================
    // PREVIEW CSV
    // ===============================

    private void showCSVPreview() {

        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            getContentResolver().openInputStream(selectedFileUri)
                    )
            );

            // skip header
            reader.readLine();

            String firstRow = reader.readLine();

            if (firstRow != null) {

                String[] columns = firstRow.split(",");

                if (columns.length >= 3) {

                    String date = columns[0].trim();
                    String amount = columns[1].trim();
                    String note = columns[2].trim();

                    txtPreviewDate.setText("Date: " + date);
                    txtPreviewAmount.setText("Amount: " + amount);
                    txtPreviewNote.setText("Note: " + note);

                    previewDate = date;

                    try {
                        previewAmount = Double.parseDouble(amount);
                    } catch (Exception e) {
                        previewAmount = 0;
                    }

                    previewNote = note;
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot preview CSV", Toast.LENGTH_SHORT).show();
        }
    }

    // ===============================
    // GET FILE NAME
    // ===============================

    private String getFileName(Uri uri) {

        String result = null;

        if ("content".equals(uri.getScheme())) {

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            try {

                if (cursor != null && cursor.moveToFirst()) {

                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }

            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
        }

        return result;
    }

    // ===============================
    // IMPORT CSV
    // ===============================

    private void importCSV() {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            try {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                getContentResolver().openInputStream(selectedFileUri)
                        )
                );

                String line;

                FintrackDatabase db =
                        FintrackDatabase.getInstance(getApplicationContext());

                boolean isFirstLine = true;

                List<TransactionEntity> bulkList = new ArrayList<>();

                while ((line = reader.readLine()) != null) {

                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] columns = line.split(",");

                    if (columns.length < 3) continue;

                    String date = columns[0].trim();
                    double amount = Double.parseDouble(columns[1].trim());
                    String note = columns[2].trim();

                    TransactionEntity existed =
                            db.transactionDao().checkDuplicate(date, note);

                    if (existed != null) {
                        continue;
                    }

                    TransactionEntity transaction =
                            new TransactionEntity(date, "00:00", amount, note);

                    transaction.tx_id = UUID.randomUUID().toString();
                    transaction.user_id = currentUserId;

                    if (amount > 0) {
                        transaction.tx_type_id = "INCOME";
                        transaction.target_account_id = "acc001";
                    } else {
                        transaction.tx_type_id = "EXPENSE";
                        transaction.source_account_id = "acc001";
                    }

                    String noteLower = note.toLowerCase();

                    if (noteLower.contains("coffee") || noteLower.contains("cafe")) {
                        transaction.category_id = "cat_food";
                    } else if (noteLower.contains("grab") || noteLower.contains("taxi")) {
                        transaction.category_id = "cat_transport";
                    } else if (noteLower.contains("salary")) {
                        transaction.category_id = "cat_income";
                    }

                    transaction.created_at = String.valueOf(System.currentTimeMillis());
                    transaction.month = date.substring(0, 7);

                    bulkList.add(transaction);
                }

                reader.close();

                if (!bulkList.isEmpty()) {
                    db.transactionDao().insertAll(bulkList);
                }

                runOnUiThread(() -> {

                    Toast.makeText(
                            ImportBankStatementActivity.this,
                            "Import successful (" + bulkList.size() + " transactions)",
                            Toast.LENGTH_LONG
                    ).show();

                    startActivity(new Intent(
                            ImportBankStatementActivity.this,
                            HistoryActivity.class
                    ));

                    finish();
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() ->
                        Toast.makeText(
                                ImportBankStatementActivity.this,
                                "Import failed",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        });
    }

    // ===============================
    // OPEN ADD TRANSACTION
    // ===============================

    private void openAddTransaction(
            String date,
            double amount,
            String note
    ) {

        Intent intent = new Intent(this, AddTransactionActivity.class);

        if (amount > 0) {

            intent.putExtra("csv_type", "INCOME");

        } else {

            intent.putExtra("csv_type", "EXPENSE");
            amount = Math.abs(amount);
        }

        intent.putExtra("csv_date", date);
        intent.putExtra("csv_amount", amount);
        intent.putExtra("csv_note", note);

        startActivity(intent);
    }
}