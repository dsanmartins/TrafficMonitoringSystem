import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Main {
	private static String pattern = ".*se\\.lnu\\.trafficmonitoring\\..*\\.jar$";

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Properties props = new Properties();

		if (args.length != 1) {
			System.out.println("Port for camera must be provided!!!");
			System.exit(1);
		}

		System.setProperty("se.lnu.trafficmonitoring.communicator.port",
				args[0]);

		try {
			BundleContext ctx = EclipseStarter.startup(args, null);

			File classpathRoot = new File(".");
			File[] jarFiles = classpathRoot.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(pattern);

				}
			});

			Bundle bundleName;
			List<Bundle> bundleList = new ArrayList<Bundle>();
			for (File file : jarFiles) {

				bundleName = ctx.installBundle("file:" + file.getName());
				bundleList.add(bundleName);

			}

			for (Bundle bundle : bundleList) {
				bundle.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
