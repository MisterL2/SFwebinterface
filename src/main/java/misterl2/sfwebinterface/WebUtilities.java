package misterl2.sfwebinterface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebUtilities {

    public static Map<String, String> parseGETParameters(String query) {
//        String[] parameters = query.split("&");
        return Arrays.stream(query.split("&")).map(param -> param.split("=")).filter(p -> p.length==2).collect(Collectors.toMap(keyArray -> keyArray[0], keyArray -> keyArray[1]));
//        Map<String,String> parameterMap = new HashMap<>();
//        for (String param : parameters) {
//            String[] split = param.split("=");
//            parameterMap.put(split[0],split[1]);
//        }
//        return parameterMap;
    }
}
