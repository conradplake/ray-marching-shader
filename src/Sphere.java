import gframe.engine.Point3D;
import gframe.engine.Vector3D;

public class Sphere implements Surface {
	
	private Point3D origin;
	private float radius;
	
	public Sphere(Point3D origin, float radius) {
		this.origin = origin;
		this.radius = radius;		
	}
	
	public float getSurfaceDistance(Point3D point) {		
		return origin.distance(point) - radius;
	}
	
	public Vector3D getSurfaceNormal(Point3D pointOnSurface) {
		Vector3D normal = new Vector3D(pointOnSurface.x - origin.x, pointOnSurface.y - origin.y, pointOnSurface.z - origin.z);
		normal.normalize();
		return normal;
	}

}
