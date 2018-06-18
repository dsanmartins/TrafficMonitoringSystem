package se.lnu.trafficmonitoring.positionimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.communicator.AddressMapperTracker;
import se.lnu.trafficmonitoring.communicator.CommunicatorTracker;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.position.Position;

public class Activator implements BundleActivator {
	private ServiceRegistration<Position> registration;
	private AddressMapperTracker addressMapperTracker;
	private CommunicatorTracker communicatorTracker;
	
	private static MessageConsumer messageConsumer;
	private static BundleContext context;
	
	static MessageConsumer getMessageConsumer() {
		return messageConsumer;
	}

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		messageConsumer = new MessageConsumer(TMConstants.POSITION_BID);
		communicatorTracker = new CommunicatorTracker(bundleContext, messageConsumer);
		communicatorTracker.open();
		
		PositionImpl position = new PositionImpl(messageConsumer);
		addressMapperTracker = new AddressMapperTracker(bundleContext, position);
		addressMapperTracker.open();
		
		registration = context.registerService(
				Position.class,
				position,
				new Hashtable<String, Object>()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		
		registration.unregister();
		registration = null;
		
		addressMapperTracker.close();
		addressMapperTracker = null;
		
		messageConsumer.destroy();
		messageConsumer = null;
		
		communicatorTracker.close();
		communicatorTracker = null;
	}
}
