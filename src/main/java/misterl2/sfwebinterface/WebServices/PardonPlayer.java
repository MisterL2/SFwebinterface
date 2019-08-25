package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;

import java.io.IOException;

public class PardonPlayer extends WebServiceBase implements HttpHandler {
    public PardonPlayer(Logger logger) {
        super(logger);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
