package dev.jackhogan.molemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.UUID;

public class MoleMod implements ModInitializer {
    private static final int SAFE_TICKS = 300;

    private final HashMap<UUID, Integer> safeRegistry = new HashMap<>();
    private final HashMap<UUID, OffenseTracker> offenses = new HashMap<>();

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ServerTickEvents.START_WORLD_TICK.register((world) -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                // Tick players
                for (UUID uuid : safeRegistry.keySet()) {
                    int count = safeRegistry.get(uuid);
                    if (safeRegistry.get(uuid) > SAFE_TICKS) {
                        safeRegistry.remove(uuid);
                    } else {
                        if (count == 2) {
                            player.sendMessage(Text.of(Formatting.RED + "WARNING! You are in the burning light of the sun! You have 15 seconds to return to the safety of the dark."));
                        }
                        safeRegistry.put(uuid, count + 1);
                    }
                }

                if (player.isDead()) {
                    // Reset offenses if the player dies
                    if (offenses.containsKey(player.getUuid())) {
                        offenses.put(player.getUuid(), new OffenseTracker());
                    }
                    safeRegistry.put(player.getUuid(), 0);
                    continue;
                }

                if (world.isSkyVisible(player.getBlockPos()) && world.isDay() && player.world.getDimension().natural() && !safeRegistry.containsKey(player.getUuid()) && !player.isCreative()) {
                    if (!offenses.containsKey(player.getUuid())) {
                        offenses.put(player.getUuid(), new OffenseTracker());
                    }

                    OffenseTracker tracker = offenses.get(player.getUuid());
                    tracker.startOffense(player);

                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, tracker.getOffenses()));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 2 * tracker.getOffenses()));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 3 * tracker.getOffenses()));
                } else if (offenses.containsKey(player.getUuid())) {
                    offenses.get(player.getUuid()).endOffense();
                }
            }
        });
    }
}
