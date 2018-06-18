package se.lnu.trafficmonitoring.trafficmodel;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class TrafficModelTracker extends ServiceTracker<TrafficModel, TrafficModel> {
	TrafficModelUser user;
	
	public TrafficModelTracker(BundleContext context, TrafficModelUser user) {
		super(context, TrafficModel.class, null);
		this.user = user;
	}
	
	@Override
	public TrafficModel addingService(ServiceReference<TrafficModel> reference) {
		user.setTrafficModel(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<TrafficModel> reference, TrafficModel service) {
		user.setTrafficModel(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<TrafficModel> reference, TrafficModel service) {
		user.setTrafficModel(null);
		super.removedService(reference, service);
	}
}
