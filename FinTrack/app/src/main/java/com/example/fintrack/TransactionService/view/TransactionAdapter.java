package com.example.fintrack.TransactionService.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.TransactionService.data.entity.TransactionEntity;

import java.util.List;

public class TransactionAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onEdit(TransactionEntity tx);
    }

    private List<HistoryItem> list;
    private final Listener listener;

    public TransactionAdapter(List<HistoryItem> list, Listener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<HistoryItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == HistoryItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderVH(v);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_v2, parent, false);
        return new TxVH(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder h, int position) {

        HistoryItem item = list.get(position);

        if (item.type == HistoryItem.TYPE_HEADER) {
            ((HeaderVH) h).txtHeader.setText(item.header);
            return;
        }

        TransactionEntity tx = item.tx;
        TxVH vh = (TxVH) h;

        // ===== ICON (giữ logic nhóm) =====
        String icon = "📌";
        if ("FOOD".equals(tx.category_id)) icon = "🛒";
        else if ("SALARY".equals(tx.category_id)) icon = "💰";
        else if ("TRANSFER".equals(tx.tx_type_id)) icon = "🔁";

        vh.txtIcon.setText(icon);

        // ===== GỘP LOGIC CODE CỦA BẠN =====
        // title
        vh.txtName.setText(tx.note == null ? "No note" : tx.note);

        // date
        vh.txtDate.setText(tx.tx_date);

        // amount
        boolean isExpense = "EXPENSE".equals(tx.tx_type_id);

        vh.txtAmount.setText(
                (isExpense ? "-" : "+") +
                        String.format("%,.0f", tx.amount)
        );

        vh.txtAmount.setTextColor(
                isExpense ? Color.RED : Color.parseColor("#2E7D32")
        );

        // ===== EDIT BUTTON =====
        if (listener == null) {
            vh.btnEdit.setVisibility(View.GONE);
        } else {
            vh.btnEdit.setVisibility(View.VISIBLE);
            vh.btnEdit.setOnClickListener(v -> listener.onEdit(tx));
        }

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ===== VIEW HOLDERS =====

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView txtHeader;

        HeaderVH(View v) {
            super(v);
            txtHeader = v.findViewById(R.id.txtHeader);
        }
    }

    static class TxVH extends RecyclerView.ViewHolder {

        TextView txtIcon, txtName, txtDate, txtAmount, btnEdit;

        TxVH(View v) {
            super(v);

            txtIcon = v.findViewById(R.id.txtIcon);
            txtName = v.findViewById(R.id.txtName);
            txtDate = v.findViewById(R.id.txtDate);
            txtAmount = v.findViewById(R.id.txtAmount);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }
}