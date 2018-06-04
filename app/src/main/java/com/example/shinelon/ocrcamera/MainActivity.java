package com.example.shinelon.ocrcamera;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.shinelon.ocrcamera.helper.CheckApplication;
import com.example.shinelon.ocrcamera.helper.PermissionChecker;
import com.example.shinelon.ocrcamera.dataModel.UserInfoLab;
import com.example.shinelon.ocrcamera.helper.helperDialogFragment;
import com.example.shinelon.ocrcamera.task.DowanloadRecordActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;

import android.support.v7.widget.Toolbar;



public class MainActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener{
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
    private PermissionChecker mChecker;
    private AlertDialog mDialog;
    private String userName;
    private TextView textView;
    public static String downloadUrl = "";
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ImageView mCusImageView;
    private Handler mHandler;
    private File pFile;
    private File cropFile;
    private ProgressBar progressBar;
    private TextView progresssBarText;
    private Handler handler;
    GPUImage gpuImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        mGalleryButton = (ImageButton) findViewById(R.id.gallery_bt);
        mCameraButton = (ImageButton) findViewById(R.id.camera_bt);
        mCropButton = (ImageButton) findViewById(R.id.corp_bt);
        mRecognizeButton = (ImageButton) findViewById(R.id.recognize_bt);
        mChecker = new PermissionChecker();
        textView = findViewById(R.id.text_tip);

        String str1 = getString(R.string.tips1);
        String str2 = getString(R.string.tips2);
        String str3 = getString(R.string.tips3);
        String s = "<p>"+str1+"<br>"+str2+"<br>"+str3+"</p>";
        textView.setText(Html.fromHtml(s));

        mHandler = new Handler();
        userName = UserInfoLab.getUserInfo().getPhone();

        mCusImageView  = (ImageView) findViewById(R.id.image_photo);
        progressBar = findViewById(R.id.progress_bar);
        progresssBarText = findViewById(R.id.progress_bar_text);

        mCropButton.setEnabled(false);
        mRecognizeButton.setEnabled(false);
        mGalleryButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mCropButton.setOnClickListener(this);
        mRecognizeButton.setOnClickListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        gpuImage= new GPUImage(this);

        pFile = new File(Environment.getExternalStorageDirectory(),"ocrCamera");
        if (!pFile.exists()){
            pFile.mkdirs();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view =  navigationView.getHeaderView(0);
        TextView mText = (TextView) view.findViewById(R.id.description);
        mText.setText(UserInfoLab.getUserInfo().getName());


        Log.d("ACTIVITY创建","activity创建");
    }


    /**
     * 6.0以上动态权限检测
     */
    private static final String permissions [] = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    @Override
    public void onStart(){
        super.onStart();
        if(Build.VERSION.SDK_INT >= 23) {
            mChecker.checkPermissions(this, permissions);
            Log.w("onStart", "我是onStart!");
        }
        Log.e("错误：","onStart()"+ CheckApplication.isNotNativeRecognize);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PermissionChecker.REQUEST_ALL:
                for(int i = 0;i<permissions.length;i++){
                    if( grantResults[i] == PackageManager.PERMISSION_DENIED){
                    /**  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                     *   shouldShowRequestPermissionRationale(permissions[i])};
                     *   该方法第一次返回false,用户第一次拒绝返回true，用户拒绝过并点了不再提醒返回false
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
                .setNegativeButton("退出", (dialog, which) -> finish() );
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
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
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

                mFile = new File(pFile,"capturedImage" + String.valueOf(System.currentTimeMillis() + ".jpg"));
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
                Intent intent3 = new Intent(this,CameraActivity.class);
                intent3.putExtra("filePath",mFile);
                sendUpdateInfo(mUri);
                startActivityForResult(intent3,REQUEST_CAMERA);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.recognize_bt:
                Log.d("识别按钮",imagePath);
                Intent intent = SecondActivity.newInstance(this,imagePath,userName);
                System.out.println("手动识别的图片路径为"+ imagePath);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
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
                        //7.0转化为media uri
                        mUri = getImageContentUri(this,mFile);
                    }
                    crop(mUri);
                    /**
                     * 原来我们在保存成功后，还要发一个系统广播通知手机有图片更新，发生广播给系统更新图库
                     */

                    Uri uri;
                    if(Build.VERSION.SDK_INT >= 24){
                        uri = FileProvider.getUriForFile(this,getPackageName()+".ocrProvider",mFile);
                    }else{
                        uri = Uri.fromFile(mFile);
                    }
                    sendUpdateInfo(uri);
                }else {
                    if (mFile.exists()){
                        mFile.delete();
                    }
                }
                break;
            //成功裁剪完设置图片
            case CAMERA_CROP:
                if(resultCode == RESULT_OK){
                    setImage(getUri());
                    mCropButton.setEnabled(true);
                    mRecognizeButton.setEnabled(true);
                }else {
                    if(cropFile.exists()){
                        cropFile.delete();
                    }
                }
                break;
            case SELECT:
                if(resultCode == RESULT_OK && data!=null){
                    Uri uri = data.getData();
                    Log.d("图库URI",uri.toString()+ "   "+uri.getPath());
                    setUri(uri);
                    crop(getUri());
                }
                break;
            default:
                break;
        }

    }

    /**
     * 裁剪方法，如果是7.0要多加配置
     * @param uri
     */
    public void crop(Uri uri){
        //启动裁剪
        cropFile = new File(pFile,"sampleCrop"+System.currentTimeMillis()+".jpg");
        if (!cropFile.exists()){
            try {
                cropFile.createNewFile();
            }catch (Exception e){e.printStackTrace();}
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop",true);
        intent.putExtra("scale",true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(cropFile));
        Log.e("调用裁剪方法的Uri",uri.toString());

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            Uri cropUri = getImageContentUri(this,cropFile);
            setUri(cropUri);
        }else {
            setUri(Uri.fromFile(cropFile));
        }
        startActivityForResult(intent,CAMERA_CROP);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        mCropButton.setEnabled(true);
        mRecognizeButton.setEnabled(true);
    }

    /**
     *7.0拍照后裁剪为fileprvider,不能读，需要file转为media，返回meida 类型uri
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
            textView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progresssBarText.setVisibility(View.VISIBLE);
            Log.e("openUri",uri.toString());
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,out);

            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(()->{
                gpuImage.setImage(bitmap);
                GPUImageFilterGroup group = new GPUImageFilterGroup();
                group.addFilter(new GPUImageContrastFilter());
                group.addFilter(new GPUImageBrightnessFilter(-0.02F));
                group.addFilter(new GPUImageSepiaFilter(1.5F));

                group.updateMergedFilters();

                gpuImage.setFilter(group);
                Bitmap bitmap1 = gpuImage.getBitmapWithFilterApplied();

                ByteArrayOutputStream out1 = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG,20,out1);
                try{
                    File file = new File(pFile,"sampleTransformed"+System.currentTimeMillis()+".jpg");
                    imagePath = file.getAbsolutePath();
                    if (!file.exists()){
                        file.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(out1.toByteArray());
                    Log.w("glide变换后byte",out.toByteArray().length+"");
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                        Uri uri1 = getImageContentUri(MainActivity.this,file);
                        setUri(uri1);
                        sendUpdateInfo(uri1);
                    }else {
                        setUri(Uri.fromFile(file));
                        sendUpdateInfo(Uri.fromFile(file));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                mHandler.postDelayed(()->{
                    Intent intent = SecondActivity.newInstance(MainActivity.this,imagePath,userName);
                    intent.putExtra("IMAGE_PATH",imagePath);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                },500);

                mHandler.post(()->{
                    progresssBarText.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    mCusImageView.setImageBitmap(bitmap1);
                });
                if (cropFile.exists()){
                    cropFile.delete();
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 注意，此方法用于将Uri转化为绝对路径，因为图库选择的uri和savedToPictures()返回的为媒体库的路径，如
     * /external/images/media/7861。对于content://media/有效，对于ACYION_PICK的6.0以后返回的provider
     * 也有效，对于4.4以后的GET_CONTENT返回的document-provider则无效。
     * @param uri
     * @return
     */
    @Deprecated
    public String changeToPath(Uri uri){
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

    @Deprecated
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
        float tagHeight = 1920;
        float tagWith = 1080;
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
        return bitmap;

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
            case R.id.setting_item:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            case R.id.exit_item:
                finish();
                break;
            default:break;

        }
        return super.onContextItemSelected(menuItem);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }



    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case  R.id.info_item:
                Intent intent = new Intent(this,UserInfoActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.upload_image:
                Intent intent3 = new Intent(this,DowanloadRecordActivity.class);
                startActivity(intent3);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.contact_us:
                Uri uri = Uri.parse("mailto:hardblack@aliyun.com");
                Intent intent4 = new Intent(Intent.ACTION_SENDTO,uri);
                startActivity(intent4);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            case R.id.setting_item:
                Intent intent1 = new Intent(this,AboutActivity.class);
                startActivity(intent1);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                break;
            default:break;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("错误：","onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("错误：","onStop()");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("错误：","onDestroy()");
    }


    /**
     * 强制显示overflow中的icon
     * @param view
     * @param menu  Menu为接口类
     * @return
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if(menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try { Method m = menu.getClass().getDeclaredMethod( "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true); m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "unable to set icons for overflow menu", e);
                }
            }
        } return true;
    }

    public void sendUpdateInfo(Uri uri){
        Intent intentNotify = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentNotify.setData(uri);
        sendBroadcast(intentNotify);
    }

}

