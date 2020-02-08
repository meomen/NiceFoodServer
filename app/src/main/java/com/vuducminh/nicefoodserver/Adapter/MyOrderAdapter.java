package com.vuducminh.nicefoodserver.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vuducminh.nicefoodserver.Common.Common;
import com.vuducminh.nicefoodserver.Model.OrderModel;
import com.vuducminh.nicefoodserver.R;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter  extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>{

    private Context context;
    private List<OrderModel> orderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        this.simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrderModel orderModel = orderModelList.get(position);
        Glide.with(context)
                .load(orderModel.getCartItemList().get(0).getFoodImage())
                .into(holder.img_food_image);
        holder.tv_order_numner.setText(orderModelList.get(position).getKey());
        Common.setSpanStringColor("Order date ",simpleDateFormat.format(orderModel.getCreateDate()),
                holder.tv_time, Color.parseColor("#336699"));
        Common.setSpanStringColor("Order status ",Common.convertStatusToString(orderModel.getOrderStatus()),
                holder.tv_order_status, Color.parseColor("#00579A"));
        Common.setSpanStringColor("Name ",orderModel.getUserName(),
                holder.tv_name, Color.parseColor("#00574B"));
        Common.setSpanStringColor("Num of items: ",orderModel.getCartItemList() == null ? "0" :
                String.valueOf(orderModel.getCartItemList().size()),
                holder.tv_num_item, Color.parseColor("#4B647D"));

    }

    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private Unbinder unbinder;

        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.tv_order_numner)
        TextView tv_order_numner;
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.tv_order_status)
        TextView tv_order_status;
        @BindView(R.id.tv_num_item)
        TextView tv_num_item;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
