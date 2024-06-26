package com.dobbinsoft.fw.support.log;

/**
 * LogRecord 日志记录持久层
 */
public interface LogRecordPersistent {

    /**
     * @param content 日志正文
     * @param success 操作是否成功
     */
    public void write(String content, boolean success);

}
