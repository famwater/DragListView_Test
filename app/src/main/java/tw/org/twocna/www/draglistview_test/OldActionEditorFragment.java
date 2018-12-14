package tw.org.twocna.www.draglistview_test;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by water on 2018/12/14.
 */
public class OldActionEditorFragment extends Fragment {
    //-- Log --
    private static final String TAG = OldActionEditorFragment.class.getSimpleName();

    //-- Fragment --
    public static final String FRAGMENT_TYPE = "old_action_editor";

    //-- UI --
    private ViewGroup mUI_DragLayout;
    private static final int LIST_IDs[] = {R.id.uiListLeft, R.id.uiListRight};
    private RecyclerView[] mUI_List = {null, null};
    private LinearLayoutManager[] mUI_Manager = {null, null};
    private List<List<String>> mItemData = new ArrayList<>();

    //-- Drag & Drop --
    private static final String DRAG_MIME_TYPE = "com.epep.drag/action";
    private int mDragging_Column;
    private int mDragging_Position;
    private boolean mIsDragging = false;
    private int mTouchX;
    private int mTouchY;

    private Thread mDragThread;
    private boolean mTaskRunning = false;

    //========================
    //== Constructor 建構子  ==
    //========================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
        }

        if (savedInstanceState != null) {
            //mMapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_old_action_editor, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //-- Lock Rotation --
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //-- Initialize UI --
        initializeUI_DragList();
        initializeUI_Drag();
    }

    //=====================
    //==  Initialize UI  ==
    //=====================
    private void initializeUI_DragList() {
        for(int inx=0; inx<LIST_IDs.length; inx++){
            final int columnInx = inx;

            //-- Create Test Data --
            ArrayList<String> oData = new ArrayList<>();
            mItemData.add(columnInx, oData);
            for (int i = 0; i < 30; i++) {
                oData.add("Item_" + columnInx + "-" + i);
            }

            //-- RecyclerView --
            RecyclerView uiList = (RecyclerView) getView().findViewById(LIST_IDs[columnInx]);
            uiList.setMotionEventSplittingEnabled(false); //禁止多點觸發
            uiList.setTag(columnInx);
            mUI_List[columnInx] = uiList;

            //-- Layout Manager --
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            uiList.setLayoutManager(layoutManager);
            mUI_Manager[columnInx] = layoutManager;

            //-- ItemDecoration --
            DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            uiList.addItemDecoration(decoration);

            //-- Add Adapter --
            uiList.setAdapter(new RecyclerView.Adapter<ActionDragHolder>() {
                @NonNull
                @Override
                public ActionDragHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    int resource = R.layout.adapter_item_action_drag;
                    View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                    return new ActionDragHolder(view, viewType);
                }

                @Override
                public void onBindViewHolder(@NonNull ActionDragHolder holder, int position) {
                    holder.columnInx = columnInx;
                    holder.uiTestText.setText(mItemData.get(columnInx).get(position));
                    holder.uiHolderLayout.setTag(position);
                }

                @Override
                public int getItemCount() {
                    return mItemData.get(columnInx).size();
                }
            });
        }
    }

    private void initializeUI_Drag(){
        mUI_DragLayout = (ViewGroup) getView().findViewById(R.id.uiDragLayout);
        mUI_DragLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                boolean handled = false;
                ClipDescription clipDesc = dragEvent.getClipDescription();
                mTouchX = (int)dragEvent.getX();
                mTouchY = (int)dragEvent.getY();

                switch (dragEvent.getAction()) {
                    //-- Step 01 : ACTION_DRAG_STARTED --
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.d(TAG, "onDrag: ACTION_DRAG_STARTED");
                        Log.d(TAG, "onDrag: mDragging_Column = " + mDragging_Column);
                        Log.d(TAG, "onDrag: mDragging_Position = " + mDragging_Position);
                        if (clipDesc!=null && clipDesc.hasMimeType(DRAG_MIME_TYPE)) {
                            mIsDragging = true;
                            handled = true;
                            //此時的mTouchXY不知為何不准(parent coordinate?)
                            //Log.d(TAG, "onDrag: Start touch X/Y=" + mTouchX + "/" + mTouchY);
                            onDragStart();
                        }
                        break;

                    //-- Step 02 : ACTION_DRAG_ENTERED --
                    case DragEvent.ACTION_DRAG_ENTERED:
                        //Log.d(TAG, "onDrag: ACTION_DRAG_ENTERED");
                        //mIsShift = false;
                        //onAutoScroll();
                        handled = true;
                        break;

                    //-- Step 03 : ACTION_DRAG_LOCATION --
                    case DragEvent.ACTION_DRAG_LOCATION:
                        //Log.d(TAG, "onDrag: DragEvent.ACTION_DRAG_LOCATION ");
                        //Log.d(TAG, "onDrag: location touch X/Y=" + mTouchX + "/" + mTouchY);
                        //onDragLocation();
                        handled = false;
                        break;

                    //-- Step 04 : ACTION_DRAG_EXITED --
                    case DragEvent.ACTION_DRAG_EXITED:
                        //Log.d(TAG, "onDrag: ACTION_DRAG_EXITED");
                        //Log.d(TAG, "onDrag: DragEvent.ACTION_DRAG_EXITED " + msg);
                        handled = true;
                        break;

                    //-- Step 05 : ACTION_DROP --
                    case DragEvent.ACTION_DROP:
                        Log.d(TAG, "onDrag: ACTION_DROP");
//                        if (clipDesc!=null && clipDesc.hasMimeType(DRAG_MIME_TYPE)) {
//                            onDrop();
//
//                        }
                        handled = true;
                        break;

                    //-- Step 06 : ACTION_DRAG_ENDED --
                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(TAG, "onDrag: ACTION_DRAG_ENDED");
                        mIsDragging = false;
                        //mIsShift = false;
                        onDragEnd();
                        handled = dragEvent.getResult();
                        break;

                    default:
                        break;
                }

                return handled;
            }
        });
    }

    //====================
    //==== ViewHolder ====
    //====================
    public class ActionDragHolder extends RecyclerView.ViewHolder {
        //-- UI --
        private ViewGroup uiHolderLayout;
        private TextView uiTestText;
        public int columnInx;

        public ActionDragHolder(View uiView_Root, int viewType) {
            super(uiView_Root);
            //-- Find UI --
            uiHolderLayout = (ViewGroup) uiView_Root.findViewById(R.id.uiHolderLayout);
            uiTestText = (TextView) uiView_Root.findViewById(R.id.uiTestText);

            uiHolderLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //itemView.setVisibility(View.INVISIBLE);
                    //ViewHolder.this.itemView.setAlpha(0.3f);
                    int position = getLayoutPosition();
                    item_LongClick(position, uiHolderLayout, columnInx);
                    return true;
                }
            });
        }
    }

    private void item_LongClick(int position, View dragView, int columnInx) {
        if(mIsDragging){
            return;
        }

        mDragging_Column = columnInx;
        mDragging_Position = position;
        //-- https://developer.android.com/guide/topics/ui/drag-drop.html
        //-- Create a new ClipData --
        //-- create a plain text ClipData in one step
        //ClipData data = ClipData.newPlainText("", "");
        ClipData.Item item = new ClipData.Item("{'type':'action'}");
        String[] mimeTypes = {DRAG_MIME_TYPE};
        ClipData dragData = new ClipData("drag_action", mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(dragView);

        Pair<Integer, Integer> itemData = new Pair<>(columnInx, position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //-- ClipData data, View.DragShadowBuilder shadowBuilder, Object myLocalState, int flags
            mUI_DragLayout.startDragAndDrop(dragData, shadowBuilder, itemData, 0); //-- API level 24
        } else {
            mUI_DragLayout.startDrag(dragData, shadowBuilder, itemData, 0); //API level 11
        }
    }

    //===================
    //==  Drag & Drop  ==
    //===================
    private void onDragStart_A(){
        if(mDragThread==null){
            mDragThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mIsDragging && !Thread.interrupted()) {
                        try {
                            Thread.sleep(20);
                            //-- start actions in UI thread
                            mUI_DragLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    // this action have to be in UI thread
                                    onItemChange();
                                }
                            });
                        } catch (InterruptedException e) {
                            // ooops
                        }
                    }
                }
            });
        }
        mDragThread.start();
    }

    private void onDragEnd_A(){
        mDragThread.interrupt();
        mDragThread = null;
    }

    private int mCurrentPosition = -1;
    private void onDragLocation(){
        //-- Find Selected RecyclerView --
        int layoutWidth = (int)(mUI_DragLayout.getWidth()*0.5);
        int columnInx = 0;
        if(mTouchX > layoutWidth) {
            columnInx = 1;
        }

        onAutoScroll(columnInx);

        //-- Find Visible Item Position --
        int firstVisPos = mUI_Manager[columnInx].findFirstVisibleItemPosition();
        int lastVisPos = mUI_Manager[columnInx].findLastVisibleItemPosition();
        //Log.d(TAG, "onDragLocation: Vis pos = " + firstVisPos + " / " + lastVisPos);

        //
        int top = mUI_List[columnInx].getTop();
        int hitY = mTouchY - top;
        //Log.d(TAG, "onDragLocation: mTouchY=" + mTouchY + " ,top="+top + ", hitY="+hitY);

        //Rect boundRect = new Rect();
        for(int i = firstVisPos; i<=lastVisPos; i++) {
            ActionDragHolder holder = (ActionDragHolder)mUI_List[columnInx].findViewHolderForAdapterPosition(i);
            if (holder == null) {
                Log.d(TAG, "updateItemPosition: unable to get child " + i + " in " + firstVisPos + " / " + lastVisPos);
                continue;
            }

            //-- returns the visible bounds --
            //childView.getDrawingRect(boundRect);
            int childTop = holder.itemView.getTop();
            int childBottom = holder.itemView.getBottom();
            if(hitY>=childTop && hitY<=childBottom){
                Log.d(TAG, "onDragLocation: mTouchY=" + mTouchY + " ,top="+top + ", hitY="+hitY);
                Log.d(TAG, "onDragLocation: (" + i + ") " + childTop + " / " + childBottom + " => Hit");
                Log.d(TAG, "onDragLocation: " + holder.uiTestText.getText());
            } else {
                //Log.d(TAG, "onDragLocation: (" + i + ") " + childTop + " / " + childBottom + " => Miss");
            }
        }

    }

    private void onItemChange(){
        //-- Find Selected RecyclerView --
        int layoutWidth = (int)(mUI_DragLayout.getWidth()*0.5);
        int columnInx = 0;
        if(mTouchX > layoutWidth) {
            columnInx = 1;
        }

        //-- Find Visible Item Position --
        int firstVisPos = mUI_Manager[columnInx].findFirstVisibleItemPosition();
        int lastVisPos = mUI_Manager[columnInx].findLastVisibleItemPosition();
        int top = mUI_List[columnInx].getTop();
        int hitY = mTouchY - top;



        for(int i = firstVisPos; i<=lastVisPos; i++) {
            ActionDragHolder holder = (ActionDragHolder)mUI_List[columnInx].findViewHolderForAdapterPosition(i);
            if (holder == null) {
                Log.d(TAG, "updateItemPosition: unable to get child " + i + " in " + firstVisPos + " / " + lastVisPos);
                continue;
            }

            //-- returns the visible bounds --
            int childTop = holder.itemView.getTop();
            int childBottom = holder.itemView.getBottom();
            if(hitY>=childTop && hitY<=childBottom){
                //Log.d(TAG, "onDragLocation: mTouchY=" + mTouchY + " ,top="+top + ", hitY="+hitY);
                //Log.d(TAG, "onDragLocation: (" + i + ") " + childTop + " / " + childBottom + " => Hit");
                //Log.d(TAG, "onDragLocation: " + holder.uiTestText.getText());
                if(mDragging_Column!=columnInx){
                    //-- Update Data --
                    String tempData = mItemData.get(mDragging_Column).remove(mDragging_Position);
                    mItemData.get(columnInx).add(i, tempData);

                    //-- Update UI --
                    mUI_List[mDragging_Column].getAdapter().notifyItemRemoved(mDragging_Position);
                    mUI_List[columnInx].getAdapter().notifyItemInserted(i);

                    //-- Update New Item Position --
                    mDragging_Column = columnInx;
                    mDragging_Position = i;
                } else {
                    if(mDragging_Position!=i){
                        //-- Update Data --
                        Collections.swap(mItemData.get(columnInx), mDragging_Position, i);

                        //-- Update UI --
                        mUI_List[columnInx].getAdapter().notifyItemMoved(mDragging_Position, i);

                        //-- Update New Item Position --
                        mDragging_Position = i;
                    }
                }

                break;
            } else {
                //Log.d(TAG, "onDragLocation: (" + i + ") " + childTop + " / " + childBottom + " => Miss");
            }
        }

        //-- Auto Scroll --
        onAutoScroll(columnInx);
    }

    private void onAutoScroll(int columnInx){
        int autoScrollRange = 40;
        int limitUp = mUI_List[columnInx].getTop() + autoScrollRange;
        int limitDown = mUI_List[columnInx].getBottom() - autoScrollRange;
        int offsetY = 0;
        if(mTouchY<limitUp){
            //Log.d(TAG, "onAutoScroll: Auto scroll up " + mTouchY + "/" + limitUp);
            offsetY = -30;//(int)((mTouchY-limitUp)*0.3);
            mUI_List[columnInx].scrollBy(0, offsetY);
        } else if(mTouchY>limitDown) {
            //Log.d(TAG, "onAutoScroll: Auto scroll Down " + mTouchY + "/" + limitDown);
            offsetY = 30;//(int)((mTouchY-limitDown)*0.3);
            mUI_List[columnInx].scrollBy(0, offsetY);
        }

    }

    //===================
    //==  Drag & Drop  ==
    //===================
    private void onDragStart(){
        if(!mTaskRunning){
            mDragging_Column = findDragColumn();
            mDragging_Position = findDragPosition(mDragging_Column);
            mTaskRunning = true;
            Log.d(TAG, "onDragStart: Column/Position = " + mDragging_Column + " / " + mDragging_Position);
            new ItemUpdateTask().execute();
        }
    }

    private void onDragEnd(){
        mTaskRunning = false;
    }

    private int findDragColumn(){
        //-- Find Selected RecyclerView --
        int layoutWidth = (int)(mUI_DragLayout.getWidth()*0.5);
        int columnInx = 0;
        if(mTouchX > layoutWidth) {
            columnInx = 1;
        }
        return columnInx;
    }

    private int findDragPosition(int columnInx){
        //-- Find Visible Item Position --
        int firstVisPos = mUI_Manager[columnInx].findFirstVisibleItemPosition();
        int lastVisPos = mUI_Manager[columnInx].findLastVisibleItemPosition();
        int top = mUI_List[columnInx].getTop();
        int hitY = mTouchY - top;

        int position = -1;

        View hit_view = mUI_List[columnInx].findChildViewUnder(mTouchX, hitY);

        int viewPos = -1;
        if(hit_view!=null) {
            //ActionDragHolder holder = (ActionDragHolder)mUI_List[columnInx].findContainingViewHolder(hit_view);
            //viewPos = (int)hit_view.getTag();
            viewPos = mUI_List[columnInx].getChildAdapterPosition(hit_view);
        }
        Log.d(TAG, "findDragPosition: hit_view pos = " + viewPos);

//        for(int i = firstVisPos; i<=lastVisPos; i++) {
//            ActionDragHolder holder = (ActionDragHolder)mUI_List[columnInx].findViewHolderForAdapterPosition(i);
//            //ActionDragHolder holder = (ActionDragHolder)mUI_List[columnInx].findViewHolderForLayoutPosition(i);
//            if (holder == null) {
//                Log.d(TAG, "updateItemPosition: unable to get child " + i + " in " + firstVisPos + " / " + lastVisPos);
//                continue;
//            }
//
//            //-- returns the visible bounds --
//            int childTop = holder.itemView.getTop();
//            int childBottom = holder.itemView.getBottom();
//            if(hitY>=childTop && hitY<=childBottom){
//                position = i;
//                break;
//            }
//        }
//
//        Log.d(TAG, "findDragPosition : find column = " + columnInx + " / Position = " + position + ", F/L = " +
//                firstVisPos + "/" + lastVisPos);

        return viewPos;
    }

    private class ItemUpdateTask extends AsyncTask<Void, Integer, Void> {
        protected Void doInBackground(Void... params) {
            while (mTaskRunning) {
//                int dragColumn = findDragColumn();
//                int dragPosition = findDragPosition(dragColumn);
//                if(dragPosition>=0) {
//                    publishProgress(dragColumn, dragPosition);
//                }
                // Escape early if cancel() is called
                if (isCancelled()) break;

                try {
                    Thread.sleep(30);
                    publishProgress(1);
                } catch (InterruptedException e) {
                    // ooops
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            int dragColumn = findDragColumn();
            int dragPosition = findDragPosition(dragColumn); //progress[1];

            if(dragPosition<0){
                return;
            }

            if(mDragging_Column!=dragColumn){
                //-- Update Data --
                String tempData = mItemData.get(mDragging_Column).remove(mDragging_Position);
                mItemData.get(dragColumn).add(dragPosition, tempData);

                //-- Update UI --
                mUI_List[mDragging_Column].getAdapter().notifyItemRemoved(mDragging_Position);
                mUI_List[dragColumn].getAdapter().notifyItemInserted(dragPosition);

                //-- Update New Item Position --
                mDragging_Column = dragColumn;
                mDragging_Position = dragPosition;

                //--修正
                if(dragPosition==0){
                    mUI_List[dragColumn].scrollToPosition(0);
                    mUI_List[dragColumn].getAdapter().notifyDataSetChanged();
                }
            } else {
                if(mDragging_Position!=dragPosition){
                    Log.d(TAG, "onProgressUpdate : Position=> " + mDragging_Position + " to " + dragPosition);

                    // figure out the position of the first visible item
                    int firstPos = mUI_Manager[dragColumn].findFirstCompletelyVisibleItemPosition();
                    int offsetTop = 0;
                    if(firstPos >= 0) {
                        View firstView = mUI_Manager[dragColumn].findViewByPosition(firstPos);
                        offsetTop = mUI_Manager[dragColumn].getDecoratedTop(firstView) - mUI_Manager[dragColumn].getTopDecorationHeight(firstView);
                    }


                    //-- Update Data --
                    Collections.swap(mItemData.get(dragColumn), mDragging_Position, dragPosition);
                    //-- Update UI --
                    //View view1St = mUI_Manager[dragColumn].findViewByPosition(mDragging_Position);
                    //View view2nd = mUI_Manager[dragColumn].findViewByPosition(dragPosition);
                    //view1St.setTag(dragPosition);
                    //view2nd.setTag(mDragging_Position);
                    mUI_List[dragColumn].getAdapter().notifyItemMoved(mDragging_Position, dragPosition);

                    if(firstPos >= 0){
                        Log.d(TAG, "onProgressUpdate : firstPos = " + firstPos);
                        mUI_Manager[dragColumn].scrollToPositionWithOffset(firstPos, offsetTop);
                    }

                    //-- Update New Item Position --
                    mDragging_Position = dragPosition;
                }
            }

//            mUI_DragLayout.post(new Runnable() {
//                @Override
//                public void run() {
//                    // this action have to be in UI thread
//                    onAutoScroll(dragColumn);
//                }
//            });

        }

        protected void onPostExecute(Void result) {
            mTaskRunning = false;
            Log.d(TAG, "onPostExecute: Stop");
        }
    }

}
