package se.lnu.trafficmonitoring.communicator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class CommunicatorTracker extends ServiceTracker<Communicator, Communicator> {
	CommunicatorUser user;
	
	public CommunicatorTracker(BundleContext context, CommunicatorUser user) {
		super(context, Communicator.class, null);
		this.user = user;
	}
	
	@Override
	public Communicator addingService(ServiceReference<Communicator> reference) {
		user.setCommunicator(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<Communicator> reference, Communicator service) {
		user.setCommunicator(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<Communicator> reference, Communicator service) {
		user.setCommunicator(null);
		super.removedService(reference, service);
	}
}
