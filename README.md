# DustyTuba Bluetooth Library

![DustyTuba Icon](https://github.com/omegahm/DBP2P/raw/master/DustyTubaSampleApp/res/drawable-hdpi/icon.png)

### Goal: to ease the use of bluetooth in Android projects
The code behind a simple bluetooth connection can be cumbersome to setup.
DustyTuba is here to ease this connection setup, and at the same time, offer other solutions to getting the identity of the actors in the bluetooth connection.

## Getting started is halfway done
If you just want to use the library (and not develop on it), you should download the all-included zip-file [DustyTuba.zip](https://github.com/omegahm/DBP2P/raw/master/DustyTuba.zip).

![Copy folders](https://github.com/omegahm/DBP2P/raw/master/Documentation/copy-folders.png)

In order to use DustyTuba, you need to include the three folders: `libs`, `res`, and `src` into your own project.

Secondly, you must include the libraries in the libs folder into your project. In Eclipse, this can be done by right-clicking your project and selecting `Properties`, then selecting `Java Build Path`, and in the tab `Libraries` clicking the `Add JARs...` button and selecting the two included .jar files in the libs folder.

## Modifying the manifest
Now you must expand the Android manifest to include the activities provided and to use the permissions needed. The activities you must declare goes in the `<application>`-element and are the following:

    <activity android:name="dk.hotmovinglobster.dustytuba.id.GenericIPActivity"
	      android:screenOrientation="portrait" />
    <!-- Optional: Identity Providers, include one or more -->
    <activity android:name="dk.hotmovinglobster.dustytuba.id.FakeIPActivity"
              android:screenOrientation="portrait" />
    <activity android:name="dk.hotmovinglobster.dustytuba.id.ManualIPActivity"
              android:screenOrientation="portrait" />
    <activity android:name="dk.hotmovinglobster.dustytuba.id.MultipleIPActivity"
              android:screenOrientation="portrait" />
    <activity android:name="dk.hotmovinglobster.dustytuba.id.PairedIPActivity"
              android:screenOrientation="portrait" />
    <activity android:name="dk.hotmovinglobster.dustytuba.id.BumpIPActivity"
              android:configChanges="keyboardHidden|orientation" />

If you want to use [Bump™](http://bu.mp) as an identity provider, you'll have to include the following in the `<application>`-element as well

    <!-- Optional: Additional activities required by BumpIPActivity -->
    <activity android:name="com.bumptech.bumpapi.BumpAPI"
              android:configChanges="keyboardHidden|orientation" 
              android:theme="@style/BumpDialog" />
    <activity android:name="com.bumptech.bumpapi.EditTextActivity"
              android:configChanges="keyboardHidden|orientation" 
              android:theme="@style/BumpDialog" />

The permissions to be declared goes in the main `<manifest>`-element and are the following:

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

## Wow! Was that it?
Yup, it's that simple to include the DustyTuba Bluetooth Library in your own project.
Now you're ready to call the API and actually use the bluetooth connection.

In order to call the library, you will use the `BtAPI.getIntent()` method to generate an `Intent` which must be passed on to Android’s `startActivityForResult()` method.

To receive data from the other end of the bluetooth connection, you need to have a class implement the `BtAPIListener`-interface (usually this class would be your main activity).

For example, to invoke the manual identity provider, insert the following code where you want it to be invoked:
    
    Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MANUAL, uuid);
    startActivityForResult(i, REQUEST_DUSTYTUBA);

where `REQUEST_DUSTYTUBA` is an integer constant chosen to distinguish between the result of this activity and others you may be using and `uuid` is an instance of the `java.util.UUID` class used to distinguish your bluetooth application from others. Also make your main activity implement the `BtAPIListener`-interface and override the `onActivityResult()` method as follows:

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
      if (requestCode == REQUEST_DUSTYTUBA) {
        if (resultCode == RESULT_CANCELED ) {
          // User canceled
        } else if (resultCode == RESULT_OK) {
          BtConnection conn = BtConnection.getConnection(); 
          conn.setListener(this);
        }
      }
    }

DustyTuba comes fully equipped with four different identity providers:

* Bump™
 * You'll need to import extra files from the Bump™ library
* Already paired
* Manual
 * Your users will need to input their partners MAC-address
* Fake
 * Used by you while you test. Here you can provide a MAC-address directly in the code

## We made it simple to copy us
Nobody wants to do stuff from scratch, so we created a dummy project to show the use of DustyTuba Bluetooth Library.
But, nobody wants to view a boring dummy project, so we made a cool dummy project.

The dummy project, which is actually a [Battleships](http://en.wikipedia.org/wiki/Battleships) game (cool, right?), is available in the [Battleships](https://github.com/omegahm/DBP2P/tree/master/Battleships)-folder.

![Battleships](https://github.com/omegahm/DBP2P/raw/master/Battleships/res/drawable/icon.png)

## Documentation
Please find our [documentation](https://github.com/omegahm/DBP2P/raw/master/Documentation/Documentation.pdf) document for a more detailed approach.