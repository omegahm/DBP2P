package dk.hotmovinglobster.battleships;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * A bitmap that is able to rotate 90, 180 and 270 degrees and cache the
 * rotated bitmaps
 * @author Jesper
 *
 */
public class RotatableBitmap {
	
	private final Bitmap original;
	private Bitmap rotated90;
	private Bitmap rotated180;
	private Bitmap rotated270;
	
	public RotatableBitmap(Bitmap original) {
		this.original = original;
	}
	
	public Bitmap getOriginal() {
		return original;
	}
	
	public Bitmap getRotated90() {
		if (rotated90 == null) {
			rotated90 = rotateOriginal(90);
		}
		
		return rotated90;
	}
	
	public Bitmap getRotated180() {
		if (rotated180 == null) {
			rotated180 = rotateOriginal(180);
		}
		
		return rotated180;
	}
	
	public Bitmap getRotated270() {
		if (rotated270 == null) {
			rotated270 = rotateOriginal(270);
		}
		
		return rotated270;
	}
	
	private Bitmap rotateOriginal(int degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
	}
	

}
