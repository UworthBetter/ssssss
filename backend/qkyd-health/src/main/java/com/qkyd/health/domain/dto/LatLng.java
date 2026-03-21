package com.qkyd.health.domain.dto;

import java.math.BigDecimal;

/**
 * 经纬度 电子围栏
 */
public class LatLng {
    public BigDecimal lat;
    public BigDecimal lng;

    public LatLng(BigDecimal lat, BigDecimal lng) {
        this.lat = lat;
        this.lng = lng;
    }
}

