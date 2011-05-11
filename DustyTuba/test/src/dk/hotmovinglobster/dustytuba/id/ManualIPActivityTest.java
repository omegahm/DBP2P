package dk.hotmovinglobster.dustytuba.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.id.ManualIPActivity;

public class ManualIPActivityTest extends ActivityUnitTestCase<ManualIPActivity> {
	
	private ManualIPActivity mActivity;
	
	public ManualIPActivityTest() {
		super(ManualIPActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent i = new Intent();
		i.setClassName("dk.hotmovinglobster.dustytuba.id", "ManualIPActivity");
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
	
	public void testValidateMacAddress() {
		Object[] params = new Object[1];
		params[0] = "00:00:00:00:00:00";
		String result = (String) PrivateAccessor.invokePrivateMethod(mActivity, "validateMacAddress", params);
		assertEquals("00:00:00:00:00:00", result);
		
		params[0] = "not a mac address";
		result = (String) PrivateAccessor.invokePrivateMethod(mActivity, "validateMacAddress", params);
		assertNull(result);
		
	}
	
	
}