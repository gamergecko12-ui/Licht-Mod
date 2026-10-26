package de.deinname.licht;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

/**
 * "Licht" - eine rein clientseitige Mod.
 *
 * Taste R schaltet um: an = Helligkeit (Gamma) auf GAMMA_AN, aus = zurueck auf
 * den Wert, den Minecraft beim Start hatte. Beim Start der Mod ist sie AUS.
 */
@Mod(LichtMod.MOD_ID)
public class LichtMod {

    public static final String MOD_ID = "licht";

    /** Zielwert wenn eingeschaltet. */
    private static final double GAMMA_AN = 15.0;

    /** Der Gamma-Wert, den das Spiel hatte, bevor wir ihn angefasst haben. */
    private static double originalGamma = -1.0;

    private static boolean an = false;

    private static KeyMapping taste;

    public LichtMod() {
        // Keybind beim Aufbau der Client-Registries anmelden
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onRegisterKeys);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onRegisterKeys(RegisterKeyMappingsEvent event) {
        taste = new KeyMapping(
                "key.licht.toggle",
                GLFW.GLFW_KEY_R,
                "key.categories.licht");
        event.register(taste);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }

        // Ausgangswert einmalig merken
        if (originalGamma < 0) {
            originalGamma = mc.options.gamma().get();
        }

        while (taste != null && taste.consumeClick()) {
            an = !an;
            double neu = an ? GAMMA_AN : originalGamma;

            // Direkt setzen und auf die Platte schreiben,
            // damit die Aenderung sofort und dauerhaft greift.
            mc.options.gamma().set(neu);
            mc.options.save();

            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.translatable(an ? "licht.an" : "licht.aus"), true);
            }
        }
    }
}
