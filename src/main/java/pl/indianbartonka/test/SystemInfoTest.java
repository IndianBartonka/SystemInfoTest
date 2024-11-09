package pl.indianbartonka.test;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.List;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.argument.Arg;
import pl.indianbartonka.util.argument.ArgumentParser;
import pl.indianbartonka.util.logger.LogState;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.config.LoggerConfiguration;
import pl.indianbartonka.util.system.Disk;
import pl.indianbartonka.util.system.SystemUtil;

public final class SystemInfoTest {

    private static final LoggerConfiguration loggerConfiguration = LoggerConfiguration.builder()
            .setLogsPath(System.getProperty("user.dir") + File.separator + "logs")
            .build();

    private static final Logger LOGGER = new Logger(loggerConfiguration) {
    };

    public static void main(final String[] args) {
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
                            //TODO: Użyj metody print w Logger po tym jak wyjdzie nowe IndianUtils

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
    }

    private static void noArgs() {
        LOGGER.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + System.getProperty("java.version") + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4System Info");
        LOGGER.info("&aNazwa systemu: &b" + System.getProperty("os.name") + "&4 |&b " + SystemUtil.getSystem() + "&4 |&b " + SystemUtil.getSystemFamily());
        LOGGER.info("&aWersja systemu: &b" + SystemUtil.getOSVersion());
        LOGGER.info("&aArchitektura: &b" + System.getProperty("os.arch") + "&4 |&b " + SystemUtil.getCurrentArch());
        LOGGER.info("&aDystrybucja: &b" + SystemUtil.getDistribution());
        LOGGER.info("&aNazwa z dystrybucją: &b" + SystemUtil.getFullOSNameWithDistribution());

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
            LOGGER.info("&aTyp dysku:&b " + disk.type());
            LOGGER.info("&aRozmiar bloku:&b " + disk.blockSize());
            LOGGER.info("&aTylko do odczytu:&b " + disk.readOnly());
            LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getMaxDiskSpace(diskFile), false));
            LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getUsedDiskSpace(diskFile), false));
            LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getFreeDiskSpace(diskFile), false));
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

        LOGGER.alert("&4Monitory");

        logGraphicsInfo();

        LOGGER.println();
        LOGGER.println();

        LOGGER.alert("&4Inne informacje");
        logOtherInfo();
    }

    private static void logGraphicsInfo() {
        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] devices = environment.getScreenDevices();
        LOGGER.info("&aDostępne monitory: &b" + devices.length);

        for (final GraphicsDevice device : devices) {
            final DisplayMode displayMode = device.getDisplayMode();
            LOGGER.println();
            LOGGER.info("&aID: &b" + device.getIDstring());
            LOGGER.info("&aRoździelczość: &b" + displayMode.getWidth() + "&f x&b " + displayMode.getHeight());
            LOGGER.info("&aGłębia kolorów: &b" + displayMode.getBitDepth() + " &eBitów");
            LOGGER.info("&aOdświeżanie: &b" + displayMode.getRefreshRate() + " &eHz");

            final int acceleratedMemory = device.getAvailableAcceleratedMemory();
            if (acceleratedMemory != -1) {
                LOGGER.info("&aPrzyspieszona pamięć akceleracji: &b" + acceleratedMemory + " MB");
            } else {
                LOGGER.info("&aPrzyspieszona pamięć akceleracji: &cNiedostępna");
            }
        }
    }

    private static void logOtherInfo() {
        LOGGER.info("&aJęzyk: &b" + SystemUtil.LOCALE.toLanguageTag());

        LOGGER.println();
        LOGGER.println();
        LOGGER.info("&aAktualna liczba wątków aplikacji: &b" + ThreadUtil.getThreadsCount() + " &g/&b " + ThreadUtil.getPeakThreadsCount());
        try {
            LOGGER.info("&aUżycie RAM przez aktualny proces: &b" + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final IOException ioException) {
            LOGGER.error("Nie udało się pozyskać ilości RAM dla aktualnego procesu", ioException);
        }
    }
}