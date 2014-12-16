package jp.co.njc.wearablenoticemail;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

/**
 * Created by njc50031 on 2014/12/16.
 */
public class LabelListFragment extends ListFragment {

    public interface ItemClickedListener {
        void onItemClicked(int position);
    }

    private ItemClickedListener mClickListener = null;

    public LabelListFragment() {
        super();
    }

    public void setItemClickedListener(ItemClickedListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mClickListener != null) {
            mClickListener.onItemClicked(position);
        }
    }
}
