package adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jorge.mytestphotozig.DetailActivity;
import com.example.jorge.mytestphotozig.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import downloads.DownloadService;
import models.Objects;

import static common.Utility.EXTRA_DATA;
import static common.Utility.EXTRA_POSITION;
import static common.Utility.KEY_EXTRA_DATA;
import static common.Utility.PERMISSION_REQUEST_CODE;

/**
 * Created by jorge on 30/11/2017.
 * Adapter for support Recycler View OBJECT
 */

public class AdapterObject extends RecyclerView.Adapter<AdapterObject.AdapterObjectViewHolder> {

    private List<Objects> data;
    private String mUrlImage;
    private Context mContext;

    private static AdapterObjectOnClickHandler mClickHandler;



    /**
     * The interface that receives onClick messages.
     */
    public interface AdapterObjectOnClickHandler {
        void onClick(Objects objects);
    }

    /**
     * Creates a ForecastAdapter.
     */
    public AdapterObject(AdapterObjectOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }


    public AdapterObject(ArrayList<Objects> data, String urlImage) {

        this.mUrlImage = urlImage;
        this.data = data;
    }

    public AdapterObject(ArrayList<Objects> data) {

       this.data = data;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class AdapterObjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_objects_im)
        ImageView mImImageView;

        @BindView(R.id.tv_object_name)
        TextView mNameTextView;

        @BindView(R.id.acb_download)
        AppCompatButton mDownloadAppCompatButton;

        @BindView(R.id.acb_detail)
        AppCompatButton mDetail;




        public AdapterObjectViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);

            view.setOnClickListener(this);
        }

/*        *//**
         * This gets called by the child views during a click.
         *//*
        @Override
        public void onClick(View v) {
            ButterKnife.bind((Activity) v.getContext());
            int adapterPosition = getAdapterPosition();
            Objects objects = data.get(adapterPosition);
            mClickHandler.onClick(objects);
        }*/




        @OnClick({R.id.acb_download, R.id.acb_detail })
        public void onClick(View v) {


            if (v.getId()== R.id.acb_detail){
                Intent mediaIntent = new Intent(mContext, DetailActivity.class);
                mediaIntent.putExtra(EXTRA_POSITION,v.getTag().toString());
                Bundle bundle = new Bundle();
                bundle.putSerializable(KEY_EXTRA_DATA, (Serializable) data);
                mediaIntent.putExtra(EXTRA_DATA, bundle);
                mContext.startActivity(mediaIntent);

            }else {
                if (checkPermission()) {
                    startDownload(data.get(Integer.parseInt(v.getTag().toString())).getSg());
                    startDownload(data.get(Integer.parseInt(v.getTag().toString())).getBg());
                } else {
                    requestPermission();
                }
            }

        }




    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     */
    @Override
    public AdapterObject.AdapterObjectViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_assets, viewGroup, false);
        mContext = viewGroup.getContext();
        return new AdapterObject.AdapterObjectViewHolder(v);
    }


    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     */
    @Override
    public void onBindViewHolder(AdapterObjectViewHolder holder, int position) {
        Objects objects = ((Objects) data.get(position));

        Picasso.with(mContext)
                .load(mUrlImage +"/"+ objects.getIm())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.mImImageView);

        holder.mNameTextView.setText(objects.getName());
        holder.mDownloadAppCompatButton.setTag(position);
        holder.mDetail.setTag(position);
    }


    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     */
    @Override
    public int getItemCount() {
        if (null == data) return 0;
        return data.size();
    }

    public List<Objects> getData() {
        return data;
    }



    /**
     * Call screen the permission for download
     */
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;
        }
    }

    private void requestPermission(){

        ActivityCompat.requestPermissions((Activity) mContext,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);

    }

    /**
     * Call intent the Download with put extra
     */
    private void startDownload(String fileName){

        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(EXTRA_POSITION,fileName);
        mContext.startService(intent);

    }



}