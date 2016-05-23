package yesuaini.chinoisinteractif.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import yesuaini.chinoisinteractif.models.Mission;

public class MissionsByEpisodeMap extends ImageMap {

    private static final String TAG = "MissionsByEpisodeMap";
    private List<Mission> missions;


    public MissionsByEpisodeMap(Context context) {
        super(context);
    }

    public MissionsByEpisodeMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MissionsByEpisodeMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void loadData() {
        try {
            InputStream inputStream = getResources().getAssets().open("episodes/"+level+"/missions.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();
            missions = Arrays.asList(gson.fromJson(reader, Mission[].class));
            nbMissions = missions.size();
            inputStream.close();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to parse JSON due to: " + ex);
        }

        Integer initialX = 10;
        Integer initialY = 10 * nbMissions;
        Integer width = 70;
        Integer height = 50;
        Integer x = initialX;
        Integer y = initialY;

        for (int i = 1; i <= nbMissions; i++) {
            if (i % 15 == 0) {
                x = initialX + 300;
                y = y - 25;
            } else if (i % 10 == 0 || i % 11 == 0 || i % 13 == 0) {
                x = x + 125;
                y = y - 45;
            } else if (i % 4 == 0) {
                x = x + 55 - i * 4;
                y = y + 45;
            } else if (i % 6 == 0 || i % 7 == 0 || i % 5 == 0) {
                x = x - 100;
                y = y + 25;
            } else if (i % 3 == 0) {
                x = x + 125;
                y = y + 35;
            } else {
                x = x + (100 + i * 5);
                y = y + i * 7;
            }

            Area a = new RectArea(i, i + "", x, y, x + width, y + height);
            a.addValue("id", i + "");
            a.addValue("name", "Mission " + i);
            addArea(a);
        }
        setMapResource();
    }



    public void setMapResource() {
        final String imageKey = String.valueOf("mission"+level);
        BitmapHelper bitmapHelper = BitmapHelper.getInstance();
        Bitmap bitmap = bitmapHelper.getBitmapFromMemCache(imageKey);
        options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if (bitmap == null) {
            try {
                InputStream inputStream = getResources().getAssets().open("episodes/"+level+"/map.jpg");
                bitmap = BitmapFactory.decodeStream(inputStream,null,options);
                inputStream.close();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to parse JSON due to: " + ex);
            }
            bitmapHelper.addBitmapToMemoryCache(imageKey, bitmap);
        }
        setImageBitmap(Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()*nbMissions/15,bitmap.getHeight()*nbMissions/15, false));
    }


    protected String getLevelImage(int dataId) {
        return missions.get(dataId).getImage();
    }






}