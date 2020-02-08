package com.vuducminh.nicefoodserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.R;
import com.vuducminh.nicefoodserver.eventbus.UpdateShipperEvent;
import com.vuducminh.nicefoodserver.model.ShipperModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperAdapter extends RecyclerView.Adapter<MyShipperAdapter.MyViewHolder> {
    private Context context;
    private List<ShipperModel> shipperModelList;

    public MyShipperAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_shipper,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ShipperModel shipperModel = shipperModelList.get(position);
        holder.tv_name.setText(new StringBuilder(shipperModel.getName()));
        holder.tv_phone.setText(new StringBuilder(shipperModel.getPhone()));
        holder.btn_enable.setChecked(shipperModel.isActive());

        holder.btn_enable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            EventBus.getDefault().postSticky(new UpdateShipperEvent(shipperModelList.get(position),isChecked));
        });
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private Unbinder unbinder;

        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_phone)
        TextView tv_phone;
        @BindView(R.id.btn_enable)
        SwitchCompat btn_enable;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
