package com.vuducminh.nicefoodserver.view_holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatTextHolder extends RecyclerView.ViewHolder {

    private Unbinder unbinder;
    @BindView(R.id.tv_time)
    public TextView tv_time;
    @BindView(R.id.tv_email)
    public TextView tv_email;
    @BindView(R.id.tv_chat_message)
    public TextView tv_chat_message;
    @BindView(R.id.profile_image)
    public CircleImageView profile_image;

    public ChatTextHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this,itemView);
    }
}
