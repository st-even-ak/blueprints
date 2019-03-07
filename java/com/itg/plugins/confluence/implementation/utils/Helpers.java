/*
 * Author: steve.killelay
 * Last Updated: 21/02/19 22:13
 *
 * Copyright {c} 2019, ITG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,  this list of conditions and the following disclaimer in the documentation  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.itg.plugins.confluence.implementation.utils;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.itg.plugins.confluence.implementation.abstrct.AbstractModels;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.util.*;

/**
 *
 * @author ITG
 * @param <T>
 */
public class Helpers<T extends AbstractModels> {

    public static JSONObject mapToJson(Map<String, Object> data) {

        JSONObject json = new JSONObject();
        if (data == null) {
            return null;
        }
        try {
            data.forEach((key, value) -> json.put(key, value));
            return json;
        } catch (JSONException e) {

        }
        return null;
    }

    public static JSONArray mapToJson(List<Map<String, Object>> data) {

        JSONArray json = new JSONArray();
        if (data == null) {
            return null;
        }
        try {
            data.stream().map((datum) -> {
                JSONObject obj = new JSONObject();
                datum.forEach((key, value) -> obj.put(key, value));
                return obj;
            }).forEachOrdered(json::put);

        } catch (JSONException e) {

        }
        return json;
    }

    private static HashMap<String, Object> jsonToMap(JSONObject object) throws JSONException {

        HashMap<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private static List<Object> jsonToList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = jsonToList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static <T extends AbstractModels> HashMap<String, Object> createParamMap(T model) {

        HashMap<String, Object> params = new HashMap<>();
        try {

            Class<?> cls = model.getClass();
            Field[] flds = cls.getDeclaredFields();
            for (Field fld : flds) {
                try {
                    fld.setAccessible(true);
                    Object value = fld.get(model);

                    if (fld.isAnnotationPresent(XmlElement.class) && (fld.getDeclaredAnnotation(XmlElement.class).required() && null != value)) {
                        params.put(fld.getDeclaredAnnotation(XmlElement.class).name(), value);
                    }
                } catch (IllegalAccessException e) {

                }
            }
        } catch (IllegalArgumentException e) {
        }
        return params;
    }

    public static <T extends AbstractModels> String getFieldAlias(T model, String fieldName) {

        Class<?> cls = model.getClass();
        Field[] flds = cls.getDeclaredFields();
        for (Field fld : flds) {
            fld.setAccessible(true);
            if (fld.isAnnotationPresent(XmlElement.class) && fld.getDeclaredAnnotation(XmlElement.class).name().equals(fieldName)) {

                return model.getFieldAlias(fld.getDeclaredAnnotation(XmlElement.class).name());
            }
        }
        return null;
    }
}
