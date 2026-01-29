package com.ueit.health.service.impl;
import com.alibaba.fastjson2.*;
import com.ueit.common.utils.StringUtils;
import com.ueit.health.domain.UeitDeviceInfo;
import com.ueit.health.domain.dto.RealTimeTracking;
import com.ueit.health.domain.dto.UserDevice;
import com.ueit.health.service.*;
import com.ueit.system.service.ISysDictDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);
    @Autowired
    private IUeitHeartRateService heartRateService;
    @Autowired
    private IUeitBloodService bloodService;
    @Autowired
    private IUeitSpo2Service spo2Service;
    @Autowired
    private IUeitTempService tempService;
    @Autowired
    private IUeitLocationService locationService;
    @Autowired
    private IUeitStepsService stepsService;
    @Autowired
    private IUeitExceptionService exceptionService;
    @Autowired
    private IUeitDeviceInfoService ueitDeviceInfoService;
    @Autowired
    private ISysDictDataService dictDataService;

    /**
     * 健康数据看板 七种类型
     *
     * @param userId
     * @param type
     * @param beginReadTime
     * @param endReadTime
     * @return
     */
    @Override
    public synchronized List getDataBoard(int userId, String type, Date beginReadTime, Date endReadTime) {
        log.info("【---操作：'{}'，用户id：'{}'】", type, userId);
        // 返回信息
        List result = null;
        switch (type) {
            case "blood":
                // 查询血压数据
                result = getBloodPressureData(userId, type, beginReadTime, endReadTime);
                break;
            case "heartRate":
                // 查询心率数据
                result = getHeartRateData(userId, type, beginReadTime, endReadTime);
                break;
            case "spo2":
                // 查询血氧数据
                result = getBloodOxygenData(userId, type, beginReadTime, endReadTime);
                break;
            case "temperature":
                // 查询体温数据
                result = getBodyTemperatureData(userId, type, beginReadTime, endReadTime);
                break;
            case "alarm":
                // 查询告警数据
                result = getWarningData(userId, type, beginReadTime, endReadTime);
                break;
            case "steps":
                // 查询步数数据
                result = getStepsData(userId, type, beginReadTime, endReadTime);
                break;
            case "location":
                // 查询定位数据
                result = getLocationData(userId, type, beginReadTime, endReadTime);
                break;
            default:
                // 未知操作
                result = getUnknownAction();
        }
        return result;
    }

    /**
     * 查询用户设备列表
     *
     * @param userDevice
     * @return
     */
    @Override
    public List<UserDevice> getUserDevice(UserDevice userDevice) {
        return ueitDeviceInfoService.getUserDevice(userDevice);
    }

    /**
     * 查询没有绑定用户的设备列表
     *
     * @return
     */
    @Override
    public List<UeitDeviceInfo> getDeviceListWithoutUser(UeitDeviceInfo ueitDeviceInfo) {
        List deviceListWithoutUser = ueitDeviceInfoService.getDeviceListWithoutUser(ueitDeviceInfo);
        return deviceListWithoutUser;
    }

    /**
     * 把设备绑定给某个用户
     *
     * @return
     */
    @Override
    public int addDeviceToUser(Long[] deviceIds, int userId) {
        int i = 0;
        if (deviceIds != null && deviceIds.length > 0) {
            for (Long id : deviceIds) {
                ueitDeviceInfoService.addDeviceToUser(id, userId);
                i++;
            }
        }
        return i;
    }

    /**
     * 实时跟踪
     * @param coordinateType
     * @param userId
     * @return
     */
    @Override
    public RealTimeTracking realTimeTracking(String coordinateType, int userId) {
        RealTimeTracking realTimeTracking = locationService.realTimeTracking(coordinateType, userId);
        return realTimeTracking;
    }

    /**
     * 历史轨迹 --- 把GPS定位获得的转换成高德经纬度
     * @param coordinateType
     * @param beginReadTime
     * @param endReadTime
     * @param userId
     * @return
     */
    @Override
    public List<RealTimeTracking> pathList(String coordinateType, Date beginReadTime, Date endReadTime, int userId) {
        //存放转换后的经纬度
//        ArrayList<RealTimeTracking> trackingList = new ArrayList<>();
//        List<RealTimeTracking> realTimeTrackings = locationService.pathList(coordinateType, beginReadTime, endReadTime, userId);
//        if(realTimeTrackings != null && realTimeTrackings.size() > 0) {
//            for (RealTimeTracking realTimeTracking : realTimeTrackings) {
//                //GPS定位获得的转换成高德经纬度
//                if (realTimeTracking.getType() != null && "1".equals(realTimeTracking.getType())) {
//                    log.info("转换前经度：{}，转换前纬度：{}",realTimeTracking.getLongitude(),realTimeTracking.getLatitude());
//                    BigDecimal latitude = new BigDecimal(realTimeTracking.getLatitude());
//                    double lat = latitude.doubleValue();
//                    BigDecimal longitude = new BigDecimal(realTimeTracking.getLongitude());
//                    double lng = longitude.doubleValue();
//                    CoordinateUtil.Coordinate gpsCoordinate = new CoordinateUtil.Coordinate(lat, lng);
//                    CoordinateUtil.Coordinate gdCoordinate = CoordinateUtil.gpsToGd(gpsCoordinate);
//                    log.info("高德经度：{}，高德纬度：{}", gdCoordinate.longitude, gdCoordinate.latitude);
//                    realTimeTracking.setLongitude(gdCoordinate.longitude + "");
//                    realTimeTracking.setLatitude(gdCoordinate.latitude + "");
//                } else {
//                    //2基站和3WIFI的直接放到trackingList中
//                    trackingList.add(realTimeTracking);
//                }
//            }
//        }
//        return realTimeTrackings;
        List<RealTimeTracking> pathList = locationService.pathList(coordinateType, beginReadTime, endReadTime, userId);
        /**
        //获取步行路线轨迹
        List<RealTimeTracking> resultList = new ArrayList<>();
        //在遍历中同时获取两个相邻的元素
        for (int i = 0; i < pathList.size() - 1; i++) {
            RealTimeTracking currentPath = pathList.get(i);
            RealTimeTracking nextPath = pathList.get(i + 1);
            try {
                // 设置起点和终点的经纬度
                String origin = currentPath.getLongitude() + "," + currentPath.getLatitude();
                String destination = nextPath.getLongitude() + "," + nextPath.getLatitude();

                // 构建URL
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append("https://restapi.amap.com/v3/direction/walking?");
                urlBuilder.append("origin=").append(URLEncoder.encode(origin, "UTF-8"));
                urlBuilder.append("&destination=").append(URLEncoder.encode(destination, "UTF-8"));
                urlBuilder.append("&key=cc20443bcb042b7b65de4aa3c45ef375");

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    JSONObject jsonResult = JSON.parseObject(result.toString());
                    String routeS = jsonResult.get("route") + "";
                    JSONObject route = JSON.parseObject(routeS);
                    if (StringUtils.isNotEmpty(route)) {
                        JSONArray paths = route.getJSONArray("paths");
                        if (paths != null && pathList.size() > 0) {
                            for (Object pathO : paths) {
                                String pathS = JSON.toJSON(pathO).toString(); // 将Object转换为JSON字符串
                                JSONObject path = JSON.parseObject(pathS, JSONObject.class); // 将JSON字符串转换为JsonObject对象
                                JSONArray steps = path.getJSONArray("steps");
                                if (steps != null && steps.size() > 0) {
                                    for (Object stepO : steps) {
                                        String stepS = JSON.toJSON(stepO).toString(); // 将Object转换为JSON字符串
                                        JSONObject step = JSON.parseObject(stepS, JSONObject.class); // 将JSON字符串转换为JsonObject对象
                                        //polyline是"113.575747,34.766762;113.575495,34.766771"这样的
                                        String polyline = step.getString("polyline");
                                        if (StringUtils.isNotEmpty(polyline)) {
                                            //把各个坐标分开
                                            String[] polylineArr = polyline.split(";");
                                            for (String s : polylineArr) {
                                                if(StringUtils.isNotEmpty(s)) {
                                                    //把经纬度分开
                                                    String[] ll = s.split(",");
                                                    if (ll != null && ll.length > 0) {
                                                        RealTimeTracking tracking = new RealTimeTracking();
                                                        tracking.setLongitude(ll[0]);
                                                        tracking.setLatitude(ll[1]);
                                                        resultList.add(tracking);
                                                        log.info("经纬度--》 Latitude: " + ll[1] + ", Longitude: " + ll[0]);
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                    reader.close();
                } else {
                    throw new IOException("Error code: " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String resultListStr = JSON.toJSONString(resultList);
        log.info("高德步行规划路线：" + resultListStr);
         **/
        return pathList;
    }

    /**
     * 通过UserId查询用户设备列表
     * @param userId
     * @return
     */
    @Override
    public List getUserDeviceByUserId(int userId) {
        return ueitDeviceInfoService.getUserDeviceByUserId(userId);
    }

    private List getBloodPressureData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return bloodService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getHeartRateData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return heartRateService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getLocationData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return locationService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getStepsData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return stepsService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getWarningData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return exceptionService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getBodyTemperatureData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return tempService.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getBloodOxygenData(int userId, String type, Date beginReadTime, Date endReadTime) {
        return spo2Service.getDataBoard(userId, beginReadTime, endReadTime);
    }

    private List getUnknownAction() {
        return null;
    }
}

