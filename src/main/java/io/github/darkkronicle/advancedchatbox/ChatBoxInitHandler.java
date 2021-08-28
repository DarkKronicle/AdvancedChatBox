package io.github.darkkronicle.advancedchatbox;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatbox.config.GuiChatBoxConfig;
import io.github.darkkronicle.advancedchatbox.formatter.CommandColorer;
import io.github.darkkronicle.advancedchatbox.registry.ChatFormatterRegistry;
import io.github.darkkronicle.advancedchatbox.registry.ChatSuggestorRegistry;
import io.github.darkkronicle.advancedchatbox.suggester.CalculatorSuggestor;
import io.github.darkkronicle.advancedchatbox.suggester.PlayerSuggestor;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ChatBoxInitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(AdvancedChatBox.MOD_ID, new ChatBoxConfigStorage());
        GuiConfigHandler.getInstance().addGuiSection(new GuiConfigHandler.Tab() {
            @Override
            public String getName() {
                return StringUtils.translate("advancedchatbox.tab.general");
            }

            @Override
            public Screen getScreen(List<GuiConfigHandler.TabButton> buttons) {
                return new GuiChatBoxConfig(buttons);
            }
        });

        ChatFormatterRegistry chatRegistry = ChatFormatterRegistry.getInstance();
        chatRegistry.register(CommandColorer::new, "commandcolorer", "advancedchatbox.config.chatformatter.commandcolorer", "advancedchatbox.config.chatformatter.info.commandcolorer", true, true);

        // Initiate chat suggestors
        ChatSuggestorRegistry suggestorRegistry = ChatSuggestorRegistry.getInstance();
        suggestorRegistry.register(PlayerSuggestor::new, "players", "advancedchatbox.config.chatsuggestor.players", "advancedchatbox.config.chatsuggestor.info.players", true, true);
        suggestorRegistry.register(CalculatorSuggestor::new, "calculator", "advancedchatbox.config.chatsuggestor.calculator", "advancedchatbox.config.chatsuggestor.info.calculator", true, false);


    }

}
