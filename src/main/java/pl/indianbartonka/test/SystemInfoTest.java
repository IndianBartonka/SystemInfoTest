package pl.indianbartonka.test;

import com.sun.management.OperatingSystemMXBean;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import pl.indianbartonka.util.MathUtil;
import pl.indianbartonka.util.logger.Logger;
import pl.indianbartonka.util.logger.LoggerConfiguration;
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
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(SystemUtil.availableDiskSpace(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(SystemUtil.usedDiskSpace(), false));
        LOGGER.info("&aMaksymalne: &b" + MathUtil.formatBytesDynamic(SystemUtil.maxDiskSpace(), false));

        LOGGER.print();
        LOGGER.print();

        LOGGER.alert("&4Pamięć Ram");
        LOGGER.info("&aWolne: &b" + MathUtil.formatBytesDynamic(getFreeRam(), false));
        LOGGER.info("&aUżyte: &b" + MathUtil.formatBytesDynamic(getUsedRam(), false));
        LOGGER.info("&aDostępne: &b" + MathUtil.formatBytesDynamic(getAvailableRam(), false));

        LOGGER.print();
        LOGGER.print();

        final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        LOGGER.info("&aJęzyk: &b" + SystemUtil.LOCALE.toLanguageTag());
        LOGGER.info("&aLiczba monitorów: &b" + environment.getScreenDevices().length);

        LOGGER.print();
        LOGGER.print();

        try {
            LOGGER.info("&aUzycje ramu przez aktualny process&b: " + MathUtil.formatBytesDynamic(SystemUtil.getRamUsageByPid(ProcessHandle.current().pid()), false));
        } catch (final IOException ioException) {
            LOGGER.error("Nie udało się pozyskać ilości ram dla aktualnego procesu", ioException);
        }
    }

    //TODO: Usun to jak wyhdzie nowe IndianUtils
    public static long getUsedRam() {
        return getAvailableRam() - getFreeRam();
    }

    public static long getAvailableRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalMemorySize();
    }

    public static long getFreeRam() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreeMemorySize();
    }
}