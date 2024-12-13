package com.dobbinsoft.fw.support.mq;

public interface DelayedMessageRedisKeyParser {

    public String[] parse(String key);

}
