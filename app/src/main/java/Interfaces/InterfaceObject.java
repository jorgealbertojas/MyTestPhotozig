package Interfaces;

import java.util.List;

import common.ListWrapper;
import common.Utility;
import models.Assets;
import models.Objects;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by jorge on 30/11/2017.
 * Interface Assets for support Json
 */
public interface InterfaceObject {

    /** Get Data Assets Json with Retrofit */
    @GET(Utility.COMPLEMENT_URL)
    Call<ListWrapper<Objects>> getObject();
}
