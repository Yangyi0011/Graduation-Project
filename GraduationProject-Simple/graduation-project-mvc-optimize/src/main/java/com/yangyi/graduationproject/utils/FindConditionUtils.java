package com.yangyi.graduationproject.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangyi.graduationproject.exception.FindConditionMapException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义查询条件工具类
 */
public class FindConditionUtils {
    /**
     * 构建自定义查询条件 key-value
     *
     * @param clazz：需要做条件查询的数据表所对应的类
     * @param conditionJsonStr：查询条件的json字符串
     * @return 返回 key-value 的自定义查询条件，查询形式 如 where key=value
     */
    public static Map<String, Object> findConditionBuild(Class<?> clazz, String conditionJsonStr){
        FindConditionMapException findConditionMapException;
        //得到一个线程安全的 HashMap
        Map<String, Object> condition = new ConcurrentHashMap<>();
        if (StringUtil.isNotEmpty(conditionJsonStr)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                condition = mapper.readValue(conditionJsonStr, ConcurrentHashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
                findConditionMapException = new FindConditionMapException("查询条件映射失败，json转换异常");
                throw findConditionMapException;
            }
        }

        //判断查询条件在对应表中是否存在
        if (condition.size() > 0) {
            //得到一个线程安全的 HashSet
            Collection<String> fieldNames = Collections.synchronizedSet(new HashSet<String>());
            Field[] fields = clazz.getDeclaredFields(); //获取查询对象的所有字段
            for (Field f : fields) {
                String name = f.getName();
                fieldNames.add(name);
            }
            Set<String> keySet = Collections.synchronizedSet(condition.keySet());
            for (String conditionKey : keySet) {
                //searchContent：模糊查询的key
                if (!conditionKey.equals("searchContent") && !fieldNames.contains(conditionKey)) {
                    findConditionMapException = new FindConditionMapException("查询条件映射失败，查询字段在对应表中不存在");
                    throw findConditionMapException;
                }
            }
        }
        return condition;
    }
} 