package com.bluefletch.visioninventory.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ProductModel implements Serializable
{

    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("dpci")
    @Expose
    private String dpci;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("units")
    @Expose
    private String units;
    @SerializedName("unitType")
    @Expose
    private String unitType;
    @SerializedName("unitsSoldLastWeek")
    @Expose
    private String unitsSoldLastWeek;
    @SerializedName("salesTrend")
    @Expose
    private String salesTrend;
    @SerializedName("onHand")
    @Expose
    private String onHand;
    @SerializedName("reorderThreshold")
    @Expose
    private String reorderThreshold;
    @SerializedName("competitorLowPrice")
    @Expose
    private String competitorLowPrice;
    @SerializedName("competitorLowName")
    @Expose
    private String competitorLowName;
    @SerializedName("unitTrendData")
    @Expose
    private List<Float> unitTrendData;
    @SerializedName("salesTrendData")
    @Expose
    private List<Float> salesTrendData;
    @SerializedName("onHandTrendData")
    @Expose
    private List<Float> onHandTrendData;

    private final static long serialVersionUID = -62000557001913163L;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDpci() {
        return dpci;
    }

    public void setDpci(String dpci) {
        this.dpci = dpci;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceFormatted() {
        return "$ " + price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getUnitsSoldLastWeek() {
        return unitsSoldLastWeek;
    }

    public void setUnitsSoldLastWeek(String unitsSoldLastWeek) {
        this.unitsSoldLastWeek = unitsSoldLastWeek;
    }

    public String getSalesTrend() {

        if (salesTrend.contains("-")) {
            return salesTrend;
        } else if (salesTrend.equals("")) {
            return "0";
        }

        return "+" + salesTrend;

    }

    public String getPricePerUnit() {

        Float pricePerUnit = Float.valueOf(this.price) / Float.valueOf(this.units);
        return String.format("%.2f", pricePerUnit);
    }

    public void setSalesTrend(String salesTrend) {
        this.salesTrend = salesTrend;
    }

    public String getOnHand() {
        return onHand;
    }

    public void setOnHand(String onHand) {
        this.onHand = onHand;
    }

    public String getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(String reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public String getCompetitorLowPrice() {
        return competitorLowPrice;
    }

    public void setCompetitorLowPrice(String competitorLowPrice) {
        this.competitorLowPrice = competitorLowPrice;
    }

    public String getCompetitorLowName() {
        return competitorLowName;
    }

    public void setCompetitorLowName(String competitorLowName) {
        this.competitorLowName = competitorLowName;
    }

    public List<Float> getUnitTrendData() {
        return unitTrendData;
    }

    public void setUnitTrendData(List<Float> unitTrendData) {
        this.unitTrendData = unitTrendData;
    }

    public List<Float> getSalesTrendData() {
        return salesTrendData;
    }

    public void setSalesTrendData(List<Float> salesTrendData) {
        this.salesTrendData = salesTrendData;
    }

    public List<Float> getOnHandTrendData() {
        return onHandTrendData;
    }

    public void setOnHandTrendData(List<Float> onHandTrendData) {
        this.onHandTrendData = onHandTrendData;
    }
}
