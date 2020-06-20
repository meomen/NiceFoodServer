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
import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.model.BestDealsModel;
import com.vuducminh.nicefoodserver.model.MostPopularModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyMostPopularAdapter extends RecyclerView.Adapter<MyMostPopularAdapter.MyViewHolder> {

    Context context;
    List<MostPopularModel> mostPopularModels;

    public MyMostPopularAdapter(Context context, List<MostPopularModel> mostPopularModels) {
        this.context = context;
        this.mostPopularModels = mostPopularModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(mostPopularModels.get(position).getImage()).into(holder.img_category);
        holder.tv_category.setText(new StringBuffer(mostPopularModels.get(position).getName()));

        //Sự kiện
        holder.setListener((view, pos) -> {

        });
    }

    @Override
    public int getItemCount() {
        return mostPopularModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;

        @BindView(R.id.img_category)
        ImageView img_category;
        @BindView(R.id.tv_category)
        TextView tv_category;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

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
