package yesuaini.chinoisinteractif.lazyimagedownload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.tabs.MissionActivity;
import yesuaini.chinoisinteractif.models.MissionSummary;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

public class LazyImageLoadAdapter extends BaseAdapter implements OnClickListener {
    private Activity activity;
    private List<MissionSummary> lessonSummaries;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyImageLoadAdapter(Activity a, List<MissionSummary> l) {
        activity = a;
        lessonSummaries =l;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return lessonSummaries.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder {
        public TextView text;
        public TextView text1;
        public TextView textWide;
        public ImageView image;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        if(convertView==null){
            vi = inflater.inflate(R.layout.listview_row, null);
            holder = new ViewHolder();
            holder.text = (TextView) vi.findViewById(R.id.text);
            holder.text1=(TextView)vi.findViewById(R.id.text1);
            holder.image=(ImageView)vi.findViewById(R.id.image);
            vi.setTag( holder );
        }
        else 
            holder=(ViewHolder)vi.getTag();
        
        holder.text.setText(lessonSummaries.get(position).getTitle());
        holder.text1.setText(lessonSummaries.get(position).getDescription());
        ImageView image = holder.image;
        imageLoader.DisplayImage(lessonSummaries.get(position).getImage(), image);

        vi.setOnClickListener(new OnItemClickListener(position));
        return vi;
    }

	@Override
	public void onClick(View arg0) {
	}

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements OnClickListener{           
        private int mPosition;
       OnItemClickListener(int position){
        	 mPosition = position;
        }
        
        @Override
        public void onClick(View arg0) {
      //  	LessonListActivity sct = (LessonListActivity)activity;
        //	sct.onItemClick(mPosition);
            Intent intent = new Intent(activity, MissionActivity.class);
            intent.putExtra(SELECTED_WORD_LIST_EXTRA, mPosition +1);
            activity.startActivity(intent);
            activity.finish();
        }               
    }

}