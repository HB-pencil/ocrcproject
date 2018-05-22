package com.example.shinelon.ocrcamera.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.ArrayMap;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
    private UpdateListener listener;
    private  Request request;
    private OkHttpClient client;
    private File file;
    private final Integer shareFlag = 1000;
    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str1 ="";
    private static String str2 ="";


    public AsycProcessTask(ProgressDialog d,String imageUrl){
        file = new File(imageUrl);
        mProgressDialog = d;
        if(!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }
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
        publishProgress("开始处理");
        //开启百度识别
        if (CheckApplication.isNotNativeRecognize){
            baiduRecognize();
        }
    }

    @Override
    public List<String> doInBackground(String... args){
        if (CheckApplication.isNotNativeRecognize){
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
                        itemString = itemString.replaceAll("[\\s]+","").trim();
                        DataString da = new DataString();
                        da.setItemString(itemString);
                        da.setX(itemsBean.getItemcoord().getX());
                        da.setY(itemsBean.getItemcoord().getY());
                        daTengxuduList.add(da);
                        Log.e("腾讯坐标xy",itemsBean.getItemcoord().getX()+"  "+itemsBean.getItemcoord().getY());
                        Log.w("腾讯每个字段及其字符数",itemString+"  "+itemString.length());
                    }
                    //对于dataList里面的Data字符串数据进行分类分行，和百度一样，同一行y坐标相差10以内的归类在一起
                    List<List<DataString>> lists = handleDataList( daTengxuduList,2);
                    //归类以后在进行排序，因为腾讯在按他的识别结果出来的行显示时会出现各种乱序。所以还要排序
                    sortData(lists,2);
                }else {
                    Log.e("错误",string);
                }
            }catch (Exception e){e.printStackTrace();}
            publishProgress("正在处理");
            try {
                //同步
                synchronized (shareFlag){
                    if(daBaiduList.size()==0){
                        if( !"null".equals(getResult()) ){
                            shareFlag.wait(20000);
                        }
                    }
                    shareFlag.notifyAll();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            //百度分行，因为距离远会被分开
            if(daBaiduList.size()>0){
                List<List<DataString>> lists =  handleDataList(daBaiduList,1);
                sortData(lists,1);
            }
            if(baiduRs.size()==0){
                setResult("null","null");
            }
            publishProgress("正在处理");

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
        if(!"".equals(result) && !result.equals("null")){
            listener.updateResult(result.get(0),result.get(1));
        }else{
            listener.updateResult("识别出错，请重试！","");
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
        Log.e("FINISH","------------------dialog-------------------"+mProgressDialog.isShowing());
    }
    private void setResult(String result1,String result2){
        str1 = result1;
        str2 = result2;
    }
    public List<String> getResult(){
        List<String> list = new ArrayList<>();
        list.add(str1);
        list.add(str2);
        return list;
    }


    private void compareListString(List<String> baiduRs,List<String> tengxuRs){
        //比较两者都有返回结果的行
        int n = Math.min(baiduRs.size(),tengxuRs.size());
        for(int i=0;i<n;i++){
            //各自每一行数据
            String a =baiduRs.get(i);
            String b =tengxuRs.get(i);
            Log.e("长度B&T",a.length()+"   "+b.length());
            //比较时去除不重要的标点符号干扰

            String tempA = a.replaceAll("\\s*\\p{Punct}\\s*","");
            String tempB = b.replaceAll("\\s*\\p{Punct}\\s*","");

            Log.w("temBaidu&temTengxu ",tempA+"\n"+tempB );
            if(tempA.equalsIgnoreCase(tempB)){
                Log.e("比较","两者相等");
            }else if(tempA.length()==tempB.length())
            {   //不相等但是长度一样
                a = insertMark1(tempA,tempB,a);
                b = insertMark1(tempB,tempA,b);
                baiduRs.set(i,a);
                tengxuRs.set(i,b);
                Log.e("a与b",a+"  长度相同内容比较  "+b);
            } else if(tempA.length()>tempB.length()){
                //不相等长度B>T
                a = insertMark2(tempA,tempB,a);
                baiduRs.set(i,a);
                Log.e("a与b",a+"  a长度>b长度  "+b);
            }else {
                //不相等长度B<T
                b = insertMark2(tempB,tempA,b);
                tengxuRs.set(i,b);
                Log.e("a与b",a+"  a长度<b长度  "+b);
            }
        }
    }

    private String insertMark1(String tempA,String tempB,String a){
        ArrayMap<Character,Integer> map = new ArrayMap<>();
        for(int k=0;k<tempA.length();k++){
            char t = tempA.charAt(k);
            if(map.containsKey(t)){
                map.put(t,map.get(t)+1);
            }else{
                map.put(t,1);
            }
            if(t!=tempB.charAt(k)){
                int count = map.get(t);
                int index = -1;
                int start = 0;
                for(int m=0;m<count;m++){
                    index = a.indexOf(t,start);
                    if(index>=0) {
                        start = index + 1;
                    }
                }
                StringBuilder sb = new StringBuilder(a);
                sb.replace(index,index+1,"<font color=\"#ff0000\">"+t+"</font>");
                a = sb.toString();
            }
        }
        return a;
    }

    private String insertMark2(String tempA,String tempB,String a){
        ArrayMap<Character,Integer> map = new ArrayMap<>();
        int n=0;
        for(int k=0;k<tempA.length()&&(k-n)<tempB.length();k++){
            char t = tempA.charAt(k);
            if(map.containsKey(t)){
                map.put(t,map.get(t)+1);
            }else{
                map.put(t,1);
            }
            if(t!=tempB.charAt(k-n)){
                int count = map.get(t);
                int index = -1;
                int start = 0;
                for(int m=0;m<count;m++){
                    index = a.indexOf(t,start);
                    if(index>=0) {
                        start = index + 1;
                    }
                }
                StringBuilder sb = new StringBuilder(a);
                sb.replace(index,index+1,"<font color=\"#ff0000\">"+t+"</font>");
                a = sb.toString();
                n++;
            }
        }
        return a;
    }


    /**
     *对腾讯/百度返回的结果进行分类，根据y坐标相差10判断字段为同一行
     */
    private List<List<DataString>> handleDataList(List<DataString> list,int or){
        List<List<DataString>> listList = new ArrayList<>();
        //对返回结果集按y坐标值进行排序，从第一个开始遍历，按照一定的阈值将后面的归类。直到找到不满足，继续过程
        quickSort(0,list.size()-1,list,2,or);
        List<DataString> l1 = new ArrayList<>();
        listList.add(l1);
        List<DataString> current = l1;
        int temp = list.get(0).getXY(2);
        int point = (or==1)?70:20;
        for(int i=0;i<list.size();i++){
            if(Math.abs(temp - list.get(i).getXY(2))<=point){
                current.add(list.get(i));
            }else{
                temp = list.get(i).getXY(2);
                List<DataString> l2 = new ArrayList<>();
                listList.add(l2);
                current = l2;
                current.add(list.get(i));
            }
        }
        Log.e("分类结果",listList.size()+"行\n");
        return listList;
    }


    /**
     *
     * @param lists
     * @param or  or==1 表示百度/or==2表示腾讯
     */
    private void sortData(List<List<DataString>> lists,int or){
        for (int i=0;i<lists.size();i++){
            List<DataString> stringList = lists.get(i);
            quickSort(0,stringList.size()-1,stringList,1,or);

            StringBuffer stringBuffer = new StringBuffer();
            for(int m=0;m<stringList.size();m++){
                stringBuffer.append(stringList.get(m).getItemString());
            }
            String rs = stringBuffer.toString();
            Log.e( "sortData",rs );
            if(or==1) {baiduRs.add(rs);}
            else {tengxuRs.add(rs);}
        }
    }

    private void quickSort(int s,int e,List<DataString> list,int flag,int or){
        if(s>=e||(or==1)&&flag==2) {return;}
        int i = s+1;
        int j = e;
        while(i<j){
            while(i<j && list.get(i).getXY(flag)<=list.get(j).getXY(flag)){
                i++;
            }
            while(i<j && list.get(j).getXY(flag)>list.get(i).getXY(flag)){
                j--;
            }
            if(i<j){
                swap(i,j,list);
            }
        }
        if(list.get(j).getXY(flag)<list.get(s).getXY(flag)){
            swap(s,j,list);
        }
        quickSort(s,j-1,list,flag,or);
        quickSort(j+1,e,list,flag,or);
    }

    private void baiduRecognize(){
        // 这一部分是百度通用文字识别参数设置(集成的sdk)
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setImageFile(file);
        // 调用百度通用文字识别服务
        OCR.getInstance().recognizeGeneral(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                 new Thread(()->{
                     // 调用成功，返回GeneralResult对象，注意此方法UI线程回调
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
                     synchronized (shareFlag){
                         if(tengxuRs.size()==0){
                             try {
                                 shareFlag.wait(20000);
                             }catch (Exception e){
                                 e.printStackTrace();
                             }
                         }
                         shareFlag.notifyAll();
                     }
                 }).start();
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                setResult(error.getMessage(),error.getMessage());
                synchronized (shareFlag){
                    shareFlag.notifyAll();
                }
            }
        });
    }

    private void swap(int i,int j,List<DataString> list){
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
