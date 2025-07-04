package com.dobbinsoft.fw.support.config.db.cache;

import com.dobbinsoft.fw.core.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public interface CacheKeyConditionProvider {

    public String provideKey();

    @Component
    class UserId implements CacheKeyConditionProvider {

        @Autowired
        private SessionUtil sessionUtil;

        @Override
        public String provideKey() {
            return sessionUtil.getUser().getId().toString();
        }
    }


}
