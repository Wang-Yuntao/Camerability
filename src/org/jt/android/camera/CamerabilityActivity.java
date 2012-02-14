package org.jt.android.camera;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class CamerabilityActivity extends Activity implements Callback,
		PreviewCallback {
	// private SurfaceView preview;
	private Camera camera;
	private long lastFrameTime;

	private static final byte[] BUFFER = new byte[320 * 240 * 3 / 2];
	private TextView statusView;

	private int currentCameraId;
	private SurfaceView preview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.main);

		preview = (SurfaceView) findViewById(R.id.preview_surface);
		preview.getHolder().addCallback(this);

		statusView = (TextView) findViewById(R.id.status);

		onCameraChanged(CameraInfo.CAMERA_FACING_FRONT);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		camera.setPreviewCallbackWithBuffer(this);
		camera.addCallbackBuffer(BUFFER);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
	}

	@Override
	protected void onDestroy() {
		camera.release();
		super.onDestroy();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		long current = System.currentTimeMillis();
		long delay = 1;
		if (lastFrameTime != 0) {
			delay = current - lastFrameTime;
		}
		lastFrameTime = current;
		long fps = 1000 / delay;
		statusView.setText(getString(R.string.preview_status, fps));
		camera.addCallbackBuffer(data);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.front_camera_item) {
			setCurrentCamera(CameraInfo.CAMERA_FACING_FRONT);
		} else if (itemId == R.id.back_camera_item) {
			setCurrentCamera(CameraInfo.CAMERA_FACING_BACK);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setCurrentCamera(int id) {
		if (currentCameraId != id) {
			onCameraChanged(id);
		}
	}
	
	protected void onCameraChanged(int id) {
		if (preview.getVisibility() == View.VISIBLE) {
			preview.setVisibility(View.GONE);
		}
		if (camera != null) {
			camera.release();
		}
		
		currentCameraId = id;
		
		setProgressBarIndeterminateVisibility(true);
		new OpenCameraTask().execute();
	}
	
	class OpenCameraTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
			camera = Camera.open(currentCameraId);
			} catch (RuntimeException e) {
				return Boolean.FALSE;
			}
			Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(320, 240);
			camera.setParameters(parameters);
			return Boolean.TRUE;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			preview.setVisibility(View.VISIBLE);
			setProgressBarIndeterminateVisibility(false);
		}
	}
}