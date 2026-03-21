package com.qkyd.health.service;

import java.util.List;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.domain.dto.UserDevice;
import org.apache.ibatis.annotations.Param;

/**
 * 设备信息Service接口
 *
 * @author douwq
 * @date 2023-12-22
 */
public interface IUeitDeviceInfoService
{
    /**
     * 查询设备信息
     *
     * @param id 设备信息主键
     * @return 设备信息
     */
    public UeitDeviceInfo selectUeitDeviceInfoById(Long id);

    /**
     * 查询设备信息
     *
     * @param imei IMEI信息
     * @return 设备信息
     */
    public UeitDeviceInfo selectUeitDeviceInfoByImei(String imei);

    /**
     * 查询设备信息列表
     *
     * @param ueitDeviceInfo 设备信息
     * @return 设备信息集合
     */
    public List<UeitDeviceInfo> selectUeitDeviceInfoList(UeitDeviceInfo ueitDeviceInfo);

    /**
     * 新增设备信息
     *
     * @param ueitDeviceInfo 设备信息
     * @return 结果
     */
    public int insertUeitDeviceInfo(UeitDeviceInfo ueitDeviceInfo);

    /**
     * 修改设备信息
     *
     * @param ueitDeviceInfo 设备信息
     * @return 结果
     */
    public int updateUeitDeviceInfo(UeitDeviceInfo ueitDeviceInfo);

    /**
     * 批量删除设备信息
     *
     * @param ids 需要删除的设备信息主键集合
     * @return 结果
     */
    public int deleteUeitDeviceInfoByIds(Long[] ids);

    /**
     * 删除设备信息信息
     *
     * @param id 设备信息主键
     * @return 结果
     */
    public int deleteUeitDeviceInfoById(Long id);

    /**
     * 根据用户id查询用户设备列表
     */
    public List<UserDevice> getUserDevice(UserDevice userDevice);

    /**
     * 查询没有绑定用户的设备列表
     * @return
     */
    List getDeviceListWithoutUser(UeitDeviceInfo ueitDeviceInfo);

    /**
     * 把设备绑定给某个用户
     * @param id
     * @param userId
     * @return
     */
    int addDeviceToUser(Long id, int userId);

    /**
     * 通过UserId查询用户设备列表
     * @param userId
     * @return
     */
    List<UserDevice>  getUserDeviceByUserId(int userId);
}

