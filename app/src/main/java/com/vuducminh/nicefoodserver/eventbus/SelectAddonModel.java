package com.vuducminh.nicefoodserver.eventbus;

import com.vuducminh.nicefoodserver.model.AddonModel;

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
