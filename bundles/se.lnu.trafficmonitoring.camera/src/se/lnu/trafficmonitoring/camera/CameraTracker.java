package se.lnu.trafficmonitoring.camera;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class CameraTracker extends ServiceTracker<Camera, Camera> {
	CameraUser user;
	
	public CameraTracker(BundleContext context, CameraUser user) {
		super(context, Camera.class, null);
		this.user = user;
	}
	
	@Override
	public Camera addingService(ServiceReference<Camera> reference) {
		user.setCamera(context.getService(reference));
		return super.addingService(reference);
	}
	
	@Override
	public void modifiedService(ServiceReference<Camera> reference, Camera service) {
		user.setCamera(context.getService(reference));
		super.modifiedService(reference, service);
	}
	
	@Override
	public void removedService(ServiceReference<Camera> reference, Camera service) {
		user.setCamera(null);
		super.removedService(reference, service);
	}
}
