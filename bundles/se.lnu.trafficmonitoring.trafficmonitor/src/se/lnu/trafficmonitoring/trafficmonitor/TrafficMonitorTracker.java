package se.lnu.trafficmonitoring.trafficmonitor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class TrafficMonitorTracker extends ServiceTracker<TrafficMonitor, TrafficMonitor> {
	TrafficMonitorUser user;
	
	public TrafficMonitorTracker(BundleContext context, TrafficMonitorUser user) {
		super(context, TrafficMonitor.class, null);
		this.user = user;
	}
	
	@Override
	public TrafficMonitor addingService(ServiceReference<TrafficMonitor> reference) {
		user.setTrafficMonitor(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<TrafficMonitor> reference, TrafficMonitor service) {
		user.setTrafficMonitor(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<TrafficMonitor> reference, TrafficMonitor service) {
		user.setTrafficMonitor(null);
		super.removedService(reference, service);
	}
}
