package com.oldschoolnetwork.core.lite;
import android.accounts.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import com.oldschoolnetwork.lite.Api;
import org.json.*;

public final class MainActivity extends Activity {
 private EditText login,password;private TextView status,profile;private SharedPreferences saved;
 private AccountAuthenticatorResponse response;
 public void onCreate(Bundle state){super.onCreate(state);setContentView(R.layout.main);saved=getSharedPreferences("account",0);login=(EditText)findViewById(R.id.login);password=(EditText)findViewById(R.id.password);status=(TextView)findViewById(R.id.status);profile=(TextView)findViewById(R.id.profile);
  response=getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);if(response!=null)response.onRequestContinued();
  findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener(){public void onClick(View v){signIn();}});
  findViewById(R.id.help).setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(MainActivity.this,com.oldschoolnetwork.lite.DocumentsActivity.class).putExtra("url","https://oldschoolnetwork.online/downloads/docs/categories/account/"));}});
  findViewById(R.id.recovery).setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(MainActivity.this,RecoveryActivity.class));}});
  findViewById(R.id.join).setOnClickListener(new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(MainActivity.this,RegistrationActivity.class));}});
  findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener(){public void onClick(View v){saved.edit().clear().commit();getContentResolver().notifyChange(Uri.parse("content://com.oldschoolnetwork.core.lite.account/active"),null);render();}});
  findViewById(R.id.store).setOnClickListener(new View.OnClickListener(){public void onClick(View v){try{startActivity(getPackageManager().getLaunchIntentForPackage("com.oldschoolnetwork.store.lite"));}catch(Exception e){startActivity(new Intent(MainActivity.this,com.oldschoolnetwork.lite.OfficialDownloadActivity.class).putExtra("service","store"));}}});
  findViewById(R.id.sessions).setOnClickListener(new View.OnClickListener(){public void onClick(View v){dashboard();}});
  Api.account(this);render();if(saved.contains("token"))refresh();
 }
 private void render(){String avatar=saved.getString("avatar","");if(avatar.length()>0)com.oldschoolnetwork.lite.Images.load(this,(ImageView)findViewById(R.id.avatar),avatar.startsWith("data:")?avatar:"data:image/png;base64,"+avatar,128);boolean in=saved.contains("token")&&response==null;findViewById(R.id.login_form).setVisibility(in?View.GONE:View.VISIBLE);findViewById(R.id.account_panel).setVisibility(in?View.VISIBLE:View.GONE);profile.setText("@"+saved.getString("username","")+"\nON Core Lite · Android "+Build.VERSION.RELEASE);}
 private void signIn(){
  if(login.getText().toString().trim().length()==0||password.length()==0){status.setText("Enter your username, email or ON ID and password.");return;}
  JSONObject body=new JSONObject();try{body.put("login",login.getText().toString().trim()).put("password",password.getText().toString()).put("client_id","on_core_lite");}catch(JSONException e){return;}
  findViewById(R.id.sign_in).setEnabled(false);status.setText("Connecting securely…");
  Api.call(this,"oauth_login",body,false,new Api.Callback(){public void complete(JSONObject result,String error){if(isFinishing())return;findViewById(R.id.sign_in).setEnabled(true);password.setText("");if(error!=null){status.setText(error);return;}
   JSONObject user=result.optJSONObject("user");if(user==null||user.optString("on_id").length()==0||result.optString("access_token").length()==0){status.setText("The server did not return a valid account.");return;}
   save(user,result.optString("access_token"));status.setText("Signed in.");render();
   if(response!=null){Bundle b=new Bundle();b.putString(AccountManager.KEY_ACCOUNT_NAME,user.optString("on_id"));b.putString(AccountManager.KEY_ACCOUNT_TYPE,AuthenticatorService.TYPE);response.onResult(b);response=null;finish();}
  }});
 }
 private void save(JSONObject user,String token){
  String id=user.optString("on_id");saved.edit().putString("on_id",id).putString("username",user.optString("username")).putString("avatar",user.optString("avatar_base64")).putString("token",token).commit();
  AccountManager manager=AccountManager.get(this);boolean exists=false;for(Account account:manager.getAccountsByType(AuthenticatorService.TYPE))if(account.name.equalsIgnoreCase(id))exists=true;
  if(!exists)manager.addAccountExplicitly(new Account(id,AuthenticatorService.TYPE),null,null);
  getContentResolver().notifyChange(Uri.parse("content://com.oldschoolnetwork.core.lite.account/active"),null);
 }
 private void refresh(){JSONObject body=new JSONObject();try{body.put("token",saved.getString("token",""));}catch(JSONException ignored){}Api.call(this,"oauth_saved_profile",body,false,new Api.Callback(){public void complete(JSONObject r,String error){if(isFinishing())return;if(r!=null&&r.optJSONObject("user")!=null){save(r.optJSONObject("user"),saved.getString("token",""));render();}else status.setText(error==null?"Reconnect to refresh your account.":error);}});}
 private void dashboard(){Api.call(this,"account_dashboard_data",new JSONObject(),true,new Api.Callback(){public void complete(final JSONObject r,String error){if(isFinishing())return;if(error!=null){status.setText(error);return;}new AlertDialog.Builder(MainActivity.this).setTitle("ON Account").setItems(new String[]{"Active sessions","Linked service profiles"},new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){if(n==0)sessions(r.optJSONArray("sessions"));else profiles(r.optJSONArray("profiles"));}}).show();}});}
 private void sessions(final JSONArray sessions){if(sessions==null)return;String[] names=new String[sessions.length()];for(int i=0;i<names.length;i++){JSONObject item=sessions.optJSONObject(i);names[i]=item.optString("client_id")+" · "+item.optString("user_agent");}new AlertDialog.Builder(this).setTitle("Active sessions").setItems(names,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,final int n){new AlertDialog.Builder(MainActivity.this).setTitle("End this session?").setMessage(sessions.optJSONObject(n).optString("user_agent")).setNegativeButton("Cancel",null).setPositiveButton("End session",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int ignored){JSONObject body=new JSONObject();try{body.put("session_id",sessions.optJSONObject(n).optInt("id"));}catch(Exception e){}Api.call(MainActivity.this,"account_revoke_session",body,true,new Api.Callback(){public void complete(JSONObject r,String error){status.setText(error==null?"Session ended.":error);}});}}).show();}}).setNegativeButton("Close",null).show();}
 private void profiles(final JSONArray profiles){if(profiles==null)return;String[] names=new String[profiles.length()];for(int i=0;i<names.length;i++){JSONObject item=profiles.optJSONObject(i);names[i]=item.optString("service")+" · "+item.optString("name");}new AlertDialog.Builder(this).setTitle("Linked service profiles").setItems(names,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int n){String url=profiles.optJSONObject(n).optString("url");if(url.startsWith("https://"))open(url);}}).setNegativeButton("Close",null).show();}
 private void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){status.setText(url);}}
 protected void onSaveInstanceState(Bundle state){state.putString("login",login.getText().toString());super.onSaveInstanceState(state);}
}
