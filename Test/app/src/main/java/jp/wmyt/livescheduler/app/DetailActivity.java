package jp.wmyt.livescheduler.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import jp.wmyt.livescheduler.app.Master.LiveHouseTrait;
import jp.wmyt.livescheduler.app.Master.LiveInfoTrait;

/**
 * Created by miyata on 2014/05/05.
 */
public class DetailActivity extends Activity implements View.OnClickListener{
    private boolean _isFavorite;
    private ImageButton _favButton;
    private String uniqueId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        uniqueId = intent.getStringExtra("uniqueId");
        LiveInfoTrait trait = LiveInfoTrait.getInstance().getTraitOfUniqueID(uniqueId);

        _isFavorite = trait.isFavorite();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String dateStr = dateFormat.format(trait.getLiveDate());
        setTitle(dateStr + "(" + trait.getDayOfWeek() + ")");

        //場所
        TextView detailView = (TextView) this.findViewById(R.id.detail_place);
        detailView.setText(LiveHouseTrait.getInstance().getLiveHouseName(trait.getLiveHouseNo()));

        //タイトル
        detailView = (TextView) this.findViewById(R.id.detail_title);
        detailView.setText(trait.getEventTitle());

        //出演者
        String act = trait.getAct();
        act = act.replace("/", "\n");
        act = act.replace("\n ", "\n");
        detailView = (TextView) this.findViewById(R.id.detail_act);
        detailView.setText(act);

        //その他情報
        detailView = (TextView) this.findViewById(R.id.detail_other);
        detailView.setText(trait.getOtherInfo());

        this.findViewById(R.id.fav_view).setOnClickListener(this);

        _favButton = (ImageButton)this.findViewById(R.id.fav_button);
        _favButton.setScaleX(0.5f);
        _favButton.setScaleY(0.5f);
        _favButton.setOnClickListener(this);

        changeFavColor();
    }

    @Override
    public void onClick(View view) {
        _isFavorite = !_isFavorite;
        if(_isFavorite){
            Common.getInstance().addFavoriteList(uniqueId);
        }else{
            Common.getInstance().removeFavoriteList(uniqueId);
        }
        changeFavColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFavColor(){
        int favButtonColor = getResources().getColor(R.color.fav);
        if(!_isFavorite){
            favButtonColor = getResources().getColor(R.color.fav_disable);
        }
        _favButton.setColorFilter(favButtonColor);
    }
}
