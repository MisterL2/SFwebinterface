package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.HandledInvalidInputException;
import misterl2.sfwebinterface.InvalidInputException;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class KickPlayer extends WebServiceBase implements HttpHandler {
    public KickPlayer(SFwebinterface plugin, Logger logger) {
        super(plugin, logger);
    }

    @Override
    public void handle(HttpExchange t) {
        System.out.println("Kick player activated");
        try {
            parameterMap = parseGETParameters(t);
            System.out.println("Parameters parsed!");
        } catch (HandledInvalidInputException e) {
            System.out.println("Breakpoint A1");
            return;
        }


        if(!parameterMap.containsKey("player")) { // When the user to be kicked is not specified in the request
            returnResponse(t,400,"The supplied get parameters did not have a 'player' attribute!");
            System.out.println("Breakpoint A2");
            return;
        }

        Optional<String> reason = getGETParamValue("reason");

        try {
            kickPlayer(parameterMap.get("player"),reason); //Use optional.empty, NOT NULL
            System.out.println("Player was kicked!");
            returnResponse(t,200,parameterMap.get("player") + " was kicked!");
        } catch(InvalidInputException ex) { //If the player to be kicked is not online / doesn't exist
            System.out.println("No player with that name currently online -> 428");
            returnResponse(t,428,"There is currently no player with that name online!");
        }

    }

    private void kickPlayer(String playerName, Optional<String> reason) throws InvalidInputException {
        Optional<Player> player = Sponge.getServer().getPlayer(playerName); //When using NAME rather than UUID, it only retrieves currently online players
        if(!player.isPresent()) {
            throw new InvalidInputException();
        }
        if(!reason.isPresent()) {
            player.get().kick();
        } else {
            player.get().kick(Text.of(reason.get()));
        }
    }

}
