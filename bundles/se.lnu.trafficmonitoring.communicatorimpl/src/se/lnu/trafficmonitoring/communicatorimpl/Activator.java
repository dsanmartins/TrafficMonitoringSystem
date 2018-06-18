package se.lnu.trafficmonitoring.communicatorimpl;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Random;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.lnu.trafficmonitoring.communicator.AddressMapper;
import se.lnu.trafficmonitoring.communicator.Communicator;
import se.lnu.trafficmonitoring.communicator.MessageFactory;
import se.lnu.trafficmonitoring.communicatorutil.MessageConsumer;
import se.lnu.trafficmonitoring.data.TMConstants;

public class Activator implements BundleActivator {
	private ServiceRegistration<Communicator> communicatorRegistration;
	private ServiceRegistration<MessageFactory> messageFactoryRegistration;
	private ServiceRegistration<AddressMapper> addressMapperRegistration;
	
	private CommunicatorImpl communicator;
	
	private static MessageConsumer messageConsumer;
	private static BundleContext context;

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
		
		String host = System.getProperty("se.lnu.trafficmonitoring.communicator.host");
		
		if (host == null) {
			if (TMConstants.DEFAULT_HOST != null) {
				host = TMConstants.DEFAULT_HOST;
			} else {
				host = "localhost";
			}
		}
		
		int port;
		
		try {
			port = Integer.parseInt(System.getProperty("se.lnu.trafficmonitoring.communicator.port"));
		} catch (Exception e) {
			if (TMConstants.DEFAULT_PORT != null) {
				port = TMConstants.DEFAULT_PORT;
			} else {
				port = new Random().nextInt(64512) + 1024;
			}
		}
		
		messageConsumer = new MessageConsumer(TMConstants.COMMUNICATOR_BID);
		AddressMapper addressMapper = new AddressMapperImpl(messageConsumer);
		communicator = new CommunicatorImpl(port, addressMapper);
		messageConsumer.setCommunicator(communicator);
		
		System.out.println("se.lnu.trafficmonitoring.communicator.host=" + host);
		System.out.println("se.lnu.trafficmonitoring.communicator.port=" + port);
		addressMapper.setOwnAddress(new InetSocketAddress(host, port));
		
		addressMapperRegistration = context.registerService(
				AddressMapper.class,
				addressMapper,
				new Hashtable<String, Object>()
		);
		
		communicatorRegistration = context.registerService(
				Communicator.class,
				communicator,
				new Hashtable<String, Object>()
		);
		
		messageFactoryRegistration = context.registerService(
				MessageFactory.class,
				new MessageFactoryImpl(),
				new Hashtable<String, Object>()
		);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		messageConsumer.destroy();
		messageConsumer = null;
		
		messageFactoryRegistration.unregister();
		messageFactoryRegistration = null;
		
		communicatorRegistration.unregister();
		communicatorRegistration = null;
		
		addressMapperRegistration.unregister();
		addressMapperRegistration = null;
		
		communicator.destroy();
		communicator = null;
		
		Activator.context = null;
	}

}
