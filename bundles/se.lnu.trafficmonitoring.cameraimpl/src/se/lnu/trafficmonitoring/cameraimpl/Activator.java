package se.lnu.trafficmonitoring.cameraimpl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.camera.Camera;
import se.lnu.trafficmonitoring.communicator.AddressMapperTracker;
import se.lnu.trafficmonitoring.communicator.CommunicatorTracker;
import se.lnu.trafficmonitoring.communicator.MessageFactoryTracker;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.orgcontroller.OrgControllerTracker;
import se.lnu.trafficmonitoring.position.PositionTracker;
import se.lnu.trafficmonitoring.trafficmonitor.TrafficMonitorTracker;

public class Activator implements BundleActivator {
	private static BundleContext context;
	private static MessageConsumer messageConsumer;
	
	private CommunicatorTracker mcCommunicatorTracker;
	
	private ServiceRegistration<Camera> registration;
	private CommunicatorTracker cameraCommunicatorTracker;
	private MessageFactoryTracker cameraMessageFactoryTracker;
	
	private CameraReporter cameraReporter;
	private AddressMapperTracker addressMapperTracker;
	private TrafficMonitorTracker trafficMonitorTracker;
	private OrgControllerTracker orgControllerTracker;
	private CommunicatorTracker reporterCommunicatorTracker;
	private MessageFactoryTracker reporterMessageFactoryTracker;
	private PositionTracker positionTracker;
	
	static BundleContext getContext() {
		return context;
	}
	
	static MessageConsumer getMessageConsumer() {
		return messageConsumer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		messageConsumer = new MessageConsumer(TMConstants.CAMERA_BID);
		mcCommunicatorTracker = new CommunicatorTracker(bundleContext, messageConsumer);
		mcCommunicatorTracker.open();
		
		CameraImpl camera = new CameraImpl(messageConsumer);
		cameraCommunicatorTracker = new CommunicatorTracker(bundleContext, camera);
		cameraCommunicatorTracker.open();
		cameraMessageFactoryTracker = new MessageFactoryTracker(bundleContext, camera);
		cameraMessageFactoryTracker.open();
		
		registration = context.registerService(
				Camera.class,
				camera,
				new Hashtable<String, Object>()
		);
		
		cameraReporter = new CameraReporter(messageConsumer, camera);
		trafficMonitorTracker = new TrafficMonitorTracker(bundleContext, cameraReporter);
		trafficMonitorTracker.open();
		addressMapperTracker = new AddressMapperTracker(bundleContext, cameraReporter);
		addressMapperTracker.open();
		orgControllerTracker = new OrgControllerTracker(bundleContext, cameraReporter);
		orgControllerTracker.open();
		reporterCommunicatorTracker = new CommunicatorTracker(bundleContext, cameraReporter);
		reporterCommunicatorTracker.open();
		reporterMessageFactoryTracker = new MessageFactoryTracker(bundleContext, cameraReporter);
		reporterMessageFactoryTracker.open();
		positionTracker = new PositionTracker(bundleContext, cameraReporter);
		positionTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		cameraReporter.destroy();
		cameraReporter = null;
		
		positionTracker.close();
		positionTracker = null;
		
		reporterMessageFactoryTracker.close();
		reporterMessageFactoryTracker = null;
		
		reporterCommunicatorTracker.close();
		reporterCommunicatorTracker = null;
		
		orgControllerTracker.close();
		orgControllerTracker = null;
		
		addressMapperTracker.close();
		addressMapperTracker = null;
		
		trafficMonitorTracker.close();
		trafficMonitorTracker = null;
		
		messageConsumer.destroy();
		messageConsumer = null;
		
		mcCommunicatorTracker.close();
		mcCommunicatorTracker = null;
		
		registration.unregister();
		registration = null;
		
		cameraMessageFactoryTracker.close();
		cameraMessageFactoryTracker = null;
		
		cameraCommunicatorTracker.close();
		cameraCommunicatorTracker = null;
		
		Activator.context = null;
	}
}
