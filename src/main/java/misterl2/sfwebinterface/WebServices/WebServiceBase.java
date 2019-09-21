package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import misterl2.sfwebinterface.HandledInvalidInputException;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class WebServiceBase {
    protected Logger logger;
    protected SFwebinterface plugin;
    protected Map<String, String> parameterMap;

    public WebServiceBase(SFwebinterface plugin, Logger logger) {
        this.plugin = plugin; this.logger=logger;
    }

    protected Map<String, String> parseGETParameters(HttpExchange conn) throws HandledInvalidInputException {
        System.out.println("......-------.....");
        String query = conn.getRequestURI().getQuery();
        System.out.println(query);
        if(query==null) { //Very important null-check, .getQuery() returns NULL rather than an empty string when there are no GET parameters!
            return new HashMap<>(); //Empty map.
        }
        List<String[]> parsedParams = Arrays.stream(query.split("&")).map(param -> param.split("=")).collect(toList());
        if(!parsedParams.stream().allMatch(parameter -> parameter.length==2)) { //If any of the get parameters are faulty, e.g. have either 0 or >1 occurences of the '=' sign, which is used to split it
            returnResponse(conn,400,"Invalid GET parameters!");
            System.out.println("Invalid GET parameters! " + query);
            throw new HandledInvalidInputException();
        }
        return parsedParams.stream().collect(Collectors.toMap(keyArray -> keyArray[0], keyArray -> keyArray[1]));
    }

    protected void returnResponse(HttpExchange conn, int code, String info) {
        try {
            System.out.println("Sending response: " + info);
            conn.sendResponseHeaders(code,info.getBytes("UTF-8").length);
            OutputStream os = conn.getResponseBody();
            os.write(info.getBytes());
            os.close();
            conn.close();
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
        }

    }
    protected Optional<String> getGETParamValue(String key) {
        return Optional.ofNullable(parameterMap.get(key));
    }
}
