package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class GetPlayerList extends WebServiceBase implements HttpHandler {
    public GetPlayerList(SFwebinterface plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = getPlayersCSV();
        OutputStream os = t.getResponseBody();
        if(response.length()==0) {
            t.sendResponseHeaders(204,-1);
            os.close();
            t.close();
        } else {
            returnResponse(t,200,response);
        }

    }

    private String getPlayersCSV() {
        return Sponge.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.joining(","));
    }
}
