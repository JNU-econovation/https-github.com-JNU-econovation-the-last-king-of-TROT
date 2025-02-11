package com.trot.trotwithtabs;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Comedy extends Fragment {

    MainActivity activity;
    Context context;

    int querySize;
    int singerPosition;
    String[] list_singer = {"뽕숭아학당","사랑의 콜센타","트롯 전국체전","트로트의 민족", "미스트롯1", "미스트롯2", "미스터트롯", "가요무대", "보이스트롯"};
    ArrayList<SingerInfoList> singerInfoList;
    ArrayList<SingerInfoList> singerInfoList2;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        querySize = 50;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.singer, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listViewSinger);
        SingerAdapter adapter = new SingerAdapter();
        for (int i = 0; i < list_singer.length; i++) {
            adapter.addItem(new SingerItem(list_singer[i]));
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YoutubeAsyncTask youtubeAsyncTask = new YoutubeAsyncTask();
                youtubeAsyncTask.execute();
                singerPosition= ReturnSingerPosition(position);
            }
        });

        return rootView;
    }

    public int ReturnSingerPosition(int position){
        return position;
    }

    private class YoutubeAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
                final JsonFactory JSON_FACTORY = new JacksonFactory();
                final long NUMBER_OF_VIDEOS_RETURNED = querySize;

                YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName("youtube-search-sample").build();

                String singerName = list_singer[singerPosition];
                YouTube.Search.List search = youtube.search().list("id,snippet");
                search.setKey(getString(R.string.youtube_api_key));
                search.setQ(singerName);
                search.setOrder("relevance");
                search.setType("video");
                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();
                if (searchResultList != null) {
                    prettyPrint(searchResultList.iterator(), singerName);
                }
            } catch (GoogleJsonResponseException e) {
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
                System.err.println("There was a service error 2: " + e.getLocalizedMessage() + " , " + e.toString());
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Fragment comedyDetailFragment = new ComedyDetail();
            singerInfoList = singerInfoList2;

            Bundle bundle=new Bundle();
            bundle.putParcelableArrayList("singerInfoList",(ArrayList<? extends Parcelable>) singerInfoList);
            comedyDetailFragment.setArguments(bundle);

            ((MainActivity)getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.container, comedyDetailFragment).addToBackStack(null).commit();
        }

        public void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {
            if (!iteratorSearchResults.hasNext()) {
                System.out.println(" There aren't any results for your query.");
            }

            singerInfoList2 = new ArrayList<>();

            while (iteratorSearchResults.hasNext()) {
                SearchResult singleVideo = iteratorSearchResults.next();
                ResourceId rId = singleVideo.getId();
                if (rId.getKind().equals("youtube#video")) {
                    Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");
                    singerInfoList2.add(new SingerInfoList(singleVideo.getSnippet().getTitle(), rId.getVideoId(), thumbnail.getUrl()));
                }
            }
        }
    }


    class SingerAdapter extends BaseAdapter {
        ArrayList<SingerItem> items = new ArrayList<SingerItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(SingerItem item){
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GenreItemView view = new GenreItemView(getContext());

            SingerItem item = items.get(position);
            view.setName(item.getName());
            return view;
        }

        private void getApplicationContext() {
        }
    }

}
