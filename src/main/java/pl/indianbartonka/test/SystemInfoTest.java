package pl.indianbartonka.test;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.List;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.ThreadUtil;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.LoggerConfiguration;
import pl.indianbartonka.util.system.Disk;
import pl.indianbartonka.util.system.SystemUtil;

public final class SystemInfoTest {

    private static final Logger LOGGER = new Logger(new LoggerConfiguration(true, System.getProperty("user.dir") + File.separator + "logs", true)) {
    };

    public static void main(final String[] args) {
        LOGGER.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + System.getProperty("java.version") + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");

        LOGGER.print();
        LOGGER.print();

        LOGGER.info("&aNazwa systemu: &b" + System.getProperty("os.name") + "&4 |&b " + SystemUtil.getSystem() + "&4 |&b " + SystemUtil.getSystemFamily());
        LOGGER.info("&aWersia systemu: &b" + SystemUtil.getOSVersion());
        LOGGER.info("&aArchitektura: &b" + System.getProperty("os.arch") + "&4 |&b " + SystemUtil.getCurrentArch());
        LOGGER.info("&aDystrybucja: &b" + SystemUtil.getDistribution());
        LOGGER.info("&aNazwa z dystrybujcą: &b" + SystemUtil.getFullOSNameWithDistribution());

        LOGGER.print();
        LOGGER.print();

        LOGGER.alert("&4Pamięć komputera");
        final List<Disk> disks = SystemUtil.getAvailableDisk();

        LOGGER.info("&aDostępne dyski: &d" + disks.size());

        for (final Disk disk : disks) {
            final File diskFile = disk.diskFile();
            LOGGER.print();
            LOGGER.info("&aNazwa: &3" + disk.name());
            LOGGER.info("&aŚcieżka: &3" + diskFile.getAbsolutePath());
            LOGGER.info("&aTyp dysku:&b " + disk.type());
            LOGGER.info("&aRozmiar bloku:&b " + disk.blockSize());
            LOGGER.info("&aTylko do odczytu:&b " + disk.readOnly());

            LOGGER.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getMaxDiskSpace(diskFile), false));
            LOGGER.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getUsedDiskSpace(diskFile), false));
            LOGGER.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getFreeDiskSpace(diskFile), false));
        }

        LOGGER.print();
        LOGGER.print();

        LOGGER.alert("&4Pamięć Ram");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), false));

        LOGGER.print();
        LOGGER.print();

        LOGGER.alert("&4Pamięć SWAP");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeSwap(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedSwap(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap(), false));

        LOGGER.print();
        LOGGER.print();

        LOGGER.alert("&4Pamięć RAM + SWAP");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam() + SystemUtil.getFreeSwap(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam() + SystemUtil.getUsedSwap(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap() + SystemUtil.getMaxRam(), false));

        LOGGER.print();
        LOGGER.print();

        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        LOGGER.info("&aJęzyk: &b" + SystemUtil.LOCALE.toLanguageTag());
        LOGGER.info("&aDostępne monitory: &b" + environment.getScreenDevices().length);

        LOGGER.print();
        LOGGER.print();

        LOGGER.info("&aAktualna liczba wątków aplikacji: &b" + ThreadUtil.getThreadsCount() + " &g/&b " + ThreadUtil.getPeakThreadsCount());
        try {
            LOGGER.info("&aUzycje ramu przez aktualny process&b: " + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final IOException ioException) {
            LOGGER.error("Nie udało się pozyskać ilości ram dla aktualnego procesu", ioException);
        }
    }
}