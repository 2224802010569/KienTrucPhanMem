package com.example.fintrack.AccountService.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.AccountService.data.AccountRepository;
import com.example.fintrack.AccountService.model.AccountEntity;
import com.example.fintrack.R;
import java.text.DecimalFormat;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<AccountEntity> accountList;

    public AccountAdapter(List<AccountEntity> accountList) {
        this.accountList = accountList;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountEntity account = accountList.get(position);
        holder.tvName.setText(account.name);
        holder.tvType.setText(account.typeId);
        DecimalFormat df = new DecimalFormat("#,###");
        holder.tvBalance.setText(df.format(account.balance) + " đ");

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            if (AccountEntity.STATUS_HIDDEN.equals(account.status)) {
                showRestoreDialog(context, account, holder.getAdapterPosition());
            } else {
                Intent intent = new Intent(context, WalletDetailActivity.class);
                intent.putExtra("WALLET_ID", account.accountId);
                context.startActivity(intent);
            }
        });
    }

    private void showRestoreDialog(Context context, AccountEntity account, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Restore Wallet")
                .setMessage("Do you want to unhide the wallet \"" + account.name + "\" and display it again on the main screen?")
                .setPositiveButton("Confirm", (dialog, which) -> {

                    // Thêm Context vào đây
                    AccountRepository.getInstance(context).updateStatus(account.accountId, AccountEntity.STATUS_ACTIVE);

                    accountList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, accountList.size());
                    Toast.makeText(context, "Wallet restored successfully!", Toast.LENGTH_SHORT).show();

                    if (accountList.isEmpty() && context instanceof HiddenWalletsActivity) {
                        ((HiddenWalletsActivity) context).finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvBalance;
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvType = itemView.findViewById(R.id.tvItemType);
            tvBalance = itemView.findViewById(R.id.tvItemBalance);
        }
    }
}