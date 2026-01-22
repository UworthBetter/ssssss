package com.ueit.health.service;

import com.ueit.health.domain.UeitDeviceInfo;
import com.ueit.health.domain.dto.RealTimeTracking;
import com.ueit.health.domain.dto.UserDevice;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * 数据查询接口
 */
public interface DataService {
    /**
     * 健康数据看板 七种类型
     * @param userId
     * @param type
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    List getDataBoard(int userId, String type, Date beginReadTime, Date endReadTime);

    /**
     * 根据用户id查询用户设备列表
     * @return
     */
    List<UserDevice> getUserDevice(UserDevice userDevice);

    /**
     * 查询没有绑定用户的设备列表
     * @return
     */
    List<UeitDeviceInfo> getDeviceListWithoutUser(UeitDeviceInfo ueitDeviceInfo);

    /**
     * 把设备绑定给某个用户
     * @return
     */
    int addDeviceToUser(Long[] deviceIds, int userId);

    RealTimeTracking realTimeTracking(String coordinateType, int userId);

    /**
     * 历史轨迹
     * @param coordinateType
     * @param beginReadTime
     * @param endReadTime
     * @param userId
     * @return
     */
    List<RealTimeTracking> pathList(String coordinateType, Date beginReadTime, Date endReadTime, int userId);

    /**
     * 通过UserId查询用户设备列表
     * @param userId
     * @return
     */
    List getUserDeviceByUserId(int userId);
}

