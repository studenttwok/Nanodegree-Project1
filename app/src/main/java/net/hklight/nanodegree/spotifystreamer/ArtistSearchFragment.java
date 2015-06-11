package net.hklight.nanodegree.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistSearchFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    // view
    private boolean isSearching = false;
    //private TextView artistNameEditText;
    private ListView searchResultListView;
    // adapter
    private ArtistAdapter artistAdapter;

    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artistsearch, menu);

        View v = (View) menu.findItem(R.id.action_search).getActionView();
        EditText artistNameEditText = (EditText) v.findViewById(R.id.edittext_artistName);
        artistNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !isSearching) {

                    String keyword = v.getText().toString().trim();

                    if (keyword.length() == 0) {
                        // Toast user..
                        Toast.makeText(getActivity(), R.string.toast_enterKeywordPlease, Toast.LENGTH_SHORT).show();
                    } else {
                        // start search
                        ArtistSearchAsyncTask asat = new ArtistSearchAsyncTask();
                        asat.execute(keyword);
                    }

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_artistsearch, container, false);

        //artistNameEditText = (TextView) rootView.findViewById(R.id.edittext_artistName);
        // get the lsit view
        searchResultListView = (ListView) rootView.findViewById(R.id.listview_searchResult);
        searchResultListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        searchResultListView.setOnItemClickListener(this);

        rootView.findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
        // create asyncTask
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Hashtable<String, String> selectedArtist = artistAdapter.getItem(position);

        Intent detailIntent = new Intent(getActivity(), TopTenTracksActivity.class);
        detailIntent.putExtra("selectedArtist", selectedArtist);
        startActivity(detailIntent);
    }

    public class ArtistSearchAsyncTask extends AsyncTask<String,Void,List<Artist>> {

        @Override
        protected void onPreExecute() {
            isSearching = true;
        }

        @Override
        protected List<Artist> doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }

            // incoke the spotify api
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager ap = spotify.searchArtists(params[0]);

            return ap.artists.items;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            isSearching = false;
            if (artists == null) {
                // Error occure
                Toast.makeText(getActivity(), R.string.errorOccurs, Toast.LENGTH_SHORT).show();
                return;
            }

            if (artists.size() == 0) {
                // let the user know
                Toast.makeText(getActivity(), R.string.artistNotFound, Toast.LENGTH_LONG).show();
            }

            // convert it into hashtable
            ArrayList<Hashtable<String, String>> dataset = new ArrayList<Hashtable<String, String>>();

            for (Artist artist : artists) {
                Hashtable<String, String> eachArtist = new Hashtable<String, String>();
                eachArtist.put("artistName", artist.name);
                String artistImage = "";
                if (artist.images.size() > 0) {
                    artistImage = artist.images.get(0).url;
                }
                eachArtist.put("artistImage", artistImage);
                eachArtist.put("artistId", artist.id);

                dataset.add(eachArtist);
            }

            artistAdapter = new ArtistAdapter(getActivity(), dataset);
            searchResultListView.setAdapter(artistAdapter);
        }
    }



}
