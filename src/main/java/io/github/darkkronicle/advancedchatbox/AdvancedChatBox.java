package io.github.darkkronicle.advancedchatbox;

import fi.dy.masa.malilib.event.InitializationHandler;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;

@Environment(EnvType.CLIENT)
public class AdvancedChatBox implements ClientModInitializer {

    public static final String MOD_ID = "advancedchatbox";

    @Override
    public void onInitializeClient() {
        // This will run after AdvancedChatCore's because of load order
        InitializationHandler
            .getInstance()
            .registerInitializationHandler(new ChatBoxInitHandler());
    }
}
