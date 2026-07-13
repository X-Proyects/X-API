package com.fabian.xapi;

import com.fabian.xapi.config.ConfigManager;
import com.fabian.xapi.config.ConfigUpdater;
import com.fabian.xapi.language.LanguageManager;
import com.fabian.xapi.metrics.Metrics;
import com.fabian.xapi.scheduler.SchedulerUtil;
import com.fabian.xapi.scheduler.ServerSupport;
import com.fabian.xapi.update.UpdateChecker;
import com.fabian.xapi.utils.ParticleUtil;
import com.fabian.xapi.utils.SoundUtil;
import com.fabian.xapi.colors.TextUtil;
import net.byteflux.libby.BukkitLibraryManager;
import org.bukkit.plugin.Plugin;

/**
 * X-API — Central provider and entry point for the X-Projects shared library.
 *
 * <p>This class acts as a facade for all common utilities and managers used across
 * the X-Projects plugin suite. It is designed to be shaded into each plugin JAR.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // In your plugin's onEnable():
 * XAPI.init(this);
 *
 * // Access static utilities directly:
 * TextUtil.sendMessage(player, "&aHello!");
 * SchedulerUtil.runAsync(plugin, () -> { ... });
 * ServerSupport.isFolia();
 *
 * // Create managers for your plugin:
 * ConfigManager configManager = XAPI.createConfigManager(this);
 * LanguageManager langManager = XAPI.createLanguageManager(this, "en");
 * UpdateChecker updateChecker = XAPI.createUpdateChecker(this, 12345);
 * Metrics metrics = XAPI.createMetrics(this, 12345);
 * }</pre>
 *
 * @author Fabian
 * @since 1.0.0
 */
public final class XAPI {

    /** The X-API library version. */
    public static final String VERSION = "1.0.0";

    private static volatile boolean initialized = false;
    private static Plugin hostPlugin;
    private static BukkitLibraryManager libraryManager;

    // Private constructor — this is a static utility/facade class
    private XAPI() {
        throw new UnsupportedOperationException("X-API is a static facade and cannot be instantiated.");
    }

    /**
     * Initializes the X-API library. Must be called once during plugin startup.
     *
     * <p>This sets up the shared {@link BukkitLibraryManager} for runtime dependency
     * downloading and configures common repositories (Maven Central, Sonatype, PaperMC).</p>
     *
     * @param plugin the plugin instance that is hosting X-API (typically your main plugin class).
     * @throws IllegalStateException if X-API has already been initialized.
     */
    public static void init(Plugin plugin) {
        if (initialized) {
            throw new IllegalStateException("X-API has already been initialized.");
        }
        hostPlugin = plugin;
        libraryManager = new BukkitLibraryManager(plugin);
        libraryManager.addMavenCentral();
        libraryManager.addSonatype();
        libraryManager.addRepository("https://repo.papermc.io/repository/maven-public/");
        initialized = true;
    }

    // =========================================================================
    //  Factory Methods — Create manager instances for your plugin
    // =========================================================================

    /**
     * Creates a new {@link ConfigManager} for the given plugin.
     *
     * <p>The ConfigManager handles config loading, code-version migration, and
     * automatic rebuilding when the config-code in the JAR is newer than the
     * on-disk version.</p>
     *
     * @param plugin the plugin instance.
     * @return a new ConfigManager instance.
     */
    public static ConfigManager createConfigManager(Plugin plugin) {
        return new ConfigManager(plugin);
    }

    /**
     * Creates a new {@link LanguageManager} for the given plugin.
     *
     * <p>The LanguageManager loads messages from a configurable {@code messages/}
     * folder inside the plugin's data directory. It supports 6 built-in languages
     * (en, es, ja, pt, ru, custom) with automatic sync of new keys from defaults.</p>
     *
     * @param plugin        the plugin instance.
     * @param language      the initial language code (e.g. "en").
     * @param prefix        the message prefix (e.g. "&8[&bMyPlugin&8]&r ").
     * @param defaultLanguages the list of default language files to extract from JAR resources.
     * @return a new LanguageManager instance.
     */
    public static LanguageManager createLanguageManager(Plugin plugin, String language, String prefix,
                                                         String... defaultLanguages) {
        return new LanguageManager(plugin, language, prefix, defaultLanguages);
    }

    /**
     * Creates a new {@link UpdateChecker} for the given plugin and SpigotMC resource.
     *
     * @param plugin    the plugin instance.
     * @param resourceId the SpigotMC resource ID.
     * @return a new UpdateChecker instance.
     */
    public static UpdateChecker createUpdateChecker(Plugin plugin, int resourceId) {
        return new UpdateChecker(plugin, resourceId);
    }

    /**
     * Creates a new {@link Metrics} instance for bStats integration.
     *
     * @param plugin    the plugin instance.
     * @param serviceId the bStats service/plugin ID.
     * @return a new Metrics instance.
     */
    public static Metrics createMetrics(Plugin plugin, int serviceId) {
        return new Metrics(plugin, serviceId);
    }

    // =========================================================================
    //  Accessors
    // =========================================================================

    /**
     * Returns the shared {@link BukkitLibraryManager} for runtime dependency downloading.
     *
     * <p>Plugins can use this to load additional libraries at runtime without bundling
     * them in the plugin JAR.</p>
     *
     * @return the shared library manager.
     * @throws IllegalStateException if X-API has not been initialized.
     */
    public static BukkitLibraryManager getLibraryManager() {
        ensureInitialized();
        return libraryManager;
    }

    /**
     * Returns the plugin instance that initialized X-API.
     *
     * @return the host plugin.
     * @throws IllegalStateException if X-API has not been initialized.
     */
    public static Plugin getHostPlugin() {
        ensureInitialized();
        return hostPlugin;
    }

    /**
     * Returns whether X-API has been initialized.
     *
     * @return true if initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the X-API version string.
     *
     * @return the version.
     */
    public static String getVersion() {
        return VERSION;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("X-API has not been initialized. Call XAPI.init(plugin) first.");
        }
    }
}