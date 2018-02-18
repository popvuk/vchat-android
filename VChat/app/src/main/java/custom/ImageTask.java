package custom;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;

import android.util.TypedValue;

import roundedimageview.CircleImageView;


public class ImageTask extends AsyncTask<Void,Void,Bitmap>{

	private String slika;
	private int widthImg;
	private int heightImg;
	private WeakReference<CircleImageView> circleImg ;
	static Context ctx;

	public ImageTask(Context con, String slika, CircleImageView ci, int width, int height)
	{
		this.circleImg = new WeakReference<CircleImageView>(ci);
		this.ctx = con;
		this.slika = slika;
		this.widthImg = width;
		this.heightImg = height;
	}
	
	@Override
	protected Bitmap doInBackground(Void... params) {
		Bitmap bmp=null;
				    
		final Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(slika, options);
		options.inSampleSize = calculateInSampleSize(options, this.widthImg, this.heightImg);
				    
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		bmp = BitmapFactory.decodeFile(slika, options);

		return bmp;
	}
	
	public static int calculateInSampleSize(Options options, int widthDp, int heightDp) 
	{
    
		int height = options.outHeight;
        int width = options.outWidth;
        Resources r = ctx.getResources();
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, r.getDisplayMetrics());
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, r.getDisplayMetrics());
        
        int inSampleSize = 1;

        if (height > heightPx || width > widthPx)
        {
   
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
        
            while ((halfHeight / inSampleSize) > heightPx && (halfWidth / inSampleSize) > widthPx) 
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

	@Override
	protected void onPostExecute(Bitmap result) {
		final CircleImageView civ = circleImg.get();
		
		if(civ != null)
		{
			civ.setImageBitmap(result);
		}
	}
}
