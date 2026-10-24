package com.oldschoolnetwork.core.lite;
import android.accounts.*;
import android.app.Service;
import android.content.*;
import android.os.*;
public final class AuthenticatorService extends Service {
 public static final String TYPE="com.oldschoolnetwork.account.lite";
 private AbstractAccountAuthenticator authenticator;
 public void onCreate(){super.onCreate();authenticator=new AbstractAccountAuthenticator(this){
  private Bundle login(AccountAuthenticatorResponse response){Bundle b=new Bundle();b.putParcelable(AccountManager.KEY_INTENT,new Intent(AuthenticatorService.this,MainActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,response));return b;}
  public Bundle addAccount(AccountAuthenticatorResponse r,String type,String token,String[] features,Bundle options){return login(r);}
  public Bundle confirmCredentials(AccountAuthenticatorResponse r,Account a,Bundle options){return login(r);}
  public Bundle getAuthToken(AccountAuthenticatorResponse r,Account a,String type,Bundle options){Bundle b=new Bundle();b.putInt(AccountManager.KEY_ERROR_CODE,AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);b.putString(AccountManager.KEY_ERROR_MESSAGE,"Use the signed ON Lite account bridge. Third-party consent is not available in this Lite release.");return b;}
  public String getAuthTokenLabel(String type){return "ON Account Lite";}
  public Bundle updateCredentials(AccountAuthenticatorResponse r,Account a,String type,Bundle options){return login(r);}
  public Bundle editProperties(AccountAuthenticatorResponse r,String type){return login(r);}
  public Bundle hasFeatures(AccountAuthenticatorResponse r,Account a,String[] features){Bundle b=new Bundle();b.putBoolean(AccountManager.KEY_BOOLEAN_RESULT,false);return b;}
 };}
 public IBinder onBind(Intent i){return authenticator.getIBinder();}
}
