package mytown.proxies.mod;

import cpw.mods.fml.common.Loader;
import mytown.MyTown;

import java.util.ArrayList;
import java.util.List;

public class ModProxies {
    private static List<ModProxy> proxies = new ArrayList<ModProxy>();
    private static boolean loaded = false;

    private ModProxies() {
    }

    public static void load() {
        MyTown.instance.log.info("Starting proxies...");

        if (!ModProxies.loaded) {
            ModProxies.loaded = true;
            MyTown.instance.config.getCategory("modproxies").setComment("Holds the enable state of the different ModProxies.\nModProxies handle interaction with other mods.\nIf a mod interaction causes issues, just set it to false.");
        }

        // Load ModProxies
        for (ModProxy p : ModProxies.proxies) {
            // TODO Re-add config options for proxies
            // TODO Check for the mod's API as well?
            if (p.getModID() != null && Loader.isModLoaded(p.getModID())) {
                p.load();
            }
        }
    }

    /**
     * Adds all the {@link ModProxy}'s to the list
     */
    public static void addProxies() {
        addProxy(new IC2Proxy());
        addProxy(new ForgePermsProxy());
    }

    /**
     * Adds the given {@link ModProxy}
     *
     * @param proxy
     */
    public static void addProxy(ModProxy proxy) {
        ModProxies.proxies.add(proxy);
    }

    /**
     * Removes the given {@link ModProxy}
     *
     * @param proxy
     */
    public static void removeProxy(ModProxy proxy) {
        ModProxies.proxies.remove(proxy);
    }
}