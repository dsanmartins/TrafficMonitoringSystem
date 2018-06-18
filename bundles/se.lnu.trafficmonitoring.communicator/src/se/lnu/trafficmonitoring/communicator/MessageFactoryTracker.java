package se.lnu.trafficmonitoring.communicator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class MessageFactoryTracker extends ServiceTracker<MessageFactory, MessageFactory> {
	MessageFactoryUser user;
	
	public MessageFactoryTracker(BundleContext context, MessageFactoryUser user) {
		super(context, MessageFactory.class, null);
		this.user = user;
	}
	
	@Override
	public MessageFactory addingService(ServiceReference<MessageFactory> reference) {
		user.setMessageFactory(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<MessageFactory> reference, MessageFactory service) {
		user.setMessageFactory(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<MessageFactory> reference, MessageFactory service) {
		user.setMessageFactory(null);
		super.removedService(reference, service);
	}
}
