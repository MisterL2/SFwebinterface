package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.HandledInvalidInputException;
import misterl2.sfwebinterface.InvalidInputException;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.Optional;

public class KickPlayer extends WebServiceBase {
    public KickPlayer(SFwebinterface plugin, Logger logger, String password) {
        super(plugin, logger, password);
    }

    @Override
    public void handleAuthenticatedRequest() throws IOException {
        System.out.println("Kick player activated");

        String playerName = getMandatoryGETParam("player"); //If param does not exist, this will throw HandledInvalidInputException (extends IOException) and close connection automatically.

        Optional<String> reason = getOptionalGETParam("reason");

        try {
            kickPlayer(playerName,reason); //Use optional.empty, NOT NULL
            logger.info("Player " + playerName + " was kicked!");
            returnResponse(200,playerName + " was kicked!");
        } catch(InvalidInputException ex) { //If the player to be kicked is not online / doesn't exist
            logger.warn("Declining kick-request: No player with that name currently online -> 428");
            returnResponse(428,"There is currently no player with that name online!");
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
