package in.codeseed.tubely.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.util.List;

import in.codeseed.tubely.R;

/**
 * Created by bala on 30/10/14.
 */
public class TubelyPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_dev_app);

        //Setup Listeners
        Preference prefFeedback = findPreference("pref_feedback");
        prefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
                feedbackIntent.setData(Uri.parse("mailto:appkiddo007@gmail.com"));
                feedbackIntent.setType("text/plain");
                feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"appkiddo007@gmail.com"});
                feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "Tubely - Feedback");

                // Verify it resolves
                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(feedbackIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if(isIntentSafe)
                    startActivity(Intent.createChooser(feedbackIntent, "Choose your Email app"));
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Sorry. You don't have any app installed to send an email!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference prefShareWithFriends = findPreference("share_with_friends");
        prefShareWithFriends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent spreadIntent = new Intent(Intent.ACTION_SEND);
                spreadIntent.setType("text/plain");
                spreadIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.spread_tubely));

                if(spreadIntent.resolveActivity(getActivity().getPackageManager())!=null)
                    startActivity(Intent.createChooser(spreadIntent, "Choose an app to share.."));
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Sorry. You don't have any app installed to share!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        Preference prefAppkiddo = findPreference("pref_appkiddo");
        prefAppkiddo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:AppKiddo"));

                if(intent.resolveActivity(getActivity().getPackageManager())!= null)
                    startActivity(intent);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Sorry. You need Playstore application to checkout my apps!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference prefRate = findPreference("pref_rate");
        prefRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=in.codeseed.tubely"));

                // Verify it resolves
                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if(isIntentSafe)
                    startActivity(intent);
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Sorry. You need Playstore application to rate Tubely!", Toast.LENGTH_SHORT).show();

                return true;
            }
        });

    }
}
