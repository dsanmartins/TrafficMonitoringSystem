package se.lnu.trafficmonitoring.trafficmonitorimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.camera.CameraTracker;
import se.lnu.trafficmonitoring.orgcontroller.OrgControllerTracker;
import se.lnu.trafficmonitoring.trafficmonitor.TrafficMonitor;

public class Activator implements BundleActivator {
	private static BundleContext context;
	
	private ServiceRegistration<TrafficMonitor> registration;
	
	private OrgControllerTracker orgControllerTracker;
	private CameraTracker cameraTracker;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		TrafficMonitorImpl trafficMonitorImpl = new TrafficMonitorImpl();
		
		cameraTracker = new CameraTracker(context, trafficMonitorImpl);
		cameraTracker.open();
		
		orgControllerTracker = new OrgControllerTracker(context, trafficMonitorImpl);
		orgControllerTracker.open();
		
		registration = context.registerService(
				TrafficMonitor.class,
				trafficMonitorImpl,
				new Hashtable<String, Object>()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
		registration = null;
		
		orgControllerTracker.close();
		orgControllerTracker = null;
		
		cameraTracker.close();
		cameraTracker = null;
		
		Activator.context = null;
	}
}
