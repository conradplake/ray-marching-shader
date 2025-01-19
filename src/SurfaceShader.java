import java.awt.Color;
import java.util.List;

import gframe.engine.AbstractShader;
import gframe.engine.Lightsource;
import gframe.engine.Point3D;
import gframe.engine.RenderFace;
import gframe.engine.Vector3D;

/*
 * A ray marching shader to visualize surfaces using signed distance functions.
 */
public class SurfaceShader extends AbstractShader {

	private static final int MAX_MARCHING_STEPS = 200;
	private static final float MIN_DISTANCE = 0.2f;
	private static final float MAX_DISTANCE = 2000f;
	private static final int DEFAULT_COLOR = new Color(200, 200, 255).getRGB();

	private List<Surface> surfaces;
	

	public SurfaceShader(Lightsource lightsource, List<Surface> surfaces) {
		super(lightsource);
		this.setAddSpecularity(false);
		this.surfaces = surfaces;		
	}
	
	@Override
	public boolean isPerPixelShader() {
		return true;
	}

	/**
	 * Given renderFace is assumed to be a "screen" and world_x/y/z coordinates
	 * are positions on the screen through which we shoot rays
	 */
	@Override
	public int shade(RenderFace renderFace, float world_x, float world_y, float world_z, float normal_x, float normal_y, float normal_z, 
			float texel_u, float texel_v, int screen_x, int screen_y) {
		Point3D rayMarchStartPosition = renderFace.getCameraPosition().copy();
		Vector3D rayDirection = new Vector3D(world_x - rayMarchStartPosition.x, world_y - rayMarchStartPosition.y, world_z - rayMarchStartPosition.z);
		rayDirection.normalize();
		return rayMarchColor(surfaces, rayMarchStartPosition, rayDirection, DEFAULT_COLOR);
	}

	/**
	 * Returns a color value from the nearest surface hit along the ray or given
	 * default color if no surface was hit.
	 */
	int rayMarchColor(List<Surface> surfaces, Point3D startPoint, Vector3D direction, int defaultColor) {
		int color = defaultColor;
		Point3D currentRayMarchPosition = startPoint;
		float totalDistance = 0;
		for (int i = 0; i < MAX_MARCHING_STEPS; i++) {

			// march on
			Object[] distanceAndSurface = getMinDistanceAndSurface(currentRayMarchPosition, surfaces);
			float distance = (Float) distanceAndSurface[0];
			currentRayMarchPosition.move(direction.x * distance, direction.y * distance, direction.z * distance);
			totalDistance += distance;

			if (distance <= MIN_DISTANCE) {
				Surface surface = (Surface) distanceAndSurface[1];
				int surfaceColor = surface.getSurfaceColor(currentRayMarchPosition);				
				Vector3D surfaceNormal = surface.getSurfaceNormal(currentRayMarchPosition);					
				
				color = super.shade(surfaceColor, currentRayMarchPosition.x, currentRayMarchPosition.y,
						currentRayMarchPosition.z, surfaceNormal.x, surfaceNormal.y, surfaceNormal.z);
				
				// is current position inside shadow?
				Vector3D toLight = new Vector3D(currentRayMarchPosition, getLightsource());
				float distanceToLight = toLight.length();
				toLight.normalize();
				
				// ray march from here to light source; if distance traveled is less than distance to light, something was hit in between, ie. this position is in shadow
				float shadowFacor;
				if(toLight.dotProduct(surfaceNormal) < 0) {
					shadowFacor = 0;
				} else {
					shadowFacor = rayMarchSoftShadow(surfaces, currentRayMarchPosition.copy(), toLight, distanceToLight);					
				}
				color = scaleColor(color, shadowFacor);
				break;
			} else if (totalDistance >= MAX_DISTANCE) {
				break;
			}
		}
		return color;
	}
	
	float rayMarchSoftShadow(List<Surface> surfaces, Point3D startPoint, Vector3D direction, float maxDistance) {
		float shadowFactor = 1f; // 1 - no shadow; 0 - full shadow
		
		// move a little away from the surface; otherwise the ray march will terminate directly since it starts so close
		startPoint.move(direction.x * MIN_DISTANCE * 5, direction.y * MIN_DISTANCE * 5, direction.z * MIN_DISTANCE * 5);
		
		float distanceTraveled = 0;
		for (int i = 0; i < MAX_MARCHING_STEPS; i++) {
			float distance = getMinDistance(startPoint, surfaces);			
			startPoint.move(direction.x * distance, direction.y * distance, direction.z * distance);
			distanceTraveled += distance;
			if (distance < MIN_DISTANCE) {
				shadowFactor = 0f;
				break;
			}
			
			// see: https://iquilezles.org/articles/rmshadows/
			shadowFactor = Math.min(shadowFactor, 32 * distance / distanceTraveled);
			
			if(distanceTraveled >= maxDistance) {
				break;
			}
		}
		
		return shadowFactor;
	}

	float rayMarchDistance(List<Surface> surfaces, Point3D startPoint, Vector3D direction, float maxDistance) {
		float distanceTraveled = 0;
		for (int i = 0; i < MAX_MARCHING_STEPS; i++) {
			float distance = (Float)getMinDistanceAndSurface(startPoint, surfaces)[0];			
			startPoint.move(direction.x * distance, direction.y * distance, direction.z * distance);
			distanceTraveled += distance;
			if (distance < MIN_DISTANCE || distanceTraveled > maxDistance) {
				break;
			}
		}
		return distanceTraveled;
	}
	
	/**
	 * Give factor between 0 and 1. 
	 * Value 1: no change; value 0: black
	 * */
	static int scaleColor(int color, float factor) {		
		int alpha = ((color >> 24) & 0xff);
		int red = ((color >> 16) & 0xff);
		int green = ((color >> 8) & 0xff);
		int blue = ((color >> 0) & 0xff);
		
		int newRed = (int) (red * factor);
		int newGreen = (int) (green * factor);
		int newBlue = (int) (blue * factor);
		return ((alpha & 0xFF) << 24) | ((newRed & 0xFF) << 16) | ((newGreen & 0xFF) << 8) | ((newBlue & 0xFF) << 0);		
	}

	static Object[] getMinDistanceAndSurface(Point3D point, List<Surface> surfaces) {
		float distance = MAX_DISTANCE;
		Surface closestSurface = null;
		for (Surface surface : surfaces) {
			float d = surface.getSurfaceDistance(point);
			if (d < distance) {
				distance = d;
				closestSurface = surface;
			}
		}
		return new Object[] { distance, closestSurface };
	}
	
	static float getMinDistance(Point3D point, List<Surface> surfaces) {
		float distance = MAX_DISTANCE;		
		for (Surface surface : surfaces) {
			float d = surface.getSurfaceDistance(point);
			if (d < distance) {
				distance = d;
			}
		}
		return distance;
	}

}
