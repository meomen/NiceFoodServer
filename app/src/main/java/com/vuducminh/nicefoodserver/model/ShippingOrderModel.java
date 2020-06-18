package com.vuducminh.nicefoodserver.model;

public class ShippingOrderModel {
    private String key;
    private String shipperPhone,shipperName;
    private double currentLat,currentLng;
    private OrderModel orderModel;
    private boolean isStartTrip;

    public ShippingOrderModel() {
    }

    public ShippingOrderModel(String shipperPhone, String shipperName, double currentLat, double currentLng, OrderModel orderModel, boolean isStartTrip) {
        this.shipperPhone = shipperPhone;
        this.shipperName = shipperName;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.orderModel = orderModel;
        this.isStartTrip = isStartTrip;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }

    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
    }

    public boolean isStartTrip() {
        return isStartTrip;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setStartTrip(boolean startTrip) {
        isStartTrip = startTrip;
    }
}
