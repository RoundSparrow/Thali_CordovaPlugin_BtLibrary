/* Copyright (c) 2015-2016 Microsoft Corporation. This software is licensed under the MIT License.
 * See the license file delivered with this project for further information.
 */
package org.thaliproject.nativetest.app.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.thaliproject.nativetest.app.BridgeSpot;
import org.thaliproject.nativetest.app.model.Connection;
import org.thaliproject.nativetest.app.model.PeerAndConnectionModel;
import org.thaliproject.nativetest.app.R;
import org.thaliproject.nativetest.app.utils.MenuUtils;
import org.thaliproject.p2p.btconnectorlib.PeerProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A fragment containing the list of discovered peers.
 */
// @TargetApi(22)
public class PeerListFragment extends Fragment implements PeerAndConnectionModel.Listener {
    public interface Listener {
        void onPeerSelected(PeerProperties peerProperties);
        void onConnectRequest(PeerProperties peerProperties);
        void onSendDataRequest(PeerProperties peerProperties);
    }

    private static final String TAG = PeerListFragment.class.getName();
    private Context mContext = null;
    private Drawable mIncomingConnectionIconNotConnected = null;
    private Drawable mIncomingConnectionIconConnected = null;
    private Drawable mIncomingConnectionIconDataFlowing = null;
    private Drawable mOutgoingConnectionIconNotConnected = null;
    private Drawable mOutgoingConnectionIconConnected = null;
    private Drawable mOutgoingConnectionIconDataFlowing = null;
    private ListView mListView = null;
    private PeersListAdapter mPeerListAdapter = null;
    private PeerAndConnectionModel mModel = null;
    private Listener mListener = null;
    private PeerProperties mSelectedPeerProperties = null;

    public PeerListFragment() {
    }

    public synchronized void setListener(Listener listener) {
        mListener = listener;

        if (mListener != null && mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                    mSelectedPeerProperties = (PeerProperties) mListView.getItemAtPosition(index);
                    Log.i(TAG, "onItemClick: " + mSelectedPeerProperties.toString());

                    if (mListener != null) {
                        mListener.onPeerSelected(mSelectedPeerProperties);
                    }
                }
            });

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                    mSelectedPeerProperties = (PeerProperties) mListView.getItemAtPosition(index);
                    Log.i(TAG, "onItemLongClick: " + mSelectedPeerProperties.toString());

                    if (mListener != null) {
                        mListener.onPeerSelected(mSelectedPeerProperties);
                    }

                    return false; // Let the event propagate
                }
            });

            mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    mSelectedPeerProperties = (PeerProperties) mListView.getItemAtPosition(index);
                    Log.i(TAG, "onItemSelected: " + mSelectedPeerProperties.toString());

                    if (mListener != null) {
                        mListener.onPeerSelected(mSelectedPeerProperties);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    /**
     * @return The selected peer properties.
     */
    public PeerProperties getSelectedPeerProperties() {
        return mSelectedPeerProperties;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private LinearLayout peersTopInfoLayout;
    private TextView peersTopInfo0;
    private TextView peersTopInfo1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_peers, container, false);

        mModel = PeerAndConnectionModel.getInstance();
        mContext = view.getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIncomingConnectionIconNotConnected = getResources().getDrawable(R.drawable.ic_arrow_downward_gray_24dp, mContext.getTheme());
            mIncomingConnectionIconConnected = getResources().getDrawable(R.drawable.ic_arrow_downward_blue_24dp, mContext.getTheme());
            mIncomingConnectionIconDataFlowing = getResources().getDrawable(R.drawable.ic_arrow_downward_green_24dp, mContext.getTheme());
            mOutgoingConnectionIconNotConnected = getResources().getDrawable(R.drawable.ic_arrow_upward_gray_24dp, mContext.getTheme());
            mOutgoingConnectionIconConnected = getResources().getDrawable(R.drawable.ic_arrow_upward_blue_24dp, mContext.getTheme());
            mOutgoingConnectionIconDataFlowing = getResources().getDrawable(R.drawable.ic_arrow_upward_green_24dp, mContext.getTheme());
        }
        else
        {
            mIncomingConnectionIconNotConnected = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_downward_gray_24dp);
            mIncomingConnectionIconConnected = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_downward_blue_24dp);
            mIncomingConnectionIconDataFlowing = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_downward_green_24dp);
            mOutgoingConnectionIconNotConnected = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_upward_gray_24dp);
            mOutgoingConnectionIconConnected = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_upward_blue_24dp);
            mOutgoingConnectionIconDataFlowing = ContextCompat.getDrawable(mContext, R.drawable.ic_arrow_upward_green_24dp);
        }

        mainThreadHandler = new Handler(mContext.getMainLooper());
        peersTopInfoLayout = (LinearLayout) view.findViewById(R.id.peersTopInfoLayout);
        peersTopInfo0 = (TextView) view.findViewById(R.id.peersTopInfo0);
        peersTopInfo1 = (TextView) view.findViewById(R.id.peersTopInfo1);

        mPeerListAdapter = new PeersListAdapter(mContext);

        mListView = (ListView)view.findViewById(R.id.listView);
        mListView.setAdapter(mPeerListAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        registerForContextMenu(mListView);
        setListener(mListener);

        mModel.setListener(this);

        showTopMessagInfo();

        return view;
    }

    @Override
    public void onDestroy() {
        if (mModel != null) {
            mModel.setListener(null);
        }

        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_peers, menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        int position = info.position;
        PeerProperties peerProperties = (PeerProperties) mListView.getItemAtPosition(position);

        MenuUtils.PeerMenuItemsAvailability availability =
                MenuUtils.resolvePeerMenuItemsAvailability(peerProperties, mModel);

        MenuItem connectMenuItem = menu.getItem(0);
        MenuItem disconnectMenuItem = menu.getItem(1);
        MenuItem sendDataMenuItem = menu.getItem(2);

        connectMenuItem.setEnabled(availability.connectMenuItemAvailable);
        sendDataMenuItem.setEnabled(availability.sendDataMenuItemAvailable);
        disconnectMenuItem.setEnabled(availability.disconnectMenuItemAvailable);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean wasConsumed = false;

        if (mListener != null) {
            int id = item.getItemId();

            switch (id) {
                case R.id.action_connect:
                    mListener.onConnectRequest(mSelectedPeerProperties); // Has a null check
                    wasConsumed = true;
                    break;
                case R.id.action_disconnect:
                    if (mSelectedPeerProperties != null) {
                        if (mModel.closeConnection(mSelectedPeerProperties)) {
                            wasConsumed = true;
                        }
                    }
                    break;
                case R.id.action_send_data:
                    mListener.onSendDataRequest(mSelectedPeerProperties); // Has a null check
                    wasConsumed = true;
                    break;
            }
        }

        return wasConsumed || super.onContextItemSelected(item);
    }


    Handler mainThreadHandler;

    @Override
    public void onDataChanged() {
        Log.i(TAG, "onDataChanged");

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mPeerListAdapter.setFreshData(mModel.getPeers());

                mPeerListAdapter.notifyDataSetChanged();

                showTopMessagInfo();
            }
        });
    }

    private void showTopMessagInfo()
    {
        if (mPeerListAdapter.getCount() < 1)
        {
            peersTopInfoLayout.setVisibility(View.VISIBLE);
            peersTopInfo0.setText("No peers have been discovered, searching in background. The LOG tab should reveal status of serach activity.");
            peersTopInfo1.setText("Status\n" + BridgeSpot.statusConnectionEngine);
        }
        else
        {
            peersTopInfoLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPeerRemoved(final PeerProperties peerProperties) {
        Log.i(TAG, "onPeerRemoved: " + peerProperties);
        final boolean peerRemovedWasSelected;

        if (mSelectedPeerProperties != null && mSelectedPeerProperties.equals(peerProperties)) {
            mSelectedPeerProperties = null;
            peerRemovedWasSelected = true;
        } else {
            peerRemovedWasSelected = false;
        }

        // DISABLED until we track down crash!
        if (1==2) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (peerRemovedWasSelected) {
                        mListener.onPeerSelected(null);
                    }

                    mPeerListAdapter.notifyDataSetChanged();
                }
            });
        }
    }


    class PeersListAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        private Context mContext;

        public PeersListAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private ArrayList<PeerProperties> dedicatedCopyOfPeers = new ArrayList<PeerProperties>();

        public void setFreshData(ArrayList<PeerProperties> freshDataPeerList)
        {
            // This is still a shallow copy, but at least it doesn't allow add and remove of items in unexpected places
            // dedicatedCopyOfPeers.clear();

            // dedicatedCopyOfPeers.addAll(freshDataPeerList);

            Collection<PeerProperties> deepCopy = new HashSet<PeerProperties>(freshDataPeerList.size());

            Iterator<PeerProperties> iterator = freshDataPeerList.iterator();
            while(iterator.hasNext()){
                deepCopy.add(iterator.next().freshCopy());
            }

            dedicatedCopyOfPeers.clear();
            dedicatedCopyOfPeers.addAll(deepCopy);
        }

        @Override
        public int getCount() {
            return dedicatedCopyOfPeers.size();
        }

        @Override
        public Object getItem(int position) {
            return dedicatedCopyOfPeers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_peer, null);
            }

            PeerProperties peerProperties = dedicatedCopyOfPeers.get(position);

            TextView textView = (TextView) view.findViewById(R.id.peerName);
            textView.setText(peerProperties.getName());

            textView = (TextView) view.findViewById(R.id.peerId);
            textView.setText(peerProperties.getId());

            textView = (TextView) view.findViewById(R.id.peerDiscoveryMethod);
            switch (peerProperties.getDiscoveryMethod())
            {
                case PeerProperties.DISCOVERY_VIA_WIFI:
                    textView.setText("via WiFi");
                    break;
                case PeerProperties.DISCOVERY_VIA_BLUETOOTH:
                    textView.setText("via Bluetooth");
                    break;
                case PeerProperties.DISCOVERY_VIA_BLUETOOTH_LE:
                    textView.setText("via Bluetooth LE");
                    break;
                case PeerProperties.DISCOVERY_VIA_UNKNOWN:
                    textView.setText("");
                    break;
            }

            boolean hasIncomingConnection = mModel.hasConnectionToPeer(peerProperties, true);
            boolean hasOutgoingConnection = mModel.hasConnectionToPeer(peerProperties, false);
            ImageView outgoingConnectionIconImageView = (ImageView) view.findViewById(R.id.outgoingConnectionIconImageView);
            ImageView incomingConnectionIconImageView = (ImageView) view.findViewById(R.id.incomingConnectionIconImageView);
            String connectionInformationText = "";

            if (hasIncomingConnection && hasOutgoingConnection) {
                incomingConnectionIconImageView.setImageDrawable(mIncomingConnectionIconConnected);
                outgoingConnectionIconImageView.setImageDrawable(mOutgoingConnectionIconConnected);
                connectionInformationText = "Connected (incoming and outgoing)";
            } else if (hasIncomingConnection) {
                incomingConnectionIconImageView.setImageDrawable(mIncomingConnectionIconConnected);
                outgoingConnectionIconImageView.setImageDrawable(mOutgoingConnectionIconNotConnected);
                connectionInformationText = "Connected (incoming)";
            } else if (hasOutgoingConnection) {
                incomingConnectionIconImageView.setImageDrawable(mIncomingConnectionIconNotConnected);
                outgoingConnectionIconImageView.setImageDrawable(mOutgoingConnectionIconConnected);
                connectionInformationText = "Connected (outgoing)";
            } else {
                incomingConnectionIconImageView.setImageDrawable(mIncomingConnectionIconNotConnected);
                outgoingConnectionIconImageView.setImageDrawable(mOutgoingConnectionIconNotConnected);
                connectionInformationText = "Not connected";
            }

            Connection connectionResponsibleForSendingData = null;

            if (hasOutgoingConnection) {
                connectionResponsibleForSendingData = mModel.getConnectionToPeer(peerProperties, false);
            } else if (hasIncomingConnection) {
                connectionResponsibleForSendingData = mModel.getConnectionToPeer(peerProperties, true);
            }

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.sendDataProgressBar);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);

            if (connectionResponsibleForSendingData != null
                    && connectionResponsibleForSendingData.isSendingData()) {
                if (connectionResponsibleForSendingData.getIsIncoming()) {
                    incomingConnectionIconImageView.setImageDrawable(mIncomingConnectionIconDataFlowing);
                } else {
                    outgoingConnectionIconImageView.setImageDrawable(mOutgoingConnectionIconDataFlowing);
                }

                progressBar.setProgress((int)(connectionResponsibleForSendingData.getSendDataProgress() * 100));

                connectionInformationText = "Sending "
                        + String.format("%.2f", connectionResponsibleForSendingData.getTotalDataAmountCurrentlySendingInMegaBytes())
                        + " MB (" + String.format("%.3f", connectionResponsibleForSendingData.getCurrentDataTransferSpeedInMegaBytesPerSecond())
                        + " MB/s)";
            } else {
                progressBar.setProgress(0);
            }

            textView = (TextView) view.findViewById(R.id.connectionsInformation);
            textView.setText(connectionInformationText);

            return view;
        }
    }
}
