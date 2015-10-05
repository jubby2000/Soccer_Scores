package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.myFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public final String LOG_TAG = MainScreenFragment.class.getSimpleName();
    public scoresAdapter mAdapter;
    private ListView mScoreList;
    private int mPosition = ListView.INVALID_POSITION;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;

    public MainScreenFragment()
    {
    }

    private void update_scores()
    {
        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
    }
    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        if (getActivity().getIntent().getExtras() != null) {
            MainActivity.selected_match_id = getActivity().getIntent().getIntExtra("clickedMatchId", 0);
            mPosition = getActivity().getIntent().getIntExtra("clickedMatchPosition", ListView.INVALID_POSITION);
        }
        //String thisOne = AppWidgetProvider.EXTRA_MATCH_ID;
        //Log.v(LOG_TAG, "From MainScreenFragment onCreateView: "+ thisOne);
        update_scores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mScoreList = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);
        mScoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);

        Log.v(LOG_TAG, "From main: " + String.valueOf(getActivity().getIntent().getIntExtra("clickedMatchId", 0)));

//        if (MainActivity.selected_match_id == 0) {
//            ViewHolder fromWidget = (ViewHolder) getView().getTag();
//            mAdapter.detail_match_id = fromWidget.match_id;
//            mAdapter.notifyDataSetChanged();
//        }

        Log.v(LOG_TAG, "Total list items: " + mScoreList.getCount());

        mAdapter.detail_match_id = MainActivity.selected_match_id;

        mScoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();

            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,fragmentdate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        Log.v(LOG_TAG, "onLoadFinished called.");

        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);

        Log.v(LOG_TAG, "mScoreList count is: " + mScoreList.getCount() + ". From on load finished: " + cursor.getCount() + ". And mPosition is: " + mPosition + ". And MainActivity.selected_match_id is: " + MainActivity.selected_match_id);
        if (MainActivity.selected_match_id != 0 && mPosition != ListView.INVALID_POSITION) {
            if (Build.VERSION.SDK_INT > 10) {
                mScoreList.smoothScrollToPositionFromTop(mPosition, 0, 2000);
            } else {
                mScoreList.setSelection(mPosition);
            }
        }
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
