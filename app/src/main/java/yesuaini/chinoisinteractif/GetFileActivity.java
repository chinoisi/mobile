package yesuaini.chinoisinteractif;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import yesuaini.chinoisinteractif.utils.Common;

public class GetFileActivity extends AppCompatActivity {

    String url = "http://www.firstpost.com/wp-content/uploads/2013/09/01_Android-all-versions.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_file);

        findViewById(R.id.download_activity_btn_filesize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.getRemoteFileSize(GetFileActivity.this, url);
            }
        });

        findViewById(R.id.download_activity_btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.downloadRemoteFile(GetFileActivity.this, url, "rabbit.jpg");
            }
        });
    }
}
