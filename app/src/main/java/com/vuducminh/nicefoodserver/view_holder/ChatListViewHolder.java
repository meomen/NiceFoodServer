package com.vuducminh.nicefoodserver.view_holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ChatListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private Unbinder unbinder;

    @BindView(R.id.tv_email)
    public TextView tv_email;
    @BindView(R.id.tv_chat_message)
    public TextView tv_chat_message;

    IRecyclerClickListener listener;

    public void setListener(IRecyclerClickListener listener) {
        this.listener = listener;
    }

    public ChatListViewHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this,itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onItemClickListener(v,getAdapterPosition());
    }
}
