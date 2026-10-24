package com.oldschoolnetwork.core.lite;
import android.content.*;
import android.database.*;
import android.net.Uri;
public final class AccountBridge extends ContentProvider {
 public boolean onCreate(){return true;}
 public Cursor query(Uri uri,String[] columns,String selection,String[] args,String order){
  if(!"/active".equals(uri.getPath()))throw new IllegalArgumentException("Unknown account route");
  android.content.SharedPreferences p=getContext().getSharedPreferences("account",0);String[] names=columns==null?new String[]{"on_id","username","avatar","token"}:columns;for(String key:names)if(!java.util.Arrays.asList("on_id","username","avatar","token").contains(key))throw new IllegalArgumentException("Unknown account field");MatrixCursor c=new MatrixCursor(names);
  boolean present=false;for(android.accounts.Account account:android.accounts.AccountManager.get(getContext()).getAccountsByType(AuthenticatorService.TYPE))if(account.name.equalsIgnoreCase(p.getString("on_id","")))present=true;
  if(!present)p.edit().clear().commit();
  if(p.contains("token")){Object[] row=new Object[names.length];for(int i=0;i<names.length;i++)row[i]=p.getString(names[i],"");c.addRow(row);}return c;
 }
 public String getType(Uri u){return "vnd.android.cursor.item/vnd.oldschoolnetwork.lite-account";}
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
