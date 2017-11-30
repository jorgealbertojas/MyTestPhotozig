package com.example.jorge.mytestphotozig;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import Interfaces.InterfaceObject;
import adapters.AdapterObject;
import common.FunctionCommon;
import common.ListWrapper;
import common.Utility;
import models.Assets;
import models.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements AdapterObject.AdapterObjectOnClickHandler {

    AdapterObject mAdapterObject;
    private InterfaceObject mInterfaceObject;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        /* Init Recycle View for list ASSERTS */
        initRecyclerView();

        mAdapterObject = new AdapterObject(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mAdapterObject);

        /* Setting the Load in our layout. */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);


        /* Verify Internet before open Json. */
        if (FunctionCommon.isOnline(this)) {
            createStackOverflowAPI();
            mInterfaceObject.getObject().enqueue(objectCallback);

        } else {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.Error_Access, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Call Get InformationNew ASSETS .
     */
    private Callback<ListWrapper<Objects>> objectCallback = new Callback<ListWrapper<Objects>>() {
        @Override
        public void onResponse(Call<ListWrapper<Objects>> call, Response<ListWrapper<Objects>> response) {
            try {
                if (response.isSuccessful()) {
                    List<Objects> data = new ArrayList<>();

                    data.addAll(response.body().objects);
                    String urlImage = response.body().assetsLocation;

                    mAdapterObject= new AdapterObject(data,urlImage);
                    mRecyclerView.setAdapter(mAdapterObject);
                    mLoadingIndicator.setVisibility(View.INVISIBLE);

                } else {
                    Log.d("QuestionsCallback", "Code: " + response.code() + " Message: " + response.message());
                }
            } catch (NullPointerException e) {
                System.out.println("onActivityResult consume crashed");
                runOnUiThread(new Runnable() {
                    public void run() {

                        Context context = getApplicationContext();
                        Toast toast = Toast.makeText(context, R.string.Error_Access_empty, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        }

        @Override
        public void onFailure(Call<ListWrapper<Objects>> call, Throwable t) {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.Error_Access_empty, Toast.LENGTH_SHORT);
            toast.show();
        }

    };


    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_numbers);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns()));
        mRecyclerView.setHasFixedSize(true);
        mAdapterObject = new AdapterObject(this);
    }

    /**
     * Find Data the API Json with Retrofit
     */
    private void createStackOverflowAPI() {
        Gson gson = new GsonBuilder()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Utility.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        mInterfaceObject = retrofit.create(InterfaceObject.class);
    }


    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }


    @Override
    public void onClick(Objects objects) {

    }
}
