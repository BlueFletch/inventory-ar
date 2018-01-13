package com.bluefletch.visioninventory.ui.detail;

public class ProductData {

    private String _value;
    private boolean _shouldHighlight;
    private Integer _iconResourceId;

    public ProductData(String value, boolean shouldHighlight, Integer iconResourceId) {
        _value = value;
        _shouldHighlight = shouldHighlight;
        _iconResourceId = iconResourceId;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String _value) {
        this._value = _value;
    }

    public boolean shouldHighlight() {
        return _shouldHighlight;
    }

    public void setShouldHighlight(boolean _shouldHighlight) {
        this._shouldHighlight = _shouldHighlight;
    }

    public int getIconResourceId() {
        return _iconResourceId;
    }

    public void setIconResourceId(int _iconResourceId) {
        this._iconResourceId = _iconResourceId;
    }
}
