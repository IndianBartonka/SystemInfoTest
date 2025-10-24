package pl.indianbartonka.test;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.security.Security;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.IndianUtils;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.argument.ArgumentParser;
import pl.indianbartonka.util.discord.WebHookClient;
import pl.indianbartonka.util.discord.embed.Embed;
import pl.indianbartonka.util.discord.embed.EmbedBuilder;
import pl.indianbartonka.util.discord.embed.component.Author;
import pl.indianbartonka.util.discord.embed.component.Field;
import pl.indianbartonka.util.discord.embed.component.Footer;
import pl.indianbartonka.util.exception.UnsupportedSystemException;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.config.LoggerConfiguration;
import pl.indianbartonka.util.network.Network;
import pl.indianbartonka.util.network.NetworkUtil;
import pl.indianbartonka.util.system.SystemArch;
import pl.indianbartonka.util.system.SystemFamily;
import pl.indianbartonka.util.system.SystemOS;
import pl.indianbartonka.util.system.SystemUtil;
import pl.indianbartonka.util.system.parts.Disk;
import pl.indianbartonka.util.system.parts.Ram;

public final class SystemInfoTest {

    private static final LoggerConfiguration LOGGER_CONFIGURATION = LoggerConfiguration.builder()
            .setLoggingToFile(true)
            .setOneLog(true)
            .build();

    private static final Logger LOGGER = new Logger(LOGGER_CONFIGURATION) {
    };

    // --- Dane systemowe ---
    private static final SystemFamily FAMILY = SystemUtil.getSystemFamily();
    private static final String SYSTEM_NAME = SystemUtil.getFullyOSName();
    private static final SystemOS SYSTEM_OS = SystemUtil.getSystem();
    private static final SystemArch SYSTEM_ARCH = SystemUtil.getCurrentArch();
    private static final String RAW_SYSTEM_ARCH = SystemUtil.getFullyArchCode();
    private static final String OS_VERSION = SystemUtil.getOSVersion();
    private static final String PROCESSOR_NAME = SystemUtil.getProcesorName();
    private static final String GRAPHIC_CARDS = MessageUtil.stringListToString(SystemUtil.getGraphicCardsName(), " | ");
    private static final int LOGICAL_THREADS = ThreadUtil.getLogicalThreads();
    private static final String DISTRIBUTION = SystemUtil.getDistribution();
    private static final String FULL_OS_NAME = SystemUtil.getFullOSNameWithDistribution();

    private static final boolean BOX64 = IndianUtils.box64Check();
    private static final boolean WINE = IndianUtils.wineCheck();

    // --- Pamięć ---
    private static final List<Disk> DISKS = SystemUtil.getAvailableDisks();
    private static final Map<Disk, Double> DISK_TEST = new HashMap<>();
    private static final List<Ram> RAM_LIST = SystemUtil.getRamData();

    // --- Sieć ---
    private static final String WIFI_SSID = NetworkUtil.getWiFiSSID();
    private static final List<Network> IPV4 = NetworkUtil.getIPv4();
    private static final List<Network> IPV6 = NetworkUtil.getIPv6();

    // --- Java / System ---
    private static final List<String> JAVA_FLAGS = ManagementFactory.getRuntimeMXBean().getInputArguments();
    private static final Set<String> HASH_ALGORITHMS = Security.getAlgorithms("MessageDigest");

    // --- Ustawienia regionalne ---
    private static final String ENCODING = Charset.defaultCharset().displayName();
    private static final String LOCALE = SystemUtil.LOCALE.toLanguageTag();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    public static void main(final String[] args) {
        final ArgumentParser argumentParser = new ArgumentParser(args);

        start(argumentParser.contains("noDisks"));

        if (!argumentParser.contains("noDiscord")) sendSystemInfoWebhook();
    }

    private static void start(final boolean noDisks) {
        LOGGER.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + System.getProperty("java.runtime.version") + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");

        try {
            if (!noDisks) {
                final List<Thread> threads = new ArrayList<>();

                for (final Disk disk : DISKS) {
                    threads.add(new Thread(() -> {
                        LOGGER.print("&b" + disk.getName() + "&4 | &b" + disk.getModel() + " &7(&8" + disk.getType() + "&7)" + ": &4Testowanie szybkości zapisu pliku");
                        try {
                            DISK_TEST.put(disk, MathUtil.formatDecimal(SystemUtil.testDiskWrite(disk), 3));
                        } catch (final Exception exception) {
                            exception.printStackTrace();
                        }
                    }));
                }

                threads.forEach(Thread::start);

                for (final Thread thread : threads) {
                    thread.join();
                }

            }

            LOGGER.print("&aGOTOWE");
        } catch (final Exception exception) {
            exception.printStackTrace();
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4System Info");

        LOGGER.info("&aNazwa systemu: &b" + SYSTEM_NAME + "&4 |&b " + SYSTEM_OS + "&4 |&b " + FAMILY);
        LOGGER.info("&aArchitektura: &b" + RAW_SYSTEM_ARCH + "&4 |&b " + SYSTEM_ARCH);
        LOGGER.info("&aWersja systemu: &b" + OS_VERSION);
        LOGGER.info("&aProcesor:&b " + PROCESSOR_NAME);
        LOGGER.info("&aLogiczne rdzenie: &b" + LOGICAL_THREADS);
        LOGGER.info("&aKarty Graficzne: &b" + GRAPHIC_CARDS);
        LOGGER.info("&aDystrybucja: &b" + DISTRIBUTION);
        LOGGER.info("&aNazwa z dystrybucją: &b" + FULL_OS_NAME);

        LOGGER.println();
        LOGGER.alert("&3Dodadkowe Info");
        final long uptimeMillis = (System.nanoTime() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1_000_000;
        LOGGER.info("&cWirtualna maszyna javy działa przez:&b " + DateUtil.formatTimeDynamic(uptimeMillis));

        if (FAMILY == SystemFamily.UNIX) {
            LOGGER.info("&aBox64: &b" + BOX64);
            LOGGER.info("&aWine: &b" + WINE);
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Sieć WLAN i LAN");
        LOGGER.info("&aPołączono z siecią o nazwie:&b " + WIFI_SSID);

        LOGGER.println();
        LOGGER.info("&aIPv&d4&4 -&7 " + IPV4.size());
        for (final Network network : IPV4) {
            LOGGER.info("&3" + network.networkInterface().getDisplayName() + " &7(&d" + network.networkInterface().getName() + "&7)&4 -&b " + network.hostAddress());
        }

        LOGGER.println();
        LOGGER.info("&aIPv&d6&4 -&7 " + IPV6.size());
        for (final Network network : IPV6) {
            LOGGER.info("&3" + network.networkInterface().getDisplayName() + " &7(&d" + network.networkInterface().getName() + "&7)&4 -&b " + network.hostAddress());
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Pamięć komputera");

        LOGGER.info("&aDostępne dyski: &d" + DISKS.size());

        long freeSSD = 0;
        long maxSSD = 0;
        long usedSSD = 0;

        long freeHDD = 0;
        long maxHDD = 0;
        long usedHDD = 0;

        for (final Disk disk : DISKS) {
            final File diskFile = disk.getDiskFile();

            final long max = SystemUtil.getMaxDiskSpace(diskFile);
            final long free = SystemUtil.getFreeDiskSpace(diskFile);
            final long used = SystemUtil.getUsedDiskSpace(diskFile);

            switch (disk.getType()) {
                case "SSD" -> {
                    maxSSD += max;
                    freeSSD += free;
                    usedSSD += used;
                }
                case "HDD" -> {
                    maxHDD += max;
                    freeHDD += free;
                    usedHDD += used;
                }
            }

            LOGGER.println();
            LOGGER.info("&aModel dysku:&3 " + disk.getModel());
            LOGGER.info("&aNazwa: &3" + disk.getName());
            LOGGER.info("&aŚcieżka: &3" + diskFile.getAbsolutePath());
            LOGGER.info("&aSystem Plików:&b " + disk.getFileSystem());
            LOGGER.info("&aTyp Dysku:&b " + disk.getType());
            LOGGER.info("&aRozmiar bloku:&b " + disk.getBlockSize());
            LOGGER.info("&aTylko do odczytu:&b " + disk.isReadOnly());
            LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(max, false));
            LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(used, false));
            LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(free, false));

            LOGGER.println();

            LOGGER.info("&4Test szybkości zapisu pliku");

            final Double mbs = DISK_TEST.getOrDefault(disk, -1D);

            if (mbs > -1) {
                LOGGER.info("&aCzas zapisu to:&b " + mbs + "&e MB/s");
            } else {
                LOGGER.error("&cPodnieś poziom uprawnień aby przetestować dysk");
            }
        }

        LOGGER.println();
        LOGGER.alert("&4Pamięć Wszystkich Dysków&2 SSD");
        LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(maxSSD, false));
        LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(usedSSD, false));
        LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(freeSSD, false));

        if (maxHDD != 0) {
            LOGGER.println();
            LOGGER.alert("&4Pamięć Wszystkich Dysków&f HDD");
            LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(maxHDD, false));
            LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(usedHDD, false));
            LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(freeHDD, false));
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Pamięć Ram");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), false));

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Pamięć SWAP");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeSwap(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedSwap(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap(), false));

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Pamięć RAM + SWAP");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam() + SystemUtil.getFreeSwap(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam() + SystemUtil.getUsedSwap(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap() + SystemUtil.getMaxRam(), false));

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Informacje o kościach Ram");

        if (RAM_LIST.isEmpty()) {
            LOGGER.alert("&cNiema żadnych danych o kościach ramu");
            LOGGER.alert("&cAlbo musiśz podnieść poziom uprawnień użytkownika!!!");
        }

        for (final Ram ram : RAM_LIST) {
            LOGGER.info("&aPojemność RAM:&b " + MathUtil.formatBytesDynamic(ram.size(), false));
            LOGGER.info("&aNominalna prędkość:&b " + ram.basicSpeed() + "&e MHz");
            LOGGER.info("&aAktualne taktowanie:&b " + ram.configuredSpeed() + "&e MHz");
            LOGGER.info("&aTyp pamięci:&b " + ram.memoryType());
            LOGGER.info("&aNumer katalogowy:&b " + ram.partNumber());
            LOGGER.info("&aSlot pamięci:&b " + ram.bankLabel());
            LOGGER.println();
        }

        LOGGER.println();

        if (!GraphicsEnvironment.isHeadless()) {
            final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            LOGGER.alert("&4Monitory");

            final GraphicsDevice[] devices = environment.getScreenDevices();
            LOGGER.info("&aDostępne monitory: &b" + devices.length);

            for (final GraphicsDevice device : devices) {
                final DisplayMode displayMode = device.getDisplayMode();
                final DisplayMode maxMode = Arrays.stream(device.getDisplayModes())
                        .max(Comparator.comparingInt(mode -> mode.getWidth() * mode.getHeight() * mode.getRefreshRate()))
                        .orElse(displayMode);

                LOGGER.println();
                LOGGER.info("&aID: &b" + device.getIDstring());
                LOGGER.info("&aTyp urządzenia: &b" + getDeviceType(device));
                LOGGER.info("&aRoździelczość: &b" + displayMode.getWidth() + "&f x&b " + displayMode.getHeight());
                LOGGER.info("&aMaksymalna rozdzielczość: &b" + maxMode.getWidth() + "x" + maxMode.getHeight());
                LOGGER.info("&aGłębia kolorów: &b" + displayMode.getBitDepth() + " &eBitów");
                LOGGER.info("&aOdświeżanie: &b" + displayMode.getRefreshRate() + " &eHz");
                LOGGER.info("&aMaksymalne odświeżanie: &b" + maxMode.getRefreshRate() + " &eHz");
                LOGGER.info("&aLiczba dostępnych trybów: &b" + device.getDisplayModes().length);

                if (!device.isFullScreenSupported()) LOGGER.info("&cPełen ekran nie jest wspierany");

            }

            LOGGER.println();
            LOGGER.println();

            if (SystemTray.isSupported()) {
                LOGGER.info("&bSystemTray&a jest wspierany");
            } else {
                LOGGER.info("&bSystemTray&c nie jest wspierany");
            }

            if (Desktop.isDesktopSupported()) {
                final List<Desktop.Action> supportedActions = new ArrayList<>();

                for (final Desktop.Action action : Desktop.Action.values()) {
                    if (Desktop.getDesktop().isSupported(action)) {
                        supportedActions.add(action);
                    }
                }

                LOGGER.info("&aWspierane akcje:&b " + MessageUtil.objectListToString(supportedActions, "&e,&b "));

            } else {
                LOGGER.println();
                LOGGER.alert("&cKlasa Desktop nie jest obsługiwana na tym systemie.");
            }

            final List<String> fonts = List.of(environment.getAvailableFontFamilyNames());

            LOGGER.info("&aDostępne czcionki &d(&1" + fonts.size() + "&d):&b " + MessageUtil.stringListToString(fonts, " &a,&b "));

        } else {
            LOGGER.alert("&cSystem działa w trybie headless. Niektóre informacje są niedostępne");
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Inne informacje");
        LOGGER.info("&aJęzyk: &b" + LOCALE);
        LOGGER.info("&aKodowanie: &b" + ENCODING);
        LOGGER.info("&aStrefa czasowa: &b" + ZONE_ID);

        LOGGER.println();

        if (!JAVA_FLAGS.isEmpty()) {
            LOGGER.info("&aWykryte flagi startowe &d(&1" + JAVA_FLAGS.size() + "&d):&b " + MessageUtil.stringListToString(JAVA_FLAGS, " &a,&b "));
        }

        LOGGER.info("&aDostępnych &1" + HASH_ALGORITHMS.size() + "&a algorytmów&d: &b" + MessageUtil.stringListToString(HASH_ALGORITHMS.stream().toList(), "&a,&b "));

        LOGGER.println();
        LOGGER.info("&aAktualna liczba wątków aplikacji: &b" + ThreadUtil.getThreadsCount() + " &g/&b " + ThreadUtil.getPeakThreadsCount());
        try {
            LOGGER.info("&aUżycie RAM przez aktualny proces: &b" + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final UnsupportedSystemException unsupportedSystemException) {
            LOGGER.error("Nie udało się pozyskać ilości RAM dla aktualnego procesu", unsupportedSystemException);
        }

        LOGGER.println();
        LOGGER.println();
    }

    public static void sendSystemInfoWebhook() {
        final String userName = "Tescior";
        final String avatarURL = "https://th.bing.com/th/id/OIP.f3tTSSqVRSktMK8uFBqlJQHaIi?w=148&h=180&c=7&r=0&o=5&dpr=1.3&pid=1.7";
        //I don't care :)
        final String webhookURL = "https://discord.com/api/webhooks/1421525249211633795/y_jH5UC9wxFKZn6NfDjHGOnQCoVMOPdk83fpgULdomHhYZ-EL6wHkxc3L38JUU53IOad";

        final WebHookClient client = new WebHookClient(LOGGER, false, webhookURL, userName, avatarURL);

        final Author author = new Author("IndianBartonka.pl", "https://indianbartonka.pl/", "https://indianbartonka.pl/img/favicon.jpg");
        final Footer footer = new Footer("Dane systemowe", "https://th.bing.com/th/id/OIP.V4QmXbKfBya0XHTzLMuhCAHaE8?rs=1&pid=ImgDetMain");

        // 🖥️ SYSTEM INFO
        final List<Field> systemFields = Arrays.asList(
                new Field("Nazwa systemu", "`" + SYSTEM_NAME + "` | `" + SYSTEM_OS + "` | `" + FAMILY + "`", false),
                new Field("Architektura", "`" + RAW_SYSTEM_ARCH + "` | `" + SYSTEM_ARCH + "`", false),
                new Field("Wersja", "`" + OS_VERSION + "`", false),
                new Field("Procesor", "`" + PROCESSOR_NAME.trim() + "`", false),
                new Field("Logiczne rdzenie", "`" + LOGICAL_THREADS + "`", false),
                new Field("Karty graficzne", "`" + GRAPHIC_CARDS + "`", false),
                new Field("Dystrybucja", "`" + DISTRIBUTION + "`", false),
                new Field("Pełna nazwa", "`" + FULL_OS_NAME + "`", false)
        );

        final Embed systemEmbed = new EmbedBuilder()
                .setTitle("🖥️ Informacje o systemie")
                .setMessage("Podstawowe dane o systemie operacyjnym i sprzęcie.")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.CYAN)
                .setFields(systemFields)
                .setFooter(footer)
                .build();

        client.sendEmbedMessage(systemEmbed);

        // 💾 DYSKI

        long freeSSD = 0;
        long maxSSD = 0;
        long usedSSD = 0;

        long freeHDD = 0;
        long maxHDD = 0;
        long usedHDD = 0;

        final List<Field> diskFields = new ArrayList<>();
        for (final Disk disk : DISKS) {
            final File file = disk.getDiskFile();
            final long max = SystemUtil.getMaxDiskSpace(file);
            final long used = SystemUtil.getUsedDiskSpace(file);
            final long free = SystemUtil.getFreeDiskSpace(file);
            final Double speed = DISK_TEST.getOrDefault(disk, -1D);

            switch (disk.getType()) {
                case "SSD" -> {
                    maxSSD += max;
                    freeSSD += free;
                    usedSSD += used;
                }
                case "HDD" -> {
                    maxHDD += max;
                    freeHDD += free;
                    usedHDD += used;
                }
            }

            diskFields.add(new Field(disk.getModel() + " (" + disk.getType() + ")",
                    "Całkowita: `" + MathUtil.formatBytesDynamic(max, false)
                            + "`\nUżyta: `" + MathUtil.formatBytesDynamic(used, false)
                            + "`\nWolna: `" + MathUtil.formatBytesDynamic(free, false)
                            + "`\nSzybkość zapisu: `" + (speed > -1 ? speed + " MB/s" : "Brak uprawnień") + "`", false));
        }

        diskFields.add(new Field("Pamięć Wszystkich Dysków SSD",
                "Całkowita: `" + MathUtil.formatBytesDynamic(maxSSD, false)
                        + "`\nUżyta: `" + MathUtil.formatBytesDynamic(usedSSD, false)
                        + "`\nWolna: `" + MathUtil.formatBytesDynamic(freeSSD, false) + "`", false)
        );

        if (maxHDD != 0) {
            diskFields.add(new Field("Pamięć Wszystkich Dysków HDD",
                    "Całkowita: `" + MathUtil.formatBytesDynamic(maxHDD, false)
                            + "`\nUżyta: `" + MathUtil.formatBytesDynamic(usedHDD, false)
                            + "`\nWolna: `" + MathUtil.formatBytesDynamic(freeHDD, false) + "`", false)
            );
        }

        final Embed diskEmbed = new EmbedBuilder()
                .setTitle("💾 Pamięć")
                .setMessage("Informacje o dostępnych dyskach i ich pojemności")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.ORANGE)
                .setFields(diskFields)
                .setFooter(footer)
                .build();

        client.sendEmbedMessage(diskEmbed);

        // 🧠 RAM + SWAP
        final List<Field> memoryFields = Arrays.asList(
                new Field("Wolna RAM", "`" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), false) + "`", false),
                new Field("Użyta RAM", "`" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), false) + "`", false),
                new Field("Całkowita RAM", "`" + MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), false) + "`", false),
                new Field("Wolna SWAP", "`" + MathUtil.formatBytesDynamic(SystemUtil.getFreeSwap(), false) + "`", false),
                new Field("Użyta SWAP", "`" + MathUtil.formatBytesDynamic(SystemUtil.getUsedSwap(), false) + "`", false),
                new Field("Całkowita SWAP", "`" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap(), false) + "`", false)
        );

        final Embed memoryEmbed = new EmbedBuilder()
                .setTitle("🧠 Pamięć RAM i SWAP")
                .setMessage("Szczegóły wykorzystania pamięci operacyjnej i wymiany")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.GREEN)
                .setFields(memoryFields)
                .setFooter(footer)
                .build();

        client.sendEmbedMessage(memoryEmbed);

        final List<Field> ramStickFields = new ArrayList<>();

        for (final Ram ram : RAM_LIST) {
            ramStickFields.add(new Field("Pojemność", MathUtil.formatBytesDynamic(ram.size()), true));
            ramStickFields.add(new Field("Nominalna prędkość", ram.basicSpeed() + "MHz", true));
            ramStickFields.add(new Field("Aktualne taktowanie", ram.configuredSpeed() + "MHz", true));
            ramStickFields.add(new Field("Typ pamięci", ram.memoryType(), true));

            ramStickFields.add(new Field("Numer katalogowy", ram.partNumber(), true));
            ramStickFields.add(new Field("Slot pamięci", ram.bankLabel(), true));
            ramStickFields.add(new Field("", "", false));
            ramStickFields.add(new Field("", "", false));
        }

        final Embed ramStickEmbed = new EmbedBuilder()
                .setTitle("🧠 Kości Ram")
                .setMessage("Szczegóły o kościach pamięci ram")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.GREEN)
                .setFields(ramStickFields)
                .setFooter(footer)
                .build();

        if (!RAM_LIST.isEmpty()) {
            client.sendEmbedMessage(ramStickEmbed);
        }

        final List<Field> monitorFields = new ArrayList<>();

        if (!GraphicsEnvironment.isHeadless()) {
            final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] devices = environment.getScreenDevices();

            monitorFields.add(new Field("Liczba monitorów", "`" + devices.length + "`", false));

            for (final GraphicsDevice device : devices) {
                final DisplayMode displayMode = device.getDisplayMode();
                final DisplayMode maxMode = Arrays.stream(device.getDisplayModes())
                        .max(Comparator.comparingInt(mode -> mode.getWidth() * mode.getHeight() * mode.getRefreshRate()))
                        .orElse(displayMode);

                final String info =
                        "**ID:** `" + device.getIDstring() + "`\n" +
                                "**Typ:** `" + getDeviceType(device) + "`\n" +
                                "**Rozdzielczość:** `" + displayMode.getWidth() + "`x`" + displayMode.getHeight() + "`\n" +
                                "**Maks. rozdzielczość:** `" + maxMode.getWidth() + "`x`" + maxMode.getHeight() + "`\n" +
                                "**Głębia kolorów:** `" + displayMode.getBitDepth() + "` bit\n" +
                                "**Odświeżanie:** `" + displayMode.getRefreshRate() + "` Hz\n" +
                                "**Maks. odświeżanie:** `" + maxMode.getRefreshRate() + "` Hz\n" +
                                "**Tryby:** `" + device.getDisplayModes().length + "`\n" +
                                "**Pełen ekran:** " + (device.isFullScreenSupported() ? "`Wspierany`" : "`Brak wsparcia`");

                monitorFields.add(new Field(device.getIDstring(), info, false));
            }

            if (SystemTray.isSupported()) {
                monitorFields.add(new Field("🪟 SystemTray", "`Wspierany`", true));
            } else {
                monitorFields.add(new Field("🪟 SystemTray", "`Nie wspierany`", true));
            }

            if (Desktop.isDesktopSupported()) {
                final List<Desktop.Action> supportedActions = new ArrayList<>();
                for (final Desktop.Action action : Desktop.Action.values()) {
                    if (Desktop.getDesktop().isSupported(action)) supportedActions.add(action);
                }
                monitorFields.add(new Field("💻 Wspierane akcje Desktop",
                        "`" + MessageUtil.objectListToString(supportedActions, "`, `") + "`", false));
            } else {
                monitorFields.add(new Field("💻 Klasa Desktop", "❌ `Nie obsługiwana na tym systemie`", false));
            }

            final List<String> fonts = List.of(environment.getAvailableFontFamilyNames());

            monitorFields.add(new Field("📝 Dostępne czcionki (" + fonts.size() + ")", "`" + MessageUtil.objectListToString(fonts.stream().limit(10).toList(), "`, `") + "` i więcej", false));

        } else {
            monitorFields.add(new Field("⚠️ Tryb headless",
                    "System działa w trybie headless — informacje o monitorach są niedostępne.", false));
        }

        final Embed monitorEmbed = new EmbedBuilder()
                .setTitle("🖥️ Monitory")
                .setMessage("Monitory")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.GREEN)
                .setFields(monitorFields)
                .setFooter(footer)
                .build();

        client.sendEmbedMessage(monitorEmbed);

        // ⚙️ INNE INFO
        final List<Field> miscFields = Arrays.asList(
                new Field("Język", "`" + LOCALE + "`", false),
                new Field("Kodowanie", "`" + ENCODING + "`", false),
                new Field("Strefa czasowa", "`" + ZONE_ID + "`", false),
                new Field("Liczba wątków", "`" + ThreadUtil.getThreadsCount() + " / " + ThreadUtil.getPeakThreadsCount() + "`", false),
                new Field("RAM procesu", "`" + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false) + "`", false),
                new Field("Flagi JVM", "`" + (JAVA_FLAGS.isEmpty() ? "Brak" : String.join("`, `", JAVA_FLAGS)) + "`", false),
                new Field("Algorytmy hashujące", "`" + String.join(", ", HASH_ALGORITHMS) + "`", false)
        );

        final Embed miscEmbed = new EmbedBuilder()
                .setTitle("⚙️ Inne informacje")
                .setMessage("Informacje o JVM, lokalizacji i konfiguracji systemu")
                .setTimestamp(Instant.now().toString())
                .setAuthor(author)
                .setColor(Color.MAGENTA)
                .setFields(miscFields)
                .setFooter(footer)
                .build();

        client.sendEmbedMessage(miscEmbed);

        client.shutdown();

        //TODO: Dodaj info o wersji jvm i spróbuj dodać wysyłanie pliku w webhooku 
    }


    private static String getDeviceType(final GraphicsDevice device) {
        return switch (device.getType()) {
            case GraphicsDevice.TYPE_RASTER_SCREEN -> "Monitor";
            case GraphicsDevice.TYPE_PRINTER -> "Drukarka";
            case GraphicsDevice.TYPE_IMAGE_BUFFER -> "Bufor obrazu";
            default -> "Inne";
        };
    }
}
