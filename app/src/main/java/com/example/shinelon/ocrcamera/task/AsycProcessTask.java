package com.example.shinelon.ocrcamera.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private volatile boolean flagB = false;
    private volatile boolean flagT = false;
    private long time;
    /**
     * str
     * 不能为null，不然会报错，因为子线程返回值必须为String
     */
    private static String str1 ="";
    private static String str2 ="";
    private static int baidu =0;
    private static int tengxu =0;


    public AsycProcessTask(ProgressDialog d,String imageUrl){
        file = new File(imageUrl);
        mProgressDialog = d;
        if(!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }
    }


    @Override
    public void onPreExecute(){
        time = System.currentTimeMillis();
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
            publishProgress("正在处理");
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
                if(response.code()==200){
                    TentcentRs tentcentRs = JSONObject.parseObject(string,TentcentRs.class);
                    List<TentcentRs.DataBean.ItemsBean> list = tentcentRs.getData().getItems();
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext()){
                        TentcentRs.DataBean.ItemsBean itemsBean = (TentcentRs.DataBean.ItemsBean) iterator.next();
                        String itemString = itemsBean.getItemstring();
                        itemString = itemString.replaceAll("[\\s]+", "").trim();
                        DataString da = new DataString();
                        da.setItemString(itemString);
                        da.setX(itemsBean.getItemcoord().getX());
                        da.setBottomY(itemsBean.getItemcoord().getY()+itemsBean.getItemcoord().getHeight()/2);
                        daTengxuduList.add(da);
                        if(tengxu==0){
                            tengxu=itemsBean.getItemcoord().getHeight()/2;
                        }
                        Log.e("腾讯坐标xywh",itemsBean.getItemcoord().getX()+"\n"+itemsBean.getItemcoord().getY()
                        +"\n"+itemsBean.getItemcoord().getWidth()+"\n"+itemsBean.getItemcoord().getHeight());
                        Log.w("腾讯每个字段及其字符数",itemString+"  "+itemString.length());
                    }
                    //对于dataList里面的Data字符串数据进行分类分行，和百度一样，同一行y坐标相差10以内的归类在一起
                    List<List<DataString>> lists = handleDataList( daTengxuduList,2);
                    //归类以后在进行排序，因为腾讯在按他的识别结果出来的行显示时会出现各种乱序。所以还要排序
                    showtData(lists,2);
                    flagT = true;
                }else {
                    Log.e("错误",string);
                }
            }catch (Exception e){e.printStackTrace();}
            publishProgress("正在处理");
            try {
                //同步
                synchronized (shareFlag){
                    if(!flagB){
                        if( !"null".equals(getResult()) ){
                            Log.e("TENGXUN","wait");
                            shareFlag.wait(60000);
                        }
                    }
                    shareFlag.notifyAll();
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
                if(string1.length()>0&&string2.length()>0){
                    setResult("<html><body>"+string1+"</body></html>","<html><body>"+string2+"</body></html>");
                }else if(string1.length()>0){
                    setResult("<html><body>"+string1+"</body></html>",
                            "<html><body>some error has happened! <br/>  ⊙﹏⊙∥  <br/>请保证截取内容没有多余干扰</body></html>");
                }else if(string2.length()>0){
                    setResult("<html><body>some error has happened! <br/>  ⊙﹏⊙∥  <br/>请保证截取内容没有多余干扰</body></html>",
                            "<html><body>"+string2+"</body></html>");
                }else {
                    setResult("<html><body>some error has happened! <br/>  ⊙﹏⊙∥  <br/>请保证截取内容没有多余干扰</body></html>",
                            "<html><body>ssome error has happened! <br/>  ⊙﹏⊙∥  <br/>请保证截取内容没有多余干扰</body></html>");
                }

                Log.e("getResult",getResult().toString());
                Log.e("两者list字段各自长度",baiduRs.size()+"    "+tengxuRs.size());
                return getResult();
            }
            //以下为本地识别
        }else {
        /**
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
         */
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
        long current = (System.currentTimeMillis() - time)/1000;
    }
    private void setResult(String result1,String result2){
        str1 = result1;
        str2 = result2;
    }
    public List<String> getResult(){
        List<String> list = new ArrayList<>();
        list.add(str2);
        list.add(str1);
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
            }else{
                Log.e("比较","两者不等");
                List<String> list = insertMark(tempA,tempB,a,b);
                baiduRs.set(i,list.get(0));
                tengxuRs.set(i,list.get(1));
            }
        }
    }

    /**
     * 字符串差异标记算法
     * @param tempA  经过处理清除干扰的字符串——上边第一个
     * @param tempB  经过处理清除干扰的字符串——下边第二个
     * @param a 原始字符串上边
     * @param b 原始字符串下边
     * @return 原始字符串处理后的标记结果
     */
    private List<String> insertMark(String tempA,String tempB,String a,String b){
        //a,b标记次数
        int mFlag1=0;
        int mFlag2=0;
        //存储结果的集合
        List<String> list = new ArrayList<>();
        //代替HashMap节约内存，ArrayMap双数组基于二分查找，key为tempA/B的字符，value为次数
        ArrayMap<Character,Integer> map1 = new ArrayMap<>();
        ArrayMap<Character,Integer> map2 = new ArrayMap<>();
        //tempA,tempB两者的指针（下标）
        int i=0;
        int j=0;
        //tempA，tempB中各个字符出现的次数
        int count1=0;
        int count2=0;

        //循环对比两者的每一个字符
        while(i<tempA.length()&&j<tempB.length()){
            char t1 = tempA.charAt(i);
            char t2 = tempB.charAt(j);
            Integer res1=map1.get(t1);
            Integer res2 =map2.get(t2);
            if(res1!=null){
                map1.put(t1,res1+1);
            }else {
                map1.put(t1,1);
            }
            if(res2!=null){
                map2.put(t2,res2+1);
            }else {
                map2.put(t2,1);
            }
            //不相等
            if(t1!=t2 && !String.valueOf(t1).equalsIgnoreCase(String.valueOf(t2))){
                //不相等开始的索引
                int index1 = -1;
                //后面又相等了的索引
                int start1 = 0;
                int index2 = -1;
                int start2 = 0;

                //字符出现的次数
                count1=map1.get(t1);
                //插入标记后出现的次数
                count1=caCulateCount(count1,t1,mFlag1);
                count2=map2.get(t2);
                count2=caCulateCount(count2,t2,mFlag2);

                //原始字符串中跳过重复定位到最新字符
                for(int k=0;k<count1;k++){
                    index1 = a.indexOf(t1,start1);
                    if(index1>=0) {
                        start1 = index1 + 1;
                    }
                }
                for(int k=0;k<count2;k++){
                    index2 = b.indexOf(t2,start2);
                    if(index2>=0) {
                        start2= index2 + 1;
                    }
                }
                Log.d("index1、2",index1+" "+index2);

                //是否定位到
                boolean isFind=false;
                //定位到后的最后相等字符的标记
                int lastIndex=-1;
                //定位不到后最后字符的标记
                int nothing= -1;
                //定位到
                int var1=i;
                for(int n=1;n<5;n++){
                    i=var1+n;
                    if(i<tempA.length()){
                        char current = tempA.charAt(i);
                        Log.d("范围判断",current+" "+i);
                        res1=map1.get(current);
                        if(res1!=null){
                            map1.put(current,res1+1);
                        }else {
                            map1.put(current,1);
                        }
                        nothing=i;
                        //定位tempB的末尾
                        int index = tempB.indexOf(current,j);
                        Log.d("tempB-index",index+"");
                        //在5个字符的范围内
                        if(index-j<=4&&index-j>=0){
                            Log.d("char相等",current+" "+tempB.charAt(index));
                            //次数计算
                            int count=map1.get(current);
                            count = caCulateCount(count,current,mFlag1);
                            Log.d("count",count+"次");
                            //定位a的末尾相等字符的索引
                            int start=0;
                            for(int k=0;k<count;k++){
                                lastIndex = a.indexOf(current,start);
                                start = lastIndex+1;
                            }
                            StringBuilder sb1 = new StringBuilder(a);
                            Log.d("INDEXA",index1+" "+lastIndex);
                            try{
                                //标记
                                sb1.replace(index1,lastIndex,"<font color=\"#ff0000\">"+a.substring(index1,lastIndex)+"</font>");
                            }catch (StringIndexOutOfBoundsException e){
                                list.add(a);
                                list.add(b);
                                return list;
                            }
                            a=sb1.toString();
                            mFlag1++;
                            //定位b的末尾字符，一定是在index2后首次出现
                            int end2 = b.indexOf(current,index2);
                            //具有不同字符
                            if(end2-index2>0){
                                StringBuilder sb2 = new StringBuilder(b);
                                Log.d("INDEXB",index1+" "+lastIndex);
                                try{
                                    //标记
                                    sb2.replace(index2,end2,"<font color=\"#ff0000\">"+b.substring(index2,end2)+"</font>");
                                }catch (StringIndexOutOfBoundsException e){
                                    list.add(a);
                                    list.add(b);
                                    return list;
                                }
                                b=sb2.toString();
                                mFlag2++;
                            }
                            int var2 = j;
                            for (int k=1;var2+k<=index;k++){
                                j=var2+k;
                                char c = tempB.charAt(j);
                                res2=map2.get(c);
                                if(res2!=null){
                                    map2.put(c,res2+1);
                                }else {
                                    map2.put(c,1);
                                }
                            }
                            isFind=true;
                            break;
                        }
                    }else{
                        //数组越界
                        nothing=i-1;
                        break;
                    }
                }
                //范围内没有定位到
                if(!isFind){
                    //未定位到，最后一个字符
                    char last = tempA.charAt(nothing);
                    Integer rs = map1.get(last);
                    int start=0;
                    //定位a末尾索引
                    for(int k=0;k<rs;k++){
                        lastIndex = a.indexOf(last,start);
                        start = lastIndex+1;
                    }
                    StringBuilder sb1 = new StringBuilder(a);
                    Log.d("NotFind-replaceA",index1+" "+start);
                    try{
                        //标记
                        sb1.replace(index1,start,"<font color=\"#ff0000\">"+a.substring(index1,start)+"</font>");
                    }catch (StringIndexOutOfBoundsException e){
                        list.add(a);
                        list.add(b);
                        return list;
                    }
                    a=sb1.toString();
                    mFlag1++;
                    Log.d("NotFind",a);

                    StringBuilder sb2 = new StringBuilder(b);
                    int distance = lastIndex-index1;
                    //定位b末尾索引
                    int end2 = index2+distance;
                    end2=end2<b.length()?end2+1:b.length();
                    Log.d("NotFind-replaceB",index2+" "+end2);
                    try{
                        //标记
                        sb2.replace(index2,end2,"<font color=\"#ff0000\">"+b.substring(index2,end2)+"</font>");
                    }catch (StringIndexOutOfBoundsException e){
                        list.add(a);
                        list.add(b);
                        return list;
                    }
                    b=sb2.toString();
                    mFlag2++;
                    Log.d("NotFind",b);
                    //上面只变化了i的值
                    j=j+4;
                }
            }
            Log.d("结果",a+"\n"+b);
            //遍历前进
            i++;
            j++;
        }
        list.add(a);
        list.add(b);
        return list;
    }


    /**
     * 计算插入后的跳过次数
     */
    private int caCulateCount(int count,char t,int mFlag){
        switch (t){
            case '0':return count+mFlag*4;
            case 'f':return count+mFlag*4;
            case 'o':return count+mFlag*4;
            case 'n':return count+mFlag*2;
            case 't':return count+mFlag*2;
            case '#':return count+mFlag;
            case 'c':return count+mFlag;
            case 'l':return count+mFlag;
            case 'r':return count+mFlag;
            case '=':return count+mFlag;
            default:return count;
        }
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
        int point = (or==1)?baidu:tengxu;
        for(int i=0;i<list.size();i++){
            if(Math.abs(temp - list.get(i).getXY(2))<point){
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
    private void showtData(List<List<DataString>> lists,int or){
        for (int i=0;i<lists.size();i++){
            List<DataString> stringList = lists.get(i);
            quickSort(0,stringList.size()-1,stringList,1,or);
            StringBuffer stringBuffer = new StringBuffer();
            for(int m=0;m<stringList.size();m++){
                stringBuffer.append(stringList.get(m).getItemString());
            }
            String rs = stringBuffer.toString();
            Log.e( "showData",rs );
            if(or==1) {baiduRs.add(rs);}
            else {tengxuRs.add(rs);}
        }
    }

    private void quickSort(int s,int e,List<DataString> list,int flag,int or){
        if(s>=e||or==1) {return;}
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
        Log.e("调用BAIDU","执行");
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
                         dataString.setBottomY(y+word.getLocation().getHeight()/2);
                         daBaiduList.add(dataString);
                         if(baidu==0){
                             baidu=word.getLocation().getHeight()/3;
                         }
                         Log.e("百度xywh", x+"\n"+y+"\n"+word.getLocation().getWidth()+"\n"+word.getLocation().getHeight());
                         Log.e("每个字段和字符数",itemString+"  "+itemString.length());
                     }
                     //百度分行，因为距离远会被分开
                     if(daBaiduList.size()>0){
                         List<List<DataString>> lists =  handleDataList(daBaiduList,1);
                         showtData(lists,1);
                         flagB = true;
                     }
                     if(baiduRs.size()==0){
                         setResult("null","null");
                     }
                     synchronized (shareFlag){
                         if(!flagT){
                             try {
                                 Log.e("BAIDU","wait");
                                 shareFlag.wait(60000);
                             }catch (Exception e) {
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
                flagB = true;
                Log.e("BAIDU",error.getMessage());
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
