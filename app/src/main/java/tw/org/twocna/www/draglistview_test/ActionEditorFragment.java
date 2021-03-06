package tw.org.twocna.www.draglistview_test;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.woxthebox.draglistview.BoardView;
import com.woxthebox.draglistview.DragItem;

import java.util.ArrayList;

/**
 * Created by water on 2018/11/22.
 */
public class ActionEditorFragment extends Fragment {
    //-- Log --
    private static final String TAG = ActionEditorFragment.class.getSimpleName();

    //-- Fragment --
    public static final String FRAGMENT_TYPE = "action_editor";

    //-- UI --
    private BoardView mUI_BoardView;

    //-- Cache Data --
    private static int sCreatedItems = 0;
    private int mColumns;

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

        //-- Options Menu --
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_action_editor, container, false);
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
        //initializeUI_DragList(1);
        //initializeUI_Drag();
    }

    //=====================
    //==  Initialize UI  ==
    //=====================
    private void initializeUI_DragList() {
        mUI_BoardView = getView().findViewById(R.id.uiBoardView);
        mUI_BoardView.setSnapToColumnsWhenScrolling(true);
        mUI_BoardView.setSnapToColumnWhenDragging(true);
        mUI_BoardView.setSnapDragItemToTouch(true);
        mUI_BoardView.setCustomDragItem(new DragItem_Action(getActivity(), R.layout.column_item));
        mUI_BoardView.setCustomColumnDragItem(new DragItem_Daily(getActivity(), R.layout.drag_item_daily));
        mUI_BoardView.setSnapToColumnInLandscape(false);
        mUI_BoardView.setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER);

        addColumn();
        addColumn();
    }

    private void addColumn() {
        final ArrayList<Pair<Long, String>> mItemArray = new ArrayList<>();
        int addItems = 15;
        for (int i = 0; i < addItems; i++) {
            long id = sCreatedItems++;
            mItemArray.add(new Pair<>(id, "Item " + id));
        }

        final int column = mColumns;
        final ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.column_item, R.id.item_layout, true);
        final View header = View.inflate(getActivity(), R.layout.drag_item_header, null);
        ((TextView) header.findViewById(R.id.uiHeaderTitle)).setText("Column = " + (mColumns + 1));
        ((TextView) header.findViewById(R.id.uiHeaderCount)).setText("" + addItems);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = sCreatedItems++;
                Pair item = new Pair<>(id, "Item " + id);
                mUI_BoardView.addItem(mUI_BoardView.getColumnOfHeader(v), 0, item, true);
                //mBoardView.moveItem(4, 0, 0, true);
                //mBoardView.removeItem(column, 0);
                //mBoardView.moveItem(0, 0, 1, 3, false);
                //mBoardView.replaceItem(0, 0, item1, true);
                ((TextView) header.findViewById(R.id.uiHeaderCount)).setText(String.valueOf(mItemArray.size()));
            }
        });
        mUI_BoardView.addColumn(listAdapter, header, header, false);
        mColumns++;
    }

    //=================
    //==  DragItem  ==
    //=================
    private static class DragItem_Action extends DragItem {

        DragItem_Action(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);

            dragCard.setMaxCardElevation(40);
            dragCard.setCardElevation(clickedCard.getCardElevation());
            // I know the dragView is a FrameLayout and that is why I can use setForeground below api level 23
            dragCard.setForeground(clickedView.getResources().getDrawable(R.drawable.card_view_drag_foreground));
        }

        @Override
        public void onMeasureDragView(View clickedView, View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            CardView clickedCard = clickedView.findViewById(R.id.card);
            int widthDiff = dragCard.getPaddingLeft() - clickedCard.getPaddingLeft() + dragCard.getPaddingRight() -
                    clickedCard.getPaddingRight();
            int heightDiff = dragCard.getPaddingTop() - clickedCard.getPaddingTop() + dragCard.getPaddingBottom() -
                    clickedCard.getPaddingBottom();
            int width = clickedView.getMeasuredWidth() + widthDiff;
            int height = clickedView.getMeasuredHeight() + heightDiff;
            dragView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            dragView.measure(widthSpec, heightSpec);
        }

        @Override
        public void onStartDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }

        @Override
        public void onEndDragAnimation(View dragView) {
            CardView dragCard = dragView.findViewById(R.id.card);
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 6);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }
    }

    private static class DragItem_Daily extends DragItem {

        DragItem_Daily(Context context, int layoutId) {
            super(context, layoutId);
            setSnapToTouch(false);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            //--原作者寫死--
            LinearLayout clickedLayout = (LinearLayout) clickedView;
            View uiClickedHeader = clickedLayout.getChildAt(0);
            RecyclerView clickedRecyclerView = (RecyclerView) clickedLayout.getChildAt(1);

            //-- Update Header Content --
            View uiDragHeader = dragView.findViewById(R.id.uiDragHeader);
            CharSequence textTitle = ((TextView) uiClickedHeader.findViewById(R.id.uiHeaderTitle)).getText();
            CharSequence textCount = ((TextView) uiClickedHeader.findViewById(R.id.uiHeaderCount)).getText();
            ((TextView) uiDragHeader.findViewById(R.id.uiHeaderTitle)).setText(textTitle);
            ((TextView) uiDragHeader.findViewById(R.id.uiHeaderCount)).setText(textCount);

            //--
            ScrollView uiDragScrollView = dragView.findViewById(R.id.uiDragScrollView);
            LinearLayout uiDragList = dragView.findViewById(R.id.uiDragList);
            uiDragList.removeAllViews();

            for (int i = 0; i < clickedRecyclerView.getChildCount(); i++) {
                View view = View.inflate(dragView.getContext(), R.layout.column_item, null);
                ((TextView) view.findViewById(R.id.text)).setText(((TextView) clickedRecyclerView.getChildAt(i).findViewById(R.id.text)).getText());
                uiDragList.addView(view);

                if (i == 0) {
                    uiDragScrollView.setScrollY(-clickedRecyclerView.getChildAt(i).getTop());
                }
            }

            dragView.setPivotY(0);
            dragView.setPivotX(clickedView.getMeasuredWidth() / 2);
        }

        @Override
        public void onStartDragAnimation(View dragView) {
            super.onStartDragAnimation(dragView);
            dragView.animate().scaleX(0.9f).scaleY(0.9f).start();
        }

        @Override
        public void onEndDragAnimation(View dragView) {
            super.onEndDragAnimation(dragView);
            dragView.animate().scaleX(1).scaleY(1).start();
        }
    }

}
