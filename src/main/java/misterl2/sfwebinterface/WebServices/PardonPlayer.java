package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ban.BanService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PardonPlayer extends WebServiceBase {
    public PardonPlayer(SFwebinterface plugin, Logger logger, String password) {
        super(plugin, logger, password);
    }

    @Override
    public void handleAuthenticatedRequest() throws IOException {
        String playerName = getMandatoryGETParam("player");
        Task.builder().execute( //Moves execution to mainthread, which is necessary to interact with the game (i.e. ban player). Closing an HTTP connection on mainthread is considered "acceptable"
                () -> {
                    try {
                        boolean wasPreviouslyBanned = pardonPlayer(playerName);
                        String messageEnd = wasPreviouslyBanned ? " was pardoned!" : "would have been pardoned, but was never banned in the first place!";
                        logger.info("Player \"" + playerName + "\"" + messageEnd);
                        returnResponse(200,playerName + messageEnd);
                    } catch (ExecutionException e) {
                        logger.warn("Could not pardon player! There is no player with name " + playerName + " in the universe! -> 428");
                        returnResponse(428,"There is no player with the username\"" + playerName + "\" !"); //Player does not exist at all, including Mojang's database
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                        returnResponse(500,"An unexpected interruption occurred while trying to process the request!");
                    } catch(Exception ex) {
                        logger.error(ex.getMessage());
                        returnResponse(500,"An unexpected error occurred while trying to process the request!");
                    }
                }
        ).submit(plugin);
    }

    private boolean pardonPlayer(String playerName) throws ExecutionException, InterruptedException {
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();
        GameProfile gameProfile = Sponge.getServer().getGameProfileManager().get(playerName).get(); //The .get() Makes this a blocking call. Good thing this is in a seperate thread...
        return service.pardon(gameProfile);
    }
}
