package de.enderpickup.client;

import de.enderpickup.TogglePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class EnderPickupClient implements ClientModInitializer {
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.enderpickup.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.enderpickup"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.player != null && ClientPlayNetworking.canSend(TogglePayload.ID)) {
                    ClientPlayNetworking.send(new TogglePayload());
                }
            }
        });
    }
}
