package dev.jackhogan.molemod;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OffenseTracker {
    private boolean isOffending = false;
    private int offenses = 1;

    public int getOffenses() {
        return offenses;
    }

    public void startOffense(ServerPlayerEntity player) {
        if (!isOffending) {
            int end = offenses % 10;
            int second = (offenses / 10) % 10;
            String suffix = switch (end) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
            if (second == 1) {
                suffix = "th";
            }
            player.sendMessage(Text.of(Formatting.RED + "IT BURNS! This is the " + offenses + suffix + " time you've ventured out!"));
        }
        isOffending = true;
    }

    public void endOffense() {
        if (!isOffending) {
            return;
        }
        isOffending = false;
        offenses++;
    }
}
