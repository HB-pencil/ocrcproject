package com.example.shinelon.ocrcamera.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.shinelon.ocrcamera.SecondActivity;

import java.io.FileOutputStream;

public class AsyncProcessTask extends AsyncTask<String, String, Boolean> {

    private ProgressDialog dialog;
    /** application context. */
    private final SecondActivity activity;

    public AsyncProcessTask(SecondActivity activity) {
        this.activity = activity;
        dialog = new ProgressDialog(activity);
    }


    protected void onPreExecute() {
        dialog.setMessage("处理中");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    protected void onPostExecute(Boolean result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        activity.updateResult(result);
    }

    @Override
    protected Boolean doInBackground(String... args) {

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            Client restClient = new Client();
            restClient.applicationId = "chenxiaoyan";
            // You should get e-mail from ABBYY Cloud OCR SDK service with the application password
            restClient.password = "jEn6ZNoAT8iN1ri8Zp/jseu3";

            publishProgress( "正在上传图片...");

            String language = "ChinesePRC,English"; // Comma-separated list: Japanese,English or German,French,Spanish etc.

            ProcessingSettings processingSettings = new ProcessingSettings();
            processingSettings.setOutputFormat( ProcessingSettings.OutputFormat.txt );
            processingSettings.setLanguage(language);

            publishProgress("上传中...");

            Task task = restClient.processImage(inputFile, processingSettings);

            while( task.isTaskActive() ) {
                Thread.sleep(5000);
                publishProgress( "等待中..." );
                task = restClient.getTaskStatus(task.Id);
            }

            if( task.Status == Task.TaskStatus.Completed ) {
                publishProgress( "正在获取结果..." );
                FileOutputStream fos = activity.openFileOutput(outputFile,Context.MODE_PRIVATE);

                try {
                    restClient.downloadResult(task, fos);
                } finally {
                    fos.close();
                }

                publishProgress( "准备完成" );
            } else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
                throw new Exception( "Not enough credits to process task. Add more pages to your application's account." );
            } else {
                throw new Exception( "Task failed" );
            }

            return true;
        } catch (Exception e) {
            final String message = "Error: " + e.getMessage();
            publishProgress( message);
            activity.displayMessage(message);
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String stage = values[0];
        dialog.setMessage(stage);
    }

}
