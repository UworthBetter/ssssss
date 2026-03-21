package com.qkyd.health.service.impl;

import java.util.List;

import com.qkyd.common.utils.DateUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.health.domain.dto.UserDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.health.mapper.UeitDeviceInfoMapper;
import com.qkyd.health.domain.UeitDeviceInfo;
import com.qkyd.health.service.IUeitDeviceInfoService;

/**
 * 设备信息Service业务层处理
 *
 * @author douwq
 * @date 2023-12-22
 */
@Service
public class UeitDeviceInfoServiceImpl implements IUeitDeviceInfoService
{
    @Autowired
    private UeitDeviceInfoMapper ueitDeviceInfoMapper;

    /**
     * 查询设备信息
     *
     * @param id 设备信息主键
     * @return 设备信息
     */
    @Override
    public UeitDeviceInfo selectUeitDeviceInfoById(Long id)
    {
        return ueitDeviceInfoMapper.selectUeitDeviceInfoById(id);
    }

    /**
     * 查询设备信息
     *
     * @param imei IMEI信息
     * @return 设备信息
     */
    @Override
    public UeitDeviceInfo selectUeitDeviceInfoByImei(String imei)
    {
        return ueitDeviceInfoMapper.selectUeitDeviceInfoByImei(imei);
    }

    /**
     * 查询设备信息列表
     *
     * @param ueitDeviceInfo 设备信息
     * @return 设备信息
     */
    @Override
    public List<UeitDeviceInfo> selectUeitDeviceInfoList(UeitDeviceInfo ueitDeviceInfo)
    {
        return ueitDeviceInfoMapper.selectUeitDeviceInfoList(ueitDeviceInfo);
    }

    /**
     * 新增设备信息
     *
     * @param ueitDeviceInfo 设备信息
     * @return 结果
     */
    @Override
    public int insertUeitDeviceInfo(UeitDeviceInfo ueitDeviceInfo)
    {
        // 判断IMEI信息是否为空，为空时(-1)，不再进行后续操作
        String imei = ueitDeviceInfo.getImei();
        if (StringUtils.isEmpty(imei)) {
            return -1;
        }
        // 不为空时，根据IMEI信息查询设备，没有查到就新增该设备,查到提示该IMEI已存在（-2）
        UeitDeviceInfo ueitDevice = ueitDeviceInfoMapper.selectUeitDeviceInfoByImei(imei);
        if (ueitDevice == null) {
            ueitDeviceInfo.setCreateTime(DateUtils.getNowDate());
            return ueitDeviceInfoMapper.insertUeitDeviceInfo(ueitDeviceInfo);
        } else {
            return -2;
        }
    }

    /**
     * 修改设备信息
     *
     * @param ueitDeviceInfo 设备信息
     * @return 结果
     */
    @Override
    public int updateUeitDeviceInfo(UeitDeviceInfo ueitDeviceInfo)
    {
        // 判断IMEI信息是否为空，为空时(-1)，不再进行后续操作
        String imei = ueitDeviceInfo.getImei();
        if (StringUtils.isEmpty(imei)) {
            return -1;
        }
        //通过IMEI查询设备，若查到的设备和正在修改的设备id一样时能修改，不一样时该IMEI已存在（-2）
        //               若没有查到设备，修改
        UeitDeviceInfo ueitDevice = ueitDeviceInfoMapper.selectUeitDeviceInfoByImei(imei);
        if (ueitDevice != null) {
            if (ueitDeviceInfo.getId().equals(ueitDevice.getId())) {
                return ueitDeviceInfoMapper.updateUeitDeviceInfo(ueitDeviceInfo);
            } else {
                return -2;
            }
        } else {
            return ueitDeviceInfoMapper.updateUeitDeviceInfo(ueitDeviceInfo);
        }
    }

    /**
     * 批量删除设备信息
     *
     * @param ids 需要删除的设备信息主键
     * @return 结果
     */
    @Override
    public int deleteUeitDeviceInfoByIds(Long[] ids)
    {
        return ueitDeviceInfoMapper.deleteUeitDeviceInfoByIds(ids);
    }

    /**
     * 删除设备信息信息
     *
     * @param id 设备信息主键
     * @return 结果
     */
    @Override
    public int deleteUeitDeviceInfoById(Long id)
    {
        return ueitDeviceInfoMapper.deleteUeitDeviceInfoById(id);
    }

    /**
     * 根据用户id查询用户设备列表
     * @return
     */
    @Override
    public List<UserDevice> getUserDevice(UserDevice userDevice) {
        return ueitDeviceInfoMapper.getUserDevice(userDevice);
    }

    /**
     * 查询没有绑定用户的设备列表
     * @return
     */
    @Override
    public List<UeitDeviceInfo> getDeviceListWithoutUser(UeitDeviceInfo ueitDeviceInfo) {
        return ueitDeviceInfoMapper.getDeviceListWithoutUser(ueitDeviceInfo);
    }

    /**
     * 把设备绑定给某个用户
     * @param id
     * @param userId
     * @return
     */
    @Override
    public int addDeviceToUser(Long id, int userId) {
        return ueitDeviceInfoMapper.addDeviceToUser(id, userId);
    }

    /**
     * 通过UserId查询用户设备列表
     * @param userId
     * @return
     */
    @Override
    public List<UserDevice>  getUserDeviceByUserId(int userId) {
        return ueitDeviceInfoMapper.userDeviceByUserId(userId);
    }
}

