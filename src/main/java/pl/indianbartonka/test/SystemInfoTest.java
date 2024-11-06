package pl.indianbartonka.test;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
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

    public static void main(final String[] args) {
        //TODO: Wyłączyć plik logu jak wyjdzie nowe IndianUtils
        final Logger logger = new Logger(new LoggerConfiguration(true, System.getProperty("user.dir") + File.separator + "logs", true)) {
        };

        logger.info("&aUżyto Java: &b" + System.getProperty("java.vm.name") + " &1" + System.getProperty("java.version") + " &5(&d" + System.getProperty("java.vendor") + "&5)&r na&f "
                + SystemUtil.getFullOSNameWithDistribution() + " &5(&c" + SystemUtil.getFullyArchCode() + "&5)");

        logger.print();
        logger.print();

        logger.info("&aNazwa systemu: &b" + System.getProperty("os.name") + "&4 |&b " + SystemUtil.getSystem() + "&4 |&b " + SystemUtil.getSystemFamily());
        logger.info("&aWersia systemu: &b" + SystemUtil.getOSVersion());
        logger.info("&aArchitektura: &b" + System.getProperty("os.arch") + "&4 |&b " + SystemUtil.getCurrentArch());
        logger.info("&aDystrybucja: &b" + SystemUtil.getDistribution());
        logger.info("&aNazwa z dystrybujcą: &b" + SystemUtil.getFullOSNameWithDistribution());

        logger.print();
        logger.print();

        logger.alert("&4Pamięć komputera");
        final List<Disk> disks = SystemUtil.getAvailableDisk();

        logger.info("&aDostępne dyski: &d" + disks.size());

        for (final Disk disk : disks) {
            final File diskFile = disk.diskFile();
            logger.print();
            logger.info("&aNazwa: &3" + disk.name());
            logger.info("&aŚcieżka: &3" + diskFile.getAbsolutePath());
            logger.info("&aTyp dysku:&b " + disk.type());
            logger.info("&aRozmiar bloku:&b " + disk.blockSize());
            logger.info("&aTylko do odczytu:&b " + disk.readOnly());

            logger.info("&aCałkowita pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getMaxDiskSpace(diskFile), false));
            logger.info("&aUżyta pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getUsedDiskSpace(diskFile), false));
            logger.info("&aWolna pamięć:&b " + MathUtil.formatBytesDynamic(SystemUtil.getFreeDiskSpace(diskFile), false));
        }

        logger.print();
        logger.print();

        logger.alert("&4Pamięć Ram");
        logger.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam(), false));
        logger.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam(), false));
        logger.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxRam(), false));

        logger.print();
        logger.print();

        logger.alert("&4Pamięć SWAP");
        logger.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeSwap(), false));
        logger.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedSwap(), false));
        logger.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap(), false));

        logger.print();
        logger.print();

        logger.alert("&4Pamięć RAM + SWAP");
        logger.info("&aWolne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getFreeRam() + SystemUtil.getFreeSwap(), false));
        logger.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.getUsedRam() + SystemUtil.getUsedSwap(), false));
        logger.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.getMaxSwap() + SystemUtil.getMaxRam(), false));

        logger.print();
        logger.print();

        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] devices = environment.getScreenDevices();

        logger.alert("&4Monitory");
        logger.info("&aDostępne monitory: &b" + devices.length);

        for (final GraphicsDevice device : devices) {
            final DisplayMode displayMode = device.getDisplayMode();
            logger.print();

            logger.info("&aID: &b" + device.getIDstring());
            logger.info("&aRoździelczość: &b" + displayMode.getWidth() + "&f x&b " + displayMode.getHeight());
            logger.info("&aGłębia kolorów: &b" + displayMode.getBitDepth() + " &eBitów");
            logger.info("&aOdświeżanie: &b" + displayMode.getRefreshRate() + " &eHz");

            final int acceleratedMemory = device.getAvailableAcceleratedMemory();
            if (acceleratedMemory != -1) {
                logger.info("&aPrzyspieszona pamięć akceleracji: &b" + acceleratedMemory + " MB");
            } else {
                logger.info("&aPrzyspieszona pamięć akceleracji: &cNiedostępna");
            }
        }

        logger.print();
        logger.print();

        logger.alert("&4Inne informacje");
        logger.info("&aJęzyk: &b" + SystemUtil.LOCALE.toLanguageTag());

        logger.print();
        logger.print();

        logger.info("&aAktualna liczba wątków aplikacji: &b" + ThreadUtil.getThreadsCount() + " &g/&b " + ThreadUtil.getPeakThreadsCount());
        try {
            logger.info("&aUzycje ramu przez aktualny process&b: " + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final IOException ioException) {
            logger.error("Nie udało się pozyskać ilości ram dla aktualnego procesu", ioException);
        }
    }
}