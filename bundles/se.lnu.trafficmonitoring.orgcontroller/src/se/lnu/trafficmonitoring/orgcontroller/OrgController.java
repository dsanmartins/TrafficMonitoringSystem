package se.lnu.trafficmonitoring.orgcontroller;

import java.util.List;

public interface OrgController {

	/** Informs the controller a changing traffic condition */
	void setCongestedTraffic();

	/** Informs the controller a changing traffic condition */
	void setFreeflowTraffic();

	/**
	 * Each working camera is in one of three distinct roles: master of a single
	 * member organization, master of an organization with slaves, or slave in
	 * an organization.
	 */
	OrgRole getOrganizationalRole();

	/** Returns the ID of the master of the organization */
	int getMaster();

	/** Returns the list of IDs of slaves in case of master */
	List<Integer> getSlaves();
}
