package com.grokkingandroid.samplesapp.samples.recyclerviewdemo;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeTransform;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.ItemClickSupport.OnItemClickListener;
import org.lucasr.twowayview.ItemClickSupport.OnItemLongClickListener;
import org.lucasr.twowayview.ItemSelectionSupport;
import org.lucasr.twowayview.ItemSelectionSupport.ChoiceMode;
import org.lucasr.twowayview.TwoWayView;

import java.util.Date;
import java.util.List;

public class RecyclerViewDemoActivity
        extends Activity
        implements View.OnClickListener,
        ActionMode.Callback {

    TwoWayView recyclerView;
    ItemSelectionSupport itemSelection;
    RecyclerViewDemoAdapter adapter;
    int itemCount;
    GestureDetectorCompat gestureDetector;
    ActionMode actionMode;
    ImageButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setAllowExitTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());

        setContentView(R.layout.activity_recyclerview_demo);
        fab = (ImageButton) findViewById(R.id.fab_add);
        recyclerView = (TwoWayView) findViewById(R.id.recyclerView);
        itemSelection = ItemSelectionSupport.addTo(recyclerView);

        ItemClickSupport itemClick = ItemClickSupport.addTo(recyclerView);
        itemClick.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View child, int position, long id) {
                if (actionMode != null) {
                    updateSelectionCount();
                    return;
                }
                DemoModel data = adapter.getItem(position);
                View innerContainer = child.findViewById(R.id.container_inner_item);
                innerContainer.setViewName(Constants.NAME_INNER_CONTAINER + "_" + data.id);
                Activity activity = RecyclerViewDemoActivity.this;
                Intent startIntent = new Intent(activity, CardViewDemoActivity.class);
                startIntent.putExtra(Constants.KEY_ID, data.id);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(activity, innerContainer, Constants.NAME_INNER_CONTAINER);
                activity.startActivity(startIntent, options.toBundle());
            }
        });

        itemClick.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View child, int position, long id) {
                if (actionMode != null) {
                    return false;
                }
                // Start the CAB using the ActionMode.Callback defined above
                actionMode = startActionMode(RecyclerViewDemoActivity.this);
                itemSelection.setChoiceMode(ChoiceMode.MULTIPLE);
                itemSelection.setItemChecked(position, true);
                updateSelectionCount();
                return true;
            }
        });

        // allows for optimizations if all items are of the same size:
        recyclerView.setHasFixedSize(true);

        List<DemoModel> items = RecyclerViewDemoApp.getDemoData();
        adapter = new RecyclerViewDemoAdapter(items);
        recyclerView.setAdapter(adapter);

        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        // fab
        int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
        Outline outline = new Outline();
        outline.setOval(0, 0, size, size);
        View fab = findViewById(R.id.fab_add);
        fab.setOutline(outline);
        fab.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_cardview_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_remove) {
            removeItemFromList();
        }
        return true;
    }

    private void addItemToList() {
        DemoModel model = new DemoModel();
        model.label = "New Item " + itemCount;
        itemCount++;
        model.dateTime = new Date();
        int position = recyclerView.getFirstVisiblePosition();
        // needed to be able to show the animation
        // otherwise the view would be inserted before the first
        // visible item; that is outside of the viewable area
        position++;
        RecyclerViewDemoApp.addItemToList(model, position);
        adapter.addData(model, position);
    }

    private void removeItemFromList() {
        int position = recyclerView.getFirstVisiblePosition();
        RecyclerViewDemoApp.removeItemFromList(position);
        adapter.removeData(position);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_add) {
            // fab click
            addItemToList();
        }
    }

    private void updateSelectionCount() {
        String title = getString(R.string.selected_count, itemSelection.getCheckedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_recyclerviewdemoactivity, menu);
        fab.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                SparseBooleanArray selectedItemPositions = itemSelection.getCheckedItemPositions();
                int currPos;
                for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                    currPos = selectedItemPositions.keyAt(i);
                    RecyclerViewDemoApp.removeItemFromList(currPos);
                    adapter.removeData(currPos);
                }
                actionMode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        itemSelection.clearChoices();
        itemSelection.setChoiceMode(ChoiceMode.NONE);
        fab.setVisibility(View.VISIBLE);
    }
}

