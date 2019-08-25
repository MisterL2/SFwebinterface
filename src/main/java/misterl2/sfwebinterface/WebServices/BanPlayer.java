package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.HandledInvalidInputException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class BanPlayer extends WebServiceBase implements HttpHandler {
    public BanPlayer(Logger logger) {
        super(logger);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        Map<String, String> parameterMap;
        try {
            parameterMap = parseGETParameters(t);
        } catch (HandledInvalidInputException e) {
            return;
        }
        if(!parameterMap.containsKey("player")) {
            returnResponse(t,400,"The supplied get parameters did not have a 'player' attribute!");
            return;
        }
        Optional<String> reason = Optional.empty();
        if(parameterMap.containsKey("reason")) {
            reason = Optional.of(parameterMap.get("reason"));
        }
        try {
            banPlayer(parameterMap.get("player"),reason); //Use optional.empty, NOT NULL
            System.out.println("Player was kicked!");
            returnResponse(t,200,parameterMap.get("player") + " was kicked!");
        } catch(ExecutionException ex) {
            System.out.println("No player with that name exists at all! -> 428");
            returnResponse(t,428,"There is no player with this username!"); //Not at all, including Mojang's database
        } catch(Exception ex) {
            returnResponse(t,500,"An unexpected error occurred while trying to process the request!");
        }

        OutputStream os = t.getResponseBody();
        os.close();
    }

    private void banPlayer(String playerName, Optional<String> reason) throws ExecutionException, InterruptedException {
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();
        GameProfile gameProfile = Sponge.getServer().getGameProfileManager().get(playerName).get(); //The .get() Makes this a blocking call. Good thing this is in a seperate thread...
        Ban ban;
        if(reason.isPresent()) {
            ban = Ban.of(gameProfile,Text.of(reason.get()));
        } else {
            ban = Ban.of(gameProfile);
        }
        service.addBan(ban);
    }

}
