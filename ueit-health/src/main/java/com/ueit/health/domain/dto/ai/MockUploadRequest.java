package com.ueit.health.domain.dto.ai;

import java.io.Serializable;
import java.util.List;

/**
 * 模拟上传请求 DTO
 * 用于 /health/mock/upload 接口接收设备模拟数据
 *
 * @author ueit
 * @date 2026-01-29
 */
public class MockUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 用户ID (可选)
     */
    private Long userId;

    /**
     * 体征数据列表
     */
    private List<VitalSignData> dataList;

    public MockUploadRequest() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<VitalSignData> getDataList() {
        return dataList;
    }

    public void setDataList(List<VitalSignData> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "MockUploadRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", userId=" + userId +
                ", dataList=" + dataList +
                '}';
    }
}
