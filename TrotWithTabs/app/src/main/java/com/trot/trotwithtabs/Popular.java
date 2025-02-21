package com.trot.trotwithtabs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nodeal.example.you_tube_recyclerview.YoutubeSinger;

public class Popular extends Fragment {
    MainActivity activity;
    Context context;

    DBOpenHelper helper;
    SQLiteDatabase db;

    ArrayList<SingerInfoList> list;
    ArrayList<SongJjimList> songJjimList;
    int i = 0;

    private static final String TAG = "singer";
    public boolean isCheck[] = new boolean[50];

    Fragment YoutubeSinger = new YoutubeSinger();
    int querySize;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        //메시지 송수신에 필요한 객체 초기화
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        querySize=50;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //XML, java 연결
        //XML이 메인에 직접 붙으면 true, 프래그먼트에 붙으면 false
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.singer, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listViewSinger);
        try {
            helper = new DBOpenHelper(this.getContext());
            db = helper.getWritableDatabase();

            if (getArguments() != null) {
                list = getArguments().getParcelableArrayList("singerInfoList");
                Log.d(TAG, list.get(0).title);
            }

            SingerDetailAdapter adapter = new SingerDetailAdapter();

            for (int i = 0; i < querySize; i++) {
                adapter.addItem(new SingerItem(list.get(i).title));
            }

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Id", list.get(position).Id);
                    bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) list);
                    bundle.putInt("firstPosition", position);
                    YoutubeSinger.setArguments(bundle);

                    ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.container, YoutubeSinger).addToBackStack(null).commit();

                }
            });
        } catch (Exception e) {
            System.err.println("오류 있음 " + e.getMessage() + e.getCause());
        }

        return rootView;
    }

    class SingerDetailAdapter extends BaseAdapter {
        ArrayList<SingerItem> items = new ArrayList<SingerItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(SingerItem item) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Context context = parent.getContext();
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.singer_detail, parent, false);
            }
            Button button2 = (Button) v.findViewById(R.id.button2);
            TextView textView = (TextView) v.findViewById(R.id.textView);
            ImageView imageView=(ImageView) v.findViewById(R.id.imageView);
            CheckBox favoriteBtn = (CheckBox) v.findViewById(R.id.favorite);

            SingerItem item = items.get(position);
            textView.setText(item.getName());

            songJjimList = helper.selectSongJjim();

            for (int i = 0; i < songJjimList.size(); i++) {
                for (int j = 0; j < querySize; j++) {
                    if (songJjimList.get(i).Id.equals(list.get(j).Id)) {
                        isCheck[j] = true;
                    } else if (isCheck[j] == true) {
                        isCheck[j] = true;
                    } else if (isCheck[j] == false && !songJjimList.get(i).Id.equals(list.get(j).Id)){
                        isCheck[j] = false;
                    }
                }
            }

            if (item != null) {
                if (favoriteBtn != null) {
                    favoriteBtn.setChecked(false);
                    CheckBox cbox = (CheckBox)(v.findViewById(R.id.favorite));
                    cbox.setChecked(isCheck[position]);
                    cbox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isCheck[position]) {
                                isCheck[position] = false;
                                helper.deleteSongJjim(list.get(position).Id);
                                Toast.makeText(getContext(), "찜을 취소했습니다.", Toast.LENGTH_SHORT).show();
                            } else{
                                isCheck[position] = true;
                                helper.insertSongJjim(list.get(position).Id, list.get(position).title, list.get(position).thumbnail);
                                Toast.makeText(getContext(), "노래를 찜했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    favoriteBtn.setChecked(isCheck[position]);
                }
            }

            String imageUrl = list.get(position).thumbnail;
            ImageLoadTask task = new ImageLoadTask(imageUrl,imageView);
            task.execute();

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FeedTemplate params = FeedTemplate
                                .newBuilder(ContentObject.newBuilder(list.get(position).title,
                                        list.get(position).thumbnail,
                                        LinkObject.newBuilder().setWebUrl("https://www.youtube.com/watch?v="+list.get(position).Id)
                                                .setMobileWebUrl("https://www.youtube.com/watch?v="+list.get(position).Id).build())
                                        .setDescrption("이미지를 클릭하면 해당 영상의 유튜브 링크로 연결됩니다.")
                                        .build())
                                .build();

                        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
                        serverCallbackArgs.put("user_id", "${current_user_id}");
                        serverCallbackArgs.put("product_id", "${shared_product_id}");


                        KakaoLinkService.getInstance().sendDefault(getContext(), params, new ResponseCallback<KakaoLinkResponse>() {
                            @Override
                            public void onFailure(ErrorResult errorResult) {
                            }

                            @Override
                            public void onSuccess(KakaoLinkResponse result) {
                            }
                        });

                    } catch (Exception e) {
                        System.err.println("오류 있음 " + e.getMessage() + e.getCause());
                    }
                }
            });

            return v;
        }

        private void getApplicationContext() {
        }
    }

    private class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String urlStr;
        private ImageView imageView;
        private HashMap<String, Bitmap> bitmapHash = new HashMap<String, Bitmap>();

        public ImageLoadTask(String urlStr, ImageView imageView) {
            this.urlStr = urlStr;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                if (bitmapHash.containsKey(urlStr)) {
                    Bitmap oldbitmap = bitmapHash.remove(urlStr);
                    if (oldbitmap != null) {
                        oldbitmap.recycle();
                        oldbitmap = null;
                    }
                }
                URL url = new URL(urlStr);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                bitmapHash.put(urlStr, bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            imageView.setImageBitmap(bitmap);
            imageView.invalidate();
        }
    }
}
