package se.lnu.trafficmonitoring.communicator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AddressMapperTracker extends ServiceTracker<AddressMapper, AddressMapper> {
	AddressMapperUser user;
	
	public AddressMapperTracker(BundleContext context, AddressMapperUser user) {
		super(context, AddressMapper.class, null);
		this.user = user;
	}
	
	@Override
	public AddressMapper addingService(ServiceReference<AddressMapper> reference) {
		user.setAddressMapper(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<AddressMapper> reference, AddressMapper service) {
		user.setAddressMapper(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<AddressMapper> reference, AddressMapper service) {
		user.setAddressMapper(null);
		super.removedService(reference, service);
	}
}
