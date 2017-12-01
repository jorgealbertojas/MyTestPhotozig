package models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jorge on 30/11/2017.
 * Model for support Json Objects
 */

public class Objects implements Serializable {

    private String name;
    private String bg;
    private String im;
    private String sg;



    private List<Txts> txts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getIm() {
        return im;
    }

    public void setIm(String im) {
        this.im = im;
    }

    public String getSg() {
        return sg;
    }

    public void setSg(String sg) {
        this.sg = sg;
    }

    public List<Txts> getTxts() {
        return txts;
    }

    public void setTxts(List<Txts> txts) {
        this.txts = txts;
    }
}
