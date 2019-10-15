package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.HandledInvalidInputException;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class WebServiceBase implements HttpHandler {
    protected Logger logger;
    protected String password;
    protected SFwebinterface plugin;
    protected Map<String, String> parameterMap;

    public WebServiceBase(SFwebinterface plugin, Logger logger, String password) {
        this.plugin = plugin; this.logger=logger; this.password = password;
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

    @Override
    public final void handle(HttpExchange t) throws IOException {
        try {
            parameterMap = parseGETParameters(t);
            System.out.println("Parameters parsed!");
        } catch (HandledInvalidInputException e) {
            logger.error("GET-Parameters could not be parsed; Request has been declined!");
            return;
        }

        if(!parameterMap.containsKey("password")) {
            logger.error("Attempt to access webinterface service \"KickPlayer\" without supplying a password!");
            returnResponse(t, 401, "You must provide a password for this service!");
            return;
        }

        String receivedPassword = parameterMap.get("password");
        if(!password.equals(receivedPassword)) {
            logger.error("Attempt to access webinterface service \"KickPlayer\" with incorrect password \"" + receivedPassword + "\" !");
            returnResponse(t, 401, "The password you provided was incorrect!");
            return;
        }

        handleAuthenticatedRequest(t);
    }

    public abstract void handleAuthenticatedRequest(HttpExchange t) throws IOException;
}
