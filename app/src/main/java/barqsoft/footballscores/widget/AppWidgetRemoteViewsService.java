package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;

/**
 * Created by jacob on 9/11/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppWidgetRemoteViewsService extends RemoteViewsService{

    public final String LOG_TAG = AppWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
    };

    // these indices must match the projection
    static final int INDEX_HOME_COL = 0;
    static final int INDEX_HOME_GOALS_COL = 1;
    static final int INDEX_AWAY_COL = 2;
    static final int INDEX_AWAY_GOALS_COL = 3;
    static final int INDEX_MATCH_DAY = 4;
    static final int INDEX_MATCH_ID = 5;
    static final int INDEX_DATE_COL = 6;
    static final int INDEX_TIME_COL = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        Log.v(LOG_TAG, "OnGetViewFactory called");
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do

            }

            @Override
            public void onDataSetChanged() {
                Log.v(LOG_TAG, "OnDataSetChanged called");
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
//                String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri scoresForTodayUri = DatabaseContract.scores_table.buildScoreWithDate();
                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

                //Log.v(LOG_TAG, date.toString());
                Date now = new Date();

                data = getContentResolver().query(scoresForTodayUri,
                        MATCH_COLUMNS,
                        null,
                        new String[]{date.format(now)},
                        DatabaseContract.scores_table.DATE_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
                //Log.v(LOG_TAG, data.toString());
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.v(LOG_TAG, "getViewAt called");
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                String homeName = data.getString(INDEX_HOME_COL);
                String homeCrest = data.getString(INDEX_HOME_COL);
                String homeScore = data.getString(INDEX_HOME_GOALS_COL);
                String awayName = data.getString(INDEX_AWAY_COL);
                String awayCrest = data.getString(INDEX_AWAY_COL);
                String awayScore = data.getString(INDEX_AWAY_GOALS_COL);
                String matchTime = data.getString(INDEX_TIME_COL);
                String matchDay = data.getString(INDEX_MATCH_DAY);
                String matchDate = data.getString(INDEX_DATE_COL);
                String matchId = data.getString(INDEX_MATCH_ID);

                //Log.v(LOG_TAG, matchId);

                int homeCrestId = Utility.getTeamCrestByTeamName(homeCrest);
                int awayCrestId = Utility.getTeamCrestByTeamName(awayCrest);

//                Bitmap homeCrestImage = null;
//                if ( !Utility.usingLocalGraphics(AppWidgetRemoteViewsService.this) ) {
//                    String weatherArtResourceUrl = Utility.getArtUrlForWeatherCondition(
//                            AppWidgetRemoteViewsService.this, homeCrest);
//                try {
//                    homeCrestImage = Glide.with(AppWidgetRemoteViewsService.this)
//                            .load(homeCrestId)
//                            .asBitmap()
//                            .error(R.drawable.no_icon)
//                            .into(views();
//                } catch (InterruptedException | ExecutionException e) {
//                    Log.e(LOG_TAG, "Error retrieving large icon from " + weatherArtResourceUrl, e);
//                }
                views.setImageViewResource(R.id.home_crest, homeCrestId);
                views.setImageViewResource(R.id.away_crest, awayCrestId);;

                if (homeScore.equals("-1")) {
                    views.setTextViewText(R.id.home_score, "0");
                } else {
                    views.setTextViewText(R.id.home_score, homeScore);
                }

                if (awayScore.equals("-1")) {
                    views.setTextViewText(R.id.away_score, "0");
                } else {
                    views.setTextViewText(R.id.away_score, awayScore);
                }

                views.setTextViewText(R.id.next_or_time_remaining, matchTime);
                views.setTextViewText(R.id.home_name, homeName);
                views.setTextViewText(R.id.away_name, awayName);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, homeName + " image", awayName + " image");
                }

                final Intent fillInIntent = new Intent();

                int selectedMatch = 0;

                try {
                    selectedMatch = Integer.parseInt(matchId);
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }

                //String selectedMatch = matchId.;

                //fillInIntent.putExtra(selectedMatch, "selectedMatch");


//                String locationSetting =
//                        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
//                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                        locationSetting,
//                        dateInMillis);
//                fillInIntent.setData(weatherUri);

                Bundle bundle = new Bundle();
                bundle.putInt(AppWidgetProvider.EXTRA_MATCH_ID, selectedMatch);
                bundle.putInt(AppWidgetProvider.EXTRA_POSITION, position);

                fillInIntent.putExtras(bundle);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                //Log.v(LOG_TAG, "From widget: " + fillInIntent.getStringExtra("matchId"));
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String homeName, String awayName) {
                views.setContentDescription(R.id.home_crest, homeName);
                views.setContentDescription(R.id.away_crest, awayName);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
