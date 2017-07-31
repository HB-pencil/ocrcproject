package com.example.shinelon.ocrcamera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import com.example.shinelon.ocrcamera.helper.PermissionChecker;
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
    private String imagePath;
    private static final int REQUEST_CAMERA = 0;
    private static final int CAMERA_CROP = 1;
    private static final int SELECT = 2;
    private GPUImageView mGPUImageView;
    private final static String USER_NAME = "username";
    private PermissionChecker mChecker;
    private AlertDialog mDialog;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGalleryButton = (ImageButton) findViewById(R.id.gallery_bt);
        mCameraButton = (ImageButton) findViewById(R.id.camera_bt);
        mCropButton = (ImageButton) findViewById(R.id.corp_bt);
        mRecognizeButton = (ImageButton) findViewById(R.id.recognize_bt);
        mChecker = new PermissionChecker();

        handler = new Handler();
        mGPUImageView  = (GPUImageView) findViewById(R.id.image_photo);
        mCropButton.setEnabled(false);
        mRecognizeButton.setEnabled(false);
        mGPUImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        mGalleryButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mCropButton.setOnClickListener(this);
        mRecognizeButton.setOnClickListener(this);

        //授权方式
        initAccessTokenWithAkSk();
    }


    private void initAccessTokenWithAkSk() {

        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {

                String token = result.getAccessToken();
            }
            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                final String message = error.getMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"AK，SK方式获取token失败 " + message,Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }, getApplicationContext(), "qsv0ZAOxsT7cy5eIE5t92IUN", "Kl3cv5v2FaHSaS8gUZu1a16Ny9LzTMXo");

    }

    /**
     * 6.0以上动态权限检测
     */
    private static final String permissions [] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    @Override
    public void onStart(){
        super.onStart();
        if(Build.VERSION.SDK_INT >= 23) {
            mChecker.checkPermissions(this, permissions);
            Log.w("onStart", "我是onStart!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PermissionChecker.REQUEST_STORAGY:
                for(int i = 0;i<permissions.length;i++){
                    if( grantResults[i] == PackageManager.PERMISSION_DENIED){
                    /**  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                     *   shouldShowRequestPermissionRationale(permissions[i])};
                     *   该方法第一次返回false,用户拒绝过返回true，用户拒绝过并点了不再提醒返回false
                    **/
                        Log.w("回调","我是权限拒绝回调!");
                        showPermissionDialog();
                        break;
                     }
                }break;
            default:
                break;
        }
    }
    /**
     * 显示权限对话框
     */
    public void showPermissionDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("该软件需要用到读写内存权限，否则将无法正常使用，请进入设置后返回或退出！")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChecker.startAppSetting(MainActivity.this);
                    }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            //从图库中选择图片
            case R.id.gallery_bt:
                    Intent intent2 = new Intent();
                    intent2.setAction(Intent.ACTION_PICK);
                    intent2.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent2,SELECT);
                break;
            case R.id.camera_bt:
                //启动相机应用,获得一个File实例以及其所指向的抽象路径
                /**
                 *  /storage/emulated/0/Android/data/com.example.shinelon.ocrcamera/files/Pictures/capturedImage149622891598
                 *  这个目录为app私有存储，并不会服更图库
                 *  有Public和无Public区别在于有Public无法指定外部存储目录下自定义目录而另一个可以
                 *   mFile = new File(getExternalStorageDirectory(),filename);
                 *   至于getExternalFileDir顾名思义获取file的，即会随着app删除而删除
                 */
                mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"capturedImage" + String.valueOf(new Date().getTime()) + ".jpg");
                if(!mFile.exists()){
                    try{
                        mFile.createNewFile();
                        Log.d("createFile"," "+mFile.exists());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                /**
                 * 判断版本Uri
                 */
                if(Build.VERSION.SDK_INT >= 24){
                    mUri = FileProvider.getUriForFile(this,getPackageName()+".ocrProvider",mFile);
                    Log.e("FileProvider的mUri",""+mUri.toString());
                }else{
                     mUri = Uri.fromFile(mFile);
                }
                Intent intent3 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent3.putExtra(MediaStore.EXTRA_OUTPUT,mUri);
                startActivityForResult(intent3,REQUEST_CAMERA);
                break;
            case R.id.recognize_bt:
                Log.d("识别按钮",imagePath);
                Intent intent = SecondActivity.newInstance(this,imagePath,USER_NAME);
                System.out.println("手动识别的图片路径为"+ imagePath);
                startActivity(intent);
                break;
            case R.id.corp_bt:
                //从原来路径中裁剪照片
                crop(getUri());
                Log.d("裁剪按钮Uri",getUri().toString());
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
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                        mUri = getImageContentUri(this,mFile);
                    }
                    crop(mUri);
                    setUri(mUri);
                    /**
                     * 原来我们在保存成功后，还要发一个系统广播通知手机有图片更新，发生广播给系统更新图库
                     */
                    Intent intentNotify = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri;
                    if(Build.VERSION.SDK_INT >= 24){
                        uri = FileProvider.getUriForFile(this,getPackageName()+".ocrProvider",mFile);
                    }else{
                        uri = Uri.fromFile(mFile);
                    }
                    intentNotify.setData(uri);
                    this.sendBroadcast(intentNotify);
                }break;
            //成功裁剪完设置图片
            case CAMERA_CROP:
                if(resultCode == RESULT_OK){
                    setImage(getUri());
                    mCropButton.setEnabled(true);
                    mRecognizeButton.setEnabled(true);
                }break;
            case SELECT:
                if(resultCode == RESULT_OK && data!=null){
                    Uri uri = data.getData();
                    Log.d("图库URI",uri.toString()+ "   "+uri.getPath());
                    setUri(uri);
                    crop(uri);
                }
                break;
            default:
                break;
        }

    }

    /**
     * 裁剪方法，如果是7.0要多加配置，绕
     * @param uri
     */
    public void crop(Uri uri){
        //启动裁剪功能，裁剪结束后覆盖原来图片
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        Log.e("调用裁剪方法的Uri",uri.toString());
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
            if(uri.getAuthority().equals("media")){
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(mFile));
            }else{
                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                File file = new File(changeToUrl(uri));
                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(file));
            }
        }else{
            intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }

        startActivityForResult(intent,CAMERA_CROP);
        mCropButton.setEnabled(true);
        mRecognizeButton.setEnabled(true);
    }

    /**
     *7.0拍照后裁剪Uri
     */
    public Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
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
            try{
                Thread.sleep(250);
            }catch (Exception e){
                e.printStackTrace();
            }
            mGPUImageView.saveToPictures("ocrCamera", "capturedImage" + String.valueOf(new Date().getTime()) + "未识别.jpg", new GPUImageView.OnPictureSavedListener() {
                @Override
                public void onPictureSaved(Uri mUri) {
                    if(mUri != null){
                        Toast.makeText(MainActivity.this, "图片已保存", Toast.LENGTH_SHORT).show();
                        Log.d("自动图片路径为", changeToUrl(mUri));
                        imagePath = changeToUrl(mUri);
                        System.out.println("转换前uri、uri.getpath()和转换后   "+ mUri.toString()+ "   " + mUri.getPath()+ "     "+ imagePath);
                        Intent intent = SecondActivity.newInstance(MainActivity.this,imagePath,getIntent().getStringExtra(USER_NAME));
                        startActivity(intent);
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 注意，此方法用于将Uri转化为绝对路径，因为图库选择的uri和savedToPictures()返回的为媒体库的路径，如
     * /external/images/media/7861。对于content://media/有效，对于ACYION_PICK的7.0以后返回的provider
     * 也有效，对于4.4以后的GET_CONTENT返回的document-provider则无效。
     * @param uri
     * @return
     */
    public String changeToUrl(Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA };
        //sdk<=11，cursor = manageQuery(uri,proj,null,null,null)
        CursorLoader loader = new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        Log.w("changtoUri",cursor.getString(column_index));
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
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.help_item:
                FragmentManager manager = getSupportFragmentManager();
                helperDialogFragment dialogFragment = new helperDialogFragment();
                dialogFragment.show(manager,"使用技巧");
                break;
            case R.id.info_item:
                Intent intent = new Intent(this,UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_item:
                Intent i = new Intent(this,SettingActivity.class);
                startActivity(i);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("错误：","!!!!!!!!!!");
    }
}

