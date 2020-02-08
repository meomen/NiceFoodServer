package com.vuducminh.nicefoodserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuducminh.nicefoodserver.callback.IRecyclerClickListener;
import com.vuducminh.nicefoodserver.eventbus.SelectAddonModel;
import com.vuducminh.nicefoodserver.eventbus.UpdateAddonModel;
import com.vuducminh.nicefoodserver.model.AddonModel;
import com.vuducminh.nicefoodserver.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyAddonAdapter extends RecyclerView.Adapter<MyAddonAdapter.MyViewHolder>{
    private Context context;
    private List<AddonModel> addonModelList;
    private UpdateAddonModel updatAddonModel;
    int editPos;

    public MyAddonAdapter(Context context, List<AddonModel> addonModelList) {
        this.context = context;
        this.addonModelList = addonModelList;
        updatAddonModel = new UpdateAddonModel();
        editPos = -1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_size_addon_display, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AddonModel addonModel = addonModelList.get(position);

        holder.tv_name.setText(addonModel.getName());
        holder.tv_price.setText(String.valueOf(addonModel.getPrice()));

        //Event
        holder.img_delete.setOnClickListener(v -> {
            addonModelList.remove(position);
            notifyItemRemoved(position);
            updatAddonModel.setAddonModelList(addonModelList);    //Set for event
            EventBus.getDefault().postSticky(updatAddonModel);
        });

        holder.setListener((view, pos) -> {
            editPos = position;
            EventBus.getDefault().postSticky(new SelectAddonModel(addonModelList.get(pos)));
        });

    }

    @Override
    public int getItemCount() {
        return addonModelList.size();
    }

    public void addNewAddon(AddonModel addonModel) {

        addonModelList.add(addonModel);
        notifyItemChanged(addonModelList.size()-1);
        updatAddonModel.setAddonModelList(addonModelList);
        EventBus.getDefault().postSticky(updatAddonModel);
    }

    public void editAddon(AddonModel addonModel) {
        if(editPos != -1) {
            addonModelList.set(editPos,addonModel);
            notifyItemChanged(editPos);
            editPos = -1;
            updatAddonModel.setAddonModelList(addonModelList);
            EventBus.getDefault().postSticky(updatAddonModel);
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
