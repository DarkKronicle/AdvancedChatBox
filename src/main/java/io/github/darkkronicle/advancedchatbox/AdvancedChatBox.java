package io.github.darkkronicle.advancedchatbox;

import fi.dy.masa.malilib.event.InitializationHandler;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Environment(EnvType.CLIENT)
public class AdvancedChatBox implements ClientModInitializer {

    public static final String MOD_ID = "advancedchatbox";

    @Override
    public void onInitializeClient() {
        // This will run after AdvancedChatCore's because of load order
        InitializationHandler.getInstance().registerInitializationHandler(new ChatBoxInitHandler());
        File english = new File("./config/advancedchat/english.zip");
        if (!english.exists()) {
            new File("./config/advancedchat/").mkdirs();
            // Move dictionary so that we can access it easier
            try (FileOutputStream output = new FileOutputStream(english)){
                InputStream stream = AdvancedChatCore.getResource("english.zip");
                IOUtils.copy(stream, output);
                stream.close();
                System.out.println("Moved english jar!");
            } catch (Exception e) {
                System.out.println("Couldn't load english.jar");
                e.printStackTrace();
            }

        }
    }

}
