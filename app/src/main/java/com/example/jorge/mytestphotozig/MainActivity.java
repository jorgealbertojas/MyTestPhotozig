package com.example.jorge.mytestphotozig;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Parcelable;
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
import Interfaces.InterfaceObject;
import adapters.AdapterObject;
import common.FunctionCommon;
import common.ListWrapper;
import common.Utility;
import models.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static common.Utility.PERMISSION_REQUEST_CODE;


public class MainActivity extends AppCompatActivity implements AdapterObject.AdapterObjectOnClickHandler {

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private final String KEY_ADAPTER_STATE = "adapter_state";
    private final String KEY_PATH_IMAGE = "path_image";


    AdapterObject mAdapterObject;
    private InterfaceObject mInterfaceObject;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private Context mContext;

    private static Bundle mBundleRecyclerViewState;
    private ArrayList<Objects> mListAdapterObject;
    private Parcelable mListState;
    private String mPathImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

         /** If savedInstanceState was Salve no necessary execute again */
        if (savedInstanceState == null) {
        /* Init Recycle View for list ASSERTS */
            initRecyclerView();

            /* set Adapter for Recycler view */
            mAdapterObject = new AdapterObject(this);

           /* Create Bundle for salve state the activity */
            mBundleRecyclerViewState = new Bundle();
            Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
            mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

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
        }else {
            initRecyclerView();

            mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mListAdapterObject = (ArrayList<Objects>) mBundleRecyclerViewState.getSerializable(KEY_ADAPTER_STATE);
            mPathImage =  mBundleRecyclerViewState.getString(KEY_PATH_IMAGE);

            mRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
            mAdapterObject = new AdapterObject(mListAdapterObject, mPathImage);
            mRecyclerView.setAdapter(mAdapterObject);
        }
    }

    /**
     * Call Get InformationNew OBJECTS .
     */
    private Callback<ListWrapper<Objects>> objectCallback = new Callback<ListWrapper<Objects>>() {
        @Override
        public void onResponse(Call<ListWrapper<Objects>> call, Response<ListWrapper<Objects>> response) {
            try {
                if (response.isSuccessful()) {
                    ArrayList<Objects> data = new ArrayList<>();

                    data.addAll(response.body().objects);
                    mPathImage = response.body().assetsLocation;

                    mAdapterObject= new AdapterObject(data,mPathImage);
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

    /**
     * Start Recycler view
     */
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


    // Calc dynamic quantity column RecyclerView
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


    // save RecyclerView state
    @Override
    protected void onPause() {
        super.onPause();
        mBundleRecyclerViewState = new Bundle();
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mListAdapterObject = (ArrayList<Objects>) mAdapterObject.getData();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);
        mBundleRecyclerViewState.putSerializable(KEY_ADAPTER_STATE, mListAdapterObject);
        mBundleRecyclerViewState.putString(KEY_PATH_IMAGE, mPathImage);
    }


    // restore RecyclerView state
    @Override
    protected void onResume() {
        super.onResume();

        if (mBundleRecyclerViewState != null) {
            mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mListAdapterObject = (ArrayList<Objects>) mBundleRecyclerViewState.getSerializable(KEY_ADAPTER_STATE);

        }
    }

    @Override
    public void  onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {


                }
                break;
        }
    }


}
