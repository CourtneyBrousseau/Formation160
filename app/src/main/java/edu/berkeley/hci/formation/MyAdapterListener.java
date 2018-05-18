package edu.berkeley.hci.formation;

import android.view.View;

/**
 * Created by daphnenhuch on 4/11/18.
 */
//A class that allows the buttons within a card to be clickable

public interface MyAdapterListener {



    void clickImage(View v, int position);

    void moreInfo(View v, int position);
}
