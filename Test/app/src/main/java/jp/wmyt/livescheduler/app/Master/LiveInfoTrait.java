package jp.wmyt.livescheduler.app.Master;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import jp.wmyt.livescheduler.app.Common;

/**
 * Created by miyata on 2014/05/06.
 */
public class LiveInfoTrait {
    //シングルトン
    private static final LiveInfoTrait instance = new LiveInfoTrait();
    public LiveInfoTrait(){

    }
    public static LiveInfoTrait getInstance(){
        return instance;
    }

    static ArrayList<LiveInfoTrait> traitList;
    static Date minDate;
    static Date maxDate;

    static {
        traitList = new ArrayList<LiveInfoTrait>();
    }

    //メンバ変数
    private int     _liveHouseNo;
    private Date    _liveDate;
    private int     _subNo;
    private String  _eventTitle;
    private String  _act;
    private String  _otherInfo;
    private String  _uniqueID;
    private String  _dayOfWeek;
    private int     _sortNo;

    public int getSortNo() {
        return _sortNo;
    }

    public int getLiveHouseNo() {
        return _liveHouseNo;
    }

    public Date getLiveDate() {
        return _liveDate;
    }

    public int getSubNo() {
        return _subNo;
    }

    public String getAct() {
        return _act;
    }

    public String getDayOfWeek() {
        return _dayOfWeek;
    }

    public String getEventTitle() {
        return _eventTitle;
    }

    public String getOtherInfo() {
        return _otherInfo;
    }

    public String getUniqueID() {
        return _uniqueID;
    }
    //-------

    static String[] dayName = {"日", "月", "火", "水", "木", "金", "土"};

    public ArrayList<LiveInfoTrait> getTraitList(){
        return traitList;
    }

    private void removeAllMast(){
        traitList.clear();
    }

    public synchronized ArrayList<LiveInfoTrait> getTraitListOfDate(Date date){
        ArrayList liveList = new ArrayList();
        long dateTime = date.getTime() / 1;
        for(LiveInfoTrait trait : traitList){
            long traitTime = trait._liveDate.getTime();
            if(dateTime < traitTime){
                continue;
            }
            int diffDays = (int)((dateTime - traitTime) / (1000*60*60*24));

            if( diffDays == 0 ){
                liveList.add(trait);
            }
        }

        //ソート
        Collections.sort(liveList, new LiveInfoTraitComparator());

        return liveList;
    }

    public synchronized ArrayList<LiveInfoTrait> getTraitListOfLiveHouseNo(int liveHouseNo){
        ArrayList<LiveInfoTrait> liveList = new ArrayList();

        Date currentDate = new Date();
        for(LiveInfoTrait trait : traitList){
            if(trait._liveHouseNo == liveHouseNo && !trait.isPastLive()){
                liveList.add(trait);
            }
        }

        //セクションの追加
        addSection(liveList);

        return liveList;
    }

    public synchronized ArrayList<LiveInfoTrait> getTraitListOfContainsText(String searchStr){
        ArrayList liveList = new ArrayList();
        if(searchStr == null){
            return liveList;
        }

        searchStr = searchStr.toLowerCase();

        for(LiveInfoTrait trait : traitList){
            String title = trait._eventTitle.toLowerCase();
            String act = trait._act.toLowerCase();
            if(title.contains(searchStr) || act.contains(searchStr)){
                if(!trait.isPastLive()) {
                    liveList.add(trait);
                }
            }
        }

        //ソート
        Collections.sort(liveList, new LiveInfoTraitUniqueIdComparator());

        //セクションの追加
        addSection(liveList);

        return liveList;
    }

    public synchronized ArrayList<LiveInfoTrait> getTraitListOfFavorite(){
        ArrayList liveList = new ArrayList();
        for(LiveInfoTrait trait : traitList){
            if(trait.isPastLive()){
                continue;
            }

            if(trait.isFavorite()){
                liveList.add(trait);
            }
        }
        //ソート
        Collections.sort(liveList, new LiveInfoTraitUniqueIdComparator());

        //セクションの追加
        addSection(liveList);
        return liveList;
    }

    public LiveInfoTrait getTraitOfUniqueID(String uniqueID){
        for(LiveInfoTrait trait : traitList){
            if(uniqueID.equals(trait._uniqueID)){
                return trait;
            }
        }
        return null;
    }

    private void addSection(ArrayList<LiveInfoTrait> liveList){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM");
        String sectionString = "";
        for(int i = liveList.size()-1;i >= 0;i--){
            LiveInfoTrait trait = liveList.get(i);
            if( i == liveList.size()-1 ){
                sectionString = dateFormat.format(trait.getLiveDate());
            }

            String traitDate = dateFormat.format(trait.getLiveDate());
            if( !sectionString.equals(traitDate) )
            {
                LiveInfoTrait section = new LiveInfoTrait();
                section._liveHouseNo = -1;
                section._uniqueID = sectionString;
                liveList.add(i + 1, section);
                sectionString = traitDate;
            }
        }

        if(liveList.size() > 0){
            LiveInfoTrait trait = (LiveInfoTrait) liveList.get(0);
            if(trait != null) {
                LiveInfoTrait section = new LiveInfoTrait();
                section._liveHouseNo = -1;
                section._uniqueID = dateFormat.format(trait.getLiveDate());
                liveList.add(0, section);
            }
        }
    }

    public Date getMinDate(){
        if(minDate == null){
            return new Date();
        }
        return minDate;
    }

    public Date getMaxDate(){
        if(maxDate == null){
            return new Date();
        }
        return maxDate;
    }

    public void loadMast(LoadData data){
        //ライブ一覧をクリア
        removeAllMast();

        minDate = null;
        maxDate = null;

        int masterCount = data.getInt16();

        for( int i = 0;i < masterCount;i++ ){
            int     liveHouseNo  = data.getInt16();
            String  liveDate     = String.valueOf(data.getInt32());
            int     subNo        = data.getInt16();
            String  title        = data.getString16();
            String  act          = data.getString16();
            String  otherInfo    = data.getString16();

            LiveInfoTrait trait = new LiveInfoTrait();
            trait.initWithLiveHouseNo(liveHouseNo, liveDate, subNo, title, act, otherInfo);
            traitList.add(trait);
        }
    }

    private void initWithLiveHouseNo(int liveHouseNo,
                                              String liveDate,
                                              int subNo,
                                              String eventTitle,
                                              String act,
                                              String otherInfo){
        _liveHouseNo  = liveHouseNo;
        _subNo        = subNo;
        _eventTitle   = eventTitle;
        _act          = act;
        _otherInfo    = otherInfo;

        try{
            _liveDate = new SimpleDateFormat("yyyyMMdd").parse(liveDate);
        }catch (ParseException e){
            Log.e("parse Error",e.getMessage());
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        _uniqueID = sdf.format(_liveDate) + _subNo + String.format("%03d", _liveHouseNo);

        Calendar day = Calendar.getInstance();
        day.setTime(_liveDate);
        _dayOfWeek = dayName[day.get(Calendar.DAY_OF_WEEK)-1];

        LiveHouseTrait liveHouseTrait = LiveHouseTrait.getInstance().getTraitOfLiveHouseNo(liveHouseNo);
        if(liveHouseTrait != null){
            _sortNo = liveHouseTrait.getSortNo();
        }

        if( minDate == null || minDate.compareTo(_liveDate) > 0 ){
            minDate = _liveDate;
        }
        if( maxDate == null || maxDate.compareTo(_liveDate) < 0 ){
            maxDate = _liveDate;
        }
    }

    public boolean isFavorite(){
        ArrayList<String> favList = Common.getInstance().getFavList();
        return favList.contains(_uniqueID);
    }

    private boolean isPastLive(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date date = cal.getTime();

        return _liveDate.before(date);
    }
}

class LiveInfoTraitComparator implements java.util.Comparator {
    public int compare(Object s, Object t) {
        //               + (x > y)
        // compare x y = 0 (x = y)
        //               - (x < y)
        LiveInfoTrait trait1 = (LiveInfoTrait)s;
        LiveInfoTrait trait2 = (LiveInfoTrait)t;

        return trait1.getSortNo() - trait2.getSortNo();
    }
}

class LiveInfoTraitUniqueIdComparator implements java.util.Comparator {
    public int compare(Object s, Object t) {
        //               + (x > y)
        // compare x y = 0 (x = y)
        //               - (x < y)
        LiveInfoTrait trait1 = (LiveInfoTrait)s;
        LiveInfoTrait trait2 = (LiveInfoTrait)t;

        return trait1.getUniqueID().compareTo(trait2.getUniqueID());
    }
}