package downloads;

import android.app.IntentService;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.example.jorge.mytestphotozig.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Interfaces.InterfaceDownload;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Retrofit;

import static common.Utility.BASE_URL_IMAGE;
import static common.Utility.BASE_URL_IMAGE_COMPLEMENT;
import static common.Utility.EXTRA_DOWNLOAD;
import static common.Utility.EXTRA_POSITION;
import static common.Utility.EXTRA_POSITION_NUMBER;
import static common.Utility.MESSAGE_PROGRESS;
import static common.Utility.TAG_INFORMATION;

/**
 * Created by jorge on 01/12/2017.
 * The class DownloadService extends IntentService. Necessary case, creates a new thread to do work and destroys as soon as the work is finished
 * The ResponseBody object is passed to downloadFile() method which starts the download.
 * The downloaded file is stored in Downloads directory. */

public class DownloadService extends IntentService  {

    public static Intent mIntent;

    public DownloadService() {
        super("Download Service");
    }

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int mTotalFileSize;


    /** Show progress download in Notification Service*/
    @Override
    protected void onHandleIntent(Intent intent) {

        this.mIntent = intent;

        String fileName = intent.getStringExtra(EXTRA_POSITION);
        String position = intent.getStringExtra(EXTRA_POSITION_NUMBER);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true);
        mNotificationManager.notify(0, mNotificationBuilder.build());

        initDownload(fileName, position);

    }

    /** Init download with name file*/
    private void initDownload(String fileName, String position) {

        Log.i(TAG_INFORMATION,"initDownload");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_IMAGE)
                .build();

        InterfaceDownload retrofitInterface = retrofit.create(InterfaceDownload.class);

        Call<ResponseBody> request = retrofitInterface.downloadFile(BASE_URL_IMAGE_COMPLEMENT +  fileName);
        try {

            downloadFile(request.execute().body(), fileName, position);

        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    /** Show progress download in Notification Service*/
    private void downloadFile(ResponseBody body, String fileName, String position) throws IOException {
        Log.i(TAG_INFORMATION,fileName);

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            mTotalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(mTotalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download, position);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete(position);
        output.flush();
        output.close();
        bis.close();

    }


    /** Send Notification Service for progress*/
    private void sendNotification(Download download, String position) {

        sendIntent(download, position);
        mNotificationBuilder.setProgress(100, download.getProgress(), false);
        mNotificationBuilder.setContentText("Downloading file " + download.getCurrentFileSize() + "/" + mTotalFileSize + " MB");
        mNotificationManager.notify(0, mNotificationBuilder.build());
    }

    /** Send Notification Service for progress*/
    private void sendIntent(Download download, String position) {

        Intent intent = new Intent(MESSAGE_PROGRESS);
        intent.putExtra(EXTRA_DOWNLOAD, download);
        intent.putExtra(EXTRA_POSITION_NUMBER, position);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    /** Download finished and notification finished*/
    private String onDownloadComplete(String position) {

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download, position);

        mNotificationManager.cancel(0);
        mNotificationBuilder.setProgress(0, 0, false);
        mNotificationBuilder.setContentText("File Downloaded");
        mNotificationManager.notify(0, mNotificationBuilder.build());

        Log.i(TAG_INFORMATION,"onDownloadComplete");

        return "finished";

    }

    /** Remove task Notification Service*/
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mNotificationManager.cancel(0);
    }
}