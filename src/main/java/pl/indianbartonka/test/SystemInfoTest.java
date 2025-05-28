package pl.indianbartonka.test;

import java.awt.Desktop;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.security.Security;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.VisibleForTesting;
import pl.indianbartonka.util.DateUtil;
import pl.indianbartonka.util.FileUtil;
import pl.indianbartonka.util.IndianUtils;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.MemoryUnit;
import pl.indianbartonka.util.MessageUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.argument.Arg;
import pl.indianbartonka.util.argument.ArgumentParser;
import pl.indianbartonka.util.exception.UnsupportedSystemException;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.config.LoggerConfiguration;
import pl.indianbartonka.util.network.Network;
import pl.indianbartonka.util.network.NetworkUtil;
import pl.indianbartonka.util.system.Disk;
import pl.indianbartonka.util.system.Ram;
import pl.indianbartonka.util.system.SystemFamily;
import pl.indianbartonka.util.system.SystemUtil;

public final class SystemInfoTest {

    private static final LoggerConfiguration LOGGER_CONFIGURATION = LoggerConfiguration.builder()
            .setLoggingToFile(true)
            .setOneLog(true)
            .build();

    private static final Logger LOGGER = new Logger(LOGGER_CONFIGURATION) {
    };

    public static void main(final String[] args) {
        final long startTime = System.currentTimeMillis();
        final ArgumentParser argumentParser = new ArgumentParser(args);

        if (argumentParser.isAnyArgument()) {
            for (final Arg arg : argumentParser.getArgsList()) {
                final String name = arg.name();
                final String value = arg.value();

                switch (name) {
                    case "help" -> {
                        LOGGER.println();
                        LOGGER.info("&4Pomoc");
                        LOGGER.info("&a-ramTest&b:&3LICZBA&c -&b test ramu");
                        LOGGER.info("&a-swapTest&b:&3LICZBA&c -&b test swapu");
                        LOGGER.println();
                        LOGGER.alert("&cNOTE:&b Każdy test wykonywany jest co&a 1s");
                    }

                    case "ramTest" -> {
                        final int times = Integer.parseInt(value);

                        LOGGER.println();
                        LOGGER.info("&4Ram Test");
                        for (int i = 0; i < times; i++) {

                            final String used = MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), true);
                            final String max = MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), true);
                            final String free = MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), true);

                            LOGGER.print("&b" + used + "&f /&b " + max + "&7 (&aFree:&b " + free + "&7)", LogState.INFO);
                            ThreadUtil.sleep(1);
                        }
                        LOGGER.println();
                        LOGGER.info("&aWykonano &4Ram Test&b " + times + "&a razy");
                    }

                    case "swapTest" -> {
                        final int times = Integer.parseInt(value);

                        LOGGER.println();
                        LOGGER.info("&4Swap Test");
                        for (int i = 0; i < times; i++) {
                            final String used = MathUtil.formatBytesDynamic(SystemUtil.getUsedSwap(), true);
                            final String max = MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap(), true);
                            final String free = MathUtil.formatBytesDynamic(SystemUtil.getFreeSwap(), true);

                            LOGGER.print("&b" + used + "&f /&b " + max + "&7 (&aFree:&b " + free + "&7)", LogState.INFO);
                            ThreadUtil.sleep(1);
                        }
                        LOGGER.println();
                        LOGGER.info("&aWykonano &4Swap Test&b " + times + "&a razy");
                    }

                    default -> LOGGER.alert("&4Nieznany argument: &b" + arg);
                }
            }
        } else {
            noArgs();
        }

        LOGGER.println();
        LOGGER.info("&aWykonano w:&b " + DateUtil.formatTimeDynamic(System.currentTimeMillis() - startTime, true));
    }

    private static void noArgs() {
        LOGGER.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + System.getProperty("java.runtime.version") + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");

        final String processorName = SystemUtil.getProcesorName();
        final String graphicCards = MessageUtil.stringListToString(SystemUtil.getGraphicCardsName(), " | ");

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4System Info");

        final SystemFamily family = SystemUtil.getSystemFamily();

        LOGGER.info("&aNazwa systemu: &b" + System.getProperty("os.name") + "&4 |&b " + SystemUtil.getSystem() + "&4 |&b " + family);
        LOGGER.info("&aArchitektura: &b" + System.getProperty("os.arch") + "&4 |&b " + SystemUtil.getCurrentArch());
        LOGGER.info("&aWersja systemu: &b" + SystemUtil.getOSVersion());
        LOGGER.info("&aProcesor:&b " + processorName);
        LOGGER.info("&aLogiczne rdzenie: &b" + ThreadUtil.getLogicalThreads());
        LOGGER.info("&aKarty Graficzne: &b" + graphicCards);
        LOGGER.info("&aDystrybucja: &b" + SystemUtil.getDistribution());
        LOGGER.info("&aNazwa z dystrybucją: &b" + SystemUtil.getFullOSNameWithDistribution());

        LOGGER.println();
        LOGGER.alert("&3Dodadkowe Info");
        final long uptimeMillis = (System.nanoTime() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1_000_000;
        LOGGER.info("&cWirtualna maszyna javy działa przez:&b " + DateUtil.formatTimeDynamic(uptimeMillis));

        if (family == SystemFamily.UNIX) {
            LOGGER.info("&aBox64: &b" + IndianUtils.box64Check());
            LOGGER.info("&aWine: &b" + IndianUtils.wineCheck());
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Sieć WLAN i LAN");
        LOGGER.info("&aPołączono z siecią o nazwie:&b " + NetworkUtil.getWiFiSSID());

        LOGGER.println();
        final List<Network> ipv4 = NetworkUtil.getIPv4();
        LOGGER.info("&aIPv&d4&4 -&7 " + ipv4.size());
        for (final Network network : ipv4) {
            LOGGER.info("&3" + network.networkInterface().getDisplayName() + " &7(&d" + network.networkInterface().getName() + "&7)&4 -&b " + network.hostAddress());
        }

        LOGGER.println();
        final List<Network> ipv6 = NetworkUtil.getIPv6();
        LOGGER.info("&aIPv&d6&4 -&7 " + ipv6.size());
        for (final Network network : ipv6) {
            LOGGER.info("&3" + network.networkInterface().getDisplayName() + " &7(&d" + network.networkInterface().getName() + "&7)&4 -&b " + network.hostAddress());
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Pamięć komputera");

        final List<Disk> disks = SystemUtil.getAvailableDisk();
        LOGGER.info("&aDostępne dyski: &d" + disks.size());

        for (final Disk disk : disks) {
            final File diskFile = disk.diskFile();
            LOGGER.println();
            LOGGER.info("&aNazwa: &3" + disk.name());
            LOGGER.info("&aŚcieżka: &3" + diskFile.getAbsolutePath());
            LOGGER.info("&aSystem Plików:&b " + disk.type());
            LOGGER.info("&aRozmiar bloku:&b " + disk.blockSize());
            LOGGER.info("&aTylko do odczytu:&b " + disk.readOnly());
            LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getMaxDiskSpace(diskFile), false));
            LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getUsedDiskSpace(diskFile), false));
            LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getFreeDiskSpace(diskFile), false));

            LOGGER.println();
            LOGGER.info("&4Testowanie szybkości zapisu pliku&e 100mb 3razy");
            try {
                final long time = testDisk(disk, 100, 3);

                if (time > -1) {
                    LOGGER.info("&aCzas zapisu to:&b " + DateUtil.formatTimeDynamic(time));
                } else {
                    LOGGER.error("&cPodnieś poziom uprawnień aby przetestować dysk");
                }
            } catch (final IOException ioException) {
                ioException.printStackTrace();
            }
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

        final List<Ram> ramList = SystemUtil.getRamData();

        if (ramList.isEmpty()) {
            LOGGER.alert("&cNiema żadnych danych o kościach ramu");
        }

        for (final Ram ram : ramList) {
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
            LOGGER.alert("&4Monitory");

            final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice[] devices = environment.getScreenDevices();
            LOGGER.info("&aDostępne monitory: &b" + devices.length);

            for (final GraphicsDevice device : devices) {
                final DisplayMode displayMode = device.getDisplayMode();
                final DisplayMode maxMode = Arrays.stream(device.getDisplayModes())
                        .max(Comparator.comparingInt(mode -> mode.getWidth() * mode.getHeight()))
                        .orElse(displayMode);

                LOGGER.println();
                LOGGER.info("&aID: &b" + device.getIDstring());
                LOGGER.info("&aTyp urządzenia: &b" + getDeviceType(device));
                LOGGER.info("&aRoździelczość: &b" + displayMode.getWidth() + "&f x&b " + displayMode.getHeight());
                LOGGER.info("&aMaksymalna rozdzielczość: &b" + maxMode.getWidth() + "x" + maxMode.getHeight());
                LOGGER.info("&aGłębia kolorów: &b" + displayMode.getBitDepth() + " &eBitów");
                LOGGER.info("&aOdświeżanie: &b" + displayMode.getRefreshRate() + " &eHz");
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
        } else {
            LOGGER.alert("&cSystem działa w trybie headless. Niektóre informacje są niedostępne");
        }

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Inne informacje");
        LOGGER.info("&aJęzyk: &b" + SystemUtil.LOCALE.toLanguageTag());
        LOGGER.info("&aKodowanie: &b" + Charset.defaultCharset().displayName());
        LOGGER.info("&aStrefa czasowa: &b" + ZoneId.systemDefault());

        LOGGER.println();

        final List<String> flags = ManagementFactory.getRuntimeMXBean().getInputArguments();
        if (!flags.isEmpty()) {
            LOGGER.info("&aWykryte flagi startowe &d(&1" + flags.size() + "&d):&b " + MessageUtil.stringListToString(flags, " &a,&b "));
        }

        final Set<String> algorithms = Security.getAlgorithms("MessageDigest");

        LOGGER.info("&aDostępnych &1" + algorithms.size() + "&a algorytmów&d: &b" + MessageUtil.stringListToString(algorithms.stream().toList(), "&a,&b "));

        LOGGER.println();
        LOGGER.info("&aAktualna liczba wątków aplikacji: &b" + ThreadUtil.getThreadsCount() + " &g/&b " + ThreadUtil.getPeakThreadsCount());
        try {
            LOGGER.info("&aUżycie RAM przez aktualny proces: &b" + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final UnsupportedSystemException unsupportedSystemException) {
            LOGGER.error("Nie udało się pozyskać ilości RAM dla aktualnego procesu", unsupportedSystemException);
        }
    }

    private static String getDeviceType(final GraphicsDevice device) {
        return switch (device.getType()) {
            case GraphicsDevice.TYPE_RASTER_SCREEN -> "Monitor";
            case GraphicsDevice.TYPE_PRINTER -> "Drukarka";
            case GraphicsDevice.TYPE_IMAGE_BUFFER -> "Bufor obrazu";
            default -> "Inne";
        };
    }

    @VisibleForTesting
    @CheckReturnValue
    public static long testDisk(final Disk disk, final int mbSize, final int totalWrites) throws IOException {
        final File fileDir = new File(disk.diskFile(), String.valueOf(UUID.randomUUID()));
        final File file = new File(fileDir, "testFile.dat");

        try {
            Files.createDirectories(fileDir.toPath());

            if (!file.createNewFile()) {
                throw new IOException("Nie można utworzyć pliku testowego z nieznanych przyczyn");
            }

            final long startTime = System.currentTimeMillis();

            try (final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                final byte[] buffer = new byte[Math.toIntExact(MemoryUnit.MEGABYTES.to(mbSize, MemoryUnit.BYTES))];

                for (int i = 0; i < totalWrites; i++) {
                    randomAccessFile.write(buffer);
                    randomAccessFile.getChannel().force(false);
                }
            }

            return System.currentTimeMillis() - startTime;
        } catch (final AccessDeniedException accessDeniedException) {
            return -1;
        } finally {
            if (file.exists()) FileUtil.deleteFile(file);
            if (fileDir.exists()) FileUtil.deleteFile(fileDir);
        }
    }
}