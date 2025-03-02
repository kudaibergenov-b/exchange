package com.kudaibergenov.exchange.util;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CurrencyXmlParser {

    public static Map<String, Double> fetchRates(String date) {
        String urlString = "https://www.nbkr.kg/XML/daily.xml";
        if (date != null) {
            urlString += "?date=" + date; // Добавляем дату к URL
        }

        Map<String, Double> rates = new HashMap<>();

        try {
            URL url = new URL(urlString);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Currency");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                // Получаем код валюты из атрибута ISOCode
                String code = element.getAttribute("ISOCode");

                // Получаем значение курса (с запятой вместо точки)
                String valueString = element.getElementsByTagName("Value").item(0).getTextContent().replace(",", ".");

                // Преобразуем в число
                double value = Double.parseDouble(valueString);

                // Добавляем в Map
                rates.put(code, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rates;
    }
}