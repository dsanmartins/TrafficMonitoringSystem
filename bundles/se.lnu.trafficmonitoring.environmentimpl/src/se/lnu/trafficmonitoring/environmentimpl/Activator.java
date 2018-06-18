package se.lnu.trafficmonitoring.environmentimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.communicator.AddressMapperTracker;
import se.lnu.trafficmonitoring.communicator.CommunicatorTracker;
import se.lnu.trafficmonitoring.communicator.MessageFactoryTracker;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.environment.Environment;
import se.lnu.trafficmonitoring.trafficmodel.TrafficModelTracker;

public class Activator implements BundleActivator {
	private static BundleContext context;
	private static MessageConsumer messageConsumer;
	
	private ServiceRegistration<Environment> registration;
	
	private AddressMapperTracker addressMapperTracker;
	private TrafficModelTracker trafficModelTracker;
	private CommunicatorTracker communicatorTracker;
	private CommunicatorTracker mcCommunicatorTracker;
	private MessageFactoryTracker messageFactoryTracker;
	
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
		
		messageConsumer = new MessageConsumer(TMConstants.ENVIRONMENT_BID);
		mcCommunicatorTracker = new CommunicatorTracker(context, messageConsumer);
		mcCommunicatorTracker.open();
		
		EnvironmentImpl environment = new EnvironmentImpl(messageConsumer);
		
		addressMapperTracker = new AddressMapperTracker(context, environment);
		addressMapperTracker.open();
		
		trafficModelTracker = new TrafficModelTracker(context, environment);
		trafficModelTracker.open();
		
		communicatorTracker = new CommunicatorTracker(context, environment);
		communicatorTracker.open();
		
		messageFactoryTracker = new MessageFactoryTracker(context, environment);
		messageFactoryTracker.open();
		
		registration = context.registerService(
				Environment.class,
				environment,
				new Hashtable<String, Object>()
		);
		
		new MainFrame(environment);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
		
		messageFactoryTracker.close();
		messageFactoryTracker = null;
		
		communicatorTracker.close();
		communicatorTracker = null;
		
		trafficModelTracker.close();
		trafficModelTracker = null;
		
		addressMapperTracker.close();
		addressMapperTracker = null;
		
		messageConsumer.destroy();
		messageConsumer = null;
		
		mcCommunicatorTracker.close();
		mcCommunicatorTracker = null;
		
		Activator.context = null;
	}
}
