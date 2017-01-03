package bacon.john.expb_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import john.bacon.expbplus.ExpandableButtonMenu;
import john.bacon.expbplus.ExpandableMenuOverlay;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExpandableMenuOverlay button_menu = (ExpandableMenuOverlay) findViewById(R.id.button_menu);
        button_menu.add(this, R.drawable.huaji, "1");
        button_menu.add(this, R.drawable.huaji, "2");
        button_menu.add(this, R.drawable.huaji, "3");
        button_menu.add(this, R.drawable.huaji, "4");
        button_menu.add(this, R.drawable.huaji, "5");
        button_menu.add(this, R.drawable.huaji, "6");
        button_menu.add(this, R.drawable.huaji, "7");
        button_menu.add(this, R.drawable.huaji, "8");

        button_menu.setOnMenuButtonClickListener(new ExpandableButtonMenu.OnMenuButtonClick() {
            @Override
            public void onClick(int pos) {
                Toast.makeText(MainActivity.this, pos + "", Toast.LENGTH_LONG).show();
            }
        });

        button_menu.getButtonMenu().setMenuButtonImage(2, R.mipmap.ic_launcher);
        button_menu.getButtonMenu().setMenuButtonText(5, "啥？");
    }
}
