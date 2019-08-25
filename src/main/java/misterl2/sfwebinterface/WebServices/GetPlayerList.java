package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.spongepowered.api.Sponge;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class GetPlayerList implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = getPlayersCSV();
        OutputStream os = t.getResponseBody();
        if(response.length()==0) {
            t.sendResponseHeaders(204,-1);
        } else {
            t.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            os.write(response.getBytes());
        }
        os.close();
    }

    private String getPlayersCSV() {
        return Sponge.getServer().getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.joining(","));
    }
}
