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
    protected HttpExchange httpExchange;

    public WebServiceBase(SFwebinterface plugin, Logger logger, String password) {
        this.plugin = plugin; this.logger=logger; this.password = password;
    }

    protected Map<String, String> parseGETParameters() throws HandledInvalidInputException {
        System.out.println("......-------.....");
        String query = httpExchange.getRequestURI().getQuery();
        System.out.println(query);
        if(query==null) { //Very important null-check, .getQuery() returns NULL rather than an empty string when there are no GET parameters!
            return new HashMap<>(); //Empty map.
        }
        List<String[]> parsedParams = Arrays.stream(query.split("&")).map(param -> param.split("=")).collect(toList());
        if(!parsedParams.stream().allMatch(parameter -> parameter.length==2)) { //If any of the get parameters are faulty, e.g. have either 0 or >1 occurences of the '=' sign, which is used to split it
            returnResponse(400,"Invalid GET parameters!");
            System.out.println("Invalid GET parameters! " + query);
            throw new HandledInvalidInputException();
        }
        return parsedParams.stream().collect(Collectors.toMap(keyArray -> keyArray[0], keyArray -> keyArray[1]));
    }

    protected void returnResponse(int code, String info) {
        try {
            System.out.println("Sending response: " + info);
            httpExchange.sendResponseHeaders(code,info.getBytes("UTF-8").length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(info.getBytes());
            os.close();
            httpExchange.close();
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
        }

    }

    protected Optional<String> getOptionalGETParam(String key) {
        return Optional.ofNullable(parameterMap.get(key));
    }

    protected String getMandatoryGETParam(String key) throws HandledInvalidInputException {
        if(!parameterMap.containsKey(key)) { // When the user to be kicked is not specified in the request
            returnResponse(400,"The supplied get parameters did not have a '" + key  + "' attribute!");
            logger.warn("The supplied get parameters of the request did not have a '" + key + "' attribute!");
            throw new HandledInvalidInputException();
        }
        return parameterMap.get(key);
    }


    @Override
    public final void handle(HttpExchange conn) throws IOException {
        this.httpExchange=conn;
        try {
            parameterMap = parseGETParameters();
            System.out.println("Parameters parsed!");
        } catch (HandledInvalidInputException e) {
            logger.warn("GET-Parameters could not be parsed; Request has been declined!");
            return;
        }

        if(!parameterMap.containsKey("password")) {
            logger.warn("Attempt to access webinterface service \"KickPlayer\" without supplying a password!");
            returnResponse( 401, "You must provide a password for this service!");
            return;
        }

        String receivedPassword = parameterMap.get("password");
        if(!password.equals(receivedPassword)) {
            logger.warn("Attempt to access webinterface service \"KickPlayer\" with incorrect password \"" + receivedPassword + "\" !");
            returnResponse( 401, "The password you provided was incorrect!");
            return;
        }

        handleAuthenticatedRequest();
    }

    public abstract void handleAuthenticatedRequest() throws IOException; //httpExchange always exists when this is called via template pattern from @Override handle(HttpExchange conn)
}
