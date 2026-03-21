//package com.qkyd.quartz.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//import jakarta.sql.DataSource;
//import java.util.Properties;
//
///**
// * 瀹氭椂浠诲姟閰嶇疆锛堝崟鏈洪儴缃插缓璁垹闄ゆ绫诲拰qrtz鏁版嵁搴撹〃锛岄粯璁よ蛋鍐呭瓨浼氭渶楂樻晥锛?
// * 
// * @author qkyd
// */
//@Configuration
//public class ScheduleConfig
//{
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource)
//    {
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//        factory.setDataSource(dataSource);
//
//        // quartz鍙傛暟
//        Properties prop = new Properties();
//        prop.put("org.quartz.scheduler.instanceName", "QkydScheduler");
//        prop.put("org.quartz.scheduler.instanceId", "AUTO");
//        // 绾跨▼姹犻厤缃?
//        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
//        prop.put("org.quartz.threadPool.threadCount", "20");
//        prop.put("org.quartz.threadPool.threadPriority", "5");
//        // JobStore閰嶇疆
//        prop.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
//        // 闆嗙兢閰嶇疆
//        prop.put("org.quartz.jobStore.isClustered", "true");
//        prop.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
//        prop.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "10");
//        prop.put("org.quartz.jobStore.txIsolationLevelSerializable", "true");
//
//        // sqlserver 鍚敤
//        // prop.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
//        prop.put("org.quartz.jobStore.misfireThreshold", "12000");
//        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
//        factory.setQuartzProperties(prop);
//
//        factory.setSchedulerName("QkydScheduler");
//        // 寤舵椂鍚姩
//        factory.setStartupDelay(1);
//        factory.setApplicationContextSchedulerContextKey("applicationContextKey");
//        // 鍙€夛紝QuartzScheduler
//        // 鍚姩鏃舵洿鏂板繁瀛樺湪鐨凧ob锛岃繖鏍峰氨涓嶇敤姣忔淇敼targetObject鍚庡垹闄rtz_job_details琛ㄥ搴旇褰曚簡
//        factory.setOverwriteExistingJobs(true);
//        // 璁剧疆鑷姩鍚姩锛岄粯璁や负true
//        factory.setAutoStartup(true);
//
//        return factory;
//    }
//}


