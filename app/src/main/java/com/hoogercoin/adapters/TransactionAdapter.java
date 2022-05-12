package com.hoogercoin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hoogercoin.R;
import com.hoogercoin.helpers.TypoHelper;
import com.hoogercoin.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactions;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction currentTransaction = transactions.get(position);

        if (currentTransaction.getTs_type() == Transaction.TransactionType.BUY_COIN) {
            holder.tsTitle.setText("خرید هوگر");
            holder.tsDescription.setText("خرید " +
                    TypoHelper.toPersianDigit(String.valueOf(currentTransaction.getAmount()))
            + " هوگر به مبلغ" + TypoHelper.persianDigitMoney(String.valueOf(currentTransaction.getAmount() * currentTransaction.getHooger_price())) + " تومان"
            );
            holder.tsAmount.setText(TypoHelper.toPersianDigit(String.valueOf(currentTransaction.getAmount())));
            holder.tsImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.income));
            holder.tsAmount.setTextColor(ContextCompat.getColor(context, R.color.price_green));
        }
        else if (currentTransaction.getTs_type() == Transaction.TransactionType.SELL_COIN) {
            holder.tsTitle.setText("فروش هوگر");
            holder.tsDescription.setText("فروش " +
                    TypoHelper.toPersianDigit(String.valueOf(currentTransaction.getAmount()))
                    + " هوگر به مبلغ" + TypoHelper.persianDigitMoney(String.valueOf(currentTransaction.getAmount() * currentTransaction.getHooger_price())) + " تومان"
            );
            holder.tsAmount.setText(TypoHelper.toPersianDigit(String.valueOf(currentTransaction.getAmount())));
            holder.tsImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.outcome));
            holder.tsAmount.setTextColor(ContextCompat.getColor(context, R.color.price_red));
        }

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tsTitle;
        private TextView tsDescription;
        private TextView tsAmount;
        private ImageView tsImage;
        private ConstraintLayout tsRoot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tsTitle = itemView.findViewById(R.id.tsTitle);
            tsDescription = itemView.findViewById(R.id.tsDescription);
            tsAmount = itemView.findViewById(R.id.tsAmount);
            tsImage = itemView.findViewById(R.id.tsImage);
            tsRoot = itemView.findViewById(R.id.tsRoot);
        }
    }
}
