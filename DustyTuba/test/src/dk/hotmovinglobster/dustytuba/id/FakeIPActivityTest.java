package dk.hotmovinglobster.dustytuba.tests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.id.FakeIPActivity;

public class FakeIPActivityTest extends ActivityUnitTestCase<FakeIPActivity> {
	
	private FakeIPActivity mActivity;
	
	public FakeIPActivityTest() {
		super(FakeIPActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent i = new Intent();
		i.setClassName("dk.hotmovinglobster.dustytuba.id", "FakeIPActivity");
		i.putExtra(BtAPI.EXTRA_IP_MAC, "90:21:55:a1:a5:8d");
		
		mActivity = this.startActivity(i, null, null);	
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testMacAddress() {
		String mac = mActivity.getIntent().getExtras().getString(BtAPI.EXTRA_IP_MAC);
		
		assertEquals("90:21:55:a1:a5:8d", mac);
	}	
}