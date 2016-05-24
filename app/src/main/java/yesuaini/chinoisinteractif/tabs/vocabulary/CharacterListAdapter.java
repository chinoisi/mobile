package yesuaini.chinoisinteractif.tabs.vocabulary;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import yesuaini.chinoisinteractif.R;

/**
 * Created by yesuaini on 29/04/16.
 */
public class CharacterListAdapter extends BaseAdapter {

    LayoutInflater inflater;
    public Context context;
    List<Character> characters;

    public CharacterListAdapter(Context context, List<Character> characters) {
                this.context = context;
        this.characters = characters;
        inflater.from(this.context);
    }

    @Override
    public int getCount() {
        return characters.size();
    }

    @Override
    public Object getItem(int position) {
        return characters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.hsk_list_item, parent, false);

       // R.id.charListView, R.id.pinyinListView, R.id.defListView, R.id.isLearnedCheckBox};
        TextView pinyinListView = (TextView) itemView.findViewById(R.id.pinyinListView);
        pinyinListView.setText(characters.get(position).getPinyin());


        TextView defListView = (TextView) itemView.findViewById(R.id.defListView);
        defListView.setText(characters.get(position).getDefinition());

        TextView charListView = (TextView) itemView.findViewById(R.id.charListView);
        charListView.setText(characters.get(position).getWord());

        return itemView;
    }
}
