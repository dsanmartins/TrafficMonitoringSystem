package se.lnu.trafficmonitoring.environment;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class EnvironmentTracker extends ServiceTracker<Environment, Environment> {
	EnvironmentUser user;
	
	public EnvironmentTracker(BundleContext context, EnvironmentUser user) {
		super(context, Environment.class, null);
		this.user = user;
	}
	
	@Override
	public Environment addingService(ServiceReference<Environment> reference) {
		user.setEnvironment(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<Environment> reference, Environment service) {
		user.setEnvironment(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<Environment> reference, Environment service) {
		user.setEnvironment(null);
		super.removedService(reference, service);
	}
}
