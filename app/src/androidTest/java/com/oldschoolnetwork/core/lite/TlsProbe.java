package com.oldschoolnetwork.core.lite;
import android.app.Instrumentation;
import android.os.Bundle;
import com.oldschoolnetwork.lite.*;
import org.json.JSONObject;
public final class TlsProbe extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();try{
  byte[] github=HttpsTransport.request(getTargetContext(),"https://api.github.com/repos/OldschoolNetwork/core-lite",null,1024*1024);
  if(!"core-lite".equals(new JSONObject(new String(github,"UTF-8")).optString("name")))throw new Exception("Unexpected GitHub response");
  if(HttpsTransport.matchesHost("attack.example.org","*.org")||HttpsTransport.matchesHost("bad.example.org.attacker.com","*.example.org"))throw new Exception("Invalid wildcard accepted");
  if(!HttpsTransport.matchesHost("api.example.org","*.example.org"))throw new Exception("Valid wildcard rejected");
  JSONObject catalog=Api.request(getTargetContext(),"store_categories",new JSONObject(),false);
  if(catalog.optJSONArray("categories")==null)throw new Exception("Catalog missing");
  JSONObject release=OfficialDownloadActivity.fetchReleaseManifest(getTargetContext(),"store-lite");
  java.io.File apk=new java.io.File(getTargetContext().getCacheDir(),"github-download-probe.apk");
  try {HttpsTransport.download(getTargetContext(),release.getString("download_url"),apk,8L*1024*1024,null);Transfers.verify(getTargetContext(),apk,release.getString("package_name"),release.getInt("version_code"),release.getString("sha256"));}finally{apk.delete();}
  result.putString("result","PASS: verified TLS1.2 to GitHub manifest and APK download/signature, ON API, hostname constraints, SDK "+android.os.Build.VERSION.SDK_INT);finish(-1,result);
 }catch(Throwable e){result.putString("error",android.util.Log.getStackTraceString(e));finish(0,result);}}
}
