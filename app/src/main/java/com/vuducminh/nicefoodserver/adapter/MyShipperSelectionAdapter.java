package com.vuducminh.nicefoodserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.model.ShipperModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperSelectionAdapter extends RecyclerView.Adapter<MyShipperSelectionAdapter.MyViewHolder> {

    private Context context;
    List<ShipperModel>  shipperModelList;
    private ImageView lastCheckedImageView = null;
    private ShipperModel selectedShipper = null;


    public MyShipperSelectionAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ShipperModel shipperModel = shipperModelList.get(position);
        holder.tv_name.setText(new StringBuilder(shipperModel.getName()));
        holder.tv_phone.setText(new StringBuilder(shipperModel.getPhone()));
        holder.setiRecyclerClickListener((view, pos) -> {
            if(lastCheckedImageView != null) {
                lastCheckedImageView.setImageResource(0);
            }
            holder.img_checked.setImageResource(R.drawable.ic_check_black_24dp);
            lastCheckedImageView = holder.img_checked;
            selectedShipper = shipperModel;
        });
    }

    public ShipperModel getSelectedShipper() {
        return selectedShipper;
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;

        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_phone)
        TextView tv_phone;
        @BindView(R.id.img_checked)
        ImageView img_checked;

        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerClickListener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
