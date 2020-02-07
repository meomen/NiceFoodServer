package com.vuducminh.nicefoodserver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.Callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.EventBus.SelectSizeModel;
import com.vuducminh.nicefoodserver.Model.SizeModel;
import com.vuducminh.nicefoodserver.EventBus.UpdateSizeModel;
import com.vuducminh.nicefoodserver.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MySizeAdapter extends RecyclerView.Adapter<MySizeAdapter.MyViewHolder> {

    private Context context;
    private List<SizeModel> sizeModelList;
    private UpdateSizeModel updateSizeModel;
    int editPos;

    public MySizeAdapter(Context context, List<SizeModel> sizeModelList) {
        this.context = context;
        this.sizeModelList = sizeModelList;
        editPos = -1;
        updateSizeModel = new UpdateSizeModel();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SizeModel sizeModel = sizeModelList.get(position);

        holder.tv_name.setText(sizeModel.getName());
        holder.tv_price.setText(String.valueOf(sizeModel.getPrice()));

        //Event
        holder.img_delete.setOnClickListener(v -> {
            sizeModelList.remove(position);
            notifyItemRemoved(position);
            updateSizeModel.setSizeModelList(sizeModelList);    //Set for event
            EventBus.getDefault().postSticky(updateSizeModel);
        });

        holder.setListener((view, pos) -> {
            editPos = position;
            EventBus.getDefault().postSticky(new SelectSizeModel(sizeModelList.get(pos)));
        });

    }

    @Override
    public int getItemCount() {
        return sizeModelList.size();
    }

    public void addNewSize(SizeModel sizeModel) {

        sizeModelList.add(sizeModel);
        notifyItemChanged(sizeModelList.size()-1);
        updateSizeModel.setSizeModelList(sizeModelList);
        EventBus.getDefault().postSticky(updateSizeModel);
    }

    public void editSize(SizeModel sizeModel) {
        if(editPos != -1) {
            sizeModelList.set(editPos,sizeModel);
            notifyItemChanged(editPos);
            editPos = -1;
            updateSizeModel.setSizeModelList(sizeModelList);
            EventBus.getDefault().postSticky(updateSizeModel);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;

        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_price)
        TextView tv_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

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
