package org.nefu.softlab.weiboAPI.biz.service.impl;

import com.mongodb.ServerAddress;
import org.nefu.softlab.weiboAPI.biz.service.StatisticsService;
import org.nefu.softlab.weiboAPI.core.DAO.mongo.StatisticsDao;
import org.nefu.softlab.weiboAPI.core.DAO.shell.SSHDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Jiaxu_Zou on 2018-4-7
 */
@Service
public class StatisticsServiceImpl implements StatisticsService{

    // mapper

    // mongo dao
    private final StatisticsDao statisticsDao;

    // shell dao
    private final SSHDao sshDao;

    // logger
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired
    public StatisticsServiceImpl(StatisticsDao statisticsDao, SSHDao sshDao) {
        this.statisticsDao = statisticsDao;
        this.sshDao = sshDao;
    }

    @Override
    public List<Map<String, Object>> getSplitedStatistics() {
        return statisticsDao.getSplitedStatistics();
    }

    @Override
    public Map<String, Object> getTotalStatistics() {
        List<Map<String, Object>> dataList = getSplitedStatistics();
        Map<String, Object> returnMap = new HashMap<>();
        if (dataList == null || dataList.size() == 0)
            return null;
        // 配置数据库服务器数目信息
        returnMap.put("serverCount", StatisticsDao.getClients().size());
        returnMap.put("okCount", dataList.size());
        // 初始化统计信息
        returnMap.put("count", 0L);
        returnMap.put("size", 0.0D);
        returnMap.put("avgSize", 0.0D);
        returnMap.put("storageSize", 0.0D);
        returnMap.put("totalIndexSize", 0.0D);
        returnMap.put("hosts", new ArrayList<Map<String, Object>>());
        // 配置数据统计信息
        dataList.stream()
                .forEach(map -> {
                    returnMap.put("count", ((long)returnMap.get("count") + (int)map.get("count")));
                    returnMap.put("size", ((double)returnMap.get("size") + (double)map.get("size")));
                    returnMap.put("avgSize", ((double)returnMap.get("avgSize") + (int)map.get("avgSize")));
                    returnMap.put("storageSize", ((double)returnMap.get("storageSize") + (double)map.get("storageSize")));
                    returnMap.put("totalIndexSize", ((double)returnMap.get("totalIndexSize") + (double)map.get("totalIndexSize")));
                    ((List<Object>)returnMap.get("hosts")).add(map.get("host"));
                });
        // 计算平均值并返回
        returnMap.put("avgSize", ((double)returnMap.get("avgSize") / dataList.size()));
        return returnMap;
    }

    @Override
    public Map<String, Object> getSpecificServerStatistics(String socket) {
        // 调用方法getSplitedStatistics获取每台机器的信息
        List<Map<String, Object>> dataList = getSplitedStatistics();
        for (Map<String, Object> stringObjectMap : dataList) {  // 1.7
            String socketAddr = ((ServerAddress)stringObjectMap.get("host")).getSocketAddress().toString()
                    .replace(".", "-").replace(":", "-").replace("/", "");
            if (socketAddr.equals(socket) == true)  // 找到了
                return stringObjectMap;
        }
        return null;    // 没找到
//        return dataList.stream()    // 1.8，操作不够清晰故注释
//                .filter(map -> {
//                    return ((Map<String, Object>)map.get("host")).get("socketAddress").toString().equals(socket) == true;  // 利用套接字进行过滤，找出相同套接字的map
//                }).collect(Collectors.toList())
//        .get(0);    // 返回
    }

    @Override
    public List<Map<String, Object>> getMemoryStatus() {
        return sshDao.getServerMemStatus();
    }

    @Override
    public List<Map<String, Object>> getDiskSpaceStatus() {
        return sshDao.getServerDiskStatus();
    }
}