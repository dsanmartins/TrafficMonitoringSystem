package se.lnu.trafficmonitoring.orgcontroller;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;


public class OrgControllerTracker extends ServiceTracker<OrgController, OrgController> {
	OrgControllerUser user;
	
	public OrgControllerTracker(BundleContext context, OrgControllerUser user) {
		super(context, OrgController.class, null);
		this.user = user;
	}
	
	@Override
	public OrgController addingService(ServiceReference<OrgController> reference) {
		user.setOrgController(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<OrgController> reference, OrgController service) {
		user.setOrgController(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<OrgController> reference, OrgController service) {
		user.setOrgController(null);
		super.removedService(reference, service);
	}
}
