package com.example.shishir.mymusicplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shishir.mymusicplayer.Adapter.SongAdapterForSearchList;

import java.util.ArrayList;

public class SearchSongActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView searchView;
    ArrayList<Song> songList;
    ListView songListViewForSearch;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_song);

        initSearchView();
    }

    private void initSearchView() {
        intent = getIntent();
        songList = new AllSong().getSongList(this);
        searchView = (SearchView) findViewById(R.id.searchSong);
        songListViewForSearch = (ListView) findViewById(R.id.songlistSearch);
        songListViewForSearch.setAdapter(new SongAdapterForSearchList(this, songList));

        songListViewForSearch.setTextFilterEnabled(true);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search Here");

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            songListViewForSearch.clearTextFilter();
        } else {
            songListViewForSearch.setFilterText(newText.toLowerCase());
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
