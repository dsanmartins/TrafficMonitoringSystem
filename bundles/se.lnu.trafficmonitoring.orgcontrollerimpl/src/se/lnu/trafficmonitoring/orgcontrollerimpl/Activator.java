package se.lnu.trafficmonitoring.orgcontrollerimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.communicator.AddressMapperTracker;
import se.lnu.trafficmonitoring.communicator.CommunicatorTracker;
import se.lnu.trafficmonitoring.communicator.MessageFactoryTracker;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.orgcontroller.OrgController;
import se.lnu.trafficmonitoring.position.PositionTracker;

public class Activator implements BundleActivator {
	
	private static MessageConsumer messageConsumer;
	private CommunicatorTracker mcCommunicatorTracker;
	
	private ServiceRegistration<OrgController> registration;
	private AddressMapperTracker addressMapperTracker;
	private CommunicatorTracker communicatorTracker;
	private MessageFactoryTracker messageFactoryTracker;
	private PositionTracker positionTracker;

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
		
		messageConsumer = new MessageConsumer(TMConstants.ORG_CONTROLLER_BID);
		mcCommunicatorTracker = new CommunicatorTracker(bundleContext, messageConsumer);
		mcCommunicatorTracker.open();
		
		OrgControllerImpl orgController = new OrgControllerImpl(messageConsumer);
		addressMapperTracker = new AddressMapperTracker(bundleContext, orgController);
		addressMapperTracker.open();
		communicatorTracker = new CommunicatorTracker(bundleContext, orgController);
		communicatorTracker.open();
		messageFactoryTracker = new MessageFactoryTracker(bundleContext, orgController);
		messageFactoryTracker.open();
		positionTracker = new PositionTracker(bundleContext, orgController);
		positionTracker.open();
		
		registration = bundleContext.registerService(
				OrgController.class,
				orgController,
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
		
		positionTracker.close();
		positionTracker = null;
		
		messageFactoryTracker.close();
		messageFactoryTracker = null;
		
		communicatorTracker.close();
		communicatorTracker = null;
		
		addressMapperTracker.close();
		addressMapperTracker = null;
		
		messageConsumer.destroy();
		messageConsumer = null;
		
		mcCommunicatorTracker.close();
		mcCommunicatorTracker = null;
		
		Activator.context = null;
	}
}
