package com.caju.uheer.app.services;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;

public class EmailLookup
{
    private static ArrayList<String> emails = null;

    private static ArrayList<String> getNameEmailDetails(Activity activity){
        ArrayList<String> names = new ArrayList<String>();
        ContentResolver contentResolver = activity.getContentResolver();
        Cursor cur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    if(email != null){
                        System.out.println(email);
                        names.add(email);
                    }
                }
                cur1.close();
            }
        }
        System.out.println(names.size());
        Collections.sort(names);
        return names;
    }

    public static void init(Activity activity){
        new fetchEmailListTask(activity).execute();
    }

    public static boolean searchEmail(String email){
        if(emails != null)
        {
            int pos = Collections.binarySearch(emails, email);
            if(pos >= 0)
                return true;
            else
                return false;
        }
        else
            System.out.println("Lista de emails vazia");

        return false;
    }

    static class fetchEmailListTask extends AsyncTask<Void, Void, ArrayList<String>>{

        Activity mContext;

        public fetchEmailListTask(Activity c){
            mContext = c;
        }
        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {
            return EmailLookup.getNameEmailDetails(mContext);
        }

        @Override
        protected void onPostExecute(ArrayList<String> list){
            emails = list;
        }
    }
}
