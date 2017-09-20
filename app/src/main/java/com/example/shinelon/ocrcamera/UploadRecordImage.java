package com.example.shinelon.ocrcamera;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.shinelon.ocrcamera.helper.ImaInfo;
import com.example.shinelon.ocrcamera.helper.UserInfoLab;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/9/20.
 */

public class UploadRecordImage extends AppCompatActivity{

    RecyclerView recyclerView;
    ImaInfo imaInfo;
    OkHttpClient client;
    static final int TYPE_NORMAL = 0;
    static final int TYPE_FOOT = 1;
    static final int TYPE_EMPTY = 2;
    List<ImaInfo.DataBean.ListBean> imaList;
    Boolean hasNextPage ;
    LinearLayoutManager manager;
    CustomAdapter mAdapter;
    int number = 1;

    @Override
    public void onCreate(Bundle savedInstanceSate){
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.upload_record);
        client= new OkHttpClient();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);
        setData(number);
        /**
         * 异步线程
         */
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(imaInfo!=null){
            imaList = imaInfo.getData().getList();
        }else{
            Toast.makeText(this,"获取出错，请稍后再试！",Toast.LENGTH_SHORT).show();
            finish();
        }
        mAdapter = new CustomAdapter(imaList);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(manager.findLastVisibleItemPosition() + 1 == manager.getItemCount()){
                    hasNextPage =imaInfo.getData().isHasNextPage();
                    Log.e("###",""+hasNextPage);
                    if(hasNextPage){
                        number++;
                        setData(number);
                        try{
                            Thread.sleep(2000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        imaList.addAll(imaInfo.getData().getList());
                        mAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(UploadRecordImage.this,"没有更多内容了！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setData(int number){
        final Request request = new Request.Builder()
                .url("http://119.29.193.41:80/api/user/"+ UserInfoLab.getUserInfo().getUserId() +"/picture/7/"+ number)
                .addHeader("token",getToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = null;
                if(response.isSuccessful()){
                    try {
                        str = response.body().string();
                        imaInfo = JSONObject.parseObject(str,ImaInfo.class);
                        Log.e("******结果******",str);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<ImaInfo.DataBean.ListBean> listofIma;
        public CustomAdapter(List<ImaInfo.DataBean.ListBean> imalist) {
            this.listofIma = imalist;

        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_NORMAL){
                View view = getLayoutInflater().inflate(R.layout.list_view_ima,parent,false);
                return new CustomViewHolder(view);
            }else if(viewType == TYPE_FOOT){
                View view = getLayoutInflater().inflate(R.layout.list_more,parent,false);
                return new FootViewHolder(view);
            }else {
                View view = getLayoutInflater().inflate(R.layout.list_more_empty,parent,false);
                return new EmptyViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(getItemViewType(position) == TYPE_NORMAL){
                ((CustomViewHolder) holder).imaName.setText(imaList.get(position).getFileName());
                ((CustomViewHolder) holder).imaUpload.setText(imaList.get(position).getCreateTime());
                ((CustomViewHolder) holder).totalSize.setText(imaList.get(position).getFileSize());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.download);
                ((CustomViewHolder) holder).imageType.setImageBitmap(bitmap);
            }else{
                return;
            }
        }

        @Override
        public int getItemCount() {
            return listofIma.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if(position  == getItemCount() -2){
                return TYPE_FOOT;
            }else if(position == getItemCount() - 1){
                return TYPE_EMPTY;
            }else{
                return TYPE_NORMAL;
            }
        }
    }

    private class CustomViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{
        TextView imaName;
        TextView imaUpload;
        TextView uploadStatus;
        ImageView imageType;
        TextView tipsText;
        TextView totalSize;
        RelativeLayout mRelativeLayout;
        CustomViewHolder(View view){
            super(view);
            imaName = (TextView) view.findViewById(R.id.ima_name);
            imaUpload = (TextView) view.findViewById(R.id.ima_time);
            uploadStatus = (TextView) view.findViewById(R.id.upload_status);
            imageType = (ImageView) view.findViewById(R.id.image_status);
            tipsText = (TextView) view.findViewById(R.id.tips);
            totalSize = (TextView) view.findViewById(R.id.total_size_ima);
            mRelativeLayout = (RelativeLayout) view.findViewById(R.id.download_status_ima);
            mRelativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            Toast.makeText(UploadRecordImage.this,"点击了下载区域！",Toast.LENGTH_SHORT).show();
        }

    }

    private class FootViewHolder extends RecyclerView.ViewHolder{
        FootViewHolder(View view){
            super(view);
        }
    }

    private class EmptyViewHolder extends RecyclerView.ViewHolder{
        EmptyViewHolder(View view){
            super(view);
        }
    }

    public  String getToken(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sp.getString("token","");
        Log.d("SecondActivity",token);
        return token;
    }
}
