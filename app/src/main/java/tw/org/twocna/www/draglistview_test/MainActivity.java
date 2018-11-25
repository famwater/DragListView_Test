package tw.org.twocna.www.draglistview_test;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//-- https://github.com/woxblom/DragListView --
public class MainActivity extends AppCompatActivity {
    //-- Log --
    private static final String TAG = MainActivity.class.getSimpleName();

    //========================
    //== Constructor 建構子  ==
    //========================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- Initialize UI --
        initializeUI_Button();

        //-- Switch to fragment --
        ActionEditorFragment oFragment = new ActionEditorFragment();
        fragment_Switch(oFragment, oFragment.FRAGMENT_TYPE);
    }

    //=====================
    //==  Initialize UI  ==
    //=====================
    private void initializeUI_Button(){
        Button uiButtonLeft = findViewById(R.id.uiButtonLeft);
        uiButtonLeft.setText("Drag View");
        uiButtonLeft.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragment_Switch(new DragViewFragment(), DragViewFragment.FRAGMENT_TYPE);
                    }
                }
        );

        Button uiButtonCenter = findViewById(R.id.uiButtonCenter);
        uiButtonCenter.setText("Action Editor");
        uiButtonCenter.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragment_Switch(new ActionEditorFragment(), ActionEditorFragment.FRAGMENT_TYPE);
                    }
                }
        );

        Button uiButtonRight = findViewById(R.id.uiButtonRight);
        uiButtonRight.setText("Drag View");
        uiButtonRight.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragment_Switch(new DragViewFragment(), DragViewFragment.FRAGMENT_TYPE);
                    }
                }
        );
    }

    //====================
    //==  For Fragment  ==
    //====================
    public void fragment_Switch(Fragment oFragment, String fragmentType){
        FragmentTransaction oTransaction = getSupportFragmentManager().beginTransaction();
        oTransaction.replace(R.id.uiContainer, oFragment, fragmentType).commit();
    }
}
