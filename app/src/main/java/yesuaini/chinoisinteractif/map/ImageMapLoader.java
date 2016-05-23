package yesuaini.chinoisinteractif.map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.lazyimagedownload.FileCache;
import yesuaini.chinoisinteractif.lazyimagedownload.MemoryCache;
import yesuaini.chinoisinteractif.lazyimagedownload.Utils;

public class ImageMapLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;

    private Map<String, String> episodesMap = Collections.synchronizedMap(new WeakHashMap<String, String>());
    ExecutorService executorService;
        Paint idPaint = new Paint();Resources resources ;
    Handler handler = new Handler();

    public ImageMapLoader(Context context){
        fileCache = new FileCache(context);
        resources = context.getResources();

        executorService=Executors.newFixedThreadPool(5);
        idPaint.setTextSize(30);
        idPaint.setTypeface(Typeface.SERIF);
        idPaint.setTextAlign(Paint.Align.CENTER);
        idPaint.setAntiAlias(true);
        idPaint.setColor(Color.WHITE);
    }
    
    final int stub_id= R.drawable.stub;
    
    public void displayLevelImage(Canvas canvas, float x, float y, float mResizeFactorX, float mResizeFactorY, String url, String name)
    {
        episodesMap.put(name, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap!=null){
            drawLevelImage(canvas, x, y, mResizeFactorX, mResizeFactorY, name, bitmap, true);
        }
        else
        {
            queuePhoto(canvas, url, name, x, y, mResizeFactorX, mResizeFactorY, true);
            drawLevelImage(canvas, x, y, mResizeFactorX, mResizeFactorY, name, BitmapFactory.decodeResource(resources, stub_id, null),true);
        }
    }

    public void displayMap(Canvas canvas, float x, float y, float mResizeFactorX, float mResizeFactorY, String url, String name)
    {
        episodesMap.put(name, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap!=null){
            drawLevelImage(canvas, x, y, mResizeFactorX, mResizeFactorY, name, bitmap, false);
        }
        else
        {
            queuePhoto(canvas, url, name, x, y, mResizeFactorX, mResizeFactorY, false);
            drawLevelImage(canvas, x, y, mResizeFactorX, mResizeFactorY, name, BitmapFactory.decodeResource(resources, stub_id, null),false);
        }
    }




    private void drawLevelImage(Canvas canvas, float x, float y, float mResizeFactorX, float mResizeFactorY, String name, Bitmap bitmap, boolean drawText) {
        canvas.drawBitmap(bitmap, x * mResizeFactorX, y * mResizeFactorY, null);
        if (drawText) {
            canvas.drawText(name, Math.round((x + 60) * mResizeFactorX), Math.round((y + 60) * mResizeFactorY), idPaint);
        }
    }

    private void queuePhoto(Canvas canvas, String url, String name, float x, float y, float mResizeFactorX, float mResizeFactorY, boolean drawText)
    {
        PhotoToLoad p = new PhotoToLoad(canvas, url, name, x, y, mResizeFactorX, mResizeFactorY, drawText);
        executorService.submit(new PhotosLoader(p));
    }
    

    private class PhotoToLoad
    {
        public Canvas canvas;
        public String url;
        public String name;
        public float x;
        public float y;
        float mResizeFactorX; float mResizeFactorY;
        boolean drawText;

        public PhotoToLoad(Canvas canvas, String url, String name, float x, float y, float mResizeFactorX, float mResizeFactorY, boolean drawText) {
            this.canvas = canvas;
            this.url=url;
            this.name=name;
            this.x = x; this.y=y; this.mResizeFactorX = mResizeFactorX; this.mResizeFactorY = mResizeFactorY;
            this.drawText =drawText;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;


        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            try{
                if(imageViewReused(photoToLoad))
                    return;

                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                
                if(imageViewReused(photoToLoad))
                    return;

                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
                
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        try {
        	Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            conn.disconnect();

            bitmap = decodeFile(f);
            
            return bitmap;
            
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }

    private Bitmap decodeFile(File f){
    	try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1=new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();

            final int REQUIRED_SIZE=85;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with current scale values
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            FileInputStream stream2=new FileInputStream(f);
            Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
            
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
    	
        String tag=episodesMap.get(photoToLoad.name);
        return tag == null || !tag.equals(photoToLoad.url);
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p)
        {   bitmap=b;photoToLoad=p;
        }
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            
            if(bitmap!=null) {
                drawLevelImage(photoToLoad.canvas,photoToLoad.x, photoToLoad.y, photoToLoad.mResizeFactorX, photoToLoad.mResizeFactorY, photoToLoad.name, bitmap, photoToLoad.drawText);
            } else
                drawLevelImage(photoToLoad.canvas, photoToLoad.x, photoToLoad.y, photoToLoad.mResizeFactorX, photoToLoad.mResizeFactorY, photoToLoad.name, BitmapFactory.decodeResource(resources, stub_id, null),  photoToLoad.drawText);
        }
    }

    public void clearCache() {
    	//Clear cache directory downloaded images and stored data in maps
        memoryCache.clear();
        fileCache.clear();
    }

}
