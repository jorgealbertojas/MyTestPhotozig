package adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jorge.mytestphotozig.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import models.Objects;

/**
 * Created by jorge on 30/11/2017.
 * Adapter for support Recycler View OBJECT
 */

public class AdapterObject extends RecyclerView.Adapter<AdapterObject.AdapterObjectViewHolder> {

    private List<Objects> data;
    private String mUrlImage;
    private Context mContext;

    /**
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
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


    public AdapterObject(List<Objects> data, String urlImage) {

        this.mUrlImage = urlImage;
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


        public AdapterObjectViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         */
        @Override
        public void onClick(View v) {
            ButterKnife.bind((Activity) v.getContext());
            int adapterPosition = getAdapterPosition();
            Objects objects = data.get(adapterPosition);
            mClickHandler.onClick(objects);
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

}