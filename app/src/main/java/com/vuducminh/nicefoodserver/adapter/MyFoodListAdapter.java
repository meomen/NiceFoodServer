package com.vuducminh.nicefoodserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.common.Common;
import com.vuducminh.nicefoodserver.model.FoodModel;
import com.vuducminh.nicefoodserver.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;


    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_food_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);

        holder.tv_food_name.setText(new StringBuffer("")
                .append(foodModelList.get(position).getName()));

        holder.tv_food_price.setText(new StringBuffer("$")
                .append(foodModelList.get(position).getPrice()));

        // Event Bus
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.selectedFood = foodModelList.get(pos);
                Common.selectedFood.setKey(String.valueOf(pos));
            }
        });


    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public FoodModel getItemAtPosition(int position) {
        return foodModelList.get(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_food_name)
        TextView tv_food_name;
        @BindView(R.id.tv_food_price)
        TextView tv_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        private Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}
