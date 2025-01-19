import gframe.engine.Model3D;
import gframe.engine.Point3D;

public class Camera {

	private gframe.engine.Camera gframeCamera;
	private Model3D viewPlane;

	public Camera(gframe.engine.Camera gfCamera, Model3D viewPlane) {
		super();
		this.gframeCamera = gfCamera;
		this.viewPlane = viewPlane;		
	}

	public void reset(Point3D origin) {		
		viewPlane.reset();
		viewPlane.getOrigin().setCoordinates(origin.x, origin.y, origin.z);

		gframeCamera.reset();
		gframeCamera.getOrigin().setCoordinates(origin.x, origin.y, origin.z);
	}

	public void move(float units) {
		gframeCamera.move(units);
		viewPlane.move(units);
	}

	public void move(float dx, float dy, float dz) {
		gframeCamera.move(dx, dy, dz);
		viewPlane.move(dx, dy, dz);
	}

	public void rotate(float dx, float dy, float dz) {
		gframeCamera.rotate(dx, dy, dz);
		viewPlane.getMatrix().apply(gframeCamera.getMatrix().getArray());
	}

}
