package com.vuducminh.nicefoodserver.EventBus;

import com.vuducminh.nicefoodserver.Model.AddonModel;

public class SelectAddonModel {
    private AddonModel addonModel;

    public SelectAddonModel() {
    }

    public SelectAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }

    public AddonModel getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }
}
