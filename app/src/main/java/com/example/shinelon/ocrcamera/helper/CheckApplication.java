package com.example.shinelon.ocrcamera.helper;

import android.app.Application;
import android.util.Log;

import com.abbyy.mobile.ocr4.AssetDataSource;
import com.abbyy.mobile.ocr4.DataSource;
import com.abbyy.mobile.ocr4.Engine;
import com.abbyy.mobile.ocr4.FileLicense;
import com.abbyy.mobile.ocr4.License;
import com.abbyy.mobile.ocr4.RecognitionConfiguration;
import com.abbyy.mobile.ocr4.RecognitionLanguage;
import com.abbyy.mobile.ocr4.RecognitionManager;
import com.example.shinelon.ocrcamera.R;
import com.squareup.leakcanary.LeakCanary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Shinelon on 2017/9/12.
 */

public class CheckApplication extends Application {
    public static Engine engine;
    public static RecognitionManager manager;
    @Override public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

        final DataSource assetDataSrouce = new AssetDataSource( this.getAssets() );
        final List<DataSource> dataSources = new ArrayList<>();
        dataSources.add( assetDataSrouce );

        Engine.loadNativeLibrary();
        try {
           engine = Engine.createInstance( dataSources, new FileLicense( assetDataSrouce,
                           "license", "com.example.shinelon.ocrcamera" ),
                    new Engine.DataFilesExtensions( ".mp3",
                            ".mp3",
                            ".mp3" ) );
            RecognitionConfiguration configuration = new RecognitionConfiguration();
            RecognitionLanguage language_zn = RecognitionLanguage.ChineseSimplified;
            Set<RecognitionLanguage> languages = new HashSet<>();
            languages.add(RecognitionLanguage.English);
            if(engine.isLanguageAvailableForOcr(language_zn)){
                languages.add(language_zn);
            }
            configuration.setRecognitionLanguages(languages);
            manager = engine.getRecognitionManager(configuration);

        } catch(Exception e ) {
            e.printStackTrace();
            Log.w("License",e.getMessage());
        }
    }

    public static RecognitionManager getManager(){
        return manager;
    }
}
