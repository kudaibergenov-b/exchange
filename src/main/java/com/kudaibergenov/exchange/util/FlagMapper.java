package com.kudaibergenov.exchange.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("flagMapper")
public class FlagMapper {

    private static final Map<String, String> flagMap = new HashMap<>();

    static {
        flagMap.put("usd", "us");
        flagMap.put("eur", "eu");
        flagMap.put("kzt", "kz");
        flagMap.put("rub", "ru");
        flagMap.put("gbp", "gb");
        flagMap.put("cny", "cn");
        flagMap.put("jpy", "jp");
        flagMap.put("try", "tr");
        flagMap.put("krw", "kr");
        flagMap.put("inr", "in");
        flagMap.put("byn", "by");
        flagMap.put("uzs", "uz");
        flagMap.put("pln", "pl");
        flagMap.put("czk", "cz");
        flagMap.put("nzd", "nz");
        flagMap.put("aud", "au");
        flagMap.put("sgd", "sg");
        flagMap.put("hkd", "hk");
        flagMap.put("aed", "ae");
        flagMap.put("xdr", "un"); // специальный случай
        // ... добавь по желанию остальные
    }

    public String flagClass(String currencyCode) {
        String countryCode = flagMap.getOrDefault(currencyCode.toLowerCase(), null);
        if (countryCode != null) {
            return "<span class='fi fi-" + countryCode + " me-2'></span>";
        }
        return ""; // без флага
    }
}