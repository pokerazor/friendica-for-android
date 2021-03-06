package de.wikilab.android.friendica01;

/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class HomeActivity extends FragmentActivity implements FragmentParentListener, LoginListener {
	private static final String TAG = "Friendica/HomeActivity";

	public final static String SENDER_ID = "179387721673";

	public static final int RQ_SELECT_PHOTO = 44;
	public static final int RQ_TAKE_PHOTO = 55;

	public File takePhotoTarget;

	boolean isLargeMode = false;

	ContentFragment curFragment = null;

	/*
	WelcomeFragment frag_welcome = new WelcomeFragment();
	PostListFragment frag_posts = new PostListFragment();
	WritePostFragment frag_writepost = new WritePostFragment();
	PhotoGalleryFragment frag_photos = new PhotoGalleryFragment();
	FriendListFragment frag_friendlist = new FriendListFragment();
	PostDetailFragment frag_postdetail = new PostDetailFragment();
	MessageViewFragment frag_messages = new MessageViewFragment();
	//PreferenceFragment frag_preferences = new PreferenceFragment();
	 */

	String currentMMItem = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.homeactivity);

		Max.initDataDirs();

		Log.d(TAG, "screenLayout=" + getResources().getConfiguration().screenLayout);
		if (Max.isLarge(getResources().getConfiguration())) {
			// on a large screen device ...
			isLargeMode = true;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, "Please enter account data");
		} else {
			Max.tryLogin(this);

			if (savedInstanceState == null) {
				navigate("Timeline");
			} else {
				currentMMItem = savedInstanceState.getString("currentMMItem");
				if (currentMMItem != null)
					navigate(currentMMItem);
			}

			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			final String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
				Log.v(TAG, "Registering for GCM");
				GCMRegistrar.register(this, SENDER_ID);
			} else {
				Log.v(TAG, "Already registered");
			}

		}

		View toggle = findViewById(R.id.toggle_left_bar);
		if (toggle != null)
			toggle.setOnClickListener(toggleMenuBarHandler);
		// toggle = findViewById(R.id.toggle_left_bar2);
		// if (toggle != null) toggle.setOnClickListener(toggleMenuBarHandler);
		toggle = findViewById(R.id.left_bar_header);
		if (toggle != null)
			toggle.setOnClickListener(toggleMenuBarHandler);

		// ViewServer.get(this).addWindow(this);

		Log.i(TAG, "Should check for updates?");

		if (prefs.getBoolean("updateChecker", true)) {
			Log.i(TAG, "Checking for updates...");
			final TwAjax updateChecker = new TwAjax(this, true, false);
			updateChecker.getUrlContent("http://friendica-for-android.wiki-lab.net/docs/update.txt", new Runnable() {
				@Override
				public void run() {
					String res = updateChecker.getResult();
					if (res != null && res.startsWith("UPDATE=")) {
						try {
							int version = Integer.parseInt(res.substring(7));
							int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
							Log.i(TAG, "UpdateCheck onlineVersion=" + version + " currentVersion=" + currentVersion);
							if (version > currentVersion) {
								Max.alert(HomeActivity.this, "Open the app's website to download the newest version:<br><a href='https://github.com/max-weller/friendica-for-android/downloads'>https://github.com/max-weller/friendica-for-android/downloads</a><br><br>(Go to Preferences to disable update check)", "Update available!");
							}
						} catch (NameNotFoundException e) {
							e.printStackTrace();
							Log.e(TAG, "UpdateCheck failed! (2)");
						}
					} else {
						Log.e(TAG, "UpdateCheck failed!");
					}
				}
			});
		}

	}

	OnClickListener toggleMenuBarHandler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			toggleMenuBarVisible();
		}
	};

	protected void toggleMenuBarVisible() {
		View leftBar = findViewById(R.id.left_bar);
		setMenuBarVisible(leftBar.getVisibility() == View.GONE);
	}

	protected void setMenuBarVisible(boolean v) {
		View leftBar = findViewById(R.id.left_bar);
		if (v) {
			Animation anim1 = AnimationUtils.loadAnimation(HomeActivity.this, android.R.anim.slide_in_left);
			anim1.setInterpolator((new AccelerateDecelerateInterpolator()));
			// anim1.setFillAfter(true);
			leftBar.setAnimation(anim1);

			leftBar.setVisibility(View.VISIBLE);
		} else {
			Animation anim1 = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_out_left);
			anim1.setInterpolator((new AccelerateDecelerateInterpolator()));
			anim1.setFillAfter(true);
			leftBar.setAnimation(anim1);

			leftBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("currentMMItem", currentMMItem);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {

		if (message.equals(ContentFragment.FRGM_MSG_SET_HEADERTEXT)) {
			setHeadertext((String) arg1);
		}
		if (message.equals(ContentFragment.FRGM_MSG_NAV_MAINMENU)) {
			navigate((String) arg1);
		}
		if (message.equals(ContentFragment.FRGM_MSG_SHW_LOADING_ANIMATION)) {
			((ProgressBar) findViewById(R.id.glob_progressbar)).setVisibility(((Integer) arg1).intValue());
		}
		if (message.equals(ContentFragment.FRGM_MSG_NAV_CONVERSATION)) {
			navigateConversation((String) arg1);
		}
	}

	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RQ_SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				Intent in = new Intent(HomeActivity.this, FriendicaImgUploadActivity.class);
				in.putExtra(Intent.EXTRA_STREAM, data.getData());
				startActivity(in);
			}
			break;
		case RQ_TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				Intent in = new Intent(HomeActivity.this, FriendicaImgUploadActivity.class);
				in.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(takePhotoTarget));
				startActivity(in);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (curFragment instanceof PostListFragment) {
			PostBarModule postBar = ((PostListFragment) curFragment).getPostBar();
			Uri fileToUpload = null;

			switch (requestCode) {
			case HomeActivity.RQ_SELECT_PHOTO:
				if (resultCode == RESULT_OK) {
					fileToUpload = data.getData();
				}
				break;
			case HomeActivity.RQ_TAKE_PHOTO:
				if (resultCode == RESULT_OK) {
					fileToUpload = Uri.fromFile(postBar.takePhotoTarget);
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				return;
			}

			postBar.setImage(fileToUpload);

// PostBarModule.startImageUpload(this, fileToUpload);
		}
	}

	void onNavMainFragment() {
		if (!isLargeMode) {
			View leftBar = findViewById(R.id.left_bar);
			leftBar.setVisibility(View.GONE);
		}
	}

	void navigate(String navTarget) {
		currentMMItem = navTarget;

		if (navTarget.equals(getString(R.string.mm_timeline))) {
			navigatePostList("timeline");
		}

		if (navTarget.equals(getString(R.string.mm_notifications))) {
			navigatePostList("notifications");
		}

		if (navTarget.equals(getString(R.string.mm_mywall))) {
			navigatePostList("mywall");
		}

		if (navTarget.equals(getString(R.string.menuitem_map))) {
			navigateMapActivity();
		}

		if (navTarget.equals(getString(R.string.mm_friends))) {
			navigateFriendList();
		}

		if (navTarget.equals(getString(R.string.mm_myphotoalbums))) {
			navigatePhotoGallery("myalbums");
		}

		if (navTarget.equals(getString(R.string.mm_directmessages))) {
			// Intent in = new Intent(HomeActivity.this, MessagesActivity.class);
			// startActivity(in);
			navigateMessages("msg:all");
		}

		if (navTarget.equals(getString(R.string.mm_preferences))) {
			navigatePreferences();
		}

		if (navTarget.equals(getString(R.string.mm_logout))) {
			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit();
			// prefs.putString("login_server", null); //keep server and user ...
			prefs.putString("login_user", null);
			prefs.putString("login_password", null); // ...only remove password
			prefs.commit();

			finish();
		}

	}

	void setHeadertext(String ht) {
		TextView subheading = (TextView) findViewById(R.id.header_text);

		if (subheading != null)
			subheading.setText(ht);
	}

	private void navigateMainFragment(ContentFragment newFragment, String target) {
		curFragment = newFragment;
		onNavMainFragment();
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		Bundle b = new Bundle();
		b.putString("target", target);
		curFragment.setArguments(b);
		t.replace(R.id.view_fragment_container, curFragment);
		// t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		t.setCustomAnimations(R.anim.slide_in_right, android.R.anim.slide_out_right);
		t.addToBackStack(null);
		t.commit();
		curFragment.navigate(target);
	}

	private void navigateFriendList() {
		navigateMainFragment(new FriendListFragment(), "friendlist");
	}

	private void navigatePostList(String listTarget) {
		navigateMainFragment(new PostListFragment(), listTarget);
	}

	private void navigateConversation(String conversationId) {
		navigateMainFragment(new PostDetailFragment(), "conversation:" + conversationId);
	}

	private void navigatePhotoGallery(String galleryTarget) {
		navigateMainFragment(new PhotoGalleryFragment(), galleryTarget);
	}

	private void navigateMessages(String target) {
		navigateMainFragment(new MessageViewFragment(), target);
	}

	private void navigatePreferences() {
		Intent showContent = new Intent(getApplicationContext(), PreferencesActivity.class);
		startActivity(showContent);

	}

	@Override
	public void onLogin() {
		LoginListener target = (LoginListener) getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		target.onLogin();

	}

	@Override
	public void onBackPressed() {
		if (!isLargeMode) {
			View leftBar = findViewById(R.id.left_bar);
			if (leftBar.getVisibility() != View.GONE) {
				leftBar.setVisibility(View.GONE);
				return;
			}
		}
		Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
		if (viewerFragment instanceof ContentFragment) {
			if (((ContentFragment) viewerFragment).onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggleMenuBarVisible();
			return false;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	private void navigateMapActivity() {
		startActivity(new Intent("de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon.TimelineEventMapActivity"));
	}
}
