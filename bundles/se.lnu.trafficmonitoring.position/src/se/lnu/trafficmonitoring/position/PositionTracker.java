package se.lnu.trafficmonitoring.position;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class PositionTracker extends ServiceTracker<Position, Position> {
	PositionUser user;
	
	public PositionTracker(BundleContext context, PositionUser user) {
		super(context, Position.class, null);
		this.user = user;
	}
	
	@Override
	public Position addingService(ServiceReference<Position> reference) {
		user.setPosition(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<Position> reference, Position service) {
		user.setPosition(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<Position> reference, Position service) {
		user.setPosition(null);
		super.removedService(reference, service);
	}
}
