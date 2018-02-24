package example.ASPIRE.MyoHMI_Android;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs = 3;

    private String[] tabTitles = new String[]{"EMG", "FEATURES", "CLASSIFIER"};

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                EmgFragment tab1 = new EmgFragment();
                return tab1;
            case 1:
                FeatureFragment tab2 = new FeatureFragment();
                return tab2;
            case 2:
                ClassificationFragment tab3 = new ClassificationFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
