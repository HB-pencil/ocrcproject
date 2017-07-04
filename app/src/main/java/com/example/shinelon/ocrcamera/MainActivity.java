package com.example.shinelon.ocrcamera;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.example.shinelon.ocrcamera.helper.Messager;
import com.example.shinelon.ocrcamera.helper.helperDialogFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageButton mGalleryButton;
    private ImageButton mCameraButton;
    private ImageButton mCropButton;
    private ImageButton mRecognizeButton;
    private File mFile;
    private Uri mUri;
    private Uri uri;
    private static final int REQUEST_CAMERA = 0;
    private static final int CAMERA_CROP = 1;
    private static final int SELECT = 2;
    private static final int CROP = 3;
    private GPUImageView mGPUImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGalleryButton = (ImageButton) findViewById(R.id.gallery_bt);
        mCameraButton = (ImageButton) findViewById(R.id.camera_bt);
        mCropButton = (ImageButton) findViewById(R.id.corp_bt);
        mRecognizeButton = (ImageButton) findViewById(R.id.recognize_bt);

        mGPUImageView  = (GPUImageView) findViewById(R.id.image_photo);
        mCropButton.setEnabled(false);
        mRecognizeButton.setEnabled(false);
        mGPUImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        mGalleryButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mCropButton.setOnClickListener(this);
        mRecognizeButton.setOnClickListener(this);

        //授权方式
        initAccessToken();
    }

    private void initAccessToken() {

        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Messager.showError(MainActivity.this,error.getMessage(),"licence方式获取token失败");
            }
        }, getApplicationContext());
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            //从图库中选择图片
            case R.id.gallery_bt:
                Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
                intent2.setType("image/*");
                startActivityForResult(intent2,SELECT);
                break;
            case R.id.camera_bt:
                //启动相机应用,获得一个File实例以及其所指向的抽象路
                /**
                 *  /storage/emulated/0/Android/data/com.example.shinelon.ocrcamera/files/Pictures/capturedImage149622891598
                 *  这个目录为app私有存储，并不会服更图库
                 *  有Public和无Public区别在于有Public无法指定外部存储目录下自定义目录而另一个可以
                 *   mFile = new File(getExternalStorageDirectory(),filename);
                 *   至于getExternalFileDir顾名思义获取file的，即会随着app删除而删除
                 */
                mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"capturedImage" + String.valueOf(new Date().getTime()) + ".jpg");
                mUri = Uri.fromFile(mFile);
                Intent intent3 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //putExtra()对于从图库选择的图片并不生效，但对于裁剪和相机却是可行的
                intent3.putExtra(MediaStore.EXTRA_OUTPUT,mUri);
                startActivityForResult(intent3,REQUEST_CAMERA);
                break;
            case R.id.recognize_bt:
                String imagePath = changeToUrl(getUri());
                Intent intent = SecondActivity.newInstance(this,imagePath);
                System.out.println("手动图片路径为"+ changeToUrl(getUri()));
                startActivity(intent);
                break;
            case R.id.corp_bt:
                //从原来路径中裁剪照片
                crop(getUri());
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            //成功拍到照片裁剪
            case REQUEST_CAMERA:
                if(resultCode == RESULT_OK){
                    //启动裁剪功能，裁剪结束后覆盖原来图片
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mUri,"image/*");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,mUri);
                    setUri(mUri);
                    /**
                     * 原来我们在保存成功后，还要发一个系统广播通知手机有图片更新，发生广播给系统更新图库
                     */
                    Intent intentNotify = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(mFile);
                    intentNotify.setData(uri);
                    this.sendBroadcast(intentNotify);
                    Log.d("裁剪",uri.getPath());

                    startActivityForResult(intent,CAMERA_CROP);
                }break;
            //成功裁剪完设置图片
            case CAMERA_CROP:
                if(resultCode == RESULT_OK){
                    setImage(getUri());
                    mCropButton.setEnabled(true);
                    mRecognizeButton.setEnabled(true);
                }break;
            case SELECT:
                if(data != null){
                    Uri uri = data.getData();
                    Log.d("图库URI",uri.getPath());
                    crop(uri);
                }
                break;
            case CROP:
                break;
            default:
                break;
        }

    }

    /**
     * 裁剪方法
     * @param uri
     */
    public void crop(Uri uri){
        //启动裁剪功能，裁剪结束后覆盖原来图片
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        setUri(uri);
        startActivityForResult(intent,CAMERA_CROP);
        mCropButton.setEnabled(true);
        mRecognizeButton.setEnabled(true);
    }

    /**
     * 此方法更新显示的图像
     */
    public void setImage(Uri uri){
        try{
            mGPUImageView.setImage(uri);
            mGPUImageView.setFilter(new GPUImageSharpenFilter());
            //Bitmap bitmap = compressPhoto(uri);
            //mImageView.setImageBitmap(bitmap);
            mGPUImageView.saveToPictures("ocrCamera", "capturedImage" + String.valueOf(new Date().getTime()) + ".jpg", new GPUImageView.OnPictureSavedListener() {
                @Override
                public void onPictureSaved(Uri mUri) {
                    if(mUri != null){
                        Toast.makeText(getApplicationContext(), "图片已保存", Toast.LENGTH_SHORT).show();
                        Log.d("自动图片路径为", changeToUrl(mUri));
                        String imagePath = changeToUrl(mUri);
                        Intent intent = SecondActivity.newInstance(MainActivity.this,imagePath);
                        try{
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        startActivity(intent);
                    }
                }
            });
            setUri(uri);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 注意，此方法用于将Uri转化为绝对路径，因为图库选择的uri和savedToPictures()返回的为媒体库的路径，如
     * /external/images/media/7861
     * @param uri
     * @return
     */
    public String changeToUrl(Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * 压缩图片方法由于使用GPUImageView，故而将自定义View及其相关压缩等注释掉，此方法暂时不用
     */

    public Bitmap compressPhoto(Uri uri){
        Bitmap bitmap;
        Bitmap newBitmap;
        //尺寸压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只加载尺寸信息

        options.inJustDecodeBounds = true;
        try{
            /**
             * 这里使用decodeStream而不用decodeFile，原因在于使用后者图库加载会出现FileNoFoundException,
             * 一开始不明所以，后来看到
             * Don't assume that there is a file path. Android 4.4 and up are about to remove them. And the uri you got
             * has already no path.You can still access the file content either through an InputStream
             * 所以应该是版本的原因，看来使用decodeStream要比decodeFile明显保险，虽然步骤麻烦多一点
             */
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),null,options);
        }catch(IOException e){
            e.printStackTrace();
        }


        float srcWith = options.outWidth;
        float srcHeight = options.outHeight;
        float tagHeight = 800;
        float tagWith = 400;
        //默认压缩比
        int inSampliSize =1;
        if(srcWith/tagWith>srcHeight/tagHeight && srcWith>tagWith){
            inSampliSize = Math.round(srcWith/tagWith);
        }else if(srcHeight/tagHeight>srcWith/tagWith && srcHeight>tagHeight){
            inSampliSize = Math.round(srcHeight/tagHeight);
        }

        options.inSampleSize = inSampliSize;
        options.inJustDecodeBounds = false;
        InputStream in = null;
        try{
           in  = getContentResolver().openInputStream(uri);
        }catch (IOException e){
            e.printStackTrace();
        }
        bitmap  =  BitmapFactory.decodeStream(in,null,options);
        //质量压缩
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bout);
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        newBitmap = BitmapFactory.decodeStream(bin);
        if(newBitmap != null){
            return newBitmap;
        }else{
            return bitmap;
        }
    }


    /**
     * 获得目标Uri
     */

    public Uri getUri(){
        return uri;
    }

    /**
     *设置uri
     *
     * @param mUri
     */
    public void setUri(Uri mUri){
        uri = mUri;
    }


    /**
     *设置工具栏
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.help_item:
                FragmentManager manager = getSupportFragmentManager();
                helperDialogFragment dialogFragment = new helperDialogFragment();
                dialogFragment.show(manager,"使用技巧");
                break;
            case R.id.about_item:
                break;
            case R.id.setting_item:
                break;
            case R.id.exit_item:
                finish();
                break;

        }
        return super.onContextItemSelected(menuItem);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode,keyEvent);
    }


  
}

