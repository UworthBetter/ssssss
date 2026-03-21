package com.qkyd.health.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.qkyd.common.constant.WatchBNPushActionType;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.utils.DateUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.health.domain.*;
import com.qkyd.health.domain.dto.LatLng;
import com.qkyd.health.domain.dto.WatchBNDto;
import com.qkyd.health.service.*;
import com.qkyd.system.service.ISysConfigService;
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
 * 5C-BNB02Y类型的手表数据Service业务层处理
 */
@Service
public class WatchBNServiceImpl implements WatchBNService {
    //日志
    private static final Logger log = LoggerFactory.getLogger(WatchBNServiceImpl.class);
    //注入需要的类
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
     * @param watchBNDto
     * @return
     */
    @Override
    public AjaxResult handle(WatchBNDto watchBNDto) {
        log.info("【---开始处理5C-BNB02Y类型的手表推送数据,执行包：'{}'】", watchBNDto.toString());
        if (watchBNDto == null) {
            return new AjaxResult(500, "手表推送数据为空！");
        }

        // 处理设备信息逻辑
        // 判断IMEI信息是否为空，为空时，不再进行后续操作
        String imei = watchBNDto.getImei();
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
                log.error("【新增5C-BNB02Y类型的设备信息发生异常】");
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
                log.error("【新增5C-BNB02Y类型的设备扩展信息发生异常】");
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
                    log.error("【新增5C-BNB02Y类型的设备扩展信息发生异常】");
                    e.printStackTrace();
                }
            }
        }
        // 获取执行操作 action
        String action = watchBNDto.getAction();
        log.info("【-5C-BNB02Y类型--处理中，执行操作：'{}'，设备id：'{}'，用户id：'{}'】", action, deviceId, userId);
        // 返回信息
        AjaxResult result = null;
        switch (action) {
            case WatchBNPushActionType.BLOOD:
                // 处理血压数据
                result = handleBloodPressureData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchBNPushActionType.OXYGEN:
                // 处理血氧数据
                result = handleBloodOxygenData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchBNPushActionType.TEMP:
                // 处理体温数据
                result = handleBodyTemperatureData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchBNPushActionType.WARN:
                // 处理告警数据
                result = handleWarningData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchBNPushActionType.HEARTBEAT:
                // 处理心跳数据
                result = handleHeartbeatData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
                break;
            case WatchBNPushActionType.LOCATION:
                // 处理定位数据
                result = handleLocationData(watchBNDto, action, deviceId, userId, deviceInfoExtend);
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
     *
     * @param watchBNDto
     */
    private AjaxResult handleBloodPressureData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitBlood blood = new UeitBlood();
        blood.setUserId(userId);
        blood.setDeviceId(deviceId);
        Integer diastolic = watchBNDto.getDiastolic();
        blood.setDiastolic(diastolic);
        Integer systolic = watchBNDto.getSystolic();
        blood.setSystolic(systolic);
        // 记录新增血压操作是否成功
        boolean isBloodSuccess = false;
        try {
            isBloodSuccess = ueitBloodService.insertUeitBlood(blood) > 0;
        } catch (Exception e) {
            log.error("【新增血压信息发生异常】");
            e.printStackTrace();
        }

        // 处理心率数据逻辑
        UeitHeartRate heartRate = new UeitHeartRate();
        heartRate.setUserId(userId);
        heartRate.setDeviceId(deviceId);
        heartRate.setValue(watchBNDto.getHeart() != null ? (float) watchBNDto.getHeart() : 0.0f);
        // 记录新增心率操作是否成功
        boolean isHeartSuccess = false;
        try {
            isHeartSuccess = ueitHeartRateService.insertUeitHeartRate(heartRate) > 0;
        } catch (Exception e) {
            log.error("【新增心率信息发生异常】");
            e.printStackTrace();
        }
        //判断心率是否有异常,有异常新增到异常表中
        JSONObject heartConfig = JSONObject.parseObject(configService.selectConfigByKey("sys.data.heartRateRange"));
        log.info("心率是否有异常:" + heartConfig);
        if (watchBNDto.getHeart() < heartConfig.getFloat("min") || watchBNDto.getHeart() > heartConfig.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "heart_rate", watchBNDto.getHeart() + "");
        }

        deviceInfoExtend.setBloodDiastolic(diastolic);
        deviceInfoExtend.setBloodSystolic(systolic);
        deviceInfoExtend.setBloodTime(new Date());
        deviceInfoExtend.setHeartRate(watchBNDto.getHeart() != null ? (float) watchBNDto.getHeart() : 0.0f);
        deviceInfoExtend.setHeartRateTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断血压是否有异常,有异常新增到异常表中
        JSONObject bloodConfig = JSONObject.parseObject(configService.selectConfigByKey("sys.data.bloodRange"));
        log.info("血压是否有异常:" + bloodConfig);
        JSONObject diastolicObj = JSONObject.parseObject(bloodConfig.getString("diastolic"));
        JSONObject systolicObj = JSONObject.parseObject(bloodConfig.getString("systolic"));
        if (diastolic < diastolicObj.getInteger("min") || diastolic > diastolicObj.getInteger("max") || systolic < systolicObj.getInteger("min") || systolic > systolicObj.getInteger("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "blood", diastolic + "," + systolic);
        }
        Boolean isSuccess = isBloodSuccess && isHeartSuccess;
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理血氧数据逻辑
     * 新增血氧
     * 修改设备扩展(血氧值、血氧测量时间、最后通讯时间)
     *
     * @param watchBNDto
     */
    private AjaxResult handleBloodOxygenData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitSpo2 spo2 = new UeitSpo2();
        spo2.setUserId(userId);
        spo2.setDeviceId(deviceId);
        spo2.setValue(watchBNDto.getData() != null ? (float) watchBNDto.getData() : 0.0f);
        boolean isSuccess = false;
        try {
            isSuccess = ueitSpo2Service.insertUeitSpo2(spo2) > 0;
        } catch (Exception e) {
            log.error("【新增血氧信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setSpo2(watchBNDto.getData() != null ? (float) watchBNDto.getData() : 0.0f);
        deviceInfoExtend.setSpo2Time(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断血氧是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.spo2Range"));
        log.info("血氧是否有异常:" + config);
        if (watchBNDto.getData() < config.getFloat("min") || watchBNDto.getData() > config.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "spo2", watchBNDto.getData() + "");
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理体温数据逻辑
     * 新增体温
     * 修改设备扩展(体温值、体温测量时间、最后通讯时间)
     *
     * @param watchBNDto
     */
    private AjaxResult handleBodyTemperatureData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitTemp temp = new UeitTemp();
        temp.setUserId(userId);
        temp.setDeviceId(deviceId);
        temp.setValue(watchBNDto.getTemp());
        boolean isSuccess = false;
        try {
            isSuccess = ueitTempService.insertUeitTemp(temp) > 0;
        } catch (Exception e) {
            log.error("【新增体温信息发生异常】");
            e.printStackTrace();
        }
        deviceInfoExtend.setTemp(watchBNDto.getTemp());
        deviceInfoExtend.setTempTime(new Date());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        //判断体温是否有异常,有异常新增到异常表中
        JSONObject config = JSONObject.parseObject(configService.selectConfigByKey("sys.data.temperatureRange"));
        log.info("体温是否有异常:" + config);
        if (watchBNDto.getTemp() < config.getFloat("min") || watchBNDto.getTemp() > config.getFloat("max")) {
            handleException(deviceId, userId, deviceInfoExtend, "temperature", watchBNDto.getTemp() + "");
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    /**
     * 处理告警数据逻辑  异常
     * 新增异常
     * 修改设备扩展(告警类型、告警测量时间、最后通讯时间)
     *
     * @param watchBNDto
     */
    private AjaxResult handleWarningData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        if (watchBNDto.getData() != null && watchBNDto.getData() == 3) {
            exception.setType("sos");
        } else {
            exception.setType(watchBNDto.getData() + "");
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
        deviceInfoExtend.setAlarmContent(watchBNDto.getData() + "");
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
     * @param watchBNDto
     */
    private AjaxResult handleHeartbeatData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        UeitSteps steps = new UeitSteps();
        steps.setUserId(userId);
        steps.setDeviceId(deviceId);
        steps.setDate(new Date());
        // 检查该天是否已经存在步数数据，没有新增，有修改
        UeitSteps ueitSteps = ueitStepsService.selectUeitSteps(steps);
        steps.setValue(watchBNDto.getStep());
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
        deviceInfoExtend.setBatteryLevel(watchBNDto.getVolt());
        deviceInfoExtend.setStep(watchBNDto.getStep());
        deviceInfoExtend.setLastCommunicationTime(new Date());
        int j = 0;
        try {
            j = ueitDeviceInfoExtendService.updateUeitDeviceInfoExtend(deviceInfoExtend);
        } catch (Exception e) {
            log.error("【新增5C-BNB02Y类型的设备扩展信息发生异常】");
            e.printStackTrace();
        }
        return returnResult(i > 0, j > 0, action, deviceId);
    }

    /**
     * 处理定位数据逻辑
     * 新增定位
     * 修改设备扩展(经度、纬度、详细地址、定位方式、定位时间、最后通讯时间)
     *
     * @param watchBNDto
     */
    private AjaxResult handleLocationData(WatchBNDto watchBNDto, String action, Long deviceId, Long userId, UeitDeviceInfoExtend deviceInfoExtend) {
        boolean isSuccess = false;
        if (userId != null) {
            //根据userId查询最新一条位置信息
            UeitLocation lastLocation = ueitLocationService.selectUeitLocationByUserId(userId);
            UeitLocation location = new UeitLocation();
            location.setUserId(userId);
            location.setDeviceId(deviceId);
            location.setLatitude(watchBNDto.getLatitude() + "");
            location.setLongitude(watchBNDto.getLongitude() + "");
            location.setLocation(watchBNDto.getLocation());
//            location.setType(watchBNDto.getType());
            isSuccess = false;
            try {
                isSuccess = ueitLocationService.insertUeitLocation(location) > 0;
            } catch (Exception e) {
                log.error("【新增定位信息发生异常】");
                e.printStackTrace();
            }
            deviceInfoExtend.setLongitude(watchBNDto.getLongitude());
            deviceInfoExtend.setLatitude(watchBNDto.getLatitude());
            deviceInfoExtend.setLocation(watchBNDto.getLocation());
//            deviceInfoExtend.setType(watchBNDto.getType());
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
            locationHistory.add(new LatLng(watchBNDto.getLatitude(), watchBNDto.getLongitude()));     // 当前位置

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
                        LatLng currentLocation = new LatLng(watchBNDto.getLatitude(), watchBNDto.getLongitude()); // 当前位置
                        boolean prevInFence = isLocationInFence(prevLocation, fence);
                        boolean currentInFence = isLocationInFence(currentLocation, fence);
                        //报警类型：1进入；2离开；3进入&离开
                        if ("1".equals(fence.getWarnType())) {
                            if (!prevInFence && currentInFence) {
                                log.info("用户Id：" + userId + " 进入电子围栏");
                                fence.setWarnType("进入");
                                UeitException exception = getFenceException(watchBNDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                            }
                        } else if ("2".equals(fence.getWarnType())) {
                            if (prevInFence && !currentInFence) {
                                log.info("用户Id：" + userId + " 离开电子围栏");
                                fence.setWarnType("离开");
                                UeitException exception = getFenceException(watchBNDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                            }
                        } else if ("3".equals(fence.getWarnType())) {
                            if (!prevInFence && currentInFence) {
                                fence.setWarnType("进入");
                                UeitException exception = getFenceException(watchBNDto, deviceId, userId, fence);
                                //写到异常表
                                ueitExceptionService.insertUeitException(exception);
                                log.info("用户Id：" + userId + " 进入&离开电子围栏");
                            } else if (prevInFence && !currentInFence) {
                                fence.setWarnType("离开");
                                UeitException exception = getFenceException(watchBNDto, deviceId, userId, fence);
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
            location.setLatitude(watchBNDto.getLatitude() + "");
            location.setLongitude(watchBNDto.getLongitude() + "");
            location.setLocation(watchBNDto.getLocation());
//            location.setType(watchBNDto.getType());
            isSuccess = false;
            try {
                isSuccess = ueitLocationService.insertUeitLocation(location) > 0;
            } catch (Exception e) {
                log.error("【新增定位信息发生异常】");
                e.printStackTrace();
            }
            deviceInfoExtend.setLongitude(watchBNDto.getLongitude());
            deviceInfoExtend.setLatitude(watchBNDto.getLatitude());
            deviceInfoExtend.setLocation(watchBNDto.getLocation());
//            deviceInfoExtend.setType(watchBNDto.getType());
            deviceInfoExtend.setPositioningTime(new Date());
            deviceInfoExtend.setLastCommunicationTime(new Date());
        }
        return returnTo(isSuccess, action, deviceId, deviceInfoExtend);
    }

    //处理电子围栏异常数据
    private UeitException getFenceException(WatchBNDto watchBNDto, Long deviceId, Long userId, UeitFence fence) {
        UeitException exception = new UeitException();
        exception.setUserId(userId);
        exception.setDeviceId(deviceId);
        exception.setType("fence");
        exception.setValue(fence.getWarnType()); //告警类型
        exception.setLongitude(watchBNDto.getLongitude());
        exception.setLatitude(watchBNDto.getLatitude());
        exception.setState("0"); //未处理
        exception.setLocation(watchBNDto.getLocation());
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
        AjaxResult result = new AjaxResult(500, "警告！执行操作不是HEARTRATE、OXYGEN、TEMP、WARN、HEARTBEAT、LOCATION其中的！");
        log.warn("【-5C-BNB02Y类型的--处理未知操作：'{}'】", result.toString());
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
            log.info("【-5C-BNB02Y类型的--成功处理执行操作：'{}'，设备id：'{}'，结果：'{}'】", action, deviceId, result.toString());
        } else {
            result = new AjaxResult(500, "处理手表推送数据异常");
            log.error("【-5C-BNB02Y类型的--异常处理执行操作：'{}'，设备id：'{}'，结果：'{}'】", action, deviceId, result.toString());
        }
        return result;
    }

    // 处理设备表的最后信息
    private AjaxResult returnTo(boolean isSuccess, String action, Long deviceId, UeitDeviceInfoExtend deviceInfoExtend) {
        boolean isSuccessA = false;
        try {
            // 修改设备扩展表
            isSuccessA = ueitDeviceInfoExtendService.updateUeitDeviceInfoExtend(deviceInfoExtend) > 0;
        } catch (Exception e) {
            log.error("【新增5C-BNB02Y类型的设备扩展信息发生异常】");
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

