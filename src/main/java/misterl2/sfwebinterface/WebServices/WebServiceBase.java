package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import misterl2.sfwebinterface.HandledInvalidInputException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class WebServiceBase {
    protected Logger logger;

    public WebServiceBase(Logger logger) {
        this.logger=logger;
    }

    protected Map<String, String> parseGETParameters(HttpExchange conn) throws HandledInvalidInputException {
        String query = conn.getRequestURI().getQuery();
        Stream<String[]> parsedParams = Arrays.stream(query.split("&")).map(param -> param.split("="));
        if(!parsedParams.allMatch(parameter -> parameter.length==2)) { //If any of the get parameters are faulty, e.g. have either 0 or >1 occurences of the '=' sign, which is used to split it
            returnResponse(conn,400,"Invalid GET parameters!");
            System.out.println("Invalid GET parameters! " + query);
            throw new HandledInvalidInputException();
        }
        return parsedParams.collect(Collectors.toMap(keyArray -> keyArray[0], keyArray -> keyArray[1]));
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
}
