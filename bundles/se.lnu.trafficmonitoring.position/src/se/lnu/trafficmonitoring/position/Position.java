package se.lnu.trafficmonitoring.position;

import java.util.List;

/**
 * Handles information about physical positions.
 */
public interface Position {
	
	/**
	 * @return The position of a specific Agent, or -1 on failure.
	 */
	public int getPosition(int cameraId);
	
	/**
	 * @return The position of this Agent, or -1 on failure.
	 */
	public int getOwnPosition();
	
	/**
	 * @return The ID of this Agent's left neighbor, or unknown on failure.
	 */
	public int getLeftCameraId();
	
	/**
	 * @return The ID of this Agent's left neighbor, or unknown on failure.
	 */
	public int getRightCameraId();
	
	/**
	 * Defines positions using a list.
	 */
	public void setPositions(List<Integer> positions);
}
