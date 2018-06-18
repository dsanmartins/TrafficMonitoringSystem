package se.lnu.trafficmonitoring.trafficmodelimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import se.lnu.trafficmonitoring.data.TMConstants;
import se.lnu.trafficmonitoring.trafficmodel.TrafficModel;
import se.lnu.trafficmonitoring.trafficmodel.TrafficModelException;

/**
 * Default TrafficModel implementation. Provides the Environment with generated
 * traffic degrees for a specific number of cameras and time steps.
 */
public class TrafficModelImpl implements TrafficModel {
	private List<Integer> trafficList = new ArrayList<Integer>();
	
	private int numberOfCameras;
	private int numberOfTimeSteps;
	
	private int currentTimeStep = 0;
	private int[] currentTimeStepData = new int[0];

	@Override
	public synchronized void setTotalCameras(int totalCameras) {
		numberOfCameras = totalCameras;
		
		if (TMConstants.TRAFFIC_RANDOM) {
			currentTimeStepData = Arrays.copyOf(currentTimeStepData, totalCameras + 1);
		} else {
			generateTraffic(numberOfCameras + numberOfTimeSteps - 1);
		}
	}

	@Override
	public synchronized void setTotalTimeSteps(int totalSteps) {
		numberOfTimeSteps = totalSteps;
		
		if (!TMConstants.TRAFFIC_RANDOM) {
			generateTraffic(numberOfCameras + numberOfTimeSteps - 1);
		}
	}

	@Override
	public synchronized void setLocalTraffic(int camera, int timestep, int traffic) {
		if (numberOfCameras <= camera || numberOfTimeSteps <= timestep) {
			throw new TrafficModelException("Invalid number of cameras or time slots");
		}
		
		if (TMConstants.TRAFFIC_RANDOM) {
			if (currentTimeStep == timestep) {
				currentTimeStepData[camera] = traffic;
			}
		} else {
			trafficList.set(camera + timestep, traffic);
		}
	}

	@Override
	public synchronized int getLocalTraffic(int camera, int timestep) {
		if (numberOfCameras <= camera && numberOfTimeSteps <= timestep) {
			throw new TrafficModelException("Invalid number of cameras or time slots");
		}
		
		if (TMConstants.TRAFFIC_RANDOM) {
			if (currentTimeStep != timestep) {
				currentTimeStep = timestep;
				Random random = ThreadLocalRandom.current();
				
				for (int i = 0; i < currentTimeStepData.length; i++) {
					currentTimeStepData[i] = random.nextInt(TMConstants.TRAFFIC_STOP); 
				}
			}
			
			return currentTimeStepData[camera];
		} else {
			return trafficList.get(camera + timestep);
		}
	}
	
	private synchronized void generateTraffic(int limit) {
		if (limit < trafficList.size()) {
			return;
		}
		
		Random random = ThreadLocalRandom.current();
		
		while (trafficList.size() <= limit) {
			trafficList.add(random.nextInt(TMConstants.TRAFFIC_STOP));
		}
	}
}
