package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by jacob on 9/11/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppWidgetProvider extends android.appwidget.AppWidgetProvider{
    public final String LOG_TAG = AppWidgetProvider.class.getSimpleName();
    public static final String EXTRA_MATCH_ID = "barqsoft.footballscores.widget.EXTRA_MATCH_ID";
    public static final String EXTRA_POSITION = "barqsoft.footballscores.widget.EXTRA_POSITION";
    public static final String UPDATE_ACTION = "barqsoft.footballscores.widget.UPDATE_ACTION";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        Log.v(LOG_TAG, "OnUpdate called");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Set up the collection
            Intent remoteIntent = new Intent(context, AppWidgetRemoteViewsService.class);
            remoteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

//            boolean useAltActivity = context.getResources()
//                    .getBoolean(R.bool.use_alt_activity);

            Intent clickIntentTemplate = new Intent(context, AppWidgetProvider.class);//.putExtra("thisThing", AppWidgetRemoteViewsService.class.);
//            useAltActivity
//                    ?
//                    : new Intent(context, MainActivity.class);

//            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
//                    .addNextIntentWithParentStack(clickIntentTemplate)
//                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            clickIntentTemplate.setAction(UPDATE_ACTION);
            PendingIntent clickPendingIntentTemplate = PendingIntent.getBroadcast(context, 0, clickIntentTemplate,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Log.v(LOG_TAG, "From AppWidgetProvider: " + String.valueOf(MainActivity.selected_match_id));

            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        Log.v(LOG_TAG, "onReceive called");

        Log.v(LOG_TAG, "intent is: " + intent.getAction());
        if (intent.getAction().equals(UPDATE_ACTION)) {
            int matchId = intent.getExtras().getInt(AppWidgetProvider.EXTRA_MATCH_ID);
            int position = intent.getExtras().getInt(AppWidgetProvider.EXTRA_POSITION);

            //if (SyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            //int extraThing = intent.getIntExtra(AppWidgetProvider.EXTRA_MATCH_ID, 3);
            Log.v(LOG_TAG, "This is some thing: " +String.valueOf(matchId));
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            Intent mainIntent = new Intent(context, MainActivity.class)
                    .putExtra("clickedMatchId", matchId)
                    .putExtra("clickedMatchPosition", position);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Log.v(LOG_TAG, "setRemoteAdapter called");
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, AppWidgetRemoteViewsService.class));
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, AppWidgetRemoteViewsService.class));
    }
}
