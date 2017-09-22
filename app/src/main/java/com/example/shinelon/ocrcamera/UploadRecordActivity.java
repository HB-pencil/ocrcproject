package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.shinelon.ocrcamera.helper.TxtInfo;
import com.example.shinelon.ocrcamera.helper.UserInfoLab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Shinelon on 2017/9/18.
 */

public class UploadRecordActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TxtInfo txtInfo;
    OkHttpClient client;
    static final int TYPE_NORMAL = 0;
    static final int TYPE_FOOT = 1;
    static final int TYPE_EMPTY = 2;
    List<TxtInfo.DataBean.ListBean> txtList;
    Boolean hasNextPage;
    LinearLayoutManager manager;
    CustomAdapter mAdapter;
    int number = 1;
    File mFile;


    public void setFile(File file) {
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.upload_record);
        client = new OkHttpClient();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        manager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);
        setData(number);
        /**
         * 异步线程
         */
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (txtInfo != null) {
            txtList = txtInfo.getData().getList();
        } else {
            Toast.makeText(this, "获取出错，请稍后再试！", Toast.LENGTH_SHORT).show();
            finish();
        }
        mAdapter = new CustomAdapter(txtList);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager.findLastVisibleItemPosition() + 1 == manager.getItemCount()) {
                    hasNextPage = txtInfo.getData().isHasNextPage();
                    Log.e("###", "" + hasNextPage);
                    if (hasNextPage) {
                        number++;
                        setData(number);
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        txtList.addAll(txtInfo.getData().getList());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(UploadRecordActivity.this, "没有更多内容了！", Toast.LENGTH_SHORT).show();
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

    public void setData(int number) {
        final Request request = new Request.Builder()
                .url("http://119.29.193.41/api/user/" + UserInfoLab.getUserInfo().getUserId() + "/txt/7/" + number)
                .addHeader("token", getToken())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = null;
                if (response.isSuccessful()) {
                    try {
                        str = response.body().string();
                        txtInfo = JSONObject.parseObject(str, TxtInfo.class);
                        Log.e("******结果******", str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<TxtInfo.DataBean.ListBean> listofTxt;

        public CustomAdapter(List<TxtInfo.DataBean.ListBean> txtlist) {
            this.listofTxt = txtlist;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_NORMAL) {
                View view = getLayoutInflater().inflate(R.layout.list_view_txt, parent, false);
                return new CustomViewHolder(view);
            } else if (viewType == TYPE_FOOT) {
                View view = getLayoutInflater().inflate(R.layout.list_more, parent, false);
                return new FootViewHolder(view);
            } else {
                View view = getLayoutInflater().inflate(R.layout.list_more_empty, parent, false);
                return new EmptyViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_NORMAL) {
                ((CustomViewHolder) holder).txtName.setText(txtList.get(position).getFileName());
                ((CustomViewHolder) holder).txtUpload.setText(txtList.get(position).getCreateTime());
                ((CustomViewHolder) holder).totalSize.setText(txtList.get(position).getFileSize());
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.download);
                ((CustomViewHolder) holder).imageType.setImageBitmap(bitmap);
            } else {
                return;
            }
        }

        @Override
        public int getItemCount() {
            return listofTxt.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 2) {
                return TYPE_FOOT;
            } else if (position == getItemCount() - 1) {
                return TYPE_EMPTY;
            } else {
                return TYPE_NORMAL;
            }
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtUpload;
        TextView uploadStatus;
        ImageView imageType;
        TextView tipsText;
        TextView totalSize;
        RelativeLayout mRelativeLayout;

        CustomViewHolder(View view) {
            super(view);
            txtName = (TextView) view.findViewById(R.id.txt_name);
            txtUpload = (TextView) view.findViewById(R.id.txt_time);
            uploadStatus = (TextView) view.findViewById(R.id.upload_status_txt);
            imageType = (ImageView) view.findViewById(R.id.image_status_txt);
            tipsText = (TextView) view.findViewById(R.id.tips_txt);
            totalSize = (TextView) view.findViewById(R.id.total_size_txt);
            mRelativeLayout = (RelativeLayout) view.findViewById(R.id.download_status_txt);
            mRelativeLayout.setOnClickListener(new ClickListener(view));
        }


        private class ClickListener implements View.OnClickListener {
            View itemView;

            public ClickListener(View view) {
                this.itemView = view;
            }

            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(itemView);
                Log.e("位置", position + "");
                Toast.makeText(UploadRecordActivity.this, "点击了下载区域！", Toast.LENGTH_SHORT).show();
                Log.e("POSITION", position + " ");
                downloadFile(position);
                if (uploadStatus.getText().toString().equals("已下载")) {
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri uri = FileProvider.getUriForFile(UploadRecordActivity.this, getPackageName() + ".ocrProvider", getFile());
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(uri, "text/plain");
                    } else {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(getFile()), "text/plain");
                    }
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                }
            }
        }
    }
        private class FootViewHolder extends RecyclerView.ViewHolder {
            FootViewHolder(View view) {
                super(view);
            }
        }

        private class EmptyViewHolder extends RecyclerView.ViewHolder {
            EmptyViewHolder(View view) {
                super(view);
            }
        }

        public String getToken() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String token = sp.getString("token", "");
            Log.d("SecondActivity", token);
            return token;
        }


        public Boolean downloadFile(final int position) {
            Boolean isSuccess = false;
            Request request = new Request.Builder()
                    .addHeader("token", getToken())
                    .url("http://119.29.193.41/api/file/" + txtList.get(position).getFileId() + "/download")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                writeToDisk(response.body().byteStream(), position);
                            }
                        }
                    }

                }
            });
            return isSuccess;
        }

        /**
         * 写入磁盘
         */
        public void writeToDisk(InputStream inputStream, int position) {
            FileOutputStream out = null;
            byte[] buf = new byte[1024];
            int length = -1;
            try {
                File file = new File(Environment.getExternalStorageDirectory(), "ocrCamera");
                if (!file.exists()) {
                    file.mkdirs();//创建文件夹
                }
                File afile = new File(file, txtList.get(position).getFileName());
                if (!afile.exists()) {
                    afile.createNewFile();//创建文件
                }
                out = new FileOutputStream(afile);
                while ((length = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                out.close();
                setFile(afile);
                CustomViewHolder holder = (CustomViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(position));

                holder.uploadStatus.setText("已下载");
                holder.uploadStatus.setTextColor(getResources().getColor(R.color.red));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.open);
                holder.imageType.setImageBitmap(bitmap);

                holder.tipsText.setText("点击打开");
                holder.tipsText.setTextColor(getResources().getColor(R.color.red));

                holder.totalSize.setTextColor(getResources().getColor(R.color.red));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UploadRecordActivity.this, "下载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }


    }
