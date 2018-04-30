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
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



/**
 * Created by Shinelon on 2017/6/30.重点处理过程在  doInBackground()  里面
 */

public class AsycProcessTask extends AsyncTask<String,String,List<String>> {

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
    private static String str1 ="";
    private static String str2 ="";


    public AsycProcessTask(ProgressDialog d){
        mProgressDialog = d;
        mProgressDialog.show();
    }

    @Override
    public void onPreExecute(){
        setResult("","");
        if(CheckApplication.isNotNativeRecognize){
            daTengxuduList = new ArrayList<>();
            daBaiduList = new ArrayList<>();
            baiduRs = new ArrayList<>();
            tengxuRs = new ArrayList<>();

        }

    }

    @Override
    public List<String> doInBackground(String... args){
        String inputFile = args[0];
        File file = new File(inputFile);
        publishProgress("开始识别");
        if (CheckApplication.isNotNativeRecognize){
            // 这一部分是百度通用文字识别参数设置(集成的sdk)
            GeneralParams param = new GeneralParams();
            param.setDetectDirection(true);
            param.setVertexesLocation(true);
            param.setImageFile(file);
            // 调用百度通用文字识别服务
            OCR.getInstance().recognizeGeneral(param, new OnResultListener<GeneralResult>() {
                @Override
                public void onResult(GeneralResult result) {
                    // 调用成功，返回GeneralResult对象，此方法UI线程回调
                    for (WordSimple wordSimple : result.getWordList()) {
                        // wordSimple不包含位置信息
                        Word word = (Word) wordSimple;
                        //获得识别的每一行结果并除去空格，此处识别结果是sdk封装的方法,结果是非json，也就是处理好的数据了
                        String itemString = word.getWords().replaceAll("\\s*","").trim();
                        int x = word.getLocation().getLeft(); //x坐标
                        int y = word.getLocation().getTop();  //y坐标
                        DataString dataString = new DataString();
                        dataString.setItemString(itemString);
                        dataString.setX(x);
                        dataString.setY(y);
                        daBaiduList.add(dataString);
                        Log.e("百度xy", x+"  "+y);
                        Log.e("每个字段和字符数",itemString+"  "+itemString.length());
                    }

                    for(int i=0;i<daBaiduList.size();i++){
                        baiduRs.add(daBaiduList.get(i).getItemString());
                    }

                    if(baiduRs.size()==0){
                        setResult("null","null");
                    }
                    synchronized (str1){
                        str1.notifyAll();
                    }

                    publishProgress("正在处理");

                    Log.e("TAG", "baidu "+Thread.currentThread().getName() );
                }
                @Override
                public void onError(OCRError error) {
                    // 调用失败，返回OCRError对象
                    setResult(error.getMessage(),error.getMessage());
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
                        daTengxuduList.add(da);
                        Log.e("腾讯坐标xy",itemsBean.getItemcoord().getX()+"  "+itemsBean.getItemcoord().getY());
                        Log.w("腾讯每个字段及其字符数",itemString+"  "+itemString.length());
                    }
                    //对于dataList里面的Data字符串数据进行分类分行，和百度一样，同一行y坐标相差10以内的归类在一起
                    List<List<DataString>> listOfList = handleDataList( daTengxuduList,2);
                    //归类以后在进行排序，因为腾讯在按他的识别结果出来的行显示时会出现各种乱序。所以还要排序
                    sortData(listOfList);
                }else {
                    Log.e("错误",string);
                }


            }catch (Exception e){e.printStackTrace();}

            publishProgress("正在处理");

            try {
                //同步
                synchronized (str1){
                    if(baiduRs.size()==0){
                        if( !"null".equals(getResult()) ){
                            str1.wait();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            if(("null").equals(getResult())){
                Log.e("getResult",getResult().toString());
                return getResult();
            }else {
                publishProgress("识别完成");
                compareListString(baiduRs,tengxuRs);
                Iterator<String> iterator1 = baiduRs.iterator();
                StringBuffer stringBuffer1 = new StringBuffer();
                while(iterator1.hasNext()){
                    stringBuffer1.append(iterator1.next()+"<br/>");
                }

                Iterator<String> iterator2 = tengxuRs.iterator();
                StringBuffer stringBuffer2 = new StringBuffer();
                while(iterator2.hasNext()){
                    stringBuffer2.append(iterator2.next()+"<br/>");
                }

                String string1 = stringBuffer1.toString();
                String string2 = stringBuffer2.toString();

                Log.e("stringbuffer",string1);
                setResult("<html><body>"+string1+"</body></html>",string2);
                Log.e("getResult",getResult().toString());
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
                setResult(result,"");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Log.e("getResult",getResult().toString());
        publishProgress("识别完成");
        return getResult();

    }

    @Override
    public void onProgressUpdate(String... values){
        mProgressDialog.setMessage(values[0]);
    }

    @Override
    public void onPostExecute(List<String> result){
        Log.d("onPostExecute","");
        if(!"".equals(result) && !result.equals("null")){
            listener.updateResult(result.get(0),result.get(1));
        }else{
            listener.updateResult("识别出错，请重试！","");
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
    public void setResult(String result1,String result2){
        str1 = result1;
        str2 = result2;
    }
    public List<String> getResult(){
        List<String> list = new ArrayList<>();
        list.add(str1);
        list.add(str2);
        return list;
    }


    public void compareListString(List<String> baiduRs,List<String> tengxuRs){
        int n = baiduRs.size()-tengxuRs.size();
        //判断谁的行数多，有时候最后一行一两个字，腾讯的才能识别出来
        if(n<0){
            //腾讯识别行数多时，百度全部校验，否则百度校验少n个
            n = 0;
        }
        for(int i=0;i<baiduRs.size()-n;i++){
            //各自每一行数据
            String a =baiduRs.get(i);
            String b =tengxuRs.get(i);
            Log.e("长度",a.length()+"   "+b.length());
            //比较时去除不重要的标点符号干扰
            String tempA = a.replaceAll("[\\p{Punct}\\p{Space}]+","");
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
                Log.e("a与b",a+"  长度相同内容比较  "+b);
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
     *对腾讯/百度返回的结果进行分类，根据y坐标相差10判断字段为同一行
     */
    public List<List<DataString>> handleDataList(List<DataString> list,int or){
        List<List<DataString>> listList = new ArrayList<>();
        quickSort(0,list.size()-1,list,or);

        List<DataString> l = new ArrayList<>();
        listList.add(l);
        int temp = list.get(0).getXY(or);

        for(int i=0;i<list.size();i++){
            if(Math.abs(temp - list.get(i).getXY(or))<=20){
                l.add(list.get(i));
            }else{
                temp = list.get(i).getXY(or);
                l = new ArrayList<>();
                listList.add(l);
                l.add(list.get(i));
            }
        }
        Log.e("分类结果",listList.size()+"行\n");
        return listList;
    }


    /**
     * 对归类也就是分行后的数据进行排序，按照x坐标大小
     */
    public void sortData(List<List<DataString>> lists){
        for (int i=0;i<lists.size();i++){
            List<DataString> stringList = lists.get(i);
            quickSort(0,stringList.size()-1,stringList,1);

            StringBuffer stringBuffer = new StringBuffer();
            for(int m=0;m<stringList.size();m++){
                stringBuffer.append(stringList.get(m).getItemString());
            }
            String rs = stringBuffer.toString();
            Log.e( "sortData",rs );
            tengxuRs.add(rs);
        }
    }

    public void quickSort(int s,int e,List<DataString> list,int or){
        if(s>=e) {return;}
        int i = s;
        int j = e;
        while(i<j){
            while(i<j && list.get(i).getXY(or)<=list.get(j).getXY(or)){
                i++;
            }
            while(i<j && list.get(j).getXY(or)>list.get(i).getXY(or)){
                j--;
            }
            if(i<j){
                swap(i,j,list);
            }
        }
        if(list.get(j).getXY(or)<list.get(s).getXY(or)){
            swap(s,j,list);
        }
        quickSort(s,j-1,list,or);
        quickSort(j+1,e,list,or);
    }

    public void swap(int i,int j,List<DataString> list){
        DataString temp = list.get(i);
        list.set(i,list.get(j));
        list.set(j,temp);
    }

    public interface UpdateListener{
        void updateResult(String result1,String result2);
    }

    public void registerlistener(UpdateListener l){
        listener = l;
    }

    public void unregisterListener(){
        listener = null;
    }

}
