package com.ueit.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.ueit.common.constant.WatchPushActionType;
import com.ueit.common.core.domain.AjaxResult;
import com.ueit.common.utils.DateUtils;
import com.ueit.common.utils.StringUtils;
import com.ueit.health.domain.*;
import com.ueit.health.domain.dto.LatLng;
import com.ueit.health.domain.dto.WatchDto;
import com.ueit.health.service.*;
import com.ueit.system.service.ISysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 手表数据Service业务层处理
 */
@Service
public class WatchServiceImpl implements WatchService {
    private static final Logger log = LoggerFactory.getLogger(WatchServiceImpl.class);
    @Autowired
    private IUeitDeviceInfoService ueitDeviceInfoService;
    @Autowired
    private IUeitDeviceInfoExtendService ueitDeviceInfoExtendService;
    @Autowired
    private IUeitBloodService ueitBloodService;
    @Autowired
    private IUeitHeartRateService ueitHeartRateService;
    @Autowired
    private IUeitSpo2Service ueitSpo2Service;
    @Autowired
    private IUeitTempService ueitTempService;
    @Autowired
    private IUeitLocationService ueitLocationService;
    @Autowired
    private IUeitStepsService ueitStepsService;
    @Autowired
    private IUeitExceptionService ueitExceptionService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private IUeitFenceService ueitFenceService;
    /**
     * 处理手表推送数据
     *
     * @param watchDto
     * @return
     */
    @Override
    public synchronized AjaxResult handle(WatchDto watchDto) {
        log.info("【---开始处理手表推送数据,执行包：'{}'】", watchDto.toString());
        if (watchDto == null) {
            return new AjaxResult(500, "手表推送数据为空！");
        }

        // 处理设备信息逻辑
        // 判断IMEI信息是否为空，为空时，不再进行后续操作
        String imei = watchDto.getImei();
        if (StringUtils.isEmpty(imei)) {
            return new AjaxResult(500, "IMEI信息为空！");
        }
        // 不为空时，根据IMEI信息查询设备，没有查到就新增该设备
        UeitDeviceInfo ueitDevice = ueitDeviceInfoService.selectUeitDeviceInfoByImei(imei);
        // 设备id
        Long deviceId;
        // 用户id
        Long userId;
        // 设备扩展信息
        UeitDeviceInfoExtend deviceInfoExtend;
        if (ueitDevice == null) {
            UeitDeviceInfo ueitDeviceInfo = new UeitDeviceInfo();
            ueitDeviceInfo.setImei(imei);
            try {
                ueitDeviceInfoService.insertUeitDeviceInfo(ueitDeviceInfo);
            } catch (Exception e) {
                log.error("【新增设备信息发生异常】");
                e.printStackTrace();
            }
            deviceId = ueitDeviceInfo.getId();
            userId = ueitDeviceInfo.getUserId();
            // 新增设备扩展信息
            deviceInfoExtend = new UeitDeviceInfoExtend();
            deviceInfoExtend.setDeviceId(deviceId);
            try {
                ueitDeviceInfoExtendService.insertUeitDeviceInfoExtend(deviceInfoExtend);
            } catch (Exception e) {
                log.error("【新增设备扩展信息发生异常】");
                e.printStackTrace();
            }
        } else {
            deviceId = ueitDevice.getId();
            UeitDeviceInfo ueitDevice1 = ueitDeviceInfoService.selectUeitDeviceInfoById(deviceId);
            userId = ueitDevice1.getUserId();
            //表中有该设备时，把该设备的扩展信息查出来
            deviceInfoExtend = ueitDeviceInfoExtendService.selectUeitDeviceInfoExtendByDeviceId(deviceId);
            // 判断设备的扩展信息是否为空，为空时新增（若有人把该设备的扩展信息删除了，下面的代码会出现空指针异常）
            if (deviceInfoExtend == null) {
                deviceInfoExtend = new UeitDeviceInfoExtend();
                deviceInfoExtend.setDeviceId(deviceId);
                try {
                    ueitDeviceInfoExtendService.insertUeitDeviceInfoExtend(deviceInfoExtend);
                } catch (Exception e) {
                    log.error("【新增设备扩展信息发生异常】");
                    e.printStackTrace();
                }
            }
        }
        // 获取执行操作 action
        String action = watchDto.getAction();
        log.info("【---处理中，执行操作：'{}'，设备id：'{}'，用户id：'{}'】", action, deviceId, userId);
        // 返回信息
        AjaxResult result = null;
        switch (action) {
            case WatchPushActionType.BLOOD:
                // 处理血压数据
                result = handleBloodPressureData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.HEART:
                // 处理心率数据
                result = handleHeartRateData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.OXYGEN:
                // 处理血氧数据
                result = handleBloodOxygenData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.TEMP:
                // 处理体温数据
                result = handleBodyTemperatureData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.WARN:
                // 处理告警数据
                result = handleWarningData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.HEARTBEAT:
                // 处理心跳数据
                result = handleHeartbeatData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchPushActionType.LOCATION:
                // 处理定位数据
                result = handleLocationData(watchDto, action, deviceId, userId, deviceInfoExtend);
                break;
            default:
                // 处理未知操作
                result = handleUnknownAction();
        }
        return result;
    }

    /**
     * 处理血压数据逻辑
     * 新增血压
     * 修改设备扩展(舒张压、收缩压、血压测量时间、最后通讯时间)
     * 判断血压是否有异常
     * @param watchDto
     */
    private AjaxResult handleBloodPressureData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitBlood blood = new UeitBlood();
        blood.setUserId(userId);
        blood.setDeviceId(deviceId);
        Integer diastolic = watchDto.getDiastolic();
        blood.setDiastolic(diastolic);
        Integer systolic = watchDto.getSystolic();
        blood.setSystolic(systolic);
        boolean isSuccess = false;
        try {
            isSuccess = ueitBloodService.insertUeitBlood(blood) > 0;
        } catch (Exception e) {
            log.error("【新增血压信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setBloodDiastolic(diastolic);
        deviceInfoExtend.setBloodSystolic(systolic);
        deviceInfoExtend.setBloodTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断血压是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.bloodRange"));
        log.info("血压是否有异常:" + config);
        JSONObject diastolicObj = JSONObject.parseObject(config.getString("diastolic"));
        JSONObject systolicObj = JSONObject.parseObject(config.getString("systolic"));
        if (diastolic < diastolicObj.getInteger("min") || diastolic > diastolicObj.getInteger("max") || systolic < systolicObj.getInteger("min") || systolic > systolicObj.getInteger("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "blood", diastolic + "," + systolic);
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理心率数据逻辑
     * 新增心率
     * 修改设备扩展(心率值、心率测量时间、最后通讯时间)
     *
     * @param watchDto
     */
    private AjaxResult handleHeartRateData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitHeartRate heartRate = new UeitHeartRate();
        heartRate.setUserId(userId);
        heartRate.setDeviceId(deviceId);
        heartRate.setValue(watchDto.getNumber());
        boolean isSuccess = false;
        try {
            isSuccess = ueitHeartRateService.insertUeitHeartRate(heartRate) > 0;
        } catch (Exception e) {
            log.error("【新增心率信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setHeartRate(watchDto.getNumber());
        deviceInfoExtend.setHeartRateTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断心率是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.heartRateRange"));
        log.info("心率是否有异常:" + config);
        if (watchDto.getNumber() < config.getFloat("min") || watchDto.getNumber() > config.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "heart_rate", watchDto.getNumber() + "");
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理血氧数据逻辑
     * 新增血氧
     * 修改设备扩展(血氧值、血氧测量时间、最后通讯时间)
     *
     * @param watchDto
     */
    private AjaxResult handleBloodOxygenData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitSpo2 spo2 = new UeitSpo2();
        spo2.setUserId(userId);
        spo2.setDeviceId(deviceId);
        spo2.setValue(watchDto.getNumber());
        boolean isSuccess = false;
        try {
            isSuccess = ueitSpo2Service.insertUeitSpo2(spo2) > 0;
        } catch (Exception e) {
            log.error("【新增血氧信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setSpo2(watchDto.getNumber());
        deviceInfoExtend.setSpo2Time(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断血氧是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.spo2Range"));
        log.info("血氧是否有异常:" + config);
        if (watchDto.getNumber() < config.getFloat("min") || watchDto.getNumber() > config.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "spo2", watchDto.getNumber() + "");
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理体温数据逻辑
     * 新增体温
     * 修改设备扩展(体温值、体温测量时间、最后通讯时间)
     *
     * @param watchDto
     */
    private AjaxResult handleBodyTemperatureData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitTemp temp = new UeitTemp();
        temp.setUserId(userId);
        temp.setDeviceId(deviceId);
        temp.setValue(watchDto.getNumber());
        boolean isSuccess = false;
        try {
            isSuccess = ueitTempService.insertUeitTemp(temp) > 0;
        } catch (Exception e) {
            log.error("【新增体温信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setTemp(watchDto.getNumber());
        deviceInfoExtend.setTempTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断体温是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.temperatureRange"));
        log.info("体温是否有异常:" + config);
        if (watchDto.getNumber() < config.getFloat("min") || watchDto.getNumber() > config.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "temperature", watchDto.getNumber() + "");
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理告警数据逻辑  异常
     * 新增异常
     * 修改设备扩展(告警类型、告警测量时间、最后通讯时间)
     *
     * @param watchDto
     */
    private AjaxResult handleWarningData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        if (watchDto.getAlarm() != null && watchDto.getAlarm() == 3) {
            exception.setType("sos");
        } else {
            exception.setType(watchDto.getAlarm() + "");
        }
        exception.setLongitude(deviceInfoExtend.getLongitude());
        exception.setLatitude(deviceInfoExtend.getLatitude());
        exception.setLocation(deviceInfoExtend.getLocation());
        exception.setCreateTime(DateUtils.getNowDate());
        boolean isSuccess = false;
        try {
            isSuccess = ueitExceptionService.insertUeitException(exception) > 0;
        } catch (Exception e) {
            log.error("【新增异常信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setAlarmContent(watchDto.getAlarm() + "");
        deviceInfoExtend.setAlarmTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理心跳数据逻辑
     * 对步数表进行维护(新增或修改)，一天内一个设备只能在步数表有一条数据
     * 修改设备扩展(电量、步数、最后通讯时间)
     * 有关步数异常：一个用户一天只有一条步数异常数据
     * 新增步数记录时，判断用户前一天的步数是否异常
     *
     * @param watchDto
     */
    private AjaxResult handleHeartbeatData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitSteps steps = new UeitSteps();
        steps.setUserId(userId);
        steps.setDeviceId(deviceId);
        steps.setDate(new Date());
        // 检查该天是否已经存在步数数据，没有新增，有修改
        UeitSteps ueitSteps = ueitStepsService.selectUeitSteps(steps);
        steps.setValue(watchDto.getStep());
        int i = 0;
        if (ueitSteps == null) {
            try {
                // 新增步数记录时，判断用户前一天的步数是否异常
                // 获取当前日期时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());

                // 将日期减一天
                calendar.add(Calendar.DAY_OF_MONTH, -1);

                // 设置时间为23:59:00
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 00);

                // 获取前一天的日期时间
                Date previousDay = calendar.getTime();

                UeitSteps previousSteps = new UeitSteps();
                previousSteps.setUserId(userId);
                previousSteps.setDeviceId(deviceId);
                previousSteps.setDate(previousDay);
                // 查询昨天的步数数据
                UeitSteps previousSteps1 = ueitStepsService.selectUeitSteps(previousSteps);
                //判断昨天的步数是否有异常,有异常新增到异常表中
                JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.stepsRange"));
                log.info("步数是否有异常:" + config);
                if (previousSteps1 != null) {
                    if (previousSteps1.getValue() < config.getInteger("min") || previousSteps1.getValue() > config.getInteger("max")) {
                        handleStepException(deviceId, userId, deviceInfoExtend, "step", previousSteps1.getValue() + "", previousDay);
                    }
                }
                //新增步数
                i = ueitStepsService.insertUeitSteps(steps);
            } catch (Exception e) {
                log.error("【新增步数信息发生异常】");
                e.printStackTrace();
            }
        } else {
            steps.setId(ueitSteps.getId());
            i = ueitStepsService.updateUeitSteps(steps);
        }
        deviceInfoExtend.setBatteryLevel(watchDto.getVolt());
        deviceInfoExtend.setStep(watchDto.getStep());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        int j = 0;
        try {
            j = ueitDeviceInfoExtendService.updateUeitDeviceInfoExtend(deviceInfoExtend);
        } catch (Exception e) {
            log.error("【新增设备扩展信息发生异常】");
            e.printStackTrace();
        }
        return returnResult(i > 0, j > 0, action, deviceId);
    }

    /**
     * 处理定位数据逻辑
     * 新增定位
     * 修改设备扩展(经度、纬度、详细地址、定位方式、定位时间、最后通讯时间)
     *
     * @param watchDto
     */
    private AjaxResult handleLocationData(WatchDto watchDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        boolean isSuccess = false;
        if (userId != null) {
            //根据userId查询最新一条位置信息
            UeitLocation lastLocation = ueitLocationService.selectUeitLocationByUserId(userId);
            UeitLocation location = new UeitLocation();
            location.setUserId(userId);
            location.setDeviceId(deviceId);
            location.setLatitude(watchDto.getLatitude() + "");
            location.setLongitude(watchDto.getLongitude() + "");
            location.setLocation(watchDto.getLocation());
            location.setType(watchDto.getType());
            isSuccess = false;
            try {
                isSuccess = ueitLocationService.insertUeitLocation(location) > 0;
            } catch (Exception e) {
                log.error("【新增定位信息发生异常】");
                e.printStackTrace();
            }
            deviceInfoExtend.setLongitude(watchDto.getLongitude());
            deviceInfoExtend.setLatitude(watchDto.getLatitude());
            deviceInfoExtend.setLocation(watchDto.getLocation());
            deviceInfoExtend.setType(watchDto.getType());
            deviceInfoExtend.setPositioningTime(new Date());
            deviceInfoExtend.setLastCommunicationTime(new Date());
//            超出围栏的写到异常表
//            1、先查询该用户的围栏，围栏是由圆点（经纬度）、半径组成
//            2、判断该位置是否在报警时段内超出围栏（进入、离开、进入&离开） 比较前一个地址坐标和这个坐标：
//             ①前一个不在围栏，这一个在时 ---》进入
//             ②前一个在围栏，这一个不在时 ---》离开
            //3、超出的添加到异常表
            List<UeitFence> fenceList = ueitFenceService.selectUeitFenceListByUserId(userId);

            List<LatLng> locationHistory = new ArrayList<>();
            locationHistory.add(new LatLng(new BigDecimal(lastLocation.getLatitude()), new BigDecimal(lastLocation.getLongitude())));  // 前一个位置
            locationHistory.add(new LatLng(watchDto.getLatitude(), watchDto.getLongitude()));     // 当前位置

            if (fenceList != null && fenceList.size() > 0) {
                for (UeitFence fence : fenceList) {
                    //是否在报警时段内
                    Date beginReadTime = fence.getBeginReadTime(); //开始
                    Date endReadTime = fence.getEndReadTime();     //结束
                    Date currentDate = new Date(); // 获取当前日期
                    //如果当前日期在开始日期之后或等于开始日期，并且在结束日期之前或等于结束日期，则当前日期在日期段内
                    if (currentDate.compareTo(beginReadTime) >= 0 && currentDate.compareTo(endReadTime) <= 0) {
                        log.info("当前日期在报警日期段内");
                        //检查是否超出围栏（进入、离开、进入&离开） 比较前一个地址坐标和这个坐标
                        LatLng prevLocation = new LatLng(new BigDecimal(lastLocation.getLatitude()), new BigDecimal(lastLocation.getLongitude()));// 前一个位置
                        LatLng currentLocation = new LatLng(watchDto.getLatitude(), watchDto.getLongitude()); // 当前位置
                        boolean prevInFence = isLocationInFence(prevLocation, fence);
                        boolean currentInFence = isLocationInFence(currentLocation, fence);
                        //报警类型：1进入；2离开；3进入&离开
                        if ("1".equals(fence.getWarnType())){
                            if (!prevInFence && currentInFence) {
                                log.info("用户Id：" + userId + " 进入电子围栏");
                                fence.setWarnType("进入");
                                UeitException exception = getFenceException(watchDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                            }
                        } else if ("2".equals(fence.getWarnType())) {
                            if (prevInFence && !currentInFence) {
                                log.info("用户Id：" + userId + " 离开电子围栏");
                                fence.setWarnType("离开");
                                UeitException exception = getFenceException(watchDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                            }
                        } else if ("3".equals(fence.getWarnType())) {
                            if (!prevInFence && currentInFence) {
                                fence.setWarnType("进入");
                                UeitException exception = getFenceException(watchDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                                log.info("用户Id：" + userId + " 进入&离开电子围栏");
                            } else if (prevInFence && !currentInFence) {
                                fence.setWarnType("离开");
                                UeitException exception = getFenceException(watchDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                                log.info("用户Id：" + userId + " 进入&离开电子围栏");
                            }
                        }
                    }
                }
            }
        } else {
            UeitLocation location = new UeitLocation();
            location.setDeviceId(deviceId);
            location.setLatitude(watchDto.getLatitude() + "");
            location.setLongitude(watchDto.getLongitude() + "");
            location.setLocation(watchDto.getLocation());
            location.setType(watchDto.getType());
            isSuccess = false;
            try {
                isSuccess = ueitLocationService.insertUeitLocation(location) > 0;
            } catch (Exception e) {
                log.error("【新增定位信息发生异常】");
                e.printStackTrace();
            }
            deviceInfoExtend.setLongitude(watchDto.getLongitude());
            deviceInfoExtend.setLatitude(watchDto.getLatitude());
            deviceInfoExtend.setLocation(watchDto.getLocation());
            deviceInfoExtend.setType(watchDto.getType());
            deviceInfoExtend.setPositioningTime(new Date());
            deviceInfoExtend.setLastCommunicationTime(new Date());
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }
    //处理电子围栏异常数据
    private UeitException getFenceException(WatchDto watchDto, Long deviceId, Long userId, UeitFence fence) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        exception.setType("fence");
        exception.setValue(fence.getWarnType()); //告警类型
        exception.setLongitude(watchDto.getLongitude());
        exception.setLatitude(watchDto.getLatitude());
        exception.setState("0"); //未处理
        exception.setLocation(watchDto.getLocation());
        exception.setCreateTime(new Date());
        return exception;
    }

    //判断一个位置是否在围栏内，它通过计算两个位置之间的距离与围栏半径进行比较来判断
    private static boolean isLocationInFence(LatLng location, UeitFence fence) {
        LatLng center = new LatLng(new BigDecimal(fence.getLatitude()), new BigDecimal(fence.getLongitude()));
        //计算两个经纬度之间的距离
        double distance = getDistance(Math.toRadians(location.lat.doubleValue()), Math.toRadians(location.lng.doubleValue()), Math.toRadians(center.lat.doubleValue()), Math.toRadians(center.lng.doubleValue()));
        log.info(location.lng + "," + location.lat + "," + center.lng + "," + center.lat + "两个经纬度之间的距离" + distance + "米");
        //比较大小
        return distance <= Double.parseDouble(fence.getRadius());
    }
    //使用哈弗辛Haversine公式进行近似计算
    private static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        //两点纬度之间的差值
        double dlat = lat2 - lat1;
        //两点经度之间的差值
        double dlng = lng2 - lng1;
        //计算经度差值的正弦值的平方，并乘以两点的纬度的余弦值的乘积
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dlng / 2) * Math.sin(dlng / 2);
        //使用反正切函数来计算两点之间的弧度。这里使用的是哈弗辛公式的公式
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        //地球半径约为6371公里，乘以1000转为单位米
        return 6371000 * c;
    }
    /**
     * 处理未知操作逻辑
     */
    private AjaxResult handleUnknownAction() {
        AjaxResult result = new AjaxResult(500, "警告！执行操作不是blood、heart、oxygen、temp、warn、heartbeat、location其中的！");
        log.warn("【---处理未知操作：'{}'】", result.toString());
        return result;
    }

    /**
     * 处理返回信息
     *
     * @param isSuccess
     * @param isSuccessA
     */
    private AjaxResult returnResult(boolean isSuccess, boolean isSuccessA, String action, Long deviceId) {
        AjaxResult result = null;
        if (isSuccess && isSuccessA) {
            result = new AjaxResult(200, "接收成功");
            log.info("【---成功处理执行操作：'{}'，设备id：'{}'，结果：'{}'】", action, deviceId, result.toString());
        } else {
            result = new AjaxResult(500, "处理手表推送数据异常");
            log.error("【---异常处理执行操作：'{}'，设备id：'{}'，结果：'{}'】", action, deviceId, result.toString());
        }
        return result;
    }
    private AjaxResult returnTo(boolean isSuccess, String action, Long deviceId, UeitDeviceInfoExtend deviceInfoExtend) {
        boolean isSuccessA = false;
        try {
            // 修改设备扩展表
            isSuccessA = ueitDeviceInfoExtendService.updateUeitDeviceInfoExtend(deviceInfoExtend) > 0;
        } catch (Exception e) {
            log.error("【新增设备扩展信息发生异常】");
            e.printStackTrace();
        }
        return returnResult(isSuccess, isSuccessA, action, deviceId);
    }
    // 新增异常数据
    private void handleException(Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend, String type, String value) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        exception.setType(type);
        exception.setValue(value);
        exception.setLongitude(deviceInfoExtend.getLongitude());
        exception.setLatitude(deviceInfoExtend.getLatitude());
        exception.setState("0");
        exception.setLocation(deviceInfoExtend.getLocation());
        exception.setCreateTime(DateUtils.getNowDate());
        ueitExceptionService.insertUeitException(exception);
    }
    // 处理步数异常数据
    private void handleStepException(Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend, String type, String value, Date date) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        exception.setType(type);
        exception.setValue(value);
        exception.setLongitude(deviceInfoExtend.getLongitude());
        exception.setLatitude(deviceInfoExtend.getLatitude());
        exception.setState("0");
        exception.setLocation(deviceInfoExtend.getLocation());
        exception.setCreateTime(date);
        ueitExceptionService.insertUeitException(exception);
    }
}
