package net.hklight.nanodegree.spotifystreamer;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


public class TopTenTracksActivity extends ActionBarActivity {
    private final String LOG_TAG = TopTenTracksActivity.class.getSimpleName();

    private TrackAdapter trackAdapter;
    private ListView topTenTracksListView;
    private HashMap<String, String> artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);
        topTenTracksListView = (ListView) findViewById(R.id.listview_toptentracks);
        topTenTracksListView.setEmptyView(findViewById(android.R.id.empty));


        artist = (HashMap<String, String>) getIntent().getSerializableExtra("selectedArtist");

        // set the title
        getSupportActionBar().setTitle(R.string.title_activity_top_ten_tracks);
        getSupportActionBar().setSubtitle(artist.get("artistName"));

        // start async task to get the top 10 track
        TopTenTracksAsyncTask topTenTracksAsyncTask = new TopTenTracksAsyncTask();
        topTenTracksAsyncTask.execute(artist.get("artistId"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_ten_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class TopTenTracksAsyncTask extends AsyncTask<String,Void, List<Track>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<Track> doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }

            // incoke the spotify api
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            HashMap<String, Object> spotifyParams = new HashMap<String, Object>();
            spotifyParams.put("country", "HK");


            Tracks tracks = spotify.getArtistTopTrack(params[0], spotifyParams);

            return tracks.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {

            if (tracks == null) {
                // Error occure
                Toast.makeText(TopTenTracksActivity.this, R.string.errorOccurs, Toast.LENGTH_SHORT).show();
                return;
            }

            // convert it into hashtable
            ArrayList<Hashtable<String, String>> dataset = new ArrayList<Hashtable<String, String>>();

            if (tracks.size() == 0) {
                // let the user know
                Toast.makeText(TopTenTracksActivity.this, R.string.trackNotFound, Toast.LENGTH_LONG).show();
            }


            for (Track track : tracks) {
                String trackName = track.name;
                String albumName = track.album.name;
                String smallAlbumImage = "";
                String largeAlbumImage = "";
                if (track.album.images.size() > 0) {
                    for (int i=0; i < track.album.images.size(); i++) {
                        Image image = track.album.images.get(i);

                        if (image.width == 640) {
                            // This is large
                            largeAlbumImage = image.url;
                        } else if (image.width == 200) {
                            smallAlbumImage = image.url;
                        }
                    }

                    // if image is not in 640 or 200, pick first one...
                    smallAlbumImage = track.album.images.get(0).url;
                    largeAlbumImage = track.album.images.get(0).url;
                }

                // and then save them
                Hashtable<String, String> eachTrack = new Hashtable<String, String>();
                eachTrack.put("trackName", trackName);
                eachTrack.put("albumName", albumName);
                eachTrack.put("smallAlbumImage", smallAlbumImage);
                eachTrack.put("previewUrl", track.preview_url);


                dataset.add(eachTrack);
            }


            trackAdapter = new TrackAdapter(TopTenTracksActivity.this, dataset);
            topTenTracksListView.setAdapter(trackAdapter);
        }
    }

}
