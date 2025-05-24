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
        flagMap.put("dkk", "dk");
        flagMap.put("inr", "in");
        flagMap.put("cad", "ca");
        flagMap.put("cny", "cn");
        flagMap.put("krw", "kr");
        flagMap.put("nok", "no");
        flagMap.put("xdr", "un"); // специальный случай
        flagMap.put("sek", "se");
        flagMap.put("chf", "ch");
        flagMap.put("jpy", "jp");
        flagMap.put("amd", "am");
        flagMap.put("byr", "by");
        flagMap.put("mdl", "md");
        flagMap.put("tjs", "tj");
        flagMap.put("uzs", "uz");
        flagMap.put("uah", "ua");
        flagMap.put("kwd", "kw");
        flagMap.put("huf", "hu");
        flagMap.put("czk", "cz");
        flagMap.put("nzd", "nz");
        flagMap.put("pkr", "pk");
        flagMap.put("aud", "au");
        flagMap.put("try", "tr");
        flagMap.put("azn", "az");
        flagMap.put("sgd", "sg");
        flagMap.put("afn", "af");
        flagMap.put("bgn", "bg");
        flagMap.put("brl", "br");
        flagMap.put("gel", "ge");
        flagMap.put("aed", "ae");
        flagMap.put("irr", "ir");
        flagMap.put("myr", "my");
        flagMap.put("mnt", "mn");
        flagMap.put("twd", "tw");
        flagMap.put("tmt", "tm");
        flagMap.put("pln", "pl");
        flagMap.put("sar", "sa");
        flagMap.put("byn", "by");
        flagMap.put("omr", "om");
        flagMap.put("hkd", "hk");
        flagMap.put("idr", "id");
    }

    public String flagClass(String currencyCode) {
        String countryCode = flagMap.getOrDefault(currencyCode.toLowerCase(), null);
        if (countryCode != null) {
            return "<span class='fi fi-" + countryCode + " me-2'></span>";
        }
        return ""; // без флага
    }
}