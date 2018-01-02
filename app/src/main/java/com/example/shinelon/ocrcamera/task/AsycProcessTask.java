package com.example.shinelon.ocrcamera.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.abbyy.mobile.ocr4.ImageLoadingOptions;
import com.abbyy.mobile.ocr4.RecognitionManager;
import com.abbyy.mobile.ocr4.layout.MocrLayout;
import com.abbyy.mobile.ocr4.layout.MocrPrebuiltLayoutInfo;
import com.alibaba.fastjson.JSONObject;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.example.shinelon.ocrcamera.dataModel.DataString;
import com.example.shinelon.ocrcamera.dataModel.TentcentRs;
import com.example.shinelon.ocrcamera.helper.Authorization;
import com.example.shinelon.ocrcamera.helper.CheckApplication;
import com.example.shinelon.ocrcamera.helper.LogInterceptor;
import com.example.shinelon.ocrcamera.helper.RetorfitRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;



/**
 * Created by Shinelon on 2017/6/30.
 */

public class AsycProcessTask extends AsyncTask<String,String,String> {

    private List<DataString> daTengxuduList;
    private List<DataString> daBaiduList;
    private List<String> baiduRs;
    private List<String> tengxuRs;
    private ProgressDialog mProgressDialog;
    private final int BAIDU = 0;
    private final int TENGXU =1;
    private UpdateListener listener;
    private  Request request;
    private OkHttpClient client;
    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str ="";


    public AsycProcessTask(ProgressDialog d){
        mProgressDialog = d;
        mProgressDialog.show();
    }

    @Override
    public void onPreExecute(){
        setResult("");
        if(CheckApplication.isNotNativeRecognize){
            daTengxuduList = new ArrayList<>();
            daBaiduList = new ArrayList<>();
            baiduRs = new ArrayList<>();
            tengxuRs = new ArrayList<>();


        }

    }

    @Override
    public String doInBackground(String... args){
        String inputFile = args[0];
        File file = new File(inputFile);

        publishProgress("开始识别");
        if (CheckApplication.isNotNativeRecognize){
            // 通用文字识别参数设置
            GeneralParams param = new GeneralParams();
            param.setDetectDirection(true);
            param.setImageFile(file);
            // 调用通用文字识别服务
            OCR.getInstance().recognizeGeneral(param, new OnResultListener<GeneralResult>() {
                @Override
                public void onResult(GeneralResult result) {
                    // 调用成功，返回GeneralResult对象
                    for (WordSimple wordSimple : result.getWordList()) {
                        // wordSimple不包含位置信息
                        Word word = (Word) wordSimple;
                        String itemString = word.getWords().replaceAll("\\s*","").trim();
                        //百度只需要分行
                        int x = word.getLocation().getLeft();
                        int y = word.getLocation().getTop();
                        DataString dataString = new DataString();
                        dataString.setItemString(itemString);
                        dataString.setX(x);
                        dataString.setY(y);
                        dataString.setFlag(false);
                        daBaiduList.add(dataString);
                        Log.e("百度xy", x+"  "+y);
                        Log.e("每个字段和字符数",itemString+"  "+itemString.length());
                    }
                    List<List<DataString>> listOflist = handleDataList(daBaiduList);
                    if(listOflist.size()>0){
                        sortData(listOflist,BAIDU);
                    }else {
                        setResult("null");
                    }

                    synchronized (str){
                        str.notifyAll();
                    }

                    publishProgress("正在处理");

                    Log.e("TAG", "baidu "+Thread.currentThread().getName() );
                }
                @Override
                public void onError(OCRError error) {
                    // 调用失败，返回OCRError对象
                    setResult(error.getMessage());
                }
            });

            String authorization = "";
            try {
                authorization = Authorization.generateKey();
                Log.e("权限",authorization);
            }catch (Exception e){
                e.printStackTrace();
            }

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition","form-data;name=\"appid\""),
                            RequestBody.create(null,"1253939683"))
                    .addPart(Headers.of("Content-Disposition","form-data;name=\"bucket\""),
                            RequestBody.create(null,"hardblack"))
                    .addPart(Headers.of("Content-Disposition","form-data;name=\"image\";filename=\"" + file.getName() + "\""),
                            RequestBody.create(MediaType.parse("image/*"),file))
                    .build();
            LogInterceptor interceptor = new LogInterceptor(CheckApplication.getCotex());
            request = new Request.Builder()
                    .url("http://recognition.image.myqcloud.com/ocr/general")
                    .addHeader("Authorization",authorization)
                    .post(body)
                    .build();
            Response response;
            try {
                publishProgress("正在处理");
                Log.e("腾讯-------------------","true");
                client = new OkHttpClient();
                response = client.newBuilder()
                        .connectTimeout(30,TimeUnit.SECONDS)
                        .readTimeout(30,TimeUnit.SECONDS)
                        .writeTimeout(30,TimeUnit.SECONDS)
                        .addInterceptor(interceptor)
                        .build()
                        .newCall(request)
                        .execute();
                String string = response.body().string();
                Log.e("腾讯string",string);
                if(response.code()==200){
                    TentcentRs tentcentRs = JSONObject.parseObject(string,TentcentRs.class);
                    List<TentcentRs.DataBean.ItemsBean> list = tentcentRs.getData().getItems();
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext()){
                        TentcentRs.DataBean.ItemsBean itemsBean = (TentcentRs.DataBean.ItemsBean) iterator.next();
                        String itemString = itemsBean.getItemstring();
                        itemString = itemString.replaceAll("\\s*","").trim();
                        DataString da = new DataString();
                        da.setItemString(itemString);
                        da.setX(itemsBean.getItemcoord().getX());
                        da.setY(itemsBean.getItemcoord().getY());
                        da.setFlag(false);
                        daTengxuduList.add(da);
                        Log.e("腾讯坐标xy",itemsBean.getItemcoord().getX()+"  "+itemsBean.getItemcoord().getY());
                        Log.w("腾讯每个字段及其字符数",itemString+"  "+itemString.length());
                    }
                    //对于dataList里面的Data字符串数据进行分类，同一行y坐标相差10以内的归类在一起
                    List<List<DataString>> listOfList = handleDataList( daTengxuduList);
                    //归类以后在进行排序
                    sortData(listOfList,TENGXU);
                }else {
                    Log.e("错误",string);
                }


            }catch (Exception e){e.printStackTrace();}

            publishProgress("正在处理");

            try {
                synchronized (str){
                    if(baiduRs.size()==0){
                        if( !"null".equals(getResult()) ){
                            str.wait();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            if(("null").equals(getResult())){
                Log.e("getResult",getResult());
                return getResult();
            }else {
                publishProgress("识别完成");
                compareListString(baiduRs,tengxuRs);
                Iterator<String> iterator = baiduRs.iterator();
                StringBuffer stringBuffer = new StringBuffer();
                while(iterator.hasNext()){
                    stringBuffer.append(iterator.next()+"<br/>");
                }
                String string = stringBuffer.toString();
                Log.e("stringbuffer",string);
                setResult("<html><body>"+string+"</body></html>");
                Log.e("getResult",getResult());
                Log.e("两者list字段各自长度",baiduRs.size()+"    "+tengxuRs.size());
                return getResult();
            }

            //以下为本地识别
        }else {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                ImageLoadingOptions options = new ImageLoadingOptions();
                options.setShouldUseOriginalImageResolution(true);
                MocrLayout mocrLayout =   CheckApplication.getManager().recognizeText(inputStream, options, new RecognitionManager.RecognitionCallback() {
                    @Override
                    public boolean onRecognitionProgress(int i, int i1) {
                        Log.e("i和i1",i+"");
                        while (i<10){
                            publishProgress("正在处理");
                        }
                        publishProgress("识别进度"+i+"%");
                        return false;
                    }
                    @Override
                    public void onRotationTypeDetected(RecognitionManager.RotationType rotationType) {
                    }
                    @Override
                    public void onPrebuiltWordsInfoReady(MocrPrebuiltLayoutInfo mocrPrebuiltLayoutInfo) {
                    }
                });
                String result = mocrLayout.getText();
                String regex = "[`~!@#$%^&*()\\-+={}\\[\\].<>/?￥_+|【】？乂』\\s]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(result);
                result = matcher.replaceAll("").trim();
                Log.e("native原始",result);
                setResult(result);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Log.e("getResult",getResult());
        publishProgress("识别完成");
        return getResult();

    }

    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }

    @Override
    public void onPostExecute(String result){
        Log.d("onPostExecute",result);
        if(!"".equals(result) && !result.equals("null")){
            listener.updateResult(result);
        }else{
            listener.updateResult("识别出错，请重试！");
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
    public void setResult(String result){
        str = result;
        Log.d("setResult()","我是setReult "+str);
    }
    public String getResult(){
        Log.d("getResult()","我是getReult "+str);
        return str;
    }


    public void compareListString(List<String> baiduRs,List<String> tengxuRs){
        int n = baiduRs.size()-tengxuRs.size();
        //判断谁的行数多
        if(n<0){
            //腾讯识别行数多时，百度全部校验，否则百度校验少n个
            n = 0;
        }
        for(int i=0;i<baiduRs.size()-n;i++){
            String a =baiduRs.get(i);
            String b =tengxuRs.get(i);
            Log.e("长度",a.length()+"   "+b.length());
            //“<br/>的/被前面替换没有了”
            String tempA = (a.replaceAll("[\\p{Punct}\\p{Space}]+","")).replaceAll("<br>","");
            String tempB = b.replaceAll("[\\p{Punct}\\p{Space}]+","");
            Log.w("temBaidu temTengxu ",tempA+"\n"+tempB );
            if(tempA.equalsIgnoreCase(tempB)){
                Log.e("比较","相等");
            }else if(tempA.length()==tempB.length())
            {
                for(int k=0;k<tempA.length();k++){
                    if(tempA.charAt(k)!=tempB.charAt(k)){
                        a.replaceAll(tempA.substring(k,k+1),"<font color=\"#ff0000\">" + tempA.charAt(k) + "</font>");
                    }
                }
                Log.e("a与b",a+"  长度相同内容不等  "+b);
            } else {
                for(int j=0;j<Math.min(tempA.length(),tempB.length());j++){
                     if(tempA.charAt(j)!=tempB.charAt(j)){
                        a = a.substring(0,j) + "<font color=\"#ff0000\">" + a.substring(j,a.length()) + "</font>";
                        break;
                     }
                  }
                Log.e("a与b",a+"  长度不等  "+b);
            }
            baiduRs.set(i,a);
        }
    }


    /**
     *对腾讯返回的结果进行分类，根据坐标判断字段为同一行
     */
    public List<List<DataString>> handleDataList(List<DataString> list){

        List<List<DataString>> listList = new ArrayList<>();
        //分类次数
        for(int i=0;i<list.size();i++){
            //未分类就分类
            boolean state = list.get(i).getFlag();
            List<DataString> dataStrings;
            if(!state){
                Log.w("分类", "handleDataList: 执行");
                dataStrings = new ArrayList<>();
                dataStrings.add(list.get(i));
                list.get(i).setFlag(true);
                //判断是否到达最后一个数据，不然会集合溢出
                if(i<list.size()-1){
                    //每次分类的具体行为
                    for(int j=i+1;j<list.size();j++){
                        if(list.get(j).getFlag()){
                            //已经分类不用再去比较
                            Log.w( "handleDataList(第二轮) ","已经分类 ");
                            continue;
                        }
                        int v = Math.abs( (list.get(i).getY()) - (list.get(j).getY()) );
                        if(v<=10){
                            list.get(j).setFlag(true);
                            dataStrings.add(list.get(j));
                        }
                    }
                }
                listList.add(dataStrings);
            }
        }
        Log.e("分类结果",listList.size()+"行\n");
        return listList;
    }


    /**
     * 对归类也就是分行后的数据进行排序
     */
    public void sortData(List<List<DataString>> lists,int flag){
        for (int i=0;i<lists.size();i++){
            List<DataString> stringList = lists.get(i);
            if(flag==TENGXU){
                //直接选择排序
                for(int j=0;j<stringList.size()-1;j++){
                    int index = j;
                    for(int k=j+1; k<stringList.size();k++){
                        int x1 =stringList.get(j).getX();
                        int x2 =stringList.get(k).getX();
                        if(x1 > x2){
                            index=k;
                        }
                        if(index!=j){
                            //交换两个数
                            DataString t = stringList.get(j);
                            DataString d = stringList.get(index);
                            stringList.set(j,d);
                            stringList.set(index,t);
                        }
                    }
                }
            }
            StringBuffer stringBuffer = new StringBuffer();
            for(int m=0;m<stringList.size();m++){
                if(flag==BAIDU){
                    stringBuffer.append(stringList.get(m).getItemString());
                }else {
                    stringBuffer.append(stringList.get(m).getItemString());
                }
            }
            String rs = stringBuffer.toString();
            Log.e( "sortData",rs );
            if(flag==TENGXU){
                tengxuRs.add(rs);
            }else if(flag==BAIDU){
                baiduRs.add(rs);
            }
        }
    }

    public interface UpdateListener{
        void updateResult(String result);
    }

    public void registerlistener(UpdateListener l){
        listener = l;
    }

    public void unregisterListener(){
        listener = null;
    }

}
