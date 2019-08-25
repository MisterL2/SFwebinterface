package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.WebUtilities;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KickPlayer implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        URI requestURI = t.getRequestURI();
        String query = requestURI.getQuery();

        System.out.println(query);

        Map<String, String> parameterMap = WebUtilities.parseGETParameters(query);
        if(!parameterMap.containsKey("player")) {
            t.sendResponseHeaders(400,-1);
        } else {
            Optional<String> reason = Optional.empty();
            if(parameterMap.containsKey("reason")) {
                reason = Optional.of(parameterMap.get("reason"));
            }
            try {
                kickPlayer(parameterMap.get("player"),reason); //Use optional.empty, NOT NULL
                System.out.println("Player was kicked!");
                t.sendResponseHeaders(200,-1);
            } catch(IllegalArgumentException ex) {
                System.out.println("No player with that name currently online -> 428");
                t.sendResponseHeaders(428,-1);
            }
        }
        OutputStream os = t.getResponseBody();
        os.close();
    }

    private void kickPlayer(String playerName, Optional<String> reason) {
        Optional<Player> player = Sponge.getServer().getPlayer(playerName); //When using NAME rather than UUID, it only retrieves currently online players
        if(!player.isPresent()) {
            throw new IllegalArgumentException("There is no player with that name currently on the server");
        }
        if(!reason.isPresent()) {
            player.get().kick();
        } else {
            player.get().kick(Text.of(reason.get()));
        }
    }

}
