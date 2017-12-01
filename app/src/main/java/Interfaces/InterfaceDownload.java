package Interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by jorge on 01/12/2017.
 * For downloading we use GET method.
 * For downloading large files we need to add @Streaming annotation to Retrofit Interface so that it does not load the complete file into memory.
 * Objective is get Files MP3 and MP4
 */

public interface InterfaceDownload {
    @GET()
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);
}
