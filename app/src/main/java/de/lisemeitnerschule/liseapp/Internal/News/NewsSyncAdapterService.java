package de.lisemeitnerschule.liseapp.Internal.News;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Jarod on 28.03.2015.
 */
public class NewsSyncAdapterService extends Service{
        // Storage for an instance of the sync adapter
        private static NewsSyncAdapter SyncAdapter = null;
        // Object to use as a thread-safe lock
        private static final Object SyncAdapterLock = new Object();
        /*
         * Instantiate the sync adapter object.
         */
        @Override
        public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
            synchronized (SyncAdapterLock) {
                if (SyncAdapter == null) {
                    SyncAdapter = new NewsSyncAdapter(getApplicationContext(), true);
                }
            }
        }
        /**
         * Return an object that allows the system to invoke
         * the sync adapter.
         *
         */
        @Override
        public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
            return SyncAdapter.getSyncAdapterBinder();
        }

}
