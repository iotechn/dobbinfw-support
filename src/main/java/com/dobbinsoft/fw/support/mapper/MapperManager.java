package com.dobbinsoft.fw.support.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MapperManager
 * Description: 用来管理 实体 -> Mapper 映射的工具
 */
@Slf4j
@Component
public class MapperManager implements InitializingBean {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${com.dobbinsoft.db.auto-build-table:false}")
    private Boolean enableAutoTable;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, IMapper> beansOfType = applicationContext.getBeansOfType(IMapper.class);
        Map<Class<?>, IMapper> map = new HashMap<>();
        beansOfType.forEach((k, v) -> {
            Class<? extends IMapper> clazz = v.getClass();
            Type[] genericInterfaces = clazz.getGenericInterfaces();
            for (Type type : genericInterfaces) {
                if (type instanceof Class) {
                    Class<?> mapperClazz = (Class<?>) type;
                    Type[] iMapperTypes = mapperClazz.getGenericInterfaces();
                    for (Type t : iMapperTypes) {
                        if (t instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) t;
                            if (IMapper.class.isAssignableFrom((Class<?>) pt.getRawType())) {
                                // 若是IMapper的子类
                                Class<?> entityClass = (Class<?>) pt.getActualTypeArguments()[0];
                                map.put(entityClass, v);
                            }
                        }
                    }
                }
            }
        });
        // 自动建表
        if (this.enableAutoTable) {
            map.forEach((k, v) -> {
                v.tableInit();
                log.info("[初始化表] " + k.getSimpleName());
            });
        }
    }
}
