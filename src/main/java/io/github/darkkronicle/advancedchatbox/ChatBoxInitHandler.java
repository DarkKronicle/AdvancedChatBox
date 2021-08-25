package io.github.darkkronicle.advancedchatbox;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ChatBoxInitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(AdvancedChatBox.MOD_ID, new ChatBoxConfigStorage());
        GuiConfigHandler.getInstance().addGuiSection(GuiConfigHandler.createGuiConfigSection(
                StringUtils.translate("advancedchatbox.tab.general"), ChatBoxConfigStorage.General.OPTIONS
        ));
    }

}
