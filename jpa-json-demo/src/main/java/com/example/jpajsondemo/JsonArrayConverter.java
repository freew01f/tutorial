package com.example.jpajsondemo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import javax.persistence.AttributeConverter;
import java.util.List;

public class JsonArrayConverter<T> implements AttributeConverter<List<T>, String> {
    private Class<T> type;

    public JsonArrayConverter(Class<T> type) {
        this.type = type;
    }
    @Override
    public String convertToDatabaseColumn(List<T> ts) {
        return JSON.toJSONString(ts);
    }

    @Override
    public List<T> convertToEntityAttribute(String s) {
        return JSONArray.parseArray(s, type);
    }
}
