package io.indy.octodo;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    MainFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    Database mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (D)
            Log.d(TAG, "onCreate");


        mDatabase = new Database(this);
        List<String> lists = mDatabase.getListNames();

        mAdapter = new MainFragmentAdapter(getSupportFragmentManager(), lists);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
                
        // TODO: when to call mDatabase.close() ???
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "clicked " + item);

        switch (item.getItemId()) {
        case R.id.menu_settings:
            Toast.makeText(this, "menu settings", Toast.LENGTH_SHORT).show();
            break;

        case R.id.menu_about:
            startAboutActivity();
            break;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
