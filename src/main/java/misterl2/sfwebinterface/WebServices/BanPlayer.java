package misterl2.sfwebinterface.WebServices;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import misterl2.sfwebinterface.HandledInvalidInputException;
import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class BanPlayer extends WebServiceBase {
    public BanPlayer(SFwebinterface plugin, Logger logger, String password) {
        super(plugin, logger, password);
    }

    @Override
    public void handleAuthenticatedRequest(HttpExchange t) throws IOException {
        final Optional<String> reason = getGETParamValue("reason");

        Task.builder().execute( //Moves execution to mainthread, which is necessary to interact with the game (i.e. ban player). Closing an HTTP connection on mainthread is considered "acceptable"
                () -> {
                    try {
                        banPlayer(parameterMap.get("player"), reason);
                        System.out.println("Player was banned!");
                        returnResponse(t,200,parameterMap.get("player") + " was banned!");
                    } catch (ExecutionException e) {
                        System.out.println("No player with that name exists at all! -> 428");
                        returnResponse(t,428,"There is no player with this username!"); //Player does not exist at all, including Mojang's database
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        returnResponse(t,500,"An unexpected interruption occurred while trying to process the request!");
                    } catch(Exception ex) {
                        System.out.println(ex.getMessage());
                        returnResponse(t,500,"An unexpected error occurred while trying to process the request!");
                    }
                }
        ).submit(plugin);
    }


    private void banPlayer(String playerName, Optional<String> reason) throws ExecutionException, InterruptedException {
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();
        GameProfile gameProfile = Sponge.getServer().getGameProfileManager().get(playerName).get(); //The .get() Makes this a blocking call. Good thing this is in a seperate thread...
        Optional<Player> player = Sponge.getServer().getPlayer(playerName); //When using NAME rather than UUID, it only retrieves currently online players
        Ban ban;
        if(reason.isPresent()) {
            ban = Ban.of(gameProfile,Text.of(reason.get()));
            if(player.isPresent()) {
                player.get().kick(Text.of(reason.get())); //Player needs to be kicked after being banned. This is not done automatically like in the /ban command
            }
        } else {
            ban = Ban.of(gameProfile);
            if(player.isPresent()) {
                player.get().kick();
            }
        }
        service.addBan(ban);
    }

}
