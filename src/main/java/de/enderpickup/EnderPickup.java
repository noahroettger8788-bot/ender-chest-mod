package de.enderpickup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EnderPickup implements ModInitializer {
    /** Reichweite in Blöcken (in alle Richtungen). */
    public static final double RANGE = 6.0;

    private static final Set<UUID> ENABLED = new HashSet<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(TogglePayload.ID, TogglePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TogglePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                UUID id = player.getUuid();
                boolean on = ENABLED.add(id);
                if (!on) ENABLED.remove(id);
                player.sendMessage(Text.literal("Ender Pickup: " + (on ? "§aAN" : "§cAUS")), true);
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ENABLED.remove(handler.player.getUuid()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ENABLED.isEmpty()) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!ENABLED.contains(player.getUuid()) || player.isSpectator() || !player.isAlive()) continue;
                vacuum(player);
            }
        });
    }

    private static void vacuum(ServerPlayerEntity player) {
        Box box = player.getBoundingBox().expand(RANGE);
        // Kein Pickup-Delay-Check: erfasst auch "Ghost"-Items (Pickup-Delay / unendlicher Delay),
        // die man normal nicht aufheben kann.
        List<ItemEntity> items = player.getServerWorld().getEntitiesByClass(
                ItemEntity.class, box, e -> e.isAlive() && !e.getStack().isEmpty());
        if (items.isEmpty()) return;

        EnderChestInventory ender = player.getEnderChestInventory();
        for (ItemEntity entity : items) {
            ItemStack remainder = ender.addStack(entity.getStack().copy());
            if (remainder.isEmpty()) {
                entity.discard();
            } else {
                entity.setStack(remainder); // Enderchest voll -> Rest bleibt liegen
            }
        }
    }
}
