import gframe.engine.Point3D;
import gframe.engine.Vector3D;


/**
 * Plane in the x,z dimensions pointing upwards
 * */
public class Plane implements Surface {
	
	private static final Vector3D NORMAL = new Vector3D(0, 1, 0);
	
	
	private float level;
	
	public Plane(float level) {
		this.level = level;
	}

	@Override
	public float getSurfaceDistance(Point3D point) {
		return point.y - level;
	}

	@Override
	public Vector3D getSurfaceNormal(Point3D pointOnSurface) {
		return NORMAL;
	}

}
