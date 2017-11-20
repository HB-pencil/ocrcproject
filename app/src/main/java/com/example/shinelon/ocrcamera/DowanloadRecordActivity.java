package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.shinelon.ocrcamera.helper.UploadInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.Toolbar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/9/20.
 */

public class DowanloadRecordActivity extends AppCompatActivity{

    Toolbar toolbar;
    RecyclerView recyclerView;
    UploadInfo uploadInfo;
    OkHttpClient client;
    static final int TYPE_NORMAL = 0;
    static final int TYPE_EMPTY = 2;
    List<UploadInfo.DataBean.ListBean> uploadList;
    Boolean hasNextPage ;
    LinearLayoutManager manager;
    CustomAdapter mAdapter;
    int number = 1;
    static String parentPath;
    private static final int IMAGE = 0;
    private static final int TEXT = 1;

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.upload_record);

        toolbar = (Toolbar) findViewById(R.id.upload_bar);
        setSupportActionBar(toolbar);

        client = new OkHttpClient();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);

        File file = new File(Environment.getExternalStorageDirectory(), "ocrCamera");
        parentPath = file.getAbsolutePath();
        uploadList = new ArrayList<>();
        setData(number);
        /**
         * 异步线程
         */
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uploadInfo != null) {
            uploadList = uploadInfo.getData().getList();
            hasNextPage = uploadInfo.getData().isHasNextPage();
        } else {
            Toast.makeText(this, "获取出错，请稍后再试！", Toast.LENGTH_SHORT).show();
            finish();
        }


        mAdapter = new CustomAdapter(uploadList);
        recyclerView.setAdapter(mAdapter);
        doUpdate();//遍历list判断文件是否下载，是否要更新视图

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager.findLastVisibleItemPosition() + 1 == manager.getItemCount()) {
                    Log.e("###", "" + hasNextPage);
                    if (hasNextPage) {
                        hasNextPage = false;
                        number++;
                        setData(number);
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
                .url("http://119.29.193.41:80/api/file/picture/list/4/" + number)
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
                        uploadInfo = JSONObject.parseObject(str,UploadInfo.class);
                        hasNextPage = uploadInfo.getData().isHasNextPage();
                        uploadList.addAll(uploadInfo.getData().getList());
                        Log.e("******结果******", str);
                        Log.e("------结果------",hasNextPage+"" );
                        notifyUpdateDone();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void notifyUpdateDone(){
        runOnUiThread(() ->  mAdapter.notifyDataSetChanged());
    }


    private class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        List<UploadInfo.DataBean.ListBean> listofIma;
        public CustomAdapter(List<UploadInfo.DataBean.ListBean> imalist) {
            this.listofIma = imalist;

        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_NORMAL){
                View view = getLayoutInflater().inflate(R.layout.list_item,parent,false);
                return new CustomViewHolder(view);
            }else {
                View view = getLayoutInflater().inflate(R.layout.list_more_empty,parent,false);
                return new EmptyViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_NORMAL) {

                ((CustomViewHolder)holder).imaName.setText(uploadList.get(position).getOriginalFileName());
                ((CustomViewHolder) holder).imaUpload.setText(uploadList.get(position).getCreateTime());
                ((CustomViewHolder) holder).totalSize.setText(uploadList.get(position).getFileSize());
                ((CustomViewHolder) holder).txtName.setText(uploadList.get(position).getOcrFileName());
                ((CustomViewHolder) holder).txtUpload.setText(uploadList.get(position).getCreateTime());

                String fileNameIma = ((CustomViewHolder)holder).imaName.getText().toString();
                File file = new File(parentPath,fileNameIma);
                String fileNameTxt = ((CustomViewHolder) holder).txtName.getText().toString();
                File sfile = new File(parentPath,fileNameTxt);

                if(file.exists()){
                ((CustomViewHolder) holder).uploadStatus_1.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) holder).uploadStatus_2.setVisibility(View.VISIBLE);

                ((CustomViewHolder) holder).imageType_1.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) holder).imageType_2.setVisibility(View.VISIBLE);

                ((CustomViewHolder) holder).tipsText_1.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) holder).tipsText_2.setVisibility(View.VISIBLE);

                }else {
                ((CustomViewHolder) holder).uploadStatus_1.setVisibility(View.VISIBLE);
                ((CustomViewHolder) holder).uploadStatus_2.setVisibility(View.INVISIBLE);

                ((CustomViewHolder) holder).imageType_1.setVisibility(View.VISIBLE);
                ((CustomViewHolder) holder).imageType_2.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) holder).tipsText_1.setVisibility(View.VISIBLE);
                ((CustomViewHolder) holder).tipsText_2.setVisibility(View.INVISIBLE);
               }

                if(sfile.exists()){
                    ((CustomViewHolder) holder).txtUploadStatus_1.setVisibility(View.INVISIBLE);
                    ((CustomViewHolder) holder).txtUploadStatus_2.setVisibility(View.VISIBLE);

                    ((CustomViewHolder) holder).txtImageType_1.setVisibility(View.INVISIBLE);
                    ((CustomViewHolder) holder).txtImageType_2.setVisibility(View.VISIBLE);

                    ((CustomViewHolder) holder).txtTipsText_1.setVisibility(View.INVISIBLE);
                    ((CustomViewHolder) holder).txtTipsText_2.setVisibility(View.VISIBLE);

                }else {
                    ((CustomViewHolder) holder).txtUploadStatus_1.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) holder).txtUploadStatus_2.setVisibility(View.INVISIBLE);

                    ((CustomViewHolder) holder).txtImageType_1.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) holder).txtImageType_2.setVisibility(View.INVISIBLE);
                    ((CustomViewHolder) holder).txtTipsText_1.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) holder).txtTipsText_2.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return listofIma.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return TYPE_EMPTY;
            } else {
                return TYPE_NORMAL;
            }
        }
    }

    private class CustomViewHolder extends  RecyclerView.ViewHolder{
        TextView imaName;
        TextView imaUpload;
        TextView uploadStatus_1;
        ImageView imageType_1;
        TextView tipsText_1;
        TextView uploadStatus_2;
        ImageView imageType_2;
        TextView tipsText_2;
        TextView totalSize;
        TextView txtName;
        TextView txtUpload;
        TextView txtUploadStatus_1;
        ImageView txtImageType_1;
        TextView txtTipsText_1;
        TextView txtUploadStatus_2;
        ImageView txtImageType_2;
        TextView txtTipsText_2;
        RelativeLayout mImaLayout;
        RelativeLayout mTxtLayout;

        CustomViewHolder(View view) {
            super(view);
            imaName = (TextView) view.findViewById(R.id.ima_name);
            imaUpload = (TextView) view.findViewById(R.id.ima_time);
            uploadStatus_1 = (TextView) view.findViewById(R.id.upload_status_ima_1);
            imageType_1 = (ImageView) view.findViewById(R.id.image_status_ima_1);
            tipsText_1 = (TextView) view.findViewById(R.id.tips_ima_1);
            uploadStatus_2 = (TextView) view.findViewById(R.id.upload_status_ima_2);
            imageType_2 = (ImageView) view.findViewById(R.id.image_status_ima_2);
            tipsText_2 = (TextView) view.findViewById(R.id.tips_ima_2);
            totalSize = (TextView) view.findViewById(R.id.total_size_ima);

            txtName = (TextView) view.findViewById(R.id.txt_name);
            txtUpload = (TextView) view.findViewById(R.id.txt_time);
            txtUploadStatus_1 = (TextView) view.findViewById(R.id.upload_status_txt_1);
            txtImageType_1 = (ImageView) view.findViewById(R.id.image_status_txt_1);
            txtTipsText_1 = (TextView) view.findViewById(R.id.tips_txt_1);
            txtUploadStatus_2 = (TextView) view.findViewById(R.id.upload_status_txt_2);
            txtImageType_2 = (ImageView) view.findViewById(R.id.image_status_txt_2);
            txtTipsText_2 = (TextView) view.findViewById(R.id.tips_txt_2);

            ClickListener listener = new ClickListener(view);

            mImaLayout = (RelativeLayout) view.findViewById(R.id.download_status_ima);
            mTxtLayout = (RelativeLayout) view.findViewById(R.id.download_status_txt);
            mImaLayout.setOnClickListener(listener);
            mTxtLayout.setOnClickListener(listener);
        }

        private class ClickListener implements View.OnClickListener {
            View itemView;

            public ClickListener(View view) {
                this.itemView = view;
            }

            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(itemView);
                int viewId = view.getId();
                Log.e("click", "onClick: "+viewId );
                if(viewId == R.id.download_status_ima){
                    if (uploadStatus_1.getVisibility() != View.VISIBLE) {
                        Intent intent = new Intent();
                        TextView nameText = (TextView)itemView.findViewById(R.id.ima_name);
                        String fileName = nameText.getText().toString();
                        File file = new File(parentPath,fileName);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri uri = FileProvider.getUriForFile(DowanloadRecordActivity.this, getPackageName() + ".ocrProvider", file);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(uri, "image/*");
                        } else {
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file), "image/*");
                        }
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    }else {
                        downloadImageFile(position);
                    }
                }else if(viewId == R.id.download_status_txt){
                    if (txtUploadStatus_1.getVisibility() != View.VISIBLE) {
                        Intent intent = new Intent();
                        TextView nameText = (TextView)itemView.findViewById(R.id.txt_name);
                        String fileName = nameText.getText().toString();
                        File file = new File(parentPath,fileName);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri uri = FileProvider.getUriForFile(DowanloadRecordActivity.this, getPackageName() + ".ocrProvider", file);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setDataAndType(uri, "text/plain");
                        } else {
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file), "text/plain");
                        }
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    }else {
                        downloadTxtFile(position);
                    }
                }

                Log.e("POSITION",position+"");

            }
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

    public void downloadImageFile(final int position) {

        Request request = new Request.Builder()
                .addHeader("token", getToken())
                .url("http://119.29.193.41/api/file/picture/" + uploadList.get(position).getFileId() + "/original/download")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            writeToDisk(response.body().byteStream(), position,IMAGE);
                        }
                    }
            }
        });
    }

    public void downloadTxtFile(final int position) {
        Request request = new Request.Builder()
                .addHeader("token", getToken())
                .url("http://119.29.193.41/api/file/picture/" + uploadList.get(position).getFileId() + "/ocr/download")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        writeToDisk(response.body().byteStream(), position,TEXT);
                    }
                }
            }
        });
    }


    /**
     * 写入磁盘
     */
    public void writeToDisk(InputStream inputStream, final int position,int type) {
        FileOutputStream out = null;
        byte[] buf = new byte[1024];
        int length = -1;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "ocrCamera");
            if (!file.exists()) {
                file.mkdirs();//创建文件夹
            }
            File afile = null;
            if(type==IMAGE){
                afile = new File(file, uploadList.get(position).getOriginalFileName());
            }else if(type == TEXT) {
                afile = new File(file, uploadList.get(position).getOcrFileName());
                Log.e("afile", "writeToDisk: "+ uploadList.get(position).getOcrFileName());
            }
            if (!afile.exists()) {
                afile.createNewFile();//创建文件
            }
            out = new FileOutputStream(afile);
            while ((length = inputStream.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            out.close();
            if(type == IMAGE){
                updateView(position,IMAGE);
            }else {
                updateView(position,TEXT);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DowanloadRecordActivity.this, "下载失败！", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 视图更新行为封装
     */
    public void doUpdate(){
        for(int i = 0;i<uploadList.size();i++){
            if(isFileDownloaded(i,IMAGE)){
                updateView(i,IMAGE);
            }
            if(isFileDownloaded(i,TEXT)){
                updateView(i,TEXT);
            }
        }
    }

    /**
     * 检查文件是否已经下载
     */
    public Boolean isFileDownloaded(int index,int type){
        File file = new File(Environment.getExternalStorageDirectory(),"ocrCamera");
        if(type == IMAGE){
            File afile = new File(file,uploadList.get(index).getOriginalFileName());
            return afile.exists();
        }else {
            File afile = new File(file,uploadList.get(index).getOcrFileName());
            return afile.exists();
        }
    }

    /**
     * 更新子View视图
     */
    public void updateView(final int position,int type){
        if(type==IMAGE){
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    int realPosition = position - manager.findFirstVisibleItemPosition();
                    CustomViewHolder holder = (CustomViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(realPosition));

                    holder.uploadStatus_1.setVisibility(View.INVISIBLE);
                    holder.uploadStatus_2.setVisibility(View.VISIBLE);

                    holder.imageType_1.setVisibility(View.INVISIBLE);
                    holder.imageType_2.setVisibility(View.VISIBLE);

                    holder.tipsText_1.setVisibility(View.INVISIBLE);
                    holder.tipsText_2.setVisibility(View.VISIBLE);

                }
            });
        }else if(type == TEXT){
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    int realPosition = position - manager.findFirstVisibleItemPosition();
                    CustomViewHolder holder = (CustomViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(realPosition));

                    holder.txtUploadStatus_1.setVisibility(View.INVISIBLE);
                    holder.txtUploadStatus_2.setVisibility(View.VISIBLE);

                    holder.txtImageType_1.setVisibility(View.INVISIBLE);
                    holder.txtImageType_2.setVisibility(View.VISIBLE);

                    holder.txtTipsText_1.setVisibility(View.INVISIBLE);
                    holder.txtTipsText_2.setVisibility(View.VISIBLE);

                }
            });
        }

    }


}
