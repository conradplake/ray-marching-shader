import java.awt.Color;

import gframe.engine.Point3D;
import gframe.engine.Vector3D;

public interface Surface {
	
	/**
	 * Returns the minimum distance from the given point to this surface. 
	 * A negative result represents interior distance from the surface.
	 * */
	float getSurfaceDistance(Point3D point);
	
	/**
	 * Returns a normal vector for a given position on the surface.
	 * */
	Vector3D getSurfaceNormal(Point3D pointOnSurface);
	
	/**
	 * Returns a color as (a)rgb integer for a given position on the surface.
	 * */
	default int getSurfaceColor(Point3D pointOnSurface) {
		return Color.white.getRGB();
	}

}
