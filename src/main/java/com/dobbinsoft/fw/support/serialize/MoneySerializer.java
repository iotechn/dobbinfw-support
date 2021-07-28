package com.dobbinsoft.fw.support.serialize;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class MoneySerializer implements ObjectSerializer, ObjectDeserializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.write(((int)object / 100.0F) + "");
    }

    @Override
    public Integer deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        try {
            String parse = (String) parser.parse();
            Integer res = (int) (Float.parseFloat(parse) * 100);
            return res;
        } catch (NumberFormatException e) {
            throw new SerializerException("价格或金额格式不正确");
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
