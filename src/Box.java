import gframe.engine.Point3D;
import gframe.engine.Vector3D;

public class Box implements Surface {

	private static final Vector3D FRONT_NORMAL = new Vector3D(0, 0, -1);
	
	private static final Vector3D BACK_NORMAL = new Vector3D(0, 0, 1);
	
	private static final Vector3D LEFT_NORMAL = new Vector3D(-1, 0, 0);
	
	private static final Vector3D RIGHT_NORMAL = new Vector3D(1, 0, 0);
	
	private static final Vector3D UP_NORMAL = new Vector3D(0, 1, 0);
	
	private static final Vector3D DOWN_NORMAL = new Vector3D(0, -1, 0);

	
	private Point3D origin;

	private float dX;
	private float dY;
	private float dZ;

	public Box(Point3D origin, float w, float h, float d) {
		this.origin = origin;
		this.dX = w / 2;
		this.dY = h / 2;
		this.dZ = d / 2;
	}

	@Override
	public float getSurfaceDistance(Point3D point) {
		float dist_x = Math.max(Math.abs(origin.x - point.x) - dX, 0);
		float dist_y = Math.max(Math.abs(origin.y - point.y) - dY, 0);
		float dist_z = Math.max(Math.abs(origin.z - point.z) - dZ, 0);
		return (float) Math.sqrt((dist_x * dist_x) + (dist_y * dist_y) + ( dist_z * dist_z));
	}

	@Override
	public Vector3D getSurfaceNormal(Point3D pointOnSurface) {
		if (pointOnSurface.z <= origin.z - dZ) {
			return FRONT_NORMAL;
		}
		if (pointOnSurface.x <= origin.x - dX) {
			return LEFT_NORMAL;
		}
		if (pointOnSurface.x >= origin.x + dX) {
			return RIGHT_NORMAL;
		}
		if (pointOnSurface.y >= origin.y + dY) {
			return UP_NORMAL;
		}
		if (pointOnSurface.y <= origin.y - dY) {
			return DOWN_NORMAL;
		}
		
		return BACK_NORMAL;		
	}

}
