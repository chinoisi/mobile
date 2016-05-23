package yesuaini.chinoisinteractif;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import yesuaini.chinoisinteractif.hsk.CharacterListActivity;
import yesuaini.chinoisinteractif.lazyimagedownload.LessonListActivity;
import yesuaini.chinoisinteractif.episodes.DictListActivity;
import yesuaini.chinoisinteractif.tabs.MissionActivity;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

public class HomeActivity extends AbstractNavigationActivity {

    private View wordListIcon = null;
    private View dictListIcon = null;
    private View learnIcon = null;
    private View practiseIcon = null;
    private View gameIcon = null;
    private View progressionIcon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.wordListIcon = findViewById(R.id.wordList);
        this.dictListIcon = findViewById(R.id.dictList);
        this.learnIcon = findViewById(R.id.learn);
        this.practiseIcon = findViewById(R.id.practise);
        this.gameIcon = findViewById(R.id.game);
        this.progressionIcon = findViewById(R.id.progression);

        this.wordListIcon.setOnClickListener(this.wordListClickListener);
        this.dictListIcon.setOnClickListener(this.dictListClickListener);
        this.learnIcon.setOnClickListener(this.learnClickListener);
        this.practiseIcon.setOnClickListener(this.practiseClickListener);
        this.gameIcon.setOnClickListener(this.gameClickListener);
        this.progressionIcon.setOnClickListener(this.progressionClickListener);
    }

    View.OnClickListener practiseClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent i = new Intent(ctx, NavigationActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        }
    };

    View.OnClickListener learnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent i = new Intent(ctx, NavigationActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    };

    View.OnClickListener wordListClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent intent = new Intent(ctx,CharacterListActivity.class);
            intent.putExtra(SELECTED_WORD_LIST_EXTRA, 1);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        }
    };

    View.OnClickListener dictListClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent i = new Intent(ctx, MissionActivity.class);
                        i.putExtra(SELECTED_WORD_LIST_EXTRA, 1);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    };


    View.OnClickListener progressionClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent i = new Intent(ctx, LessonListActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    };

    View.OnClickListener gameClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Context ctx = HomeActivity.this;
            Intent i = new Intent(ctx, DictListActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    };



    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.activity_home;
    }


}
