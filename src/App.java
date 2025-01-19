import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import gframe.app.DoubleBufferedFrame;
import gframe.engine.Engine3D;
import gframe.engine.ImageRaster;
import gframe.engine.Lightsource;
import gframe.engine.Model3D;
import gframe.engine.Point3D;
import gframe.engine.Shader;
import gframe.engine.camera.TripodCamera;

public class App extends DoubleBufferedFrame implements MouseMotionListener {

	public static int SCREENX = 640;
	public static int SCREENY = 480;
	public static boolean FULL_SCREEN_ENABLED = false;
	
	private static final float CAMERA_VIEWPLANE_DISTANCE = 200;
	private static final Point3D CAMERA_START_POSITION = new Point3D(0, 0, -CAMERA_VIEWPLANE_DISTANCE);

	
	public App() {
		super();
		frameBuffer = new ImageRaster(SCREENX, SCREENY);
		setBackground(Color.lightGray);
	}

	private void initEngine() {
		engine = new Engine3D(SCREENX, SCREENY);

		lightsource = new Lightsource(0, 0, 0, Color.white, Lightsource.MAX_INTENSITY);
		engine.setLightsource(lightsource);

		gframe.engine.Camera gframeCamera = new TripodCamera();
		Model3D viewPlane = buildViewPlane(SCREENX / 4, SCREENY / 4, CAMERA_VIEWPLANE_DISTANCE, new Point3D(), Color.white);
		camera = new Camera(gframeCamera, viewPlane);
		camera.reset(CAMERA_START_POSITION);
		engine.setCamera(gframeCamera);
		engine.register(viewPlane);

		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (FULL_SCREEN_ENABLED && device.isFullScreenSupported()) {
			DisplayMode newMode = new DisplayMode(SCREENX, SCREENY, 32, 60);
			this.setUndecorated(true);
			this.setResizable(false);
			device.setFullScreenWindow(this);
			device.setDisplayMode(newMode);
			this.setIgnoreRepaint(true);
		} else {
			setSize(SCREENX, SCREENY);
			setLocation(0, 0);
		}
	}

	private static Model3D buildViewPlane(float width, float height, float distanceFromCamera, Point3D origin, java.awt.Color col) {
		Model3D model = new Model3D(origin);
		model.addVertex(-width / 2, height / 2, distanceFromCamera);
		model.addVertex(width / 2, height / 2, distanceFromCamera);
		model.addVertex(width / 2, -height / 2, distanceFromCamera);
		model.addVertex(-width / 2, -height / 2, distanceFromCamera);
		model.stretchFace(1, 2, 3, 0, col);
		return model;
	}

	private void initWorld() {		
		camera.reset(CAMERA_START_POSITION);		

		List<Surface> surfaces = new ArrayList<>();
		surfaces.add(new Sphere(new Point3D(0, 50, 200), 50f));
		surfaces.add(new Plane(-100));
		surfaces.add(new Box(new Point3D(-40, 30, 130), 40, 40, 40));

		Shader shader = new SurfaceShader(lightsource, surfaces);
		engine.setDefaultShader(shader);

		camera.move(0, 50, -500);		
		lightsource.setCoordinates(0, 200, -100);
	}

	private void start() {
		initEngine();
		initWorld();

		setSize(SCREENX, SCREENY);
		setBackground(Color.YELLOW);
		setForeground(Color.black);
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		addMouseMotionListener(this);
		setLayout(null);
		setVisible(true);

		/** MAIN LOOP */
		while (true) {
			repaint();
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (e.isMetaDown()) {
			int distY = e.getY() - mouseY;
			camera.move(0, distY, 0);

			int distX = e.getX() - mouseX;
			camera.move(distX, 0, 0);
		} else {
			int distX = e.getX() - mouseX;
			int distY = e.getY() - mouseY;
			camera.rotate(distY / 3, -distX / 3, 0);
		}
		updateMousePointer(e);
	}

	public void mouseMoved(MouseEvent e) {
		updateMousePointer(e);
	}

	public void mousePressed(MouseEvent e) {
		updateMousePointer(e);
	}

	private void updateMousePointer(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	protected void processKeyEvent(KeyEvent event) {
		if (event.getID() == KeyEvent.KEY_PRESSED) {
			int keycode = event.getKeyCode();
			if (keycode == KeyEvent.VK_ESCAPE) {
				exit();
			} else {						
				
				if (keycode == KeyEvent.VK_S) {
					camera.move(-10);
				} else if (keycode == KeyEvent.VK_A) {
					camera.move(-5, 0, 0);
				} else if (keycode == KeyEvent.VK_D) {
					camera.move(5, 0, 0);
				} else if (keycode == KeyEvent.VK_W) {
					camera.move(10);
				} 
				
				else if (keycode == KeyEvent.VK_R) {
					initWorld();
				}				

				else if (keycode == KeyEvent.VK_LEFT) {
					lightsource.move(-5, 0, 0);
				} else if (keycode == KeyEvent.VK_RIGHT) {
					lightsource.move(5, 0, 0);
				} else if (keycode == KeyEvent.VK_UP) {
					lightsource.move(0, 5, 0);
				} else if (keycode == KeyEvent.VK_DOWN) {
					lightsource.move(0, -5, 0);
				} else if (keycode == KeyEvent.VK_PAGE_UP) {
					lightsource.move(0, 0, 5);
				} else if (keycode == KeyEvent.VK_PAGE_DOWN) {
					lightsource.move(0, 0, -5);
				}
			}
		}
		super.processKeyEvent(event);
	}

	public void paint(Graphics g) {

		long updateTime = System.currentTimeMillis();

		frameBuffer.clear();
		engine.drawScene(frameBuffer);
		g.drawImage(frameBuffer.createImage(), 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight(), null);

		updateTime = System.currentTimeMillis() - updateTime;
		if (updateTime < 16) { // 16ms ~ 60 FPS
			try {
				Thread.sleep(16 - updateTime);
				updateTime = 16;
			} catch (InterruptedException ie) {
			}
		}

		frameCounter++;
		if (frameCounter % 10 == 0) {
			System.out.println("FPS: " + (1000 / updateTime));
		}
	}

	private void exit() {
		System.exit(0);
	}

	public static void main(String[] args) {
		new App().start();
	}
	
	//------

	private int frameCounter;

	private Engine3D engine;
	private ImageRaster frameBuffer;
	private Camera camera;
	private Lightsource lightsource;

	private int mouseX;
	private int mouseY;
}